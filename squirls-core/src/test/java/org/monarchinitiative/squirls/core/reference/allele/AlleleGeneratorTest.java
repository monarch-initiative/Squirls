package org.monarchinitiative.squirls.core.reference.allele;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {TestDataSourceConfig.class})
public class AlleleGeneratorTest {

    private SequenceInterval sequence;

    private SequenceInterval donorSi;

    private SequenceInterval acceptorSi;

    private GenomePosition anchor;

    @Autowired
    public SplicingParameters splicingParameters;

    @Autowired
    public ReferenceDictionary rd;

    private AlleleGenerator generator;

    @BeforeEach
    public void setUp() {
        sequence = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 1, 0, 60))
                .sequence("aaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTT")
                .build();
        donorSi = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 1, 93, 110))
                .sequence("CGTGATGgtaggtgaaa")
                .build();

        acceptorSi = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 1, 70, 110))
                .sequence("atggcaaacactgttccttctctctttcagGTGGCCCTGC")
                .build();

        anchor = new GenomePosition(rd, Strand.FWD, 1, 100);
        generator = new AlleleGenerator(splicingParameters);
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @Test
    public void simpleSnp() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGttaggt"));
    }


    /**
     * Check that a sequence of the donor site is returned when variant is outside the site
     */
    @Test
    void variantOutsideOfTheSite() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 97, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is CGT G ATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtaggt"));
    }


    @Test
    public void shortDeletion() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "gta", "g");
        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    public void shortInsertion() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    public void insertionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    public void deletionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "gtg", "g");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    public void deletionOfWholeSite() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 96);
        final GenomeVariant variant = new GenomeVariant(position, "GATGgtaggt", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void deletionSpanningFirstBasesOfDonor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 94);
        final GenomeVariant variant = new GenomeVariant(position, "GTGAT", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("CGGgtaggt"));
    }

    @Test
    public void mismatchInContigsForDonor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 2, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void donorRefAllele() {
        final String donorSeq = generator.getDonorSiteSnippet(anchor, donorSi);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is("ATGgtaggt"));
    }

    @Test
    public void donorRefAlleleIsNullWhenBadInput() {
        final String donorSeq = generator.getDonorSiteSnippet(new GenomePosition(rd, Strand.FWD, 22, 100), donorSi);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is(nullValue()));
    }

    @Test
    public void makeDonorInterval() {
        final GenomeInterval donor = generator.makeDonorInterval(anchor);
        assertThat(donor, is(new GenomeInterval(rd, Strand.FWD, 1, 97, 106)));
    }

    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @Test
    public void simpleSnpInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagCT"));
    }

    /**
     * Check that a sequence of the acceptor site is returned when variant is outside the site.
     */
    @Test
    public void variantOutsideOfTheSiteInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 103, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGT G GCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGT"));
    }


    @Test
    public void shortDeletionInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "cag", "c");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gcaaacactgttccttctctctttcGT"));
    }


    @Test
    public void shortInsertionInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "c", "ctt");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcttagGT"));
    }


    @Test
    public void insertionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "GCC");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcagGCCT"));
    }


    @Test
    public void deletionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "GTGG", "G");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGC"));
    }


    @Test
    public void deletionOfTheWholeAcceptorSite() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gcaaacactgttccttctctctttcagGT", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }


    @Test
    public void deletionSpanningFirstBasesOfAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gca", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gaacactgttccttctctctttcagGT"));
    }

    @Test
    public void mismatchInContigsForAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 2, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void acceptorRefAllele() {
        final String acceptorSeq = generator.getAcceptorSiteSnippet(anchor, acceptorSi);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is("aaacactgttccttctctctttcagGT"));
    }

    @Test
    public void acceptorRefAlleleIsNullWhenBadInput() {
        final String acceptorSeq = generator.getAcceptorSiteSnippet(new GenomePosition(rd, Strand.FWD, 22, 100), acceptorSi);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is(nullValue()));
    }

    @Test
    public void makeAcceptorInterval() {
        final GenomeInterval acceptor = generator.makeAcceptorInterval(anchor);
        assertThat(acceptor, is(new GenomeInterval(rd, Strand.FWD, 1, 75, 102)));
    }

    // --------------------------      OTHER METHODS      -----------------------

    @Test
    public void getDonorNeighborSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");
        final String snippet = generator.getDonorNeighborSnippet(variant.getGenomeInterval(), sequence, variant.getAlt());
        assertThat(snippet, is("aaaaCCCC" + "A" + "gggggTTT"));
    }

    @Test
    public void getAcceptorNeighborSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 30, PositionType.ONE_BASED),
                "C", "A");
        final String snippet = generator.getAcceptorNeighborSnippet(variant.getGenomeInterval(), sequence, variant.getAlt());
        assertThat(snippet, is("aaCCCCCgggggTTTTTaaaaaCCCC" + "A" + "gggggTTTTTaaaaaCCCCCgggggT"));
    }

    @Test
    public void getKmerRefSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");

        String snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 0);
        assertThat(snippet, is("C"));

        snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 1);
        assertThat(snippet, is("CCg"));
    }

    @Test
    public void getKmerRefSnippet_invalidInput() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");

        // k=-1 should return null
        String snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), -1);
        assertThat(snippet, is(nullValue()));

        // not enough sequence returns null
        final SequenceInterval small = SequenceInterval.builder()
                .interval(new GenomeInterval(rd, Strand.FWD, 1, 0, 1)).sequence("C")
                .build();
        snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), small, variant.getRef(), 1);
        assertThat(snippet, is(nullValue()));
    }
}