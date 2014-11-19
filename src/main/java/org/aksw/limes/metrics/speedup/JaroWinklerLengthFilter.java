package org.aksw.limes.metrics.speedup;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.LinkedList;

/**
 * Length filter implementation
 */

public class JaroWinklerLengthFilter extends AbstractMetricFilter {

    private char[] a;
    private double aLen;

    @Override
    protected void setReference (char[] x) {
        a = x;
        aLen = x.length;
    }

    @Override
    protected double score (char[] b) {
        int p;
        aLen = Math.min(a.length,b.length);
        double bLen = Math.max(a.length, b.length);
        double upperBoundJaroWinkler = (2.0d / 3.0d) + (aLen / (3.0d * bLen));
        // this fixes correctness issues caused by floating point precision
        upperBoundJaroWinkler += 0.00000000000001d;
        if (upperBoundJaroWinkler > JaroWinklerMetric.winklerBoostThreshold) {
            int pMax = Math.min(new Double(aLen).intValue(), 4);
            for (p = 0; p < pMax && a[p] == b[p]; p++);
            if (p != 0)
                upperBoundJaroWinkler += 0.1d * p * (1.0d - upperBoundJaroWinkler);
        }
        return upperBoundJaroWinkler;
    }

    public JaroWinklerLengthFilter(double threshold) {
        super(threshold);
    }

    @Override
    public boolean apply (char[] a, char[] b) {
        boolean r = super.apply(a, b);
        return r;
    }

    public static int maxLenDeltaFor (int aLen, double threshold) {
        // when threshold is not over 0.8 then a delta can approach infinity
        // infinity is not available for integers in java, so this uses -1
        if (threshold <= 0.8d)
            return -1;
        else
            return (int) Math.round(Math.ceil((0.6d * (double) aLen)/(3.0d * threshold - 2.4d) - (double) aLen));
    }


    public static int minLenDeltaFor (int bLen, double threshold) {
            return (int) Math.round(Math.floor(
                    ((bLen * (3.0d * threshold - 2.4d)) / 0.6d) - (double) bLen
            ));
    }

    public static LinkedList<ImmutableTriple<Integer, Integer, Integer>> getSliceBoundaries(int maxSize, double threshold) {
        LinkedList<ImmutableTriple<Integer, Integer, Integer>> sliceBoundaries = new LinkedList<ImmutableTriple<Integer, Integer, Integer>>();
        for (int t = 1; t <= maxSize; t++) {
            sliceBoundaries.add(new ImmutableTriple<Integer, Integer, Integer>(t, t+minLenDeltaFor(t, threshold), t+maxLenDeltaFor(t, threshold)));
        }
        return sliceBoundaries;
    }

    public static void main (String[] args) {
        int a = JaroWinklerLengthFilter.minLenDeltaFor(5, 0.96);
        JaroWinklerLengthFilter dummy = new JaroWinklerLengthFilter(0.8d);
        double x = dummy.score("Intel".toCharArray(),"International Association of Travel Agents Network".toCharArray());
        double y= x;
    }
}
