import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulation {

    private static final int MAX_THREAD = 10;
    private static final int MIN_INTERVAL = 60;
    private static final int MAX_INTERVAL = 600;
    private static final int MAX_RUN = 10;
    private static final int LABEL_SIZE_Byte = 8;
    private static final int HEADER_SIZE_KB = 50;

    public static void main(String[] args) throws InterruptedException, IOException {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        RouterThread[] routers = new RouterThread[MAX_THREAD];
        HashSet<Integer> collected = new HashSet<>();
        FileWriter file = new FileWriter(new File("./data_overlap.txt"));

        for (int i = 0; i < MAX_THREAD; i++) {
            routers[i] = new RouterThread(i);
            pool.execute(routers[i]);
        }

        for (int interval = MIN_INTERVAL; interval <= MAX_INTERVAL; interval += 5) {
            int sum = 0;
            int synchronization_count = 0;
            int duplicate = 0;
            int totalTime;

            for (int run = 0; run < MAX_RUN; run++) {
                for (int id = 0; id < MAX_THREAD; id++) {
                    routers[id].clear();
                }

                collected.clear();

                for (totalTime = 0; totalTime <= 3600; totalTime += interval) {
                    Thread.sleep(interval);
                    ArrayList[] data = new ArrayList[MAX_THREAD];

                    for (int id = 0; id < MAX_THREAD; id++) {
                        data[id] = routers[id].getData();
                        sum += data[id].size();
                    }


                    duplicate += countDuplicate(collected, data);

                    for (int id = 0; id < MAX_THREAD; id++) {
                        routers[id].synchronize(collected);
                        synchronization_count += 1;
                    }
                }

                Thread.sleep(3600 - totalTime + interval);
                ArrayList[] data = new ArrayList[MAX_THREAD];

                for (int id = 0; id < MAX_THREAD; id++) {
                    data[id] = routers[id].getData();
                    sum += data[id].size();
                }

                duplicate += countDuplicate(collected, data);
            }

            file.write("" + interval + "," + (double)duplicate / sum * 100 + "," + ((double)(sum - duplicate) * LABEL_SIZE_Byte / 1024 + synchronization_count * HEADER_SIZE_KB) / MAX_RUN + "\n");
            System.out.printf("Interval: %d sec, duplicate: %.2f, collected: %.2f, duplicate rate: %.2f%% ", interval, (double)duplicate / MAX_RUN, (double)sum / MAX_RUN, (double)duplicate / sum * 100);
            System.out.printf("total data: %.2f\n", ((double)(sum - duplicate) * LABEL_SIZE_Byte / 1024 + synchronization_count * HEADER_SIZE_KB) / MAX_RUN);
        }

        file.close();
        pool.shutdownNow();
    }

    public static int countDuplicate(HashSet<Integer> collected, ArrayList[] data) {
        int count = 0;

        for (ArrayList router : data) {
            if (router != null) {
                for (Object number : router) {
                    if (number == null) {
                        continue;
                    }
                    if (collected.contains((int) number)) {
                        count++;
                    } else {
                        collected.add((int) number);
                    }
                }
            }
        }

        return count;
    }
}
