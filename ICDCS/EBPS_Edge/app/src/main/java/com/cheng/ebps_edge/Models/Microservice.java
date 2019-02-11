package com.cheng.ebps_edge.Models;

public class Microservice {

    public String taskName;
    public String microserviceName;
    public String className;

    public Microservice(String taskName, String className) {
        String[] params = taskName.split(":");
        this.taskName = params[0];
        this.microserviceName = params[1];
        this.className = className;
    }
}
