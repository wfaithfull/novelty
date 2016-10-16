package ac.uk.bangor.experiment.casestudy;

import ac.uk.bangor.experiment.MoaDetectorAdapter;
import ac.uk.bangor.experiment.ProgressBar;
import ac.uk.bangor.novelty.CUSUM;
import ac.uk.bangor.novelty.MultivariateRealDetector;
import moa.core.InstanceExample;
import moa.streams.ArffFileStream;
import weka.core.CommandlineRunnable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will Faithfull
 */
public class CaseStudy implements CommandlineRunnable {

    ProgressBar progressBar = new ProgressBar('=',50);
    List<ChangePoint> changePoints = new ArrayList<>();
    List<Long> detections = new ArrayList<>();

    public static void main(String[] args) {
        CaseStudy obj = new CaseStudy();
        obj.run(obj, args);
    }

    @Override
    public void run(Object o, String[] strings) throws IllegalArgumentException {
        DetectorSuite mvSuite = SuiteBuilder.getMultivariateSuite();
        DetectorSuite cusumSuite = SuiteBuilder.getEnsembleSuite("CUSUM-", () -> new CUSUM());
        DetectorSuite seq1Suite = SuiteBuilder.getEnsembleSuite("SEQ1-", () -> MoaDetectorAdapter.seq1());
        DetectorSuite seedSuite = SuiteBuilder.getEnsembleSuite("SEED-", () -> MoaDetectorAdapter.seed());

        for(String fileName : strings) {
            doSuite(fileName, mvSuite);
            doSuite(fileName, cusumSuite);
            doSuite(fileName, seq1Suite);
            doSuite(fileName, seedSuite);
        }
    }

    public void doSuite(String fileName, DetectorSuite suite) {
        while (suite.hasNext()) {
            ArffFileStream fileStream = new ArffFileStream(fileName, -1);
            process(fileStream, suite);
            progressBar = new ProgressBar('=', 50);
        }
    }

    public void process(ArffFileStream fileStream, DetectorSuite suite) {

        int features = fileStream.getHeader().numInputAttributes();
        List<String> classAttributeValues = fileStream.getHeader().classAttribute().getAttributeValues();

        MultivariateRealDetector detector = suite.newCurrentDetector(features);
        String detectorName = suite.getDetectorName();

        changePoints = new ArrayList<>();
        detections = new ArrayList<>();

        long total = 0;
        long count = 0;

        long averageRunLength = 0;
        int falsePositives = 0;
        int lastClassIndex = -1;
        for(InstanceExample instance = fileStream.nextInstance(); fileStream.hasMoreInstances(); instance = fileStream.nextInstance()) {

            if(count == total) {
                long newTotal = fileStream.estimatedRemainingInstances();
                if(newTotal > total)
                    total = newTotal;
            }

            double[] data = instance.getData().toDoubleArray();
            detector.update(data);
            int classIndex = (int)data[data.length-1];
            String label = classAttributeValues.get(classIndex);
            if(detector.isChangeDetected()) {
                if(detections.size() == 0) {
                    averageRunLength += count;
                } else {
                    averageRunLength += (count - detections.get(detections.size()-1));
                }

                if(changePoints.size() > 0) {
                    int changePointIndex = changePoints.size() - 1;
                    if(changePoints.get(changePointIndex).getDetected() != -1) {
                        falsePositives++;
                    } else {
                        changePoints.get(changePointIndex).setDetected(count);
                    }
                }
                detections.add(count);

                detector = suite.newCurrentDetector(features); // Reset
            }
            count++;
            if(count % 1000 == 0) {
                progressBar.update(count, total, "Processing " + detectorName + "... (" + count + " / " + total + ")");
            }

            if(lastClassIndex != classIndex && lastClassIndex != -1) {
                changePoints.add(new ChangePoint(count, classAttributeValues.get(lastClassIndex), label));
            }

            lastClassIndex = classIndex;
        }

        double ARL = averageRunLength / (double)detections.size();

        double meanTTD, medianTTD, detectionRatio;
        meanTTD = medianTTD = detectionRatio = 0.0;

        if(this.detections.size() > 0) {

            List<ChangePoint> detectedChangePoints = changePoints.stream().filter(cp -> cp.getDetected() != -1).collect(Collectors.toList());
            meanTTD      = detectedChangePoints.stream().mapToLong(cp -> cp.getTtd()).summaryStatistics().getAverage();
            medianTTD    = detectedChangePoints.get(detectedChangePoints.size()/2).getTtd();

            detectionRatio = detectedChangePoints.size() / (double) changePoints.size();
        }

        try(PrintStream out = new PrintStream(new FileOutputStream("detections_" + fileStream.getHeader().getRelationName() + ".csv", true))) {
            if(suite.first()) {
                out.printf("detector, Mean TTD, Median TTD, Detection Ratio, ARL, False Positives out of %d\n", total);
            }
            out.printf("%s, %.2f, %.2f, %.2f, %.2f, %d\n", detectorName, meanTTD, medianTTD, detectionRatio, ARL, falsePositives);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        suite.advance();
        System.out.println();
    }

    /**
     * Helper method which performs a binary search to find the closest ChangePoint to the supplied index
     * @param index The index for which to find the closest ChangePoint
     * @param changePoints The list of ChangePoints to search
     * @return The closest ChangePoint to the supplied index
     */
    private static ChangePoint closestBinarySearch(long index, List<ChangePoint> changePoints) {
        int lower = 0;
        int upper = changePoints.size()-1;
        int middle = 0;

        ChangePoint closest = null;

        while(lower <= upper) {
            middle = (lower+upper)/2;
            closest = changePoints.get(middle);
            if(index < closest.getIndex())
                upper = middle - 1;
            else if (index > closest.getIndex())
                lower = middle + 1;
            else
                break;
        }

        if(middle > 0) {
            ChangePoint nextLow = changePoints.get(middle-1);
            if(Math.abs(index - closest.getIndex()) > Math.abs(index - nextLow.getIndex()))
                closest = nextLow;
        }

        return closest;
    }

}
