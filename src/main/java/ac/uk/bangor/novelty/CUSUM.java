package ac.uk.bangor.novelty;

import ac.uk.bangor.novelty.windowing.FixedWindow;
import ac.uk.bangor.novelty.windowing.Window;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by wfaithfull on 08/09/16.
 */
public class CUSUM implements Detector<Double> {

    private Window<Double> window;

    public CUSUM(int windowSize) {
        window = new FixedWindow<>(windowSize, Double.class);
    }

    @Override
    public void update(Double example) {
        window.update(example);

        if(window.size() < window.capacity())
            return;

        Double[] data = window.getElements();
        DescriptiveStatistics statistics = new DescriptiveStatistics(ArrayUtils.toPrimitive(data));

        double mean = statistics.getMean();
        double sd = statistics.getStandardDeviation();

        double recp = 1/sd;
        double cusum = 0;
        for(double sample : data) {
            cusum += sample - mean;
        }

        double st = recp * cusum;

        // TODO: control limits
    }

    @Override
    public boolean isChangeDetected() {
        return false;
    }
}
