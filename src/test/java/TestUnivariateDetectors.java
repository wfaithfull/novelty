import ac.uk.bangor.novelty.*;
import ac.uk.bangor.novelty.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by wfaithfull on 06/09/16.
 */
@Slf4j
public class TestUnivariateDetectors {

    final int CHANGE_POINT = 500;

    Stream<Double> testDataProviderFactory() {

        RealDistribution pdf = new NormalDistribution(0, 1);
        RealDistribution pdf2 = new GammaDistribution(1, 2);

        Supplier<Double> function = new Supplier<Double>() {

            int counter = 0;

            @Override
            public Double get() {

                double sample;

                if (counter < CHANGE_POINT)
                    sample = pdf.sample();
                else
                    sample = pdf2.sample();

                counter++;

                return sample;
            }
        };

        return Stream.generate(function);
    }

    @Test
    public void testEWMA() {
        log.info("Starting EWMA test");
        evaluate(new EWMA(0.25));
    }

    @Test
    public void testGrubbs() {
        log.info("Starting grubbs test");
        evaluate(new Grubbs(50, 3));
    }

    @Test
    public void testMovingRange() {
        log.info("Starting moving range test");
        evaluate(new MovingRange());
    }

    @Test
    public void testCUSUM() {
        log.info("Starting CUSUM test");
        evaluate(new CUSUM());
    }

    @Test
    public void printData() {
        Stream<Double> changeStream = testDataProviderFactory().limit(1000);

        String ml = CollectionUtils.toMatlabFormat(changeStream.mapToDouble(d -> d).toArray());
        log.info(ml);
    }

    public void evaluate(Detector<Double> univariateDetector) {

        Stream<Double> changeStream = testDataProviderFactory();

        ArrayList<Boolean> decisions = new ArrayList<>();

        changeStream.limit(1000).forEachOrdered(sample -> {
            univariateDetector.update(sample);
            decisions.add(univariateDetector.isChangeDetected());
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
