package org.monarchinitiative.threes.core.reference.transcript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.model.SplicingVariant;
import org.monarchinitiative.threes.core.pwm.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.monarchinitiative.threes.core.reference.fasta.InvalidCoordinatesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class NaiveSplicingTranscriptLocatorTest {

    NaiveSplicingTranscriptLocator locator;

    @Autowired
    private GenomeCoordinatesFlipper genomeCoordinatesFlipper;

    @Autowired
    private SplicingParameters splicingParameters;

    private SplicingTranscript fwdTranscript;

    private SplicingTranscript revTranscript;

    @BeforeEach
    void setUp() throws Exception {
        fwdTranscript = PojosForTesting.getTranscriptWithThreeExons();
        revTranscript = PojosForTesting.getTranscriptWithThreeExonsOnRevStrand();
        locator = new NaiveSplicingTranscriptLocator(splicingParameters, genomeCoordinatesFlipper);
    }


    @Test
    void onDifferentContig() throws InvalidCoordinatesException {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chrX")
                        .setBegin(999)
                        .setEnd(1000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }


    @Test
    void oneBaseBeforeCds() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(999)
                        .setEnd(1000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }


    @Test
    void firstBaseOfCds() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1000)
                        .setEnd(1001)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeFirstDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1196)
                        .setEnd(1197)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1197)
                        .setEnd(1198)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }

    @Test
    void lastBaseOfFirstDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1205)
                        .setEnd(1206)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }


    @Test
    void firstBaseAfterFirstDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1206)
                        .setEnd(1207)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeFirstAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1374)
                        .setEnd(1375)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1375)
                        .setEnd(1376)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }


    @Test
    void lastBaseOfFirstAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1401)
                        .setEnd(1402)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void oneBaseAfterFirstAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1402)
                        .setEnd(1403)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeSecondDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1596)
                        .setEnd(1597)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfSecondDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1597)
                        .setEnd(1598)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void lastBaseOfSecondDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1605)
                        .setEnd(1606)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(1));
    }

    @Test
    void oneBaseAfterSecondDonor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1606)
                        .setEnd(1607)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeSecondAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1774)
                        .setEnd(1775)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void firstBaseOfSecondAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1775)
                        .setEnd(1776)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void lastBaseOfSecondAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1801)
                        .setEnd(1802)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void oneBaseAfterSecondAcceptor() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1802)
                        .setEnd(1803)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void lastBaseOfCds() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1999)
                        .setEnd(2000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void oneBaseAfterCds() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(2000)
                        .setEnd(2001)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }

    @Test
    void firstBaseOfSingleExonTranscript() throws Exception {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExons();
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1000)
                        .setEnd(1001)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    @Test
    void lastBaseOfSingleExonTranscript() throws Exception {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExons();
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(1999)
                        .setEnd(2000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    // ------------------------       REVERSE STRAND          ----------------------------------------------------------
    //
    // The chr1 has length = 10000, as it is defined in TestDataSourceConfig#contigLengthMap bean.
    // The transcript is located at chr1:8000-9000
    //
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void oneBaseBeforeCdsRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(9000)
                        .setEnd(9001)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }


    @Test
    void firstBaseOfCdsRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8999)
                        .setEnd(9000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeFirstDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8803)
                        .setEnd(8804)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8802)
                        .setEnd(8803)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }

    @Test
    void lastBaseOfFirstDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8794)
                        .setEnd(8795)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }


    @Test
    void firstBaseAfterFirstDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8793)
                        .setEnd(8794)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeFirstAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8625)
                        .setEnd(8626)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8624)
                        .setEnd(8625)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }


    @Test
    void lastBaseOfFirstAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8598)
                        .setEnd(8599)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void oneBaseAfterFirstAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8597)
                        .setEnd(8598)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeSecondDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8403)
                        .setEnd(8404)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfSecondDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8402)
                        .setEnd(8403)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void lastBaseOfSecondDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8394)
                        .setEnd(8395)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(1));
    }

    @Test
    void oneBaseAfterSecondDonorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8393)
                        .setEnd(8394)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeSecondAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8225)
                        .setEnd(8226)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void firstBaseOfSecondAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8224)
                        .setEnd(8225)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void lastBaseOfSecondAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8198)
                        .setEnd(8199)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void oneBaseAfterSecondAcceptorRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8197)
                        .setEnd(8198)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void lastBaseOfCdsRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(8000)
                        .setEnd(8001)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void oneBaseAfterCdsRevStrand() throws Exception {
        SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(7999)
                        .setEnd(8000)
                        .setStrand(true)
                        .build())
                .setRef("C")
                .setAlt("G")
                .build();
        final SplicingLocationData data = locator.locate(variant, revTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }
}