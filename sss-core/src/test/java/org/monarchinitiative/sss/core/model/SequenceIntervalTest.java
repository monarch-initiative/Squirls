package org.monarchinitiative.sss.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SequenceIntervalTest {

    private SequenceInterval instance;

    @BeforeEach
    void setUp() {
        instance = SequenceInterval.newBuilder()
                .setContig("10")
                .setBegin(10)
                .setEnd(20)
                .setStrand(true)
                .setSequence("ACGTACGTAC")
                .setContigLength(100)
                .build();
    }

    @Test
    void toReverseComplement() {
        assertThat(instance.withStrand(false), is(SequenceInterval.newBuilder()
                .setContig("10")
                .setBegin(80)
                .setEnd(90)
                .setStrand(false)
                .setSequence("GTACGTACGT")
                .setContigLength(100)
                .build()));
    }
}