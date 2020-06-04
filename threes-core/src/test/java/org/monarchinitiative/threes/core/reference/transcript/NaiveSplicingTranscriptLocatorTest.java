package org.monarchinitiative.threes.core.reference.transcript;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
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
    void setUp() {
        fwdTranscript = PojosForTesting.getTranscriptWithThreeExons(referenceDictionary);
        revTranscript = PojosForTesting.getTranscriptWithThreeExonsOnRevStrand(referenceDictionary);
        locator = new NaiveSplicingTranscriptLocator(splicingParameters);
    }


    private GenomeInterval makeInterval(int begin, int end) {
        return new GenomeInterval(referenceDictionary, Strand.FWD, 1, begin, end);
    }

    @Test
    void onDifferentContig() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 5, 999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data, is(SplicingLocationData.outside()));
    }


    @Test
    void oneBaseBeforeCds() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data, is(SplicingLocationData.outside()));
    }


    @Test
    void firstBaseOfCds() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }


    @Test
    void oneBaseBeforeFirstDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1196), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }

    @Test
    void firstBaseOfFirstDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1197), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }

    @Test
    void lastBaseOfFirstDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1205), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }


    @Test
    void firstBaseAfterFirstDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1206), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void oneBaseBeforeFirstAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1374), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1197, 1206)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void firstBaseOfFirstAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1375), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }


    @Test
    void lastBaseOfFirstAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1401), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(0));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void oneBaseAfterFirstAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1402), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void oneBaseBeforeSecondDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1596), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void firstBaseOfSecondDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1597), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void lastBaseOfSecondDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1605), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.DONOR));
        assertThat(data.getExonIdx(), is(1));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1375, 1402)));
    }

    @Test
    void oneBaseAfterSecondDonor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1606), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }


    @Test
    void oneBaseBeforeSecondAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1774), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.INTRON));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(-1));
        assertThat(data.getDonorRegion().get(), is(makeInterval(1597, 1606)));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }


    @Test
    void firstBaseOfSecondAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1775), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }

    @Test
    void lastBaseOfSecondAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1801), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.ACCEPTOR));
        assertThat(data.getIntronIdx(), is(1));
        assertThat(data.getExonIdx(), is(2));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }


    @Test
    void oneBaseAfterSecondAcceptor() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1802), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }


    @Test
    void lastBaseOfCds() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1999), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getExonIdx(), is(2));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().get(), is(makeInterval(1775, 1802)));
    }

    @Test
    void oneBaseAfterCds() {
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 2000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, fwdTranscript);
        assertThat(data, is(SplicingLocationData.outside()));
    }

    @Test
    void firstBaseOfSingleExonTranscript() {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExon(referenceDictionary);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1000), "C", "G");
        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }


    @Test
    void lastBaseOfSingleExonTranscript() {
        final SplicingTranscript se = PojosForTesting.getTranscriptWithSingleExon(referenceDictionary);
        GenomeVariant variant = new GenomeVariant(new GenomePosition(referenceDictionary, Strand.FWD, 1, 1999), "C", "G");

        final SplicingLocationData data = locator.locate(variant, se);
        assertThat(data.getPosition(), is(SplicingLocationData.SplicingPosition.EXON));
        assertThat(data.getExonIdx(), is(0));
        assertThat(data.getIntronIdx(), is(-1));
        assertThat(data.getDonorRegion().isEmpty(), is(true));
        assertThat(data.getAcceptorRegion().isEmpty(), is(true));
    }

}