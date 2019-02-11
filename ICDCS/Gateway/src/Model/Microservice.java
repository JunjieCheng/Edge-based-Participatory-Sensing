package Model;

public class Microservice {

    public String taskName;
    public String microserviceName;
    public String className;
    public String dataType;
    public String data;

    public Microservice(String taskName, String microserviceName, String className) {
        this.taskName = taskName;
        this.microserviceName = microserviceName;
        this.className = className;
    }
}
