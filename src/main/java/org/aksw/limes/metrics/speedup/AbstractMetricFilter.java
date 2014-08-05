package org.aksw.limes.metrics.speedup;

/**
 * Abstract filter class
 */

public abstract class AbstractMetricFilter {

    /**
     * threshold from which on will be filtered
     */
    protected double threshold;

    /**
     * Getter for threshold
     *
     * @return threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Setter for threshold
     *
     * @param threshold
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }


    /**
     * Default constructor stub
     *
     */
    public AbstractMetricFilter() {
        this(0.8d);
    }


    /**
     * Default constructor stub
     *
     * @param threshold
     */
    public AbstractMetricFilter(double threshold) {
        this.setThreshold(threshold);
    }

    /**
     * Applies the filter on input strings
     *
     * @param a string to be aligned
     * @param b string to align on
     * @return true if filter applies, false otherwise
     */
    public boolean apply(char[] a, char[] b) {
        return (score(a, b) < threshold);
    }

    protected abstract double score(char[] b);

    protected double score(char[] a, char[] b) {
        this.setReference(a);
        return this.score(b);
    }

    protected abstract void setReference(char[] x);

}