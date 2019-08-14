package org.monarchinitiative.threes.core.reference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SequenceInterval;
import org.monarchinitiative.threes.core.model.SplicingVariant;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GenomeCoordinatesFlipperTest {

    private static Map<String, Integer> contigLengthMap;

    private GenomeCoordinatesFlipper flipper;

    @BeforeAll
    static void setUpBefore() {
        contigLengthMap = new HashMap<>();
        contigLengthMap.put("chr1", 100);
        contigLengthMap.put("chr2", 200);
    }

    @BeforeEach
    void setUp() {
        flipper = new GenomeCoordinatesFlipper(contigLengthMap);
    }

    @Test
    void fromFwdToRev() throws Exception {
        GenomeCoordinates onFwd = GenomeCoordinates.newBuilder()
                .setContig("chr1")
                .setBegin(10)
                .setEnd(20)
                .setStrand(true)
                .build();

        final Optional<GenomeCoordinates> opFlip = flipper.flip(onFwd);
        assertThat(opFlip.isPresent(), is(true));

        final GenomeCoordinates gc = opFlip.get();

        assertThat(gc, is(GenomeCoordinates.newBuilder()
                .setContig("chr1")
                .setBegin(80)
                .setEnd(90)
                .setStrand(false)
                .build()));
    }

    @Test
    void fromRevToFwd() throws Exception {
        GenomeCoordinates onRev = GenomeCoordinates.newBuilder()
                .setContig("chr2")
                .setBegin(90)
                .setEnd(120)
                .setStrand(false)
                .build();

        final Optional<GenomeCoordinates> opFlip = flipper.flip(onRev);
        assertThat(opFlip.isPresent(), is(true));

        final GenomeCoordinates gc = opFlip.get();

        assertThat(gc, is(GenomeCoordinates.newBuilder()
                .setContig("chr2")
                .setBegin(80)
                .setEnd(110)
                .setStrand(true)
                .build()));
    }

    @Test
    void failsOnUnknownContig() throws Exception {
        final GenomeCoordinates gc = GenomeCoordinates.newBuilder()
                .setContig("chr3") // not present in the static map above
                .setBegin(10)
                .setEnd(20)
                .setStrand(true)
                .build();

        final Optional<GenomeCoordinates> flip = flipper.flip(gc);
        assertThat(flip.isPresent(), is(false));
    }

    @Test
    void flipVariantFromFwdToRev() {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(10)
                        .setEnd(11)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("T")
                .build();

        Optional<SplicingVariant> op = flipper.flip(variant);

        assertThat(op.isPresent(), is(true));

        SplicingVariant sv = op.get();
        GenomeCoordinates gc = sv.getCoordinates();

        assertThat(gc, is(GenomeCoordinates.newBuilder()
                .setContig("chr1")
                .setBegin(89)
                .setEnd(90)
                .setStrand(false)
                .build()));

        assertThat(sv.getRef(), is("G"));
        assertThat(sv.getAlt(), is("A"));
    }

    @Test
    void flipSequenceInterval() {
        SequenceInterval interval = SequenceInterval.of(GenomeCoordinates.newBuilder()
                .setContig("chr2")
                .setBegin(90)
                .setEnd(100)
                .setStrand(true)
                .build(),
                "ACGTACGTAC");

        final Optional<SequenceInterval> flipO = flipper.flip(interval);
        assertThat(flipO.isPresent(), is(true));

        final SequenceInterval sequence = flipO.get();
        assertThat(sequence.getCoordinates(), is(GenomeCoordinates.newBuilder()
                .setContig("chr2")
                .setBegin(100)
                .setEnd(110)
                .setStrand(false)
                .build()));
        assertThat(sequence.getSequence(), is("GTACGTACGT"));
    }
}