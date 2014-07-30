package org.aksw.limes.metrics.speedup;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Character frequency filter implementation
 */

public class JaroWinklerEntropyFilter extends AbstractMetricFilter {

    private char[] a;
    private double aLen;
    private HashMap<String, HashMap<Character, Integer>> aEntropyMap, bEntropyMap;
    HashMap<Character, Integer> aEntropy;

    @Override
    protected void setReference(char[] x) {
        a = x;
        aLen = x.length;
        aEntropy = aEntropyMap.get(new String(x));
    }

    @Override
    protected double score(char[] b) {
        int i;
        double m = 0;
        double bLen = b.length;
        HashMap<Character, Integer> bEntropy = aEntropyMap.get(new String(b));
        if (aEntropy == null || bEntropy == null)
            return 1.0d;
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

    public JaroWinklerEntropyFilter(List<String> listA, List<String> listB, double threshold) {
        super(threshold);
        this.aEntropyMap = this.constructEntropyMap(listA);
        this.bEntropyMap = this.constructEntropyMap(listB);
        this.aEntropy = new HashMap<Character, Integer>();
    }

    private HashMap<String, HashMap<Character, Integer>> constructEntropyMap(List<String> list) {
        HashMap<Character, Integer> returnRow;
        HashMap<String, HashMap<Character, Integer>> returnMap = new HashMap<String, HashMap<Character, Integer>>();
        for (String x : list) {
            x = x.toUpperCase();
            returnRow = new HashMap<Character, Integer>();
            for (int i = 0; i < x.length(); i++)
                returnRow.put(x.toCharArray()[i], returnRow.get(x.toCharArray()[i]) == null ? 1 : returnRow.get(x.toCharArray()[i]) + 1);
            returnMap.put(x, (HashMap<Character, Integer>) returnRow.clone());
        }
        return returnMap;
    }

    @Override
    public boolean apply(char[] a, char[] b) {
        boolean r = super.apply(a, b);
        //if (r)
            //System.err.println("Filtered input: (" + String.valueOf(a) + "," + String.valueOf(b) + ") for entropy");
        return r;
    }
}
