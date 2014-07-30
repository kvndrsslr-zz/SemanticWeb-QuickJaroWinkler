package org.aksw.limes.metrics.speedup;

/**
 * This class implements the Jaro-Winkler algorithm that was designed as
 * a string subsequence alignment method for matching names in the US Census.
 * It is thus optimized for relatively small sized strings of latin letters only.
 * It provides all the features of the original C implementation by William E. Winkler,
 * although the features that made it specific for name matching may be disabled.
 *
 * To overcome the complexity O(n*m) for non matching cases a filter is added.
 * Given a threshold it can identify pairs whose Jaro-Winkler proximity
 * is confidently less than or equal to that threshold.
 *
 * @author Kevin Dreßler
 * https://github.com/kvndrsslr/SemanticWeb-QuickJaroWinkler
 */

public class JaroWinklerMetric extends AbstractFilteredMetric {

    private static char[][] sp =
            {{'A','E'},{'A','I'},{'A','O'},{'A','U'},{'B','V'},{'E','I'},{'E','O'},{'E','U'},
                    {'I','O'},{'I','U'},{'O','U'},{'I','Y'},{'E','Y'},{'C','G'},{'E','F'},
                    {'W','U'},{'W','V'},{'X','K'},{'S','Z'},{'X','S'},{'Q','C'},{'U','V'},
                    {'M','N'},{'L','I'},{'Q','O'},{'P','R'},{'I','J'},{'2','Z'},{'5','S'},
                    {'8','B'},{'1','I'},{'1','L'},{'0','O'},{'0','Q'},{'C','K'},{'G','J'},
                    {'E',' '},{'Y',' '},{'S',' '}};

    public static double winklerBoostThreshold = 0.7d;

    private int[][] adjwt;

    private char[] yin;

    private boolean simOn;

    private boolean uppercase;

    private boolean longStrings;

    public JaroWinklerMetric() {
        this(true, false, false);
    }

    public JaroWinklerMetric(boolean uppercaseOn, boolean longStringsOn, boolean characterSimilarityOn) {
        super();
        // initialize options
        this.uppercase = uppercaseOn;
        this.longStrings = longStringsOn;
        this.simOn = characterSimilarityOn;
        int i, j;
        if (characterSimilarityOn) {
            adjwt = new int[91][91];
            for (i = 0; i < 91; i++) for (j = 0; j < 91; j++) adjwt[i][j] = 0;
            for (i = 0; i < 36; i++) {
                adjwt[sp[i][0]][sp[i][1]] = 3;
                adjwt[sp[i][1]][sp[i][0]] = 3;
            }
        }
    }

    /**
     * Is character not numeric?
     * @param c character
     * @return true if not numeric, false otherwise
     */
    private boolean notNum (char c) {
        return (c > 57) || (c < 48);
    }

    /**
     * Is Character alphanumeric?
     * @param c character
     * @return true if alphanumeric, false otherwise
     */
    private boolean inRange (char c) {
        return (c > 0) && (c < 91);
    }

    /**
     * Calculate the proximity of two input strings if
     * proximity is assured to be over given threshold threshold.
     *
     * @param yi string to be aligned
     * @param ya string to align on
     * @return similarity score (proximity)
     */
    public double proximity (String yi, String ya) {
        yi = yi.trim();
        if (uppercase)
            yi = yi.toUpperCase();
        yin = yi.toCharArray();
        this.setReference(yin);
        return proximity(ya);
    }

    /**
     * Calculate the proximity of two input strings if
     * proximity is assured to be over given threshold threshold.
     *
     * @param ya string to align on
     * @return similarity score (proximity)
     */
    public double proximity (String ya) {
        ya = ya.trim();
        if (uppercase)
            ya = ya.toUpperCase();
        char[] yang = ya.toCharArray();
        if (filter(yin, yang))
            return -1.0d;
        boolean[] yinFlags = new boolean[yin.length];
        boolean[] yangFlags = new boolean[yang.length];
        int i;
        for (i = 0; i < yin.length; i++) yinFlags[i] = false;
        for (i = 0; i < yang.length; i++) yangFlags[i] = false;
        boolean matriarch = yin.length > yang.length;
        int len = matriarch ? yang.length : yin.length;
        int range = matriarch ? yin.length : yang.length;
        range = range / 2 - 1;
        if (range < 0)
            range = 0;
        int k;
        int t;
        int m = t = k = 0;
        int j;
        for (i = 0; i < yin.length; i++) {
            int low = (i >= range) ? i - range : 0;
            int high = (i + range + 1 <= yang.length) ? i + range : yang.length - 1;
            for (j = low; j <= high; j++) {
                if (!yangFlags[j] && (yang[j]) == (yin[i])) {
                    yinFlags[i] = yangFlags[j] = true;
                    m++;
                    break;
                }
            }
        }
        if (m == 0)
            return 0.0d;

        for (i = 0; i < yin.length; i++) {
            if (yinFlags[i]) {
                for (j = k; j < yang.length; j++) {
                    if (yangFlags[j]) {
                        k = j + 1;
                        break;
                    }
                }
                if (yin[i] != yang[j])
                    t++;
            }
        }
        t /= 2;
        double sim;
        if (len > m && simOn) {
            sim = 0.0d;
            for (i = 0; i < yin.length; i++) {
                if (!yinFlags[i] && inRange(yin[i])) {
                    for (j = 0; j < yang.length; j++) {
                        if (!yangFlags[j] && inRange(yang[j])) {
                            if (adjwt[yin[i]][yang[j]] > 0) {
                                sim += adjwt[yin[i]][yang[j]];
                                yangFlags[j] = true;
                                break;
                            }
                        }
                    }
                }
            }
            sim = sim / 10.0d + m;
        } else {
            sim = (double) m;
        }
        double weight = sim / ((double) yin.length) + sim / ((double) yang.length)
                + ((double) (m - t)) / ((double) m);
        weight /= 3.0d;
        if (weight > winklerBoostThreshold) {
            k = (len >= 4) ? 4 : len;
            for (i = 0; ((i < k) && (yin[i] == yang[i]) && notNum(yin[i])); i++);
            if (i > 0)
                weight += i * 0.1d * (1.0d - weight);
            if (longStrings && len > 4 && m > i + 1 && 2 * m >= len + i && notNum(yin[0]))
                weight += (1.0d - weight) *
                        ((double) (m - i - 1)) / ((yin.length + yang.length - i * 2.0d + 2.0d));
        }
        return weight;
    }
}