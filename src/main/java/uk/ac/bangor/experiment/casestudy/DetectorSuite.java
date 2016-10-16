package uk.ac.bangor.experiment.casestudy;

import uk.ac.bangor.novelty.MultivariateRealDetector;

/**
 * Abstraction for a suite of detectors to be run
 * @author Will Faithfull
 */
public interface DetectorSuite {

    MultivariateRealDetector newCurrentDetector(int features);

    String getDetectorName();

    boolean hasNext();

    void advance();

    boolean first();

}
