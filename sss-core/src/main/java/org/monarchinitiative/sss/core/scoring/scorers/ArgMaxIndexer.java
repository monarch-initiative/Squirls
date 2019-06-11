package org.monarchinitiative.sss.core.scoring.scorers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Let there be a <code>list</code> of comparable {@link T} elements.
 * <p>
 * Use this class to create a list of indices for <code>list</code> which will contain indices for getting elements from
 * <code>list</code> in order depending on a used <code>comparator</code> (e.g. from the min value to max value if
 * {@link Comparator#naturalOrder()} is used).
 *
 * @param <T> elements to be indexed
 */
class ArgMaxIndexer<T> implements Comparator<Integer> {

    private final List<T> list;

    private final Comparator<T> comparator;


    private ArgMaxIndexer(List<T> list, Comparator<T> comparator) {
        this.list = list;
        this.comparator = comparator;
    }


    static <T> ArgMaxIndexer<T> makeIndexerFor(List<T> list, Comparator<T> comparator) {
        return new ArgMaxIndexer<>(list, comparator);
    }


    List<Integer> makeIndexList() {
        return IntStream.range(0, list.size())
                .boxed()
                .collect(Collectors.toList());
    }


    @Override
    public int compare(Integer left, Integer right) {
        return comparator.compare(list.get(left), list.get(right));
    }
}
