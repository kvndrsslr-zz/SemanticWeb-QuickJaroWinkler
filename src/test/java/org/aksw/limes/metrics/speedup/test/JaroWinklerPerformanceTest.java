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
import java.util.Properties;


@BenchmarkMethodChart(filePrefix = "benchmark-lists")
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 40)
public class JaroWinklerPerformanceTest extends AbstractBenchmark {

/*
    new H2Consumer(
            new File("benchmarks/charts/foo-db"),
            new File("benchmarks/charts"),
    "0.90");

 */
    private static H2Consumer consumer = getConsumer();
    private static ArrayList<String> listA, listB;
    private static double threshold;
    private static int lines;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule(consumer);


    public static H2Consumer getConsumer() {
        threshold = 0.7d;
        lines = 100;
        Properties prop = new Properties();
        try {
            InputStream stream = new FileInputStream("src/test/resources/testConfig.properties");
            prop.load(stream);
            stream.close();
            threshold = Double.valueOf(prop.getProperty("threshold"));
            lines = Integer.valueOf(prop.getProperty("lines"));

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
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
            NxParser nxp = new NxParser(new FileInputStream("/Users/kvn/Downloads/DownloadStorage/labels_en_new.nt"));
            int i = 0;
            while (nxp.hasNext() && i < 1000) {
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

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void nativeJaroWinkler () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }

    @Ignore
    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void allFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void rangeAndLengthFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @Ignore
    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void rangeAndEntropyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void lengthOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerLengthFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }
    @Ignore
    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void entropyOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        jw.addFilter(new JaroWinklerEntropyFilter(threshold));
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, false);
        jwm.match();
    }

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void rangeOnlyFilters () {
        JaroWinklerMetric jw = new JaroWinklerMetric(true, false, false);
        JaroWinklerMatcher jwm;
        jwm = new JaroWinklerMatcher((ArrayList<String>) listA.clone(), (ArrayList<String>) listB.clone(), jw, threshold, true);
        jwm.match();
    }
}
