package uk.ac.bangor.novelty;

import java.util.HashMap;

/**
 * @author Will Faithfull
 */
public class ControlChartConstants {

    private static final HashMap<Integer, Double> D4 = new HashMap<>();
    public static double D4(int n) { return D4.get(n); }
    static {
        D4.put(2,3.267);
        D4.put(3,2.574);
        D4.put(4,2.282);
        D4.put(5,2.114);
        D4.put(6,2.004);
        D4.put(7,1.924);
        D4.put(8,1.864);
        D4.put(9,1.816);
        D4.put(10,1.777);
        D4.put(11,1.744);
        D4.put(12,1.717);
        D4.put(13,1.693);
        D4.put(14,1.672);
        D4.put(15,1.653);
        D4.put(16,1.637);
        D4.put(17,1.622);
        D4.put(18,1.608);
        D4.put(19,1.597);
        D4.put(20,1.585);
        D4.put(21,1.575);
        D4.put(22,1.566);
        D4.put(23,1.557);
        D4.put(24,1.548);
        D4.put(25,1.541);
    }

    private static final HashMap<Integer, Double> d2 = new HashMap<>();
    public static double d2(int n) { return d2.get(n); }
    static {
        d2.put(2,1.128);
        d2.put(3,1.693);
        d2.put(4,2.059);
        d2.put(5,2.326);
        d2.put(6,2.534);
        d2.put(7,2.704);
        d2.put(8,2.847);
        d2.put(9,2.970);
        d2.put(10,3.078);
        d2.put(11,3.173);
        d2.put(12,3.258);
        d2.put(13,3.336);
        d2.put(14,3.407);
        d2.put(15,3.472);
        d2.put(16,3.532);
        d2.put(17,3.588);
        d2.put(18,3.640);
        d2.put(19,3.689);
        d2.put(20,3.735);
        d2.put(21,3.778);
        d2.put(22,3.819);
        d2.put(23,3.858);
        d2.put(24,3.895);
        d2.put(25,3.931);
    }

}