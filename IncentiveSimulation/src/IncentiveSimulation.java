import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncentiveSimulation {

    private static final int MAX_THREAD = 10;
    private static final int MAX_PHASE = 10;
    private static final double BUDGET = 8192;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
        RouterThread[] routers = new RouterThread[MAX_THREAD];

        Random random = new Random();

        int sleepTime = 10;
        double threshold = 10;
        double subbudget = BUDGET / Math.pow(2, MAX_PHASE);
        double totalCost = 0;

        for (int i = 0; i < MAX_THREAD; i++) {
            routers[i] = new RouterThread(i, random.nextGaussian() * 0.2 + 0.5, random.nextGaussian() * 2 + 5, threshold, subbudget / MAX_THREAD);
            pool.execute(routers[i]);
        }

        List<Double> accepted = new LinkedList<>();
        List<Double> all = new LinkedList<>();

        for (int p = 0; p < MAX_PHASE; p++) {
            Thread.sleep(sleepTime);

            double remainingBudget = 0;

            List<Integer> acceptedBids = new LinkedList<>();
            List<List<Double>> allBids = new ArrayList<>(MAX_THREAD);

            for (RouterThread routerThread : routers) {
                BidInfo bidInfo = routerThread.getBidInfo();
                System.out.printf("Thread %d, remaining %.2f, frequency: %.2f, price: %.2f: ", routerThread.id, routerThread.budget, routerThread.userFrequencyBase, routerThread.priceBase);

                // retrieve accepted bids
                for (Double bid : bidInfo.accepted) {
                    System.out.printf("%.2f ", bid);
                    accepted.add(bid);
                }

                System.out.println();

                // retrieve all bids
                all.addAll(bidInfo.all);

                totalCost += bidInfo.accepted.size() * threshold;
                acceptedBids.add(bidInfo.accepted.size());
                remainingBudget += routerThread.budget;
                allBids.add(bidInfo.all);
            }

            System.out.printf("Phase %d end\n", p);

            threshold = getDensityThreshold(subbudget + remainingBudget, all);
            System.out.printf("Next threshold: %.2f\n", threshold);

            List<Double> budgetDistribution = calculateBudget(threshold, allBids);

            subbudget *= 2;
            sleepTime *= 2;

            for (int i = 0; i < MAX_THREAD; i++) {
                RouterThread routerThread = routers[i];
                routerThread.setThreshold(threshold);
                routerThread.addBudget(budgetDistribution.get(i) * (subbudget + remainingBudget));
            }
        }

        System.out.printf("Accepted users: %d\t%.2f\n", accepted.size(), totalCost);
        System.out.printf("Optimal: %d\n", calculateOptimal(all));

        pool.shutdownNow();
    }

    private static double getDensityThreshold(double subbudget, List<Double> bids) {
        List<Double> selectedUsers = new LinkedList<>();
        Collections.sort(bids);

        for (double bid : bids) {
            if (bid <= subbudget / (selectedUsers.size() + 1)) {
                selectedUsers.add(bid);
            }
        }

        return 1 / (Math.max(0.1, selectedUsers.size()) / subbudget / 1.5);
    }

    private static List<Double> calculateBudget(double threshold, List<List<Double>> bids) {
        List<Double> result = new ArrayList<>(MAX_THREAD);
        int sum = 0;

        for (int i = 0; i < MAX_THREAD; i++) {
            Collections.sort(bids.get(i));

            if (bids.get(i).isEmpty()) {
                result.add(1.0);
                continue;
            }

            int count = 0;

            for (int j = 0; j < bids.get(i).size(); j++) {
                if (bids.get(i).get(j) <= threshold) {
                    count++;
                } else {
                    break;
                }
            }

            result.add((double) count);
            sum += count;
        }

        for (int i = 0; i < MAX_THREAD; i++) {
            result.set(i, result.get(i) / sum);
        }

        return result;
    }

    private static int calculateOptimal(List<Double> allBids) {
        Collections.sort(allBids);

        for (int i = 0; i < allBids.size(); i++) {
            if (i * allBids.get(i) >= BUDGET) {
                return i;
            }
        }

        return 0;
    }
}
