package ac.uk.bangor.novelty.ensemble;

import ac.uk.bangor.novelty.Detector;
import ac.uk.bangor.novelty.MultivariateRealDetector;
import ac.uk.bangor.novelty.UnivariateRealDetector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * A flexible ensemble implementation which accepts univariate and multivariate voters.
 * @author Will Faithfull
 */
@Slf4j
public class MultivariateRealEnsemble implements MultivariateRealDetector {

    Map<Detector, FeatureMapping> detectors;

    @Getter
    @Setter
    VotingScheme votingScheme;

    @Getter
    @Setter
    WeightingScheme weightingScheme;

    private boolean change;

    public MultivariateRealEnsemble() {
        this(new QuorumScheme(0.5)); // simple majority
    }

    public MultivariateRealEnsemble(VotingScheme votingScheme) {
        this.votingScheme = votingScheme;
        detectors = new HashMap<>();
    }

    @Override
    public void update(double[] example) {

        // Update all the detectors
        for(Map.Entry<Detector, FeatureMapping> entry : detectors.entrySet()) {
            FeatureMapping mapping = entry.getValue();
            if(entry.getKey() instanceof UnivariateRealDetector) {
                if(mapping.count() == 0 || mapping.count() > 1) {
                    throw new RuntimeException("Illegal feature count for univariate detector: " + mapping.count());
                }

                UnivariateRealDetector univariateRealDetector = (UnivariateRealDetector) entry.getKey();
                univariateRealDetector.update(example[mapping.getFeatures()[0]]);
            } else {
                if(mapping.count() == 0 || mapping.count() > example.length) {
                    throw new RuntimeException("Illegal feature count for multivariate detector: " + mapping.count());
                }

                MultivariateRealDetector multivariateRealDetector = (MultivariateRealDetector) entry.getKey();
                mapping.update(example, multivariateRealDetector); // Allow mapping to pass appropriate features
            }
        }

        // Voters have been updated, request their decision.
        change = votingScheme.getResult();
    }

    @Override
    public boolean isChangeDetected() {
        return change;
    }

    public void addUnivariate(UnivariateRealDetector detector, int feature) {
        addDetector(detector, FeatureMapping.ofSingle(feature));
    }

    public void addMultivariate(MultivariateRealDetector detector) {
        addDetector(detector, FeatureMapping.ofAllFeatures());
    }

    public void addMultivariate(MultivariateRealDetector detector, int... features) {
        addDetector(detector, FeatureMapping.of(features));
    }

    private void addDetector(Detector detector, FeatureMapping mapping) {
        detectors.put(detector, mapping);
        votingScheme.registerVoter(detector);
    }
}
