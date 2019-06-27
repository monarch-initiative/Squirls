package org.monarchinitiative.threes.core.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SplicingVariantTest {


    @Test
    void genomeIntervalOfSnp() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(99)
                        .setEnd(100)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("T")
                .build();

        final GenomeCoordinates co = variant.getCoordinates();
        assertThat(co.getContig(), is("chr1"));
        assertThat(co.getBegin(), is(99));
        assertThat(co.getEnd(), is(100));
        assertThat(co.getStrand(), is(true));
    }
}