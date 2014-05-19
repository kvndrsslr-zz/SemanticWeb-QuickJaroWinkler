package org.aksw.limes.metrics.speedup.test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;
import org.aksw.limes.metrics.speedup.JaroWinklerEntropyFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerLengthFilter;
import org.aksw.limes.metrics.speedup.JaroWinklerMatcher;
import org.aksw.limes.metrics.speedup.JaroWinklerMetric;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.semanticweb.yars.nx.parser.NxParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


@BenchmarkMethodChart(filePrefix = "benchmark-lists")
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 40)
public class JaroWinklerPerformanceTest extends AbstractBenchmark {

    private static ArrayList<String> listA, listB;
    private static double threshold;
    private static int lines;
    private static String testData;
    private static H2Consumer consumer = getConsumer();

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(consumer);

    public static HashMap<String, Object> getProperties() {
        threshold = 0.9d;
        lines = 1000;
        testData = "labels_en.nt";
        Properties prop = new Properties();
        try {
            InputStream stream = new FileInputStream("src/test/resources/testConfig.properties");
            prop.load(stream);
            stream.close();
            threshold = Double.valueOf(prop.getProperty("threshold"));
            lines = Integer.valueOf(prop.getProperty("lines"));
            testData = prop.getProperty("testData").equals("") ? testData : prop.getProperty("testData");
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        HashMap<String, Object> rtn = new HashMap<String, Object>();
        rtn.put("threshold",threshold);
        rtn.put("lines",lines);
        rtn.put("testData",testData);
        return rtn;
    }

    public static H2Consumer getConsumer() {
        getProperties();
        return new H2Consumer(
                new File("benchmarks/charts" + String.valueOf(lines) + "/foo-db"),
                new File("benchmarks/charts" + String.valueOf(lines)),
                String.valueOf(threshold));
    }

    @BeforeClass
    public static void setUpLists () {
        listA = new ArrayList<String>();
        listB = new ArrayList<String>();
        try {
            NxParser nxp = new NxParser(new FileInputStream(testData));
            int i = 0;
            while (nxp.hasNext() && i < lines) {
                String tmp = nxp.next()[2].toN3();
                tmp = tmp.substring(1, tmp.lastIndexOf("@")-1);
                listA.add(tmp);
                listB.add(tmp);
                i++;
            }
        } catch (Exception e) {
            // nothing to do...
        }
    }

    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void nativeJaroWinkler () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }

    @Ignore
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void allFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void rangeAndLengthFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @Ignore
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void rangeAndEntropyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void lengthOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }
    @Ignore
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void entropyOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 1)
    @Test
    public void rangeOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }
}
