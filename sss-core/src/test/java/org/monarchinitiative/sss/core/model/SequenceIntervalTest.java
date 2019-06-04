package org.monarchinitiative.sss.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SequenceIntervalTest {

    private SequenceInterval instance;

    @BeforeEach
    void setUp() {
        instance = SequenceInterval.newBuilder()
                .setInterval(GenomeInterval.newBuilder()
                        .setContig("10")
                        .setBegin(10)
                        .setEnd(20)
                        .setStrand(true)
                        .setContigLength(100)
                        .build())
                .setSequence("ACGTACGTAC")
                .build();
    }

    @Test
    void toReverseComplement() {
        assertThat(instance.withStrand(false), is(SequenceInterval.newBuilder()
                .setInterval(GenomeInterval.newBuilder()
                        .setContig("10")
                        .setBegin(80)
                        .setEnd(90)
                        .setStrand(false)
                        .setContigLength(100)
                        .build())
                .setSequence("GTACGTACGT")
                .build()));
    }

    @ParameterizedTest
    @CsvSource({"10, 11", "-1, 0", "5, 4"})
    void localSubsequenceEmptyWhenOutOfBounds(int begin, int end) {
        assertThrows(IndexOutOfBoundsException.class, () -> instance.getLocalSequence(begin, end));
    }

    @ParameterizedTest
    @CsvSource({"0, 1, A", "9, 10, C", "0, 10, ACGTACGTAC", "4, 6, AC"})
    void localSubsequenceGoodResult(int begin, int end, String expected) {
        assertThat(instance.getLocalSequence(begin, end), is(expected));
    }

}

