package ac.uk.bangor.novelty;

/**
 * Created by wfaithfull on 08/09/16.
 */
public class CUSUM implements Detector<Double> {

    private final double h;
    private final double mszu;
    private final double mszl;
    private final StatsProvider stats = new OnlineStatistics();
    private double upper = 0;
    private double lower = 0;

    private boolean change;

    /**
     * Construct a CUSUM detector with default parameters.
     */
    public CUSUM() {
        this(5,1,1);
    }

    /**
     * Construct a CUSUM detector
     * @param h
     *          Control threshold - multiplier for standard deviation of observations. Whilst the upper and lower
     *          cumulative sums stay between 0 and hσ, and 0 and -hσ respectively, we say that the process is 'in control'.
     *
     *          A normal value for this might be 5.
     *          {@see http://www.infinityqs.com/tech-notes/tabular-cumulative-summation-cusum-chart}
     */
    public CUSUM(double h) {
        this(h, 1, 1);
    }

    /**
     * Construct a CUSUM detector
     * @param h
     *          Control threshold - multiplier for standard deviation of observations. Whilst the upper and lower
     *          cumulative sums stay between 0 and hσ, and 0 and -hσ respectively, we say that the process is 'in control'.
     *
     *          A normal value for this might be 5.
     *          {@see http://www.infinityqs.com/tech-notes/tabular-cumulative-summation-cusum-chart}
     * @param mszu
     *          The factor of change in upper mean we wish to detect. Usually between 0.5σ and 1.5σ
     * @param mszl
     *          The factor of change in lower mean we wish to detect. Usually between 0.5σ and 1.5σ
     */
    public CUSUM(double h, double mszu, double mszl) {
        this.h = h;
        this.mszu = mszu;
        this.mszl = mszl;
    }

    @Override
    public void update(Double example) {
        stats.update(example);

        double mean = stats.mean();
        double sd = stats.std();

        double ku = (mszu * sd)/2;
        double kl = (mszl * sd)/2;
        upper = Math.max(0, example - (mean+ku) + upper);
        lower = Math.min(0, example - (mean-kl) - lower);

        change = upper > h*sd || lower < -h*sd;
    }

    @Override
    public boolean isChangeDetected() {
        return change;
    }
}
