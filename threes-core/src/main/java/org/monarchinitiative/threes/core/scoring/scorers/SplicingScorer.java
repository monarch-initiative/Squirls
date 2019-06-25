package org.monarchinitiative.threes.core.scoring.scorers;

import org.monarchinitiative.threes.core.model.SplicingTernate;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface SplicingScorer {

    /**
     * Create subsequences/windows of size <code>'ws'</code> from nucleotide <code>sequence</code>.
     *
     * @param sequence {@link String} with nucleotide sequence to generate subsequences from
     * @param ws       window size
     * @return {@link Stream} of {@link String}s - subsequences of given <code>sequence</code> with length
     * <code>ws</code> or empty {@link Stream}, if '<code>ws</code> > <code>sequence.length()</code>'
     */
    static Stream<String> slidingWindow(String sequence, int ws) {
        return ws > sequence.length()
                ? Stream.empty()
                : IntStream.range(0, sequence.length() - ws + 1)
                .boxed()
                .map(idx -> sequence.substring(idx, idx + ws));
    }

    Function<SplicingTernate, Double> scoringFunction();
}