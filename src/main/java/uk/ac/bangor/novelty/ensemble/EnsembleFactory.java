package uk.ac.bangor.novelty.ensemble;

import uk.ac.bangor.novelty.MultivariateRealDetector;

import java.util.Collections;
import java.util.Stack;
import java.util.function.Function;

/**
 * Factory class to build pre-configured ensembles under the implemented schemes.
 */
public class EnsembleFactory {

    /**
     * {@link #buildRandomDisjointSubspaceEnsemble(MultivariateRealEnsemble, int, int, Function)}
     */
    public static MultivariateRealEnsemble buildRandomDisjointSubspaceEnsemble(int nFeatures,
                                                                       int subspaceSize,
                                                                       Function<Integer,MultivariateRealDetector> detectorFunction) {
        return buildRandomDisjointSubspaceEnsemble(new MultivariateRealEnsemble(), nFeatures, subspaceSize, detectorFunction);
    }

    /**
     * Use the detector supplier to add multivariate detectors to equally-sized random subspaces.
     *
     * If the subspace is not cleanly divisible by the number of features then there will be one detector
     * mapped to the remainder. I.e. if we have 10 features and a subspace size of 3, we will have 4 subspaces:
     * a, b, c, d, where |a| = |b| = |c| = 3 and |d| = 1
     *
     * The subspaces are disjoint such that {a} ∩ {b} ∩ {c} ∩ {d}
     *
     * @param ensemble
     *                  The ensemble to which the detectors should be added.
     * @param nFeatures
     *                  The size of the feature space.
     * @param subspaceSize
     *                  The size of the subspaces.
     * @param detectorFunction
     *                  A factory providing a new instance of the multivariate detector accepting the subspace size.
     * @return
     *                  The provided ensemble with the mapped detectors added.
     */
    public static MultivariateRealEnsemble buildRandomDisjointSubspaceEnsemble(MultivariateRealEnsemble ensemble,
                                                                       int nFeatures,
                                                                       int subspaceSize,
                                                                       Function<Integer,MultivariateRealDetector> detectorFunction) {
        if(subspaceSize > nFeatures)
            throw new IllegalArgumentException("Subspace cannot be larger than the feature space.");

        Stack<Integer> availableFeatures = new Stack<>();
        for(int i=0;i<nFeatures;i++)
            availableFeatures.push(i);

        // Randomly permute stack
        Collections.shuffle(availableFeatures);

        for(int i=0;i<nFeatures; i+= subspaceSize) {
            int[] features = new int[Math.min(subspaceSize, availableFeatures.size())];
            int f = 0;
            int target = Math.max(0,availableFeatures.size() - subspaceSize);
            while(availableFeatures.size() > target) {
                features[f++] = availableFeatures.pop();
            }
            ensemble.addMultivariate(detectorFunction.apply(features.length), features);
        }

        return ensemble;
    }

    /**
     * {@link #buildRandomSubspaceEnsemble(MultivariateRealEnsemble, int, int, int, Function)}
     */
    public static MultivariateRealEnsemble buildRandomSubspaceEnsemble(int nFeatures,
                                                                       int subspaceSize,
                                                                       int nSubspaces,
                                                                       Function<Integer,MultivariateRealDetector> detectorFunction) {
        return buildRandomSubspaceEnsemble(new MultivariateRealEnsemble(), nFeatures, subspaceSize, nSubspaces, detectorFunction);
    }

    /**
     * Use the detector supplier to add multivariate detectors to specifically-sized random subspaces.
     *
     * The subspaces created by this method are not disjoint.
     *
     * @param ensemble
     *                  The ensemble to which the detectors should be added.
     * @param nFeatures
     *                  The size of the feature space.
     * @param subspaceSize
     *                  The size of the subspaces.
     * @param nSubspaces
     *                  The number of subspaces
     * @param detectorFunction
     *                  A factory providing a new instance of the multivariate detector accepting the subspace size.
     * @return
     *                  The provided ensemble with the mapped detectors added.
     */
    public static MultivariateRealEnsemble buildRandomSubspaceEnsemble(MultivariateRealEnsemble ensemble,
                                                                               int nFeatures,
                                                                               int subspaceSize,
                                                                               int nSubspaces,
                                                                               Function<Integer,MultivariateRealDetector> detectorFunction) {
        if(subspaceSize > nFeatures)
            throw new IllegalArgumentException("Subspace cannot be larger than the feature space.");

        for(int i = 0; i<nSubspaces; i++) {
            Stack<Integer> availableFeatures = new Stack<>();
            for(int j=0;j<nFeatures;j++)
                availableFeatures.push(j);

            Collections.shuffle(availableFeatures);

            int[] features = new int[subspaceSize];
            for(int j=0;j<subspaceSize;j++) {
                features[j]=availableFeatures.pop();
            }

            ensemble.addMultivariate(detectorFunction.apply(features.length), features);
        }

        return ensemble;
    }
}
