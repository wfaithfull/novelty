package uk.ac.bangor.experiment;

import uk.ac.bangor.novelty.*;
import uk.ac.bangor.novelty.ensemble.FeatureWeightedSubsetEnsemble;
import uk.ac.bangor.novelty.ensemble.MultivariateRealEnsemble;
import uk.ac.bangor.novelty.ensemble.QuorumScheme;
import uk.ac.bangor.novelty.windowing.FixedWindowPair;
import weka.core.CommandlineRunnable;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Resample;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;

public class Novelty implements CommandlineRunnable {

    int m_TotalLength = 1000;

    int m_ChangePoint = 500;

    int m_ChangeDuration = 100;

    int m_Repetitions = 100;

    Random m_Random = null;

    int m_Seed = 0;

    private double [] m_StandardDeviations = null;

    public boolean[] selectSubset(int n) {
        boolean[] selected = new boolean[n];

        // At least we have to have one selected
        int firstSelected = m_Random.nextInt(n);
        selected[firstSelected] = true;

        // At least we have to have one unselected
        int firstUnselected = m_Random.nextInt(n - 1);
        if (firstUnselected >= firstSelected) {
            firstUnselected++;
        }

        for (int i = 0; i < n; i++) {
            if (i != firstSelected && i != firstUnselected) {
                if (m_Random.nextBoolean()) {
                    selected[i] = true;
                }
            }
        }

        return selected;
    }


    private void extend(Instances instances, int newSize) throws Exception {
        int n = instances.numInstances();
        if( n >= newSize)
            return;

        Resample resample = new Resample();
        resample.setInputFormat(instances);
        resample.setSampleSizePercent(100.0 / n * (newSize + 10 - n));
        Instances additional = Filter.useFilter(instances, resample);

        for(Instance instance: additional) {
            double [] x = instance.toDoubleArray();
            for(int i = 0; i < x.length; i++) {
                x[i] += 0.01 * m_StandardDeviations[i] * m_Random.nextGaussian();
            }
            instances.add(new DenseInstance(instance.weight(), x));
        }
    }


    public Instances generateDataset(Instances source, boolean gradual) throws Exception {
        boolean [] selected = selectSubset(source.numClasses());

        Instances before = new Instances(source, 0);
        Instances after = new Instances(source, 0);

        for(Instance instance: source) {
            if(selected[(int)instance.classValue()])
                before.add(instance);
            else
                after.add(instance);
        }

        Remove remove = new Remove();
        remove.setAttributeIndices("last");
        remove.setInputFormat(source);
        before = Filter.useFilter(before, remove);
        after = Filter.useFilter(after, remove);

        before.randomize(m_Random);
        after.randomize(m_Random);

        extend(before, m_ChangePoint + (gradual ? m_ChangeDuration : 0));
        extend(after, m_TotalLength - m_ChangePoint);

        Instances generated = new Instances(before, 0);
        int i = 0, j = 0;
        for(; i < m_ChangePoint; i++) {
            generated.add(before.instance(i));
        }

        if(gradual) {
            for(int k = 0; k < m_ChangeDuration; k++) {
                if(m_Random.nextDouble() > (k + 0.5) / m_ChangeDuration) {
                    generated.add(before.instance(i++));
                }
                else {
                    generated.add(after.instance(j++));
                }
            }
        }

        for(; i + j < m_TotalLength; j++) {
            generated.add(after.instance(j));
        }
        return generated;
    }

    public int getNumDetectors() {
        return 5;
    }

    public String getDetectorName(int d) {
        String [] names = {"Hotelling", "KL", "SPLL", "CUSUM Ensemble","Feature Weighted Subset Ensemble"};
        return names[d];
    }

    public MultivariateRealDetector getNewDetector(int d, Instances stream) {
        int length = Integer.max(25, (stream.numAttributes() + 3)/2);
        switch (d) {
            case 0:
                return new Hotelling(new FixedWindowPair<>(length, length, double[].class));
            case 1:
                return new KL(new FixedWindowPair<>(25, 25, double[].class), 3);
            case 2:
                return new SPLL(new FixedWindowPair<>(25, 25, double[].class), 3);
            case 3:
                MultivariateRealEnsemble ensemble = new MultivariateRealEnsemble(new QuorumScheme(1d/3));
                for(int i = 0; i < stream.numAttributes(); i++) {
                    ensemble.addUnivariate(new CUSUM(), i);
                }
                return ensemble;
            case 4:
                return new FeatureWeightedSubsetEnsemble(stream.numAttributes(), stream.numAttributes() / 2, 9, x -> new SPLL(new FixedWindowPair<>(25,25,double[].class),3), () -> new CUSUM());
                /*MultivariateRealEnsemble mvEnsemble = new MultivariateRealEnsemble();
                mvEnsemble.addMultivariate(new Hotelling(new FixedWindowPair<>(length, length, double[].class)));
                mvEnsemble.addMultivariate(new KL(new FixedWindowPair<>(25, 25, double[].class), 3));
                mvEnsemble.addMultivariate(new SPLL(new FixedWindowPair<>(25, 25, double[].class), 3));
                return mvEnsemble;*/
        }
        return null;
    }

    ExecutorService executor = new ThreadPoolExecutor(2, 2, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(2));

    public void process(String fileName) throws Exception {
        Instances instances = new Instances(new BufferedReader(new FileReader(fileName)));
        instances.setClassIndex(instances.numAttributes() - 1);

        m_StandardDeviations = new double[instances.numAttributes() - 1];
        for(int i = 0; i < m_StandardDeviations.length; i++) {
            m_StandardDeviations[i] = instances.attributeStats(i).numericStats.stdDev;
        }

        m_Random = instances.getRandomNumberGenerator(m_Seed);

        boolean[] gradual = { false, true };
        for (boolean isGradual : gradual) {
            double[] meanARL = new double[getNumDetectors()]; // Average Run Length
            double[] meanTTD = new double[getNumDetectors()]; // Time To Detection
            double[] meanMDR = new double[getNumDetectors()]; // Missed Detection Ratio
            double[] probNFA = new double[getNumDetectors()]; // No False Alarms

            BlockingQueue queue = new ArrayBlockingQueue(m_Repetitions);
            for (int i = 0; i < m_Repetitions; i++) {
                //progressBar.update(i, m_Repetitions, isGradual ? "Gradual" : "Abrupt");
                int finalI = i;
                Runnable task = () -> {
                    Instances stream = null;
                    progressBar.update(finalI, m_Repetitions, isGradual ? "Gradual" : "Abrupt");
                    try {
                        stream = generateDataset(instances, isGradual);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (int d = 0; d < getNumDetectors(); d++) {
                        MultivariateRealDetector detector = getNewDetector(d, stream);
                        int arl = -1;
                        int ttd = -1;
                        int j = 0;
                        for (Instance instance : stream) {
                            double[] x = instance.toDoubleArray();
                            detector.update(x);
                            if (detector.isChangeDetected()) {
                                detector = getNewDetector(d, stream);
                                if (j < m_ChangePoint && arl < 0)
                                    arl = j;
                                if (j >= m_ChangePoint) {
                                    ttd = j - m_ChangePoint;
                                    break;
                                }
                            }
                            j++;
                        }
                        if (arl < 0) {
                            arl = m_ChangePoint;
                            probNFA[d]++;
                        }
                        meanARL[d] += arl;
                        if (ttd < 0) {
                            ttd = m_TotalLength - m_ChangePoint;
                            meanMDR[d]++;
                        }
                        meanTTD[d] += ttd;
                    }
                };
                queue.add(task);
            }
            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(8, 8, 10, TimeUnit.MINUTES, queue);
            poolExecutor.prestartAllCoreThreads();
            poolExecutor.shutdown();
            poolExecutor.awaitTermination(1, TimeUnit.HOURS);

            for (int d = 0; d < meanARL.length; d++) {
                meanARL[d] /= (double) m_Repetitions;
                meanTTD[d] /= (double) m_Repetitions;
                meanMDR[d] *= 100.0 / m_Repetitions;
                probNFA[d] *= 100.0 / m_Repetitions;
            }

            PrintStream file = new PrintStream(new FileOutputStream(
                    "detections_" + instances.relationName() + ".csv", true));

            if(!isGradual)
                file.printf("type, dataset, detector, ARL, TTD, NFA, MDR\n");

            for (int d = 0; d < meanARL.length; d++) {
                file.printf("%s, %s, %s, %f, %f, %f, %f\n", isGradual ? "gradual" : "abrupt",
                        instances.relationName(), getDetectorName(d),
                        meanARL[d], meanTTD[d], probNFA[d], meanMDR[d]);
            }
            file.close();
        }
    }

    static ProgressBar progressBar = new ProgressBar('=', 50);

    public static void main(String[] args) throws Exception {
        Novelty novelty = new Novelty();
        novelty.run(novelty, args);
    }

    @Override
    public void run(Object toRun, String[] options) {
        Locale.setDefault(new Locale("en", "US"));
        if(options.length == 0) {
            System.err.println("Usage: novelty.Novelty <arff files>");
        }
        for(String arg: options) {
            System.out.println(arg);
            try {
                process(arg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}