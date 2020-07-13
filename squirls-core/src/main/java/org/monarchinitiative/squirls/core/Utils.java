package org.monarchinitiative.squirls.core;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 */
public class Utils {

    /**
     * Create subsequences/windows of size <code>'ws'</code> from nucleotide <code>sequence</code>.
     *
     * @param sequence {@link String} with nucleotide sequence to generate subsequences from
     * @param ws       window size
     * @return {@link Stream} of {@link String}s - subsequences of given <code>sequence</code> with length
     * <code>ws</code> or empty {@link Stream}, if '<code>ws</code> > <code>sequence.length()</code>'
     */
    public static Stream<String> slidingWindow(String sequence, int ws) {
        return ws > sequence.length()
                ? Stream.empty()
                : IntStream.range(0, sequence.length() - ws + 1)
                .boxed()
                .map(idx -> sequence.substring(idx, idx + ws));
    }

    /**
     * Return index of the first maximum <code>T</code> value in the list.
     *
     * @param elements   list with elements
     * @param comparator comparator to compare the elements
     * @param <T>        type of elements to be compared
     * @return index of the first maximum value in the list
     */
    public static <T> int argmax(List<T> elements, Comparator<T> comparator) {
        int max = -1;
        if (!elements.isEmpty()) {
            max = 0;
            T maxVal = elements.get(max);
            for (int i = 1; i < elements.size(); i++) {
                final T current = elements.get(i);
                if (comparator.compare(current, maxVal) > 0) {
                    maxVal = current;
                    max = i;
                }
            }
        }
        return max;
    }

    /**
     * Return index of the first maximum <code>T</code> value in the list.
     *
     * @param elements list with elements
     * @param <T>      type of elements to be compared
     * @return index of the first maximum value in the list
     */
    public static <T extends Comparable<T>> int argmax(List<T> elements) {
        int max = -1;
        if (!elements.isEmpty()) {
            max = 0;
            T maxVal = elements.get(max);
            for (int i = 1; i < elements.size(); i++) {
                final T current = elements.get(i);

                if (current.compareTo(maxVal) > 0) {
                    maxVal = current;
                    max = i;
                }
            }
        }
        return max;
    }
}
