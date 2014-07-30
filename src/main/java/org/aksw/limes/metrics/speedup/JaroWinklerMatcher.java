package org.aksw.limes.metrics.speedup;

import org.semanticweb.yars.nx.parser.NxParser;

import java.io.FileInputStream;
import java.util.*;

/**
 * Matches one list of strings against the other,
 * using the given JaroWinklerMetric and an optional range filter
 */

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
            LengthQuicksort.sort((ArrayList<String>) listA);
            LengthQuicksort.sort((ArrayList<String>) listB);
            computeTargetRanges();
        } else {
            this.precomputeRanges = false;
        }
    }

    /**
     * precomputes relevant ranges for given threshold
     */
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

    /**
     * match lists
     * @return Map of string alignments which were better than given threshold
     */
    public HashMap<String, Map<String, Double>> match () {
        HashMap<String, Map<String, Double>> similarityBook;
        HashMap<String, Double> similarityTable;
        String a, b;
        double currentSim;
        int i, j, min, max;
        for (i = 0; i < listA.size(); i++) {
            min = precomputeRanges ? minTargetIndex.get(i) : 0;
            max = precomputeRanges ? maxTargetIndex.get(i) : listB.size() - 1;
            comps+=max-min+1;
        }
        long sumc = comps;
        similarityBook = new
                HashMap<String, Map<String, Double>>(listA.size(), 1.0f);
        comps = 0;
        for (i = 0; i < listA.size(); i++) {
            a = listA.get(i);
            min = precomputeRanges ? minTargetIndex.get(i) : 0;
            max = precomputeRanges ? maxTargetIndex.get(i) : listB.size() - 1;
            similarityTable = new HashMap<String,
                    Double>();
            for (j = min; j <= max; j++) {
                b = listB.get(j);
                if (j == min)
                    currentSim = metric.proximity(a, b);
                else
                    currentSim = metric.proximity(b);
                if (currentSim >= threshold)
                    similarityTable.put(b, currentSim);
                else if (currentSim == -1.0d)
                    comps++;
            }
            if (similarityTable.size() > 0) {
                similarityBook.put(a, (HashMap<String, Double>) (similarityTable.clone()));
            }
        }
        comps = sumc - comps;
        return similarityBook;
    }

    /**
     *
     * @return number of executed comparisons
     */
    public long getComps () {
        return this.comps;
    }


    public static void main (String[] args) {
        System.out.println(String.valueOf(Character.valueOf('X').hashCode()));
        /*
        ArrayList<String> sourceList, targetList;
        int lines = 1000;
        sourceList = new ArrayList<String>();
        targetList = new ArrayList<String>();
        TreeMap<Double, Long> compMap = new TreeMap<Double, Long>();
        try {
            NxParser nxp = new NxParser(new FileInputStream("labels_en.nt"));
            int i = 0;
            while (nxp.hasNext() && i < lines) {
                String tmp = nxp.next()[2].toN3();
                tmp = tmp.substring(1, tmp.lastIndexOf("@")-1);
                sourceList.add(tmp);
                targetList.add(tmp);
                i++;
            }
        } catch (Exception e) {
            // not
        }
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, false);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, true);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            metric.addFilter(new JaroWinklerLengthFilter(theta));
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, false);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            metric.addFilter(new JaroWinklerEntropyFilter((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), theta));
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, false);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            metric.addFilter(new JaroWinklerLengthFilter(theta));
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, true);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());

        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            metric.addFilter(new JaroWinklerEntropyFilter((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), theta));
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, true);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());
        for (double theta = 0.81d; theta < 1.0d; theta+=0.01d) {
            JaroWinklerMetric metric = new JaroWinklerMetric(true, false, false);
            metric.addFilter(new JaroWinklerLengthFilter(theta));
            metric.addFilter(new JaroWinklerEntropyFilter((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), theta));
            JaroWinklerMatcher jm = new JaroWinklerMatcher((ArrayList<String>) sourceList.clone(), (ArrayList<String>) targetList.clone(), metric, theta, true);
            jm.match();
            compMap.put(theta, jm.getComps());
        }
        System.out.println(compMap.toString());*/
    }
}
