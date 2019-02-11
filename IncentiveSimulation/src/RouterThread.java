import java.util.Random;

public class RouterThread implements Runnable {

    int id;

    // possibility of uses appearance in 1 sec, range from (0,1]
    double userFrequencyBase;

    // average bid price, range from [1,10]
    double priceBase;

    double threshold;
    double budget;

    volatile BidInfo bid;

    public RouterThread(int id, double userFrequencyBase, double priceBase, double threshold, double budget) {
        this.id = id;
        this.userFrequencyBase = userFrequencyBase;
        this.priceBase = priceBase;
        this.threshold = threshold;
        this.budget = budget;

        this.bid = new BidInfo();
    }

    @Override
    public void run() {
        Random random = new Random();

        try {
            while (true) {
                if (random.nextDouble() <= this.userFrequencyBase) {
                    double price = Math.max(random.nextGaussian() * 1 + this.priceBase, 1);

                    if (price <= this.threshold && this.budget - this.threshold >= 0) {
                        this.bid.accepted.add(price);
                        this.budget -= threshold;
                    }

                    this.bid.all.add(price);
                }

                Thread.sleep(2);
            }
        } catch (InterruptedException e) {

        }
    }

    public BidInfo getBidInfo() {
        BidInfo result = new BidInfo(this.bid);
        this.bid.clear();
        return result;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void addBudget(double budget) {
        this.budget = budget;
    }
}
