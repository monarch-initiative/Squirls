package org.monarchinitiative.squirls.cli.cmd.annotate_pos;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class VariantChangeTest {

    @Test
    void parseSnp() {
        String payload = "chr1:123C>T";
        final Optional<VariantChange> varchop = VariantChange.fromString(payload);

        assertThat(varchop.isPresent(), is(true));
        final VariantChange ch = varchop.get();

        assertThat(ch.getContig(), is("chr1"));
        assertThat(ch.getPos(), is(123));
        assertThat(ch.getRef(), is("C"));
        assertThat(ch.getAlt(), is("T"));
        assertThat(ch.getVariantChange(), is("chr1:123C>T"));
    }

    @Test
    void parseInsertion() {
        String payload = "Y:123C>CT";
        final Optional<VariantChange> varchop = VariantChange.fromString(payload);

        assertThat(varchop.isPresent(), is(true));
        final VariantChange ch = varchop.get();

        assertThat(ch.getContig(), is("chrY"));
        assertThat(ch.getPos(), is(123));
        assertThat(ch.getRef(), is("C"));
        assertThat(ch.getAlt(), is("CT"));
        assertThat(ch.getVariantChange(), is("chrY:123C>CT"));
    }

    @Test
    void parseDeletion() {
        String payload = "chrX:123CCT>C";
        final Optional<VariantChange> varchop = VariantChange.fromString(payload);

        assertThat(varchop.isPresent(), is(true));
        final VariantChange ch = varchop.get();

        assertThat(ch.getContig(), is("chrX"));
        assertThat(ch.getPos(), is(123));
        assertThat(ch.getRef(), is("CCT"));
        assertThat(ch.getAlt(), is("C"));
        assertThat(ch.getVariantChange(), is("chrX:123CCT>C"));
    }
}