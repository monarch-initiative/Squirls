package org.monarchinitiative.sss.core.reference.transcript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.PojosForTesting;
import org.monarchinitiative.sss.core.TestDataSourceConfig;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SplicingTranscript;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.monarchinitiative.sss.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.sss.core.reference.SplicingLocationData;
import org.monarchinitiative.sss.core.reference.fasta.InvalidCoordinatesException;
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

    private SplicingTranscript st;

    @BeforeEach
    void setUp() throws Exception {
        st = PojosForTesting.getTranscriptWithThreeExons();
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
        final SplicingLocationData data = locator.locate(variant, st);
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
        final SplicingLocationData data = locator.locate(variant, st);
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getFeatureIndex(), is(0));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getFeatureIndex(), is(1));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(2));
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
        final SplicingLocationData data = locator.locate(variant, st);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getFeatureIndex(), is(2));
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
        final SplicingLocationData data = locator.locate(variant, st);
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
        assertThat(data.getFeatureIndex(), is(0));
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
        assertThat(data.getFeatureIndex(), is(0));
    }
}