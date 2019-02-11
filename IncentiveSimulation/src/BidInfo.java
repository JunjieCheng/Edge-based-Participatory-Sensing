import java.util.LinkedList;
import java.util.List;

public class BidInfo {

    List<Double> accepted;
    List<Double> all;

    public BidInfo() {
        accepted = new LinkedList<>();
        all = new LinkedList<>();
    }

    public BidInfo(BidInfo bidInfo) {
        this.accepted = new LinkedList<>(bidInfo.accepted);
        this.all = new LinkedList<>(bidInfo.all);
    }

    public void clear() {
        this.accepted.clear();
        this.all.clear();
    }
}
