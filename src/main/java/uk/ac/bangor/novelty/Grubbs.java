package uk.ac.bangor.novelty;

import uk.ac.bangor.novelty.windowing.FixedWindow;
import uk.ac.bangor.novelty.windowing.Window;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

/**
 * Created by wfaithfull on 06/09/16.
 */
public class Grubbs implements UnivariateRealDetector {

    private boolean change;
    private Window<Double> window;
    private final int sigmaThreshold;

    public Grubbs(int windowSize, int sigmaThreshold) {
        this.window = new FixedWindow<>(windowSize, Double.class);
        this.sigmaThreshold = sigmaThreshold;
    }

    public void update(Double input) {
        window.update(input);

        if(window.size() != window.capacity())
            return;

        double mu = StatUtils.mean(ArrayUtils.toPrimitive(window.getElements()));
        double sigma = Math.sqrt(StatUtils.variance(ArrayUtils.toPrimitive(window.getElements())));

        double ucl = mu + (sigma*sigmaThreshold);
        double lcl = mu - (sigma*sigmaThreshold);

        this.change = input > ucl || input < lcl;
    }

    public boolean isChangeDetected() {
        return change;
    }
}
