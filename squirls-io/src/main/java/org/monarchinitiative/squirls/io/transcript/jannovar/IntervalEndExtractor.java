package org.monarchinitiative.squirls.io.transcript.jannovar;

public interface IntervalEndExtractor<T> {

    /**
     * @return begin position of <code>x</code> (inclusive)
     */
    int getBegin(T x);

    /**
     * @return begin position of <code>x</code> (exclusive)
     */
    int getEnd(T x);
}
