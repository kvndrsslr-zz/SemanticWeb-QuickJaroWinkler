package org.aksw.limes.metrics.speedup;


import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class JaroWinklerTrieFilter {

    private double threshold;

    private List<String> red, blue;

    public JaroWinklerTrieFilter (List<String> listA, List<String> listB, double threshold) {
        this.threshold = threshold;
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
    }

    public List<Pair<List<String>, List<String>>> getFilteredPairs () {
        List<Pair<List<String>, List<String>>> tempPairs = new LinkedList<Pair<List<String>, List<String>>>();
        List<Pair<List<String>, List<String>>> filteredPairs = new LinkedList<Pair<List<String>, List<String>>>();
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
        // for every length filtered partion
        for (Pair<List<String>, List<String>> tempPair : tempPairs) {
            int maxBlue = tempPair.getRight().size() == 0 ? 0 :
                    tempPair.getRight().get(tempPair.getRight().size()-1).length();
            JaroWinklerTrieNode root = new JaroWinklerTrieNode();
            // construct trie from blue part
            for (String s : tempPair.getRight()) {
                root.addChild(s, s);
            }
            // construct a map of containers from red part
            Map<String, List<String>> redContainers = new HashMap<String, List<String>>();
            for (String s : tempPair.getLeft()) {
                char[] key = s.toCharArray();
                Arrays.sort(key);
                String skey = String.valueOf(key);
                if (redContainers.get(skey) == null)
                    redContainers.put(skey, new LinkedList<String>());
                redContainers.get(skey).add(s);
            }
            // iterate through the map
            for (String redKey : redContainers.keySet()) {
                List<String> blueMatches = new LinkedList<String>();
                int maxRed = redKey.length();
                int matches = 0;
                Stack<Character> referenceStack = new Stack<Character>();
                char[] charArray = redKey.toCharArray();
                for (int i = charArray.length-1; i >= 0; i--) {
                    char c = charArray[i];
                    referenceStack.push(c);
                }
                Stack<MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>> blueStack =
                        new Stack<MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>>();
                // set inital algorithm stack
                for (Character key : root.children.keySet()) {
                    blueStack.add(new MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>(
                            (Stack<Character>) referenceStack.clone(), root.children.get(key), matches));
                }
                // until the stack is empty, pop and evaluate
                while (!blueStack.isEmpty()) {
                    MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer> current =
                            blueStack.pop();
                    int safeMismatches = maxRed - (current.getLeft().size() + current.getRight());
                    if (safeMismatches <= minMismatches(maxRed, maxBlue)) {
                        int currentOrder = current.getLeft().isEmpty() ? -1 : new Character(current.getMiddle().key).compareTo(current.getLeft().peek());
                        if (currentOrder <= 0) {
                            if (currentOrder == 0) {
                                current.getLeft().pop();
                                current.setRight(current.getRight()+1);
                                if (current.getRight() >= maxBlue - minMismatches(maxRed, maxBlue)
                                        && current.getMiddle().data != null && current.getMiddle().data.size() > 0
                                        && characterFrequencyFilter(current.getMiddle().data.get(0).length(), redKey.length(), matches) >= threshold) {
                                    blueMatches.addAll(current.getMiddle().data);
                                }
                            }
                            for (Character key : current.getMiddle().children.keySet()) {
                                blueStack.push(new MutableTriple<Stack<Character>, JaroWinklerTrieNode, Integer>(
                                        (Stack<Character>)current.getLeft().clone(), current.getMiddle().children.get(key), new Integer(current.getRight().intValue())));
                            }
                        } else {
                            current.getLeft().pop();
                            blueStack.push(current);
                        }
                    }
                }
                if (blueMatches.size() > 0)
                    filteredPairs.add(new MutablePair<List<String>, List<String>>(redContainers.get(redKey), new LinkedList<String>(blueMatches)));
            }
        }
        return filteredPairs;
    }

    private int minMismatches(int maxRed, int maxBlue) {
        return maxBlue - (int) Math.round(Math.ceil((threshold - 0.6d)*((3*(double)maxBlue*(double)maxRed)/(0.6d*(((double)maxBlue+(double)maxRed))))));
    }

    private double characterFrequencyFilter (int l1, int l2, int m) {
        double theta = ((m/l1) + (m/l2) + 1.0d) / 3.0d;
        if (theta > JaroWinklerMetric.winklerBoostThreshold)
            theta = theta + 0.4d * (1.0d-theta);
        return theta;
    }

}
