package org.aksw.limes.metrics.speedup;


import org.apache.commons.lang3.tuple.*;

import java.util.*;

/**
 * Matches one list of strings against the other,
 * using the given JaroWinklerMetric and an optional range filter
 */
@SuppressWarnings("unchecked")

public class JaroWinklerMatcher {

    private double threshold;
    private long comps;
    private List<String> listA, listB;
    private JaroWinklerMetric metric;

    public JaroWinklerMatcher (List<String> listA, List<String> listB, JaroWinklerMetric metric, double theshold) {
        this.threshold = theshold;
        this.metric = metric;
        this.listB = listB;
        this.listA = listA;
        if (JaroWinklerLengthFilter.maxLenDeltaFor(1, threshold) != -1) {
            LengthQuicksort.sort((ArrayList<String>) listA);
            LengthQuicksort.sort((ArrayList<String>) listB);
        }
    }

    /**
     * match lists
     * @return Map of string alignments which were better than given threshold
     */
    public HashMap<String, Map<String, Double>> match () {
        HashMap<String, Map<String, Double>> similarityBook;
        HashMap<String, Double> similarityTable;
        double currentSim;
        boolean first;
        similarityBook = new
                HashMap<String, Map<String, Double>>(listA.size(), 1.0f);

        /*

        List<String> red, blue;
        red = listA;
        blue = listB;
        LengthQuicksort.sort((ArrayList<String>) red);
        LengthQuicksort.sort((ArrayList<String>) blue);
        // red is the list with the longest string
        if (red.get(red.size()-1).length() < blue.get(blue.size()-1).length()) {
            List<String> temp = red;
            red = blue;
            blue = temp;
        }


        List<Pair<List<String>, List<String>>> tempPairs = new LinkedList<Pair<List<String>, List<String>>>();
        // generate length filtered partitions
        if (JaroWinklerLengthFilter.maxLenDeltaFor(1, threshold) != -1) {
            //@todo: test dual threshold specification (sweet spot around 0.91d)
            List<ImmutableTriple<Integer, Integer, Integer>> sliceBoundaries =
                    JaroWinklerLengthFilter.getSliceBoundaries(red.get(red.size()-1).length(), threshold);
            for (ImmutableTriple<Integer, Integer, Integer> sliceBoundary : sliceBoundaries) {
                MutablePair<List<String>, List<String>> m = new MutablePair<List<String>, List<String>>();
                m.setLeft(new LinkedList<String>());
                m.setRight(new LinkedList<String>());
                for (String s : red)
                    if (s.length() >= sliceBoundary.getLeft() && s.length() <= sliceBoundary.getMiddle())
                        m.getLeft().add(s);
                for (String s : blue)
                    if (s.length() >= sliceBoundary.getLeft() && s.length() <= sliceBoundary.getRight())
                        m.getRight().add(s);
                tempPairs.add(m);
            }
        } else {
            MutablePair<List<String>, List<String>> m = new MutablePair<List<String>, List<String>>();
            m.setLeft(red);
            m.setRight(blue);
            tempPairs.add(m);
        }


        //@todo: create thread pool, port triefilter to Runable
        */


        JaroWinklerTrieFilter trieFilter = new JaroWinklerTrieFilter(listA, listB, threshold);
        List<Pair<List<String>,List<String>>> filteredPairs = trieFilter.getFilteredPairs();
        for (Pair<List<String>, List<String>> filteredPair : filteredPairs) {
            for (String a : filteredPair.getLeft()) {
                first = true;
                similarityTable = new HashMap<String,
                        Double>();
                for (String b : filteredPair.getRight()) {
                    if (first)
                        currentSim = metric.proximity(a, b);
                    else
                        currentSim = metric.proximity(b);
                    if (currentSim >= threshold)
                        similarityTable.put(b, currentSim);
                    if (currentSim > -1.0d)
                        comps++;
                    first = false;
                }
                if (similarityTable.size() > 0) {
                    similarityBook.put(a, (HashMap<String, Double>) (similarityTable.clone()));
                }
            }
        }
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

    }
}
