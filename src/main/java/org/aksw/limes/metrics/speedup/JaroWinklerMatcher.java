package org.aksw.limes.metrics.speedup;

import java.util.*;

public class JaroWinklerMatcher {

    private double threshold;
    private long comps;
    private List<String> listA, listB;
    private List<Integer> minTargetIndex, maxTargetIndex;
    private JaroWinklerMetric metric;
    boolean precomputeRanges;

    public JaroWinklerMatcher (List<String> listA, List<String> listB, JaroWinklerMetric metric, double theshold, boolean precomputeRanges) {
        this.threshold = theshold;
        this.metric = metric;
        this.minTargetIndex = new ArrayList<Integer>(listA.size());
        this.maxTargetIndex = new ArrayList<Integer>(listA.size());
        this.listB = listB;
        this.listA = listA;
        this.precomputeRanges = precomputeRanges;
        if (precomputeRanges && JaroWinklerLengthFilter.maxLenDeltaFor(1, threshold) != -1) {
            Quicksort.sort((ArrayList<String>) listA);
            Quicksort.sort((ArrayList<String>) listB);
            computeTargetRanges();
        } else {
            this.precomputeRanges = false;
        }
    }

    private void computeTargetRanges () {
        String s1, s2;
        int i, j, minDelta, maxDelta, minIndex, maxIndex, offset;
        double offsetCalc;
        offsetCalc = Math.log10((double) listB.size());
        offset = (int) Math.round(Math.pow(offsetCalc / 2.0d, offsetCalc));
        minIndex = 0;
        maxIndex = listB.size() - 1;
        for (i = 0; i < listA.size(); i++) {
            s1 = listA.get(i);
            if (i == 0 || s1.length() != listA.get(i-1).length()) {
                maxIndex = listB.size() - 1;
                maxDelta = JaroWinklerLengthFilter.maxLenDeltaFor(s1.length(),
                        threshold);
                minDelta = JaroWinklerLengthFilter.minLenDeltaFor(s1.length(),
                        threshold);
                for (j = minIndex; j < listB.size(); j+=offset) {
                    s2 = listB.get(j);
                    if (s2.length() < s1.length() + minDelta)
                        minIndex = j;
                    if (s2.length() > s1.length() + maxDelta) {
                        maxIndex = j;
                        break;
                    }
                }
            }
            maxTargetIndex.add(maxIndex);
            minTargetIndex.add(minIndex);
        }
    }

    public HashMap<String, Map<String, Double>> match () {
        HashMap<String, Map<String, Double>> similarityBook = new HashMap<String, Map<String, Double>>();
        HashMap<String, Double> similarityTable = new HashMap<String, Double>();
        String a, b;
        double currentSim;
        int i, j, min, max;
        for (i = 0; i < listA.size(); i++) {
            a = listA.get(i);
            min = precomputeRanges ? minTargetIndex.get(i) : 0;
            max = precomputeRanges ? maxTargetIndex.get(i) : listB.size() - 1;
            for (j = min; j <= max; j++) {
                b = listB.get(j);
                if (j == min)
                    currentSim = metric.proximity(a, b);
                else
                    currentSim = metric.proximity(b);
                if (currentSim >= threshold)
                    similarityTable.put(b, currentSim);
                comps++;
            }
            if (similarityTable.size() > 0)
                similarityBook.put(a, (HashMap<String, Double>)(similarityTable.clone()));
            similarityTable.clear();
        }
        return similarityBook;
    }

    public long getComps () {
        return this.comps;
    }

}
