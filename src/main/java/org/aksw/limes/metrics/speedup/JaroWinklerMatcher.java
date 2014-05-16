package org.aksw.limes.metrics.speedup;

import org.semanticweb.yars.nx.parser.NxParser;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class  JaroWinklerMatcher {

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
        for(int i = 0; i < listA.size(); i++)
            minTargetIndex.add(0);
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
        int i, j, delta, maxIndex, lastIndex, offsetA, offsetB;
        double offsetCalc;
        offsetCalc= Math.log10((double) listA.size());
        offsetA = (int) Math.round(Math.pow(offsetCalc / 2.0d, offsetCalc));
        offsetCalc = Math.log10((double) listB.size());
        offsetB = (int) Math.round(Math.pow(offsetCalc / 2.0d, offsetCalc));
        lastIndex = 0;
        maxIndex = listB.size() - 1;
        for (i = 0; i < listA.size(); i++) {
            s1 = listA.get(i);
            if (i == 0 || s1.length() != listA.get(i-1).length()) {
                delta = JaroWinklerLengthFilter.maxLenDeltaFor(s1.length(), threshold);
                for (j = lastIndex; j < listB.size(); j+=offsetB) {
                    s2 = listB.get(j);
                    if (s2.length() > s1.length() + delta) {
                        maxIndex = lastIndex = j;
                        break;
                    }
                }
            }
            maxTargetIndex.add(maxIndex);
        }
        for (i = 0; i < listB.size(); i++) {
            s1 = listB.get(i);
            maxIndex = 0;
            if (i != 0 && s1.length() != listA.get(i-1).length()) {
                delta = JaroWinklerLengthFilter.maxLenDeltaFor(s1.length(), threshold);
                for (j = maxIndex; j < listA.size(); j+=offsetA) {
                    s2 = listA.get(j);
                    if (s2.length() > s1.length() + delta) {
                        maxIndex = j;
                        break;
                    }
                }
            }
            if (maxIndex > 0 && minTargetIndex.get(maxIndex) == 0)
                minTargetIndex.set(maxIndex, i);
        }
        Iterator<Integer> it = minTargetIndex.iterator();
        Integer lastHit = 0;
        for (j = 0; j < minTargetIndex.size(); j++) {
            if (minTargetIndex.get(j) < lastHit) {
                minTargetIndex.set(j, lastHit);
            } else {
                lastHit = minTargetIndex.get(j);
            }
        }
    }

    public HashMap<String, Map<String, Double>> match () {
        HashMap<String, Map<String, Double>> similarityBook = new HashMap<String, Map<String, Double>>();
        HashMap<String, Double> similarityTable = new HashMap<String, Double>();
        Iterator<String> itA, itB;
        String a, b;
        double currentSim;
        int i, j, min, max;
        itA = listA.iterator();
        for (i = 0; i < listA.size(); i++) {
            a = listA.get(i);
            min = precomputeRanges ? minTargetIndex.get(i) : 0;
            max = precomputeRanges ? maxTargetIndex.get(i) : listB.size();
            for (j = min; j < max; j++) {
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
        System.out.println(comps);
        return similarityBook;
    }

    public static void main  (String[] args) throws Exception{
        double threshold = 0.95d;
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        ArrayList<String> listA, listB;
        listA = new ArrayList<String>();
        listB = new ArrayList<String>();

        NxParser nxp = new NxParser(new FileInputStream("/Users/kvn/Downloads/DownloadStorage/labels_en_new.nt"));
        int i = 0;
        System.out.println("starting FILEREAD...");
        System.out.println(new Date().toString());
        while (nxp.hasNext() && i < 100000) {
            String tmp = nxp.next()[2].toN3();
            tmp = tmp.substring(1, tmp.lastIndexOf("@")-1);
            listA.add(tmp);
            listB.add(tmp);
            i++;
        }
        JaroWinklerMatcher jwm;
        FileWriter x;
        HashMap<String, Map<String, Double>> matches;
//        System.out.println("starting JaroWinkler with both filters and range cuts...");
//        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
//        matches = jwm.match();
//        x = new FileWriter("/Users/kvn/Downloads/DownloadStorage/out.txt");
//        x.write(matches.toString());
//        x.flush();
//        x.close();
//        System.out.println(new Date().toString());
        jw.eraseFilters();
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        System.out.println("starting JaroWinkler with length filter only and range cuts...");
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        matches = jwm.match();
        x = new FileWriter("/Users/kvn/Downloads/DownloadStorage/out.txt");
        x.write(matches.toString());
        x.flush();
        x.close();
        System.out.println(new Date().toString());
//        System.out.println("starting JaroWinkler with filters only...");
//        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
//        matches = jwm.match();
//        x = new FileWriter("/Users/kvn/Downloads/DownloadStorage/out.txt");
//        x.write(matches.toString());
//        x.flush();
//        x.close();
//        System.out.println(new Date().toString());
//        jw.eraseFilters();
//        System.out.println("starting JaroWinkler with range cut only");
//        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
//        matches = jwm.match();
//        x = new FileWriter("/Users/kvn/Downloads/DownloadStorage/out.txt");
//        x.write(matches.toString());
//        x.flush();
//        x.close();
//        System.out.println(new Date().toString());
        jw.eraseFilters();
        System.out.println("starting JaroWinkler completely naive");
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        matches = jwm.match();
        x = new FileWriter("/Users/kvn/Downloads/DownloadStorage/out.txt");
        x.write(matches.toString());
        x.flush();
        x.close();
        System.out.println(new Date().toString());
    }
}
