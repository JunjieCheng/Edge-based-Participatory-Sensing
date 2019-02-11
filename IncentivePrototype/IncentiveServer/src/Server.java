import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.*;

public class Server {

    private static int sleep = 0;
    private static double remaining = 0;
    private static double threshold = 0;

    private static List<String> routerIPs;
    private static List<List<Double>> allBids;
    private static List<List<Double>> acceptedBids;

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/incentive", new ServerHandler());
        server.setExecutor(null);
        server.start();

        initialize();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine();

            if (cmd.equals("start")) {
                startTask();
            } else if (cmd.equals("quit")) {
                break;
            }
        }
    }

    public static void initialize() {
        routerIPs = new LinkedList<>();
        allBids = new LinkedList<>();
        acceptedBids = new LinkedList<>();

        routerIPs.add("http://192.168.1.1:81/incentive/IncentiveRouter.php");
        allBids.add(new LinkedList<>());
        acceptedBids.add(new LinkedList<>());
    }

    // Should be a new thread
    private static void startTask() throws InterruptedException, UnsupportedEncodingException {
        sleep = Parameters.SLEEP;
        remaining = 0;
        threshold = Parameters.THRESHOLD;

        for (int phase = 0; phase < Parameters.MAX_PHASE; phase++) {
            List<Double> routerBudgets = Util.calculateBudget(allBids, phase, threshold);
            distributeTasks(routerBudgets, phase);
            remaining = 0;
            Thread.sleep(sleep);
            retrieveBids();
            threshold = Util.getDensityThreshold(allBids, phase, remaining);
            sleep *= 2;
        }

        displayResult();
    }

    private static void displayResult() {
        double[] optimal = Util.calculateOptimal(allBids);

        System.out.printf("Optimal cost: %.2f\n", optimal[0]);
        System.out.printf("Optimal count: %f\n", optimal[1]);

        double totalCost = 0;
        int count = 0;

        for (List<Double> list : acceptedBids) {
            for (double bid : list) {
                totalCost += bid;
                count++;
            }
        }

        System.out.printf("Actual cost: %.2f\n", totalCost);
        System.out.printf("Actual count: %d\n", count);
    }

    private static void distributeTasks(List<Double> routerBudgets, int phase) throws UnsupportedEncodingException {
        double currentBudget = Parameters.BUDGET / Math.pow(2, Parameters.MAX_PHASE) * Math.pow(2, phase) + remaining;

        for (int i = 0; i < routerIPs.size(); i++) {
            String urlParameters = "task=" + URLEncoder.encode("test", "UTF-8")
                    + "&budget=" + routerBudgets.get(i) * (currentBudget + remaining)
                    + "&end_time=" + System.currentTimeMillis() + sleep
                    + "&threshold=" + threshold;
            String response = HTTPClient.executePost(routerIPs.get(i), urlParameters);
            System.out.println(response);
        }
    }

    private static void retrieveBids() throws UnsupportedEncodingException {
        for (int i = 0; i < routerIPs.size(); i++) {
            String urlParameters = "retrieve=" + URLEncoder.encode("test", "UTF-8");
            String response = HTTPClient.executePost(routerIPs.get(i), urlParameters);
            System.out.println(response);
            parseResponse(i, response);
        }
    }

    private static void parseResponse(int id, String response) {
        String[] data = response.trim().split("&");
        String[] parameters = data[0].split("\n");

        // Parse parameters
        for (String parameter : parameters) {
            String[] kv = parameter.split("=");

            if (kv[0].equals("budget")) {
                remaining += Double.parseDouble(kv[1]);
            }
        }

        // Parse bids
        if (!data[1].equals("null")) {
            String[] acc = data[1].split(",");

            for (String bid : acc) {
                acceptedBids.get(id).add(Double.parseDouble(bid));
                allBids.get(id).add(Double.parseDouble(bid));
            }
        }

        if (!data[2].equals("null")) {
            String[] rej = data[2].split(",");
            for (String bid : rej) {
                allBids.get(id).add(Double.parseDouble(bid));
            }
        }
    }

}
