package ac.uk.bangor.novelty;

/**
 * Created by wfaithfull on 06/09/16.
 */
public class MovingRange implements Detector<Double> {
    private boolean started = false;
    private StatsProvider statsMovingRange = new OnlineStatistics();
    private StatsProvider statsIndividuals = new OnlineStatistics();
    private double lastObservation;
    private boolean change;

    private final double d2 = 3/ControlChartConstants.d2(2);
    private final double D4 = ControlChartConstants.D4(2);

    public MovingRange() {}

    public MovingRange(StatsProvider mrStats, StatsProvider individualsStats) {
        this.statsMovingRange = mrStats;
        this.statsIndividuals = individualsStats;
    }

    @Override
    public void update(Double input) {
        double mr = movingRange(input);
        statsMovingRange.update(mr);
        statsIndividuals.update(input);

        double uclMovingRange = statsMovingRange.mean() * D4;

        double ucl = statsIndividuals.mean() + d2 * statsMovingRange.mean();
        double lcl = statsIndividuals.mean() - d2 * statsMovingRange.mean();
        this.change = input >= ucl || input <= lcl || mr > uclMovingRange;
        lastObservation = input;
    }

    private double movingRange(double observation) {
        if(!started) {
            started = true;
            return 0;
        }
        return Math.abs(observation - lastObservation);
    }

    @Override
    public boolean isChangeDetected() {
        return this.change;
    }
}
