package org.aksw.limes.metrics.speedup;


import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class JaroWinklerTrieFilter implements Runnable {

    private double threshold;
    Pair<List<String>, List<String>> tempPair;
    List<Pair<List<String>, List<String>>> filteredPairs;
    Map<String, Map<String, Double>> result;
    HashMap<String, HashMap<String, Double>> tempResult;
    JaroWinklerMetric metric;

    public JaroWinklerTrieFilter (Pair<List<String>, List<String>> lists, Map<String, Map<String, Double>> result, JaroWinklerMetric metric, double threshold) {
        this.threshold = threshold;
        this.tempPair = lists;
        this.filteredPairs = new LinkedList<Pair<List<String>, List<String>>>();
        this.result = result;
        this.tempResult = new HashMap<String, HashMap<String, Double>>();
        this.metric = metric;
    }

    private int matchesNeeded(int red, int blue) {
        return (int) Math.round(Math.ceil((threshold - 0.6d)*((3*(double)blue*(double)red)/(0.6d*(((double)blue+(double)red))))));
    }

    private double characterFrequencyFilter (int l1, int l2, int m) {
        double theta = (((double)m/(double)l1) + ((double)m/(double)l2) + 1.0d) / 3.0d;
        if (theta > JaroWinklerMetric.winklerBoostThreshold)
            theta = theta + 0.4d * (1.0d-theta);
        return theta;
    }

    @Override
    public void run() {

        List<String> red, blue, _swap;
        red = tempPair.getLeft();
        blue = tempPair.getRight();
        boolean swapped = false;

        // due to certain conditions left can have larger strings than right
        // it is uncommon, but possible, so check & swap if necessary
        if (red.get(red.size()-1).length() > blue.get(blue.size()-1).length()) {
            _swap = red;
            red = blue;
            blue = _swap;
            swapped = true;
        }

        int minRed = red.get(0).length();
        int maxRed = red.get(red.size()-1).length();

        JaroWinklerTrieNode root = new JaroWinklerTrieNode();
        // construct trie from red part
        for (String s : red) {
            root.addChild(s, s);
        }
        // construct a map of containers from blue part
        Map<String, List<String>> blueContainers = new HashMap<String, List<String>>();
        for (String s : blue) {
            char[] key = s.toCharArray();
            Arrays.sort(key);
            String skey = String.valueOf(key);
            if (blueContainers.get(skey) == null)
                blueContainers.put(skey, new LinkedList<String>());
            blueContainers.get(skey).add(s);
        }
        // iterate through the map
        for (String blueKey : blueContainers.keySet()) {
            List<String> redMatches = new LinkedList<String>();
            int maxBlue = blueKey.length();
            int matches = 0;
            Stack<Character> referenceStack = new Stack<Character>();
            char[] charArray = blueKey.toCharArray();
            for (int i = charArray.length-1; i >= 0; i--) {
                char c = charArray[i];
                referenceStack.push(c);
            }
            Stack<MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>> redStack =
                    new Stack<MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>>();
            // set inital algorithm stack
            for (Character key : root.children.keySet()) {
                redStack.add(new MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>(
                        (Stack<Character>) referenceStack.clone(), root.children.get(key), matches));
            }
            // until the stack is empty, pop and evaluate
            while (!redStack.isEmpty()) {
                MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer> current =
                        redStack.pop();
                int maxPossibleMatches = Math.min(current.getLeft().size(), maxRed - current.getMiddle().getLevel() + 1) + current.getRight();
                if (maxPossibleMatches >= matchesNeeded(maxBlue, minRed)) {
                    int currentOrder = current.getLeft().isEmpty() ? -1 : new Character(current.getMiddle().key).compareTo(current.getLeft().peek());
                    if (currentOrder <= 0) {
                        if (currentOrder == 0) {
                            current.getLeft().pop();
                            current.setRight(current.getRight()+1);
                            if (current.getRight() >= matchesNeeded(maxBlue, current.getMiddle().getLevel())
                                    && current.getMiddle().data != null && current.getMiddle().data.size() > 0
                                    && characterFrequencyFilter(current.getMiddle().getLevel(), blueKey.length(), current.getRight()) >= threshold) {
                                redMatches.addAll(current.getMiddle().data);
                            }
                        }
                        for (Character key : current.getMiddle().children.keySet()) {
                            redStack.push(new MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>(
                                    (Stack<Character>)current.getLeft().clone(), current.getMiddle().children.get(key), new Integer(current.getRight().intValue())));
                        }
                    } else {
                        current.getLeft().pop();
                        redStack.push(current);
                    }
                }
            }
            if (redMatches.size() > 0)
                filteredPairs.add(new MutablePair<List<String>, List<String>>(blueContainers.get(blueKey), new LinkedList<String>(redMatches)));
        }

        boolean first;
        double currentSim;
        int comps = 0;
        HashMap<String, Double> similarityTable = new HashMap<String, Double>();


        for (Pair<List<String>, List<String>> filteredPair : filteredPairs) {
            for (String a : swapped ? filteredPair.getRight() : filteredPair.getLeft()) {
                first = true;
                similarityTable.clear();
                for (String b : !swapped ? filteredPair.getRight() : filteredPair.getLeft()) {
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
                    tempResult.put(a, (HashMap<String, Double>) (similarityTable.clone()));
                }
            }
        }
    }
}
