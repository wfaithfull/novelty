import uk.ac.bangor.novelty.*;
import uk.ac.bangor.novelty.ensemble.EnsembleFactory;
import uk.ac.bangor.novelty.ensemble.MultivariateRealEnsemble;
import uk.ac.bangor.novelty.ensemble.QuorumScheme;
import uk.ac.bangor.novelty.windowing.FixedWindowPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by wfaithfull on 06/09/16.
 */
@Slf4j
public class TestMultivariateDetectors {

    final int CHANGE_POINT = 500;
    final int FEATURES = 25;

    Stream<double[]> testDataProviderFactory() {

        RealDistribution pdf = new NormalDistribution(0, 1);
        RealDistribution pdf2 = new GammaDistribution(1, 2);

        Supplier<double[]> function = new Supplier<double[]>() {

            int counter = 0;

            @Override
            public double[] get() {

                double[] sample;

                if (counter < CHANGE_POINT)
                    sample = pdf.sample(FEATURES);
                else
                    sample = pdf2.sample(FEATURES);
                counter++;

                return sample;
            }
        };

        return Stream.generate(function);
    }

    @Test
    public void testSPLL() {
        log.info("Starting SPLL test");
        evaluate(new SPLL(new FixedWindowPair<>(25, 25, double[].class), 3));
    }

    @Test
    public void testKL() {
        log.info("Starting KL test");
        evaluate(new KL(new FixedWindowPair<>(25,25, double[].class), 3));
    }

    @Test
    public void testHotelling() {
        log.info("Starting Hotelling test");
        evaluate(new Hotelling(new FixedWindowPair<>(25, 25, double[].class)));
    }

    @Test
    public void testEnsembleOfUnivariates() {

        MultivariateRealEnsemble ensemble = new MultivariateRealEnsemble(new QuorumScheme(1.0/3));
        for(int i = 0; i < FEATURES; i++) {
            ensemble.addUnivariate(new CUSUM(), i);
            ensemble.addUnivariate(new EWMA(0.25), i);
            ensemble.addUnivariate(new Grubbs(50, 3), i);
            ensemble.addUnivariate(new MovingRange(), i);
        }

        evaluate(ensemble);
    }

    @Test
    public void testEnsembleOfMultivariates() {
        MultivariateRealEnsemble ensemble = new MultivariateRealEnsemble();
        ensemble.addMultivariate(new Hotelling(new FixedWindowPair<>(25, 25, double[].class)));
        ensemble.addMultivariate(new KL(new FixedWindowPair<>(25, 25, double[].class), 3));
        ensemble.addMultivariate(new SPLL(new FixedWindowPair<>(25, 25, double[].class), 3));

        evaluate(ensemble);
    }

    @Test
    public void testRandomDisjointSubspaceEnsemble() {
        // Define a function that takes the subspace size and returns a detector
        Function<Integer,MultivariateRealDetector> detectorFunction = subspaceSize ->
                new Hotelling(new FixedWindowPair<>(25, 25, double[].class));

        // Disjoint subspaces of size 3 (and remainder if necessary)
        MultivariateRealEnsemble ensemble = EnsembleFactory.buildRandomDisjointSubspaceEnsemble(FEATURES,3,detectorFunction);
        evaluate(ensemble);
    }

    @Test
    public void testRandomSubspaceEnsemble() throws IOException, ClassNotFoundException {

        // Define a function that takes the subspace size and returns a detector
        Function<Integer,MultivariateRealDetector> detectorFunction = subspaceSize ->
                new Hotelling(new FixedWindowPair<>(25, 25, double[].class));
        // 15 features per subspace, 10 subspaces.
        MultivariateRealEnsemble ensemble = EnsembleFactory.buildRandomSubspaceEnsemble(FEATURES,15,10,detectorFunction);
        evaluate(ensemble);
    }

    public void evaluate(Detector<double[]> multivariateDetector) {

        Stream<double[]> changeStream = testDataProviderFactory();

        ArrayList<Boolean> decisions = new ArrayList<>();

        changeStream.limit(1000).forEachOrdered(sample -> {
            multivariateDetector.update(sample);
            decisions.add(multivariateDetector.isChangeDetected());
        });

        long fp = decisions.subList(0, CHANGE_POINT).stream().filter(decision -> decision == true).count();
        List<Boolean> postDetection = decisions.subList(CHANGE_POINT, decisions.size());
        boolean detected = postDetection.stream().anyMatch(decision -> decision == true);

        log.info("FP: {}", fp);
        log.info("Detected: {}", detected ? "Yes" : "No");

        if(detected) {
            int count = 500;
            for(Boolean detection : postDetection) {
                if(detection)
                    break;
                count++;
            }

            log.info("TTD: {}", count - CHANGE_POINT);
        }
    }

}
