package ac.uk.bangor.novelty;

/**
 * Created by wfaithfull on 06/09/16.
 */
public class EWMA implements UnivariateRealDetector {

    private StatsProvider stats;
    private static final int MINIMUM_SAMPLES = 10;
    private int samples;
    private double lambda;
    private boolean change;

    public EWMA(double lambda) {
        this(lambda, new OnlineStatistics());
    }

    public EWMA(double lambda, StatsProvider stats) {
        this.lambda = lambda;
        this.stats = stats;
    }

    @Override
    public void update(Double input) {
        stats.update(input);
        samples++;

        if(samples < MINIMUM_SAMPLES) {
            this.change = false;
        } else {
            double statistic = lambda * input + (1-lambda) * stats.mean();
            double mean = stats.mean();
            double std = stats.std();

            double limit = std * Math.sqrt((lambda/(2-lambda)) * (1-Math.pow((1-lambda),2*samples)));

            double lcl = mean - limit;
            double ucl = mean + limit;

            this.change = statistic <= lcl || statistic >= ucl;
        }
    }

    @Override
    public boolean isChangeDetected() {
        return this.change;
    }
}
