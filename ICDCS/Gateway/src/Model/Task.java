package Model;

public class Task {

    public String task;
    public double budget;
    public double threshold;
    public long expiration;

    public Task(String task, double budget, double threshold, long expiration) {
        this.task = task;
        this.budget = budget;
        this.threshold = threshold;
        this.expiration = expiration;
    }
}
