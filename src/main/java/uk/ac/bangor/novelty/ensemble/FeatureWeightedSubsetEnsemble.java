package uk.ac.bangor.novelty.ensemble;

import uk.ac.bangor.novelty.Detector;
import uk.ac.bangor.novelty.MultivariateRealDetector;
import uk.ac.bangor.novelty.UnivariateRealDetector;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class FeatureWeightedSubsetEnsemble extends MultivariateRealEnsemble {

    UnivariateRealDetector[] guardians;
    private int nFeatures;
    private Supplier<UnivariateRealDetector> guardianSupplier;
    private boolean[] featureChanges;

    public FeatureWeightedSubsetEnsemble(int nFeatures,
                                         int subspaceSize,
                                         int nSubspaces,
                                         Function<Integer,MultivariateRealDetector> detectorFunction,
                                         Supplier<UnivariateRealDetector> guardianSupplier) {
        this.nFeatures = nFeatures;
        this.guardianSupplier = guardianSupplier;
        guardians = new UnivariateRealDetector[nFeatures];
        for(int i=0;i<nFeatures;i++) {
            guardians[i] = guardianSupplier.get();
        }
        featureChanges = new boolean[nFeatures];
        EnsembleFactory.buildRandomSubspaceEnsemble(this, nFeatures, subspaceSize, nSubspaces, detectorFunction);
    }

    @Override
    public void update(double[] example) {
        if(example.length != nFeatures)
            throw new RuntimeException("You cannot change the number of features without recreating the ensemble.");

        super.update(example);
        for(int i=0;i<guardians.length;i++) {
            guardians[i].update(example[i]);
            featureChanges[i] = guardians[i].isChangeDetected();
        }

        updateWeights();
    }

    public void updateWeights() {
        VotingScheme votingScheme = this.getVotingScheme();

        //Map<Detector, Double> scores = new HashMap<>();

        for(Map.Entry<Detector, FeatureMapping> entry : this.detectors.entrySet()) {
            FeatureMapping mapping = entry.getValue();

            int score = 0;
            if(mapping.isAllFeatures) {
                score = countTrue(featureChanges);
            } else {
                for(int feature : mapping.getFeatures()) {
                    if(featureChanges[feature])
                        score++;
                }
            }

            //scores.put(entry.getKey(), (double)score);
            votingScheme.setWeight(entry.getKey(), (double)score);
        }

    }

    private static int countTrue(boolean[] input) {
        int trues = 0;
        for(int i=0;i<input.length;i++) {
            if(input[i])
                trues++;
        }
        return trues;
    }
}
