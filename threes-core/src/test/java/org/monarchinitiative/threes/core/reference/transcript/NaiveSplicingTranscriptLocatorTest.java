package org.monarchinitiative.threes.core.reference.transcript;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.PojosForTesting;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.model.SplicingTranscript;
import org.monarchinitiative.threes.core.reference.SplicingLocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class NaiveSplicingTranscriptLocatorTest {

    private NaiveSplicingTranscriptLocator locator;

    @Autowired
    private SplicingParameters splicingParameters;

    @Autowired
    private ReferenceDictionary referenceDictionary;


    private SplicingTranscript fwdTranscript;

    private SplicingTranscript revTranscript;

    @BeforeEach
    void setUp() throws Exception {
        fwdTranscript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        revTranscript = PojosForTesting.getTranscriptWithThreeExonsOnRevStrand(referenceDictionary);
        locator = new NaiveSplicingTranscriptLocator(splicingParameters);
    }


    @Test
    void onDifferentContig() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 5, 999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }


    @Test
    void oneBaseBeforeCds() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }


    @Test
    void firstBaseOfCds() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeFirstDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1196), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1197), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }

    @Test
    void lastBaseOfFirstDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1205), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
    }


    @Test
    void firstBaseAfterFirstDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1206), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeFirstAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1374), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
    }

    @Test
    void firstBaseOfFirstAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1375), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }


    @Test
    void lastBaseOfFirstAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1401), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void oneBaseAfterFirstAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1402), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void oneBaseBeforeSecondDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1596), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
    }

    @Test
    void firstBaseOfSecondDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1597), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(1));
    }

    @Test
    void lastBaseOfSecondDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1605), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(1));
    }

    @Test
    void oneBaseAfterSecondDonor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1606), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void oneBaseBeforeSecondAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1774), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
    }


    @Test
    void firstBaseOfSecondAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1775), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void lastBaseOfSecondAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1801), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void oneBaseAfterSecondAcceptor() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1802), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }


    @Test
    void lastBaseOfCds() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
    }

    @Test
    void oneBaseAfterCds() throws Exception {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 2000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.OUTSIDE));
    }

    @Test
    void firstBaseOfSingleExonTranscript() throws Exception {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExon(referenceDictionary);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }


    @Test
    void lastBaseOfSingleExonTranscript() throws Exception {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExon(referenceDictionary);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1999), "C", "G");

        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
    }

}