package Model;

public class SynchronizationTask {
    public long time;
    public String task;
    public boolean last;

    public SynchronizationTask(String task, long time, boolean last) {
        this.time = time;
        this.task = task;
        this.last = last;
    }
}
