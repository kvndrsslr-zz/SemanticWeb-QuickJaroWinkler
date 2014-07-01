package org.aksw.limes.metrics.speedup.test;

import static org.junit.Assert.*;

import com.carrotsearch.junitbenchmarks.h2.H2Consumer;
import org.aksw.limes.metrics.speedup.JaroWinklerEntropyFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerLengthFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerMatcher;
import org.aksw.limes.metrics.speedup.JaroWinklerMetric;
import org.aksw.limes.metrics.speedup.test.JaroWinklerPerformanceTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JaroWinklerCorrectnessTest {

    private static ArrayList<String> listA, listB;
    private static double threshold;

    @BeforeClass
    public static void setUpLists () {
        threshold = (Double) JaroWinklerPerformanceTest.getProperties().get("threshold");
        threshold = 0.9d;
        listA = new ArrayList<String>();
        listB = new ArrayList<String>();
        try {
            NxParser nxp = new NxParser(new FileInputStream((String) JaroWinklerPerformanceTest.getProperties().get("testData")));
            int i = 0;
            while (nxp.hasNext() && i < 10000) { // (Integer)
            // JaroWinklerPerformanceTest.getProperties().get("lines")
                String tmp = nxp.next()[2].toN3();
                tmp = tmp.substring(1, tmp.lastIndexOf("@")-1);
                listA.add(tmp);
                listB.add(tmp);
                i++;
            }
        } catch (FileNotFoundException e) {
            // nothing to do...
        }
    }


    @Ignore
    @Test
    public void verify () {
        HashMap<String, Map<String, Double>> matchesNative, matchesFiltered;
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        matchesNative = jwm.match();
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        //jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        matchesFiltered = jwm.match();
        for (Map.Entry<String, Map<String,Double>> matchNative : matchesNative.entrySet()) {
            if (!matchesFiltered.get(matchNative.getKey()).equals(matchNative.getValue())) {
                System.err.println(matchesFiltered.get(matchNative.getKey()).toString());
                System.err.println(matchNative.getValue().toString());
                fail();
            }
        }
    }
}
