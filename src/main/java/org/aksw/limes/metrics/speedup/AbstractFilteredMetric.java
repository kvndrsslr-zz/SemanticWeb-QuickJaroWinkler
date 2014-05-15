package org.aksw.limes.metrics.speedup;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public abstract class AbstractFilteredMetric {
    /**
     * List of stored filters
     */
    private List<AbstractMetricFilter> filters;

    public void setFilters (List<AbstractMetricFilter> filters) {
        this.filters = filters;
    }

    public void addFilter (AbstractMetricFilter filter) {
        this.filters.add(filter);
    }

    public void eraseFilters () {
        this.filters.clear();
    }

    public List<AbstractMetricFilter> getFilters () {
        return new LinkedList<AbstractMetricFilter>(this.filters);
    }

    public AbstractFilteredMetric() {
        this.filters = new LinkedList<AbstractMetricFilter>();
    }

    protected boolean filter (char[] a, char[] b) {
        Iterator<AbstractMetricFilter> it = this.filters.iterator();
        boolean filtered = false;
        while (it.hasNext()) {
            filtered = it.next().apply(a, b);
            if (filtered)
                break;
        }
        return filtered;
    }

    protected void setReference(char[] x) {
        for (AbstractMetricFilter filter : this.filters) {
            filter.setReference(x);
        }
    }
}