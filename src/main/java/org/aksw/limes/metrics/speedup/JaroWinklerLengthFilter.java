package org.aksw.limes.metrics.speedup;

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
        //if (r)
            //System.err.println("Filtered input: (" + String.valueOf(a) + "," + String.valueOf(b) + ") for length");
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
}
