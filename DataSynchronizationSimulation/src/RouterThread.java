import java.util.*;

public class RouterThread implements Runnable {

    Random random = new Random();

    int id;
    ArrayList<Integer> data = new ArrayList<>();
    Set<Integer> collected = new HashSet<>();

    public RouterThread(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        this.data.clear();

        while (true) {
            int label = (random.nextInt(100) + id * 20) % 200;

            if (!collected.contains(label)) {
                collected.add(label);
                data.add(label);
            }

            try {
                Thread.sleep(random.nextInt(60) + 30);
            } catch (InterruptedException e) {
            }
        }
    }

    public ArrayList<Integer> getData() {
        ArrayList<Integer> result = new ArrayList<>(this.data);
        this.data.clear();
        return result;
    }

    public void synchronize(HashSet<Integer> list) {
        collected.addAll(list);
    }

    public void clear() {
        this.collected.clear();
        this.data.clear();
    }
}
