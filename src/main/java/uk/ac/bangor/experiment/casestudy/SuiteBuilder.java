package uk.ac.bangor.experiment.casestudy;

import uk.ac.bangor.novelty.windowing.FixedWindowPair;
import uk.ac.bangor.novelty.*;

import java.util.function.Supplier;

/**
 * @author Will Faithfull
 */
public class SuiteBuilder {

    public static DetectorSuite getEnsembleSuite(String name, Supplier<UnivariateRealDetector> univariateRealDetectorSupplier) {
        return new AbstractEnsembleDetectorSuite() {
            @Override
            protected String[] detectorNames() {
                return new String[] { name + 10, name + 20, name + 30, name + 40, name + 50 };
            }

            @Override
            public MultivariateRealDetector newCurrentDetector(int features) {
                switch (index) {
                    case 0:
                        return getEnsemble(features, 0.1, univariateRealDetectorSupplier);
                    case 1:
                        return getEnsemble(features, 0.2, univariateRealDetectorSupplier);
                    case 2:
                        return getEnsemble(features, 0.3, univariateRealDetectorSupplier);
                    case 3:
                        return getEnsemble(features, 0.4, univariateRealDetectorSupplier);
                    case 4:
                        return getEnsemble(features, 0.5, univariateRealDetectorSupplier);
                }
                throw new IllegalArgumentException("No detector for index " + index);
            }
        };
    }

    public static DetectorSuite getMultivariateSuite() {
        return new DetectorSuite() {

            String[] detectorNames = {"SPLL","KL","Hotelling"};
            Supplier<MultivariateRealDetector>[] detectors = new Supplier[] {
                    () -> new SPLL(new FixedWindowPair<>(25,25,double[].class), 3),
                    () -> new KL(new FixedWindowPair<>(25,25,double[].class),3),
                    () -> new Hotelling(new FixedWindowPair<>(25,25,double[].class))
            };
            int detectorIndex = 0;

            @Override
            public MultivariateRealDetector newCurrentDetector(int features) {
                return detectors[detectorIndex].get();
            }

            @Override
            public String getDetectorName() {
                return detectorNames[detectorIndex];
            }

            @Override
            public boolean hasNext() {
                return detectorIndex < detectors.length;
            }

            @Override
            public void advance() {
                detectorIndex++;
            }

            @Override
            public boolean first() {
                return detectorIndex == 0;
            }
        };
    }

}
