package uk.ac.bangor.novelty.ensemble;


import uk.ac.bangor.novelty.Detector;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class FeatureMapping {
    int[] features;
    boolean isAllFeatures = false;

    public static FeatureMapping of(int... features) {
        return new FeatureMapping(features);
    }

    public static FeatureMapping ofSingle(int feature) {
        return new FeatureMapping(feature);
    }

    public static FeatureMapping ofAllFeatures() {
        return new FeatureMapping();
    }

    private FeatureMapping(int... features) {
        this.features = Arrays.copyOf(features, features.length);
    }

    private FeatureMapping() {
        isAllFeatures = true;
    }

    public void update(double[] example, Detector<double[]> detector) {
        if(isAllFeatures) {
            detector.update(example);
            return;
        }

        double[] trimmed = new double[count()];
        int counter = 0;
        for(int feature : features) {
            trimmed[counter++] = example[feature];
        }

        detector.update(trimmed);
    }

    public boolean mapsFeature(int feature) {
        if(isAllFeatures)
            return true;

        for(int f : features) {
            if(f == feature)
                return true;
        }
        return false;
    }

    public int count() {
        return features.length;
    }
}
