import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Util {

    public static List<Double> calculateBudget(List<List<Double>> allBids, int phase, double threshold) {
        List<Double> routerBudgets = new LinkedList<>();

        if (phase == 0) {
            for (int i = 0; i < Parameters.MAX_ROUTER; i++) {
                routerBudgets.add((double) 1 / Parameters.MAX_ROUTER);
            }
        } else {
            int sum = 0;

            for (int i = 0; i < Parameters.MAX_ROUTER; i++) {
                Collections.sort(allBids.get(i));

                if (allBids.get(i).isEmpty()) {
                    routerBudgets.add(1.0);
                    continue;
                }

                int count = 0;

                for (int j = 0; j < allBids.get(i).size(); j++) {
                    if (allBids.get(i).get(j) <= threshold) {
                        count++;
                    } else {
                        break;
                    }
                }

                routerBudgets.add((double) count);
                sum += count;
            }

            if (sum != 0) {
                for (int i = 0; i < Parameters.MAX_ROUTER; i++) {
                    routerBudgets.set(i, routerBudgets.get(i) / sum);
                }
            } else {
                for (int i = 0; i < Parameters.MAX_ROUTER; i++) {
                    routerBudgets.set(i, (double) 1 / Parameters.MAX_ROUTER);
                }
            }
        }

        return routerBudgets;
    }

    public static double getDensityThreshold(List<List<Double>> allBids, int phase, double remaining) {
        List<Double> selectedUsers = new LinkedList<>();
        List<Double> bids = new ArrayList<>();
        double currentBudget = Parameters.BUDGET / Math.pow(2, Parameters.MAX_PHASE) * Math.pow(2, phase) + remaining;

        for (List<Double> list : allBids) {
            for (Double bid : list) {
                bids.add(bid);
            }
        }

        Collections.sort(bids);

        for (double bid : bids) {
            if (bid <= currentBudget / (selectedUsers.size() + 1)) {
                selectedUsers.add(bid);
            }
        }

        return 1 / (Math.max(0.01, selectedUsers.size()) / currentBudget / 1.5);
    }

    public static double[] calculateOptimal(List<List<Double>> allBids) {
        List<Double> bids = new ArrayList<>();
        double cost = 0;
        double count = 0;

        for (List<Double> list : allBids) {
            bids.addAll(list);
        }

        Collections.sort(bids);

        for (double bid : bids) {
            if (count * bid <= Parameters.BUDGET) {
                count += 1;
                cost = count * bid;
            }
        }

        return new double[]{cost, count};
    }
}
