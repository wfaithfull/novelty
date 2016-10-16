package ac.uk.bangor.experiment.casestudy;

import ac.uk.bangor.novelty.MultivariateRealDetector;
import ac.uk.bangor.novelty.UnivariateRealDetector;
import ac.uk.bangor.novelty.ensemble.MultivariateRealEnsemble;
import ac.uk.bangor.novelty.ensemble.QuorumScheme;

import java.util.function.Supplier;

/**
 * The basic plumbing for a set of ensemble detectors with different decision quorum.
 * @author Will Faithfull
 */
public abstract class AbstractEnsembleDetectorSuite implements DetectorSuite {

    protected abstract String[] detectorNames();
    protected int index = 0;

    @Override
    public String getDetectorName() {
        return detectorNames()[index];
    }

    @Override
    public boolean hasNext() {
        return index < detectorNames().length;
    }

    @Override
    public void advance() {
        if(!hasNext())
            throw new IllegalStateException("No more detectors in suite");
        index++;
    }

    protected MultivariateRealDetector getEnsemble(int features, double quorum, Supplier<UnivariateRealDetector> supplier) {
        MultivariateRealEnsemble ensemble = new MultivariateRealEnsemble(new QuorumScheme(quorum));
        for(int i = 0; i < features; i++) {
            ensemble.addUnivariate(supplier.get(), i);
        }
        return ensemble;
    }

    @Override
    public boolean first() {
        return index == 0;
    }
}
