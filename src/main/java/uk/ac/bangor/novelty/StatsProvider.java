package uk.ac.bangor.novelty;

/**
 * @author Will Faithfull
 */
public interface StatsProvider {

    void update(double input);

    double mean();

    double var();

    default double std() {
        return Math.sqrt(var());
    }
}