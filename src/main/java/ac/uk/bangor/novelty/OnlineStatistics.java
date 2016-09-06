package ac.uk.bangor.novelty;

/**
 * Created by wfaithfull on 10/06/16.
 */
public class OnlineStatistics implements StatsProvider {

    long n = 0;
    double mu = 0.0;
    double sq = 0.0;

    public void update(double x) {
        ++n;
        double muNew = mu + (x-mu)/n;
        sq += (x-mu)*(x-muNew);
        mu = muNew;
    }

    public double mean() {
        return mu;
    }

    public double var() {
        return n > 1 ? sq/n : 0.0;
    }

}
