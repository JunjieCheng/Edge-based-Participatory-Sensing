package handler;

import Model.Microservice;
import Model.Task;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class ServerHandler implements HttpHandler {

    private Map<String, Task> tasks;
    private Map<String, List<String>> taskWorkflow;

    // <Microservice name, Microservice>
    private Map<String, Microservice> microservices;

    // <Microservice name, List<Microservice>>
    private Map<String, List<Microservice>> publicMicroservices;

    // <User Id, List<Microservice>>
    private Map<String, List<Microservice>> privateMicroservices;
    private Map<String, Set<String>> collectedTarget;

    public ServerHandler() {
        tasks = new HashMap<>();
        taskWorkflow = new HashMap<>();
        microservices = new HashMap<>();
        publicMicroservices = new HashMap<>();
        privateMicroservices = new HashMap<>();
        collectedTarget = new HashMap<>();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String, String> parameters = queryToMap(httpExchange.getRequestURI().getQuery());
        String response = "Error";
        System.out.println("Received");

        for (String key : parameters.keySet()) {
            System.out.println(key + ": " + parameters.get(key));
        }

        // Server
        if (parameters.containsKey("new_task")) {
            response = handleNewTask(parameters);
        } else if (parameters.containsKey("synchronization_upload")) {
            response = handleDataSynchronizationUpload(parameters);
        } else if (parameters.containsKey("synchronization_download")) {
            response = handleDataSynchronizationDownload(parameters);
        } else if (parameters.containsKey("incentive_upload")) {
            response = handleIncentiveUpload(parameters);
        } else if (parameters.containsKey("incentive_download")) {
            response = handleIncentiveDownload(parameters);
        }
        // Edge
        else if (parameters.containsKey("action")) {
            if (parameters.get("action").equals("list")) {
                response = handleMicroserviceList(parameters);
            } else if (parameters.get("action").equals("request") &&
                    parameters.get("microservice_name").equals("RecognizeAvailableSpace")) {
                response = handleRecognizeAvailableSpace(parameters);

                File srcFile = new File(response);
                httpExchange.sendResponseHeaders(200, srcFile.length());
                OutputStream os = httpExchange.getResponseBody();
                FileUtils.copyFile(srcFile, os);
                os.close();
                return;
            } else if (parameters.get("action").equals("return")) {
                response = handleMicroserviceReturn(parameters);
            }
        }

//        System.out.println("response: " + response);
        System.out.println();

        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String handleNewTask(Map<String, String> parameters) throws IOException {
        String taskName = parameters.get("new_task");
        double budget = Double.parseDouble(parameters.get("budget"));
        double threshold = Double.parseDouble(parameters.get("threshold"));
        long expiration = Long.parseLong(parameters.get("expiration"));

        tasks.put(parameters.get("new_task"), new Task(taskName, budget, threshold, expiration));
        collectedTarget.put(parameters.get("new_task"), new HashSet<>());

        // Create workflow
        List<String> workflow = new ArrayList<>();
        workflow.add("SubmitSensingTarget");
        workflow.add("TakePhoto");
        workflow.add("RecognizeAvailableSpace");
        workflow.add("VerifyPhoto");

        taskWorkflow.put(taskName, workflow);
        publicMicroservices.put(workflow.get(0), new LinkedList<>());
        publicMicroservices.get(workflow.get(0)).add(new Microservice(taskName, workflow.get(0), workflow.get(0) + "Activity"));

        // Create microservices
        for (String microserviceName : workflow) {
            microservices.put(microserviceName, new Microservice(taskName, microserviceName, microserviceName + "Activity"));
        }

        Util.Util.createFiles(taskName);

        return "Task Created";
    }

    private String handleDataSynchronizationUpload(Map<String, String> parameters) throws IOException {
        String taskName = parameters.get("synchronization_upload");
        BufferedReader tempReader = new BufferedReader(new FileReader("./src/data/" + taskName + "_target.temp"));
        StringBuilder response = new StringBuilder();
        String line;

        // Format of file: target1\n target2\n target3
        while ((line = tempReader.readLine()) != null) {
            String target = line.trim();
            response.append(target).append(",");
        }

        tempReader.close();

        // Clear temp file
        FileWriter tempWriter = new FileWriter("./src/data/" + taskName + "_target.temp");
        tempWriter.close();

        if (response.length() > 0) {
            return response.substring(0, response.length() - 1);
        } else {
            return "";
        }
    }

    private String handleDataSynchronizationDownload(Map<String, String> parameters) throws IOException {
        String taskName = parameters.get("synchronization_download");
        String[] data = parameters.get("data").split(",");
        Set<String> targets = collectedTarget.get(taskName);

        for (String target : data) {
            if (!target.equals("")) {
                targets.add(target);
            }
        }

        return "Target Synchronized";
    }

    private String handleIncentiveUpload(Map<String, String> parameters) throws IOException {
        String taskName = parameters.get("incentive_upload");
        StringBuilder response = new StringBuilder();
        BufferedReader tempReader = new BufferedReader(new FileReader("./src/data/" + taskName + "_incentive.temp"));
        String line;

        response.append(tasks.get(taskName).budget).append(",");
        tasks.get(taskName).budget = 0;

        // Format of file: price1|acc\n price2|rej\n price3|acc\n
        while ((line = tempReader.readLine()) != null) {
            String target = line.trim();
            response.append(target).append(",");
        }

        if (response.length() == 0) {
            return "";
        } else {
            return response.substring(0, response.length() - 1);
        }
    }

    private String handleIncentiveDownload(Map<String, String> parameters) {
        String taskName = parameters.get("incentive_download");
        String threshold = parameters.get("threshold");
        String budget = parameters.get("budget");

        tasks.get(taskName).threshold = Double.parseDouble(threshold);
        tasks.get(taskName).budget = Double.parseDouble(budget);

        return "Synchronized";
    }

    private String handleMicroserviceList(Map<String, String> parameters) {
        StringBuilder response = new StringBuilder();

        for (String microserviceName : publicMicroservices.keySet()) {
            if (!publicMicroservices.get(microserviceName).isEmpty()) {
                response.append(microservices.get(microserviceName).taskName)
                        .append(":")
                        .append(microserviceName)
                        .append("|")
                        .append(microservices.get(microserviceName).className)
                        .append(",");
            }
        }

        // User specific MS
        if (privateMicroservices.containsKey(parameters.get("user_id"))) {
            for (Microservice microservice : privateMicroservices.get(parameters.get("user_id"))) {
                response.append(microservice.taskName)
                        .append(":")
                        .append(microservice.microserviceName)
                        .append("|")
                        .append(microservice.className)
                        .append(",");
            }
        }

        if (response.length() > 0) {
            return response.toString().substring(0, response.length() - 1);
        } else {
            return "";
        }
    }

    private String handleMicroserviceReturn(Map<String, String> parameters) throws IOException {
        //TODO
        String response = "Error";

        if (parameters.get("microservice_name").equals("SubmitSensingTarget")) {
            response = handleSubmitSensingTarget(parameters);
        } else if (parameters.get("microservice_name").equals("TakePhoto")) {
            response = handleTakePhoto(parameters);
        } else if (parameters.get("microservice_name").equals("RecognizeAvailableSpace")) {
            response = handleRecognitionResultUpload(parameters);
        }

        return response;
    }

    // TODO: Write into file
    // Check target and add the next MS into private MS list
    private String handleSubmitSensingTarget(Map<String, String> parameters) {
        String taskName = parameters.get("task_name");
        String data = parameters.get("data");
        double price = Double.parseDouble(parameters.get("price"));
        Task task = tasks.get(taskName);

        if ((price <= task.threshold && price <= task.budget)
                && (!collectedTarget.containsKey(taskName) || !collectedTarget.get(taskName).contains(data))) {
            String userId = parameters.get("user_id");

            task.budget -= price;

            if (!privateMicroservices.containsKey(userId)) {
                privateMicroservices.put(userId, new ArrayList<>());
            }

            int i = taskWorkflow.get(taskName).indexOf(parameters.get("microservice_name"));
            String microserviceName = taskWorkflow.get(taskName).get(i + 1);
            privateMicroservices.get(userId).add(new Microservice(taskName, microserviceName, microservices.get(microserviceName).className));
            collectedTarget.get(taskName).add(data);
            return "Accepted";
        }

        return "Rejected";
    }

    private String handleTakePhoto(Map<String, String> parameters) {
        String response = "Received";
        String taskName = parameters.get("task_name");
        String userId = parameters.get("user_id");

        privateMicroservices.get(userId).remove(0);
        int i = taskWorkflow.get(taskName).indexOf(parameters.get("microservice_name"));
        String nextMicroserviceName = taskWorkflow.get(taskName).get(i + 1);
        Microservice ms = new Microservice(taskName, nextMicroserviceName, nextMicroserviceName + "Activity");
        ms.dataType = "image";
        ms.data = "./src/data/images/" + parameters.get("file_name");

        if (!publicMicroservices.containsKey(nextMicroserviceName)) {
            publicMicroservices.put(nextMicroserviceName, new ArrayList<>());
        }

        publicMicroservices.get(nextMicroserviceName).add(ms);
        return response;
    }

    private String handleRecognizeAvailableSpace(Map<String, String> parameters) throws IOException {
        String response;
        String taskName = parameters.get("task_name");
        String microservceName = parameters.get("microservice_name");
        double price = Double.parseDouble(parameters.get("price"));
        Task task = tasks.get(taskName);

        if (price <= task.threshold && price <= task.budget) {
            task.budget -= price;

            // Remove first MS
            Microservice microservice = publicMicroservices.get(microservceName).remove(0);

            return microservice.data;
        } else {
            response = "Rejected";
        }

        return response;
    }

    // TODO: not complete
    private String handleRecognitionResultUpload(Map<String, String> parameters) {
        String response;
        String taskName = parameters.get("task_name");
        String microservceName = parameters.get("microservice_name");

        return "";
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();

        if (query == null) {
            return result;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=");

            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }

        return result;
    }
}
