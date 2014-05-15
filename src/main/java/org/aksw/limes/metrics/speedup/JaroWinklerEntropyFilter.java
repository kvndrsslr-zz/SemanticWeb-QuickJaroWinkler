package org.aksw.limes.metrics.speedup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

public class JaroWinklerEntropyFilter extends AbstractMetricFilter {

    private char[] a;
    private double aLen;
    private HashMap<Character, Integer> aEntropy;

    @Override
    protected void setReference(char[] x) {
        a = x;
        aLen = x.length;
        aEntropy = new HashMap<Character, Integer>(a.length);
        for (int i = 0; i < aLen; i++)
            aEntropy.put(a[i], aEntropy.get(a[i]) == null ? 1 : aEntropy.get(a[i]) + 1);
    }

    @Override
    protected double score(char[] b) {
        int i;
        double m = 0;
        double bLen = b.length;
        HashMap<Character, Integer> bEntropy = new HashMap<Character, Integer>(b.length);
        for (i = 0; i < bLen; i++)
            bEntropy.put(b[i], bEntropy.get(b[i]) == null ? 1 : bEntropy.get(b[i]) + 1);
        HashMap<Character, Integer> minEntropy = aLen > bLen ? bEntropy : aEntropy;
        HashMap<Character, Integer> maxEntropy = aLen > bLen ? aEntropy : bEntropy;
        for (Entry<Character, Integer> entry : minEntropy.entrySet()) {
            i = maxEntropy.get(entry.getKey()) == null ? 0 : maxEntropy.get(entry.getKey()).intValue();
            m += Math.min(entry.getValue().intValue(), i);
        }
        double upperBoundJaroWinkler = (1.0d) + (m / aLen) + (m / bLen);
        upperBoundJaroWinkler /= 3.0d;
        //System.err.println(String.valueOf(upperBoundJaroWinkler));
        if (upperBoundJaroWinkler > JaroWinklerMetric.winklerBoostThreshold) {
            int pMax = Math.min(Math.min(new Double(aLen).intValue(),new Double(bLen).intValue()), 4);
            for (i = 0; i < pMax && a[i] == b[i]; i++);
            if (i != 0)
                upperBoundJaroWinkler += 0.1d * (double) i * (1.0d - upperBoundJaroWinkler);
        }
        //System.err.println(String.valueOf(upperBoundJaroWinkler));
        return upperBoundJaroWinkler;
    }

    public JaroWinklerEntropyFilter(double threshold) {
        super(threshold);
        this.aEntropy = new HashMap<Character, Integer>();
    }

    @Override
    public boolean apply(char[] a, char[] b) {
        boolean r = super.apply(a, b);
        //if (r)
            //System.err.println("Filtered input: (" + String.valueOf(a) + "," + String.valueOf(b) + ") for entropy");
        return r;
    }
}
