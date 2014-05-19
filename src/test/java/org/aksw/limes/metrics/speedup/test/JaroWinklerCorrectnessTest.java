package org.aksw.limes.metrics.speedup.test;

import junit.framework.Assert;
import org.aksw.limes.metrics.speedup.JaroWinklerEntropyFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerLengthFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerMatcher;
import org.aksw.limes.metrics.speedup.JaroWinklerMetric;
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
        listA = new ArrayList<String>();
        listB = new ArrayList<String>();
        try {
            NxParser nxp = new NxParser(new FileInputStream((String) JaroWinklerPerformanceTest.getProperties().get("testData")));
            int i = 0;
            while (nxp.hasNext() && i < (Integer) JaroWinklerPerformanceTest.getProperties().get("lines")) {
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
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        matchesFiltered = jwm.match();
        Assert.assertEquals(matchesNative, matchesFiltered);
    }
}
