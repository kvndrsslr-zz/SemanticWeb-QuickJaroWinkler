package org.aksw.limes.metrics.speedup;


import org.apache.commons.lang3.tuple.*;
import sun.jvm.hotspot.utilities.WorkerThread;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public Map<String, Map<String, Double>> match () {

        ConcurrentHashMap<String, Map<String, Double>> similarityBook;
        //HashMap<String, Map<String, Double>> similarityBook;

        similarityBook = new
                ConcurrentHashMap<String, Map<String, Double>>(listA.size(), 1.0f);

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
        if (JaroWinklerLengthFilter.maxLenDeltaFor(1, threshold) != -1 && true) {
            List<ImmutableTriple<Integer, Integer, Integer>> sliceBoundaries =
                    JaroWinklerLengthFilter.getSliceBoundaries(blue.get(blue.size()-1).length(), threshold);
            for (ImmutableTriple<Integer, Integer, Integer> sliceBoundary : sliceBoundaries) {
                MutablePair<List<String>, List<String>> m = new MutablePair<List<String>, List<String>>();
                m.setLeft(new LinkedList<String>());
                m.setRight(new LinkedList<String>());
                for (String s : red)
                    if (s.length() >= sliceBoundary.getMiddle() && s.length() <= sliceBoundary.getRight())
                        m.getLeft().add(s);
                    else if (s.length() > sliceBoundary.getRight())
                        break;
                for (String s : blue)
                    if (s.length() == sliceBoundary.getLeft())
                        m.getRight().add(s);
                    else if (s.length() > sliceBoundary.getLeft())
                        break;
                if (m.getRight().size() > 0 && m.getLeft().size() > 0)
                    tempPairs.add(m);
            }
        } else {
            MutablePair<List<String>, List<String>> m = new MutablePair<List<String>, List<String>>();
            m.setLeft(red);
            m.setRight(blue);
            tempPairs.add(m);
        }

        System.out.println("Partitioned into " + String.valueOf(tempPairs.size()) + " sets.");
        System.out.println("Initializing Threadpool for " + String.valueOf(Runtime.getRuntime().availableProcessors()) + " threads.");

        //create thread pool, one thread per partition
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor = Executors.newFixedThreadPool(1);
        for (Pair<List<String>, List<String>> tempPair : tempPairs) {
            Runnable worker = new JaroWinklerTrieFilter(tempPair, similarityBook, metric.clone(), threshold);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
