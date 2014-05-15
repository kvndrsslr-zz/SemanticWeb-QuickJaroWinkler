package org.aksw.limes.metrics.speedup;

import java.util.ArrayList;

public class Quicksort  {

    public static void sort(ArrayList<String> values) {
        if (values == null || values.size() == 0)
            return;
        quicksort(values, 0, values.size() - 1);
    }

    private static void quicksort(ArrayList<String> strings, int low, int high) {
        int i = low, j = high;
        int pivot = strings.get(low + (high-low)/2).length();
        while (i <= j) {
            while (strings.get(i).length() < pivot)
                i++;
            while (strings.get(j).length() > pivot)
                j--;
            if (i <= j) {
                exchange(strings, i, j);
                i++;
                j--;
            }
        }
        if (low < j)
            quicksort(strings, low, j);
        if (i < high)
            quicksort(strings, i, high);
    }

    private static void exchange(ArrayList<String> strings, int i, int j) {
        String temp = strings.get(i);
        strings.set(i, strings.get(j));
        strings.set(j, temp);
    }
}