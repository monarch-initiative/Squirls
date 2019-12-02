package org.monarchinitiative.threes.core.reference.allele;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class AlleleGeneratorTest {

    private SequenceInterval donorSi;

    private SequenceInterval acceptorSi;

    private GenomePosition anchor;

    @Autowired
    private SplicingParameters splicingParameters;

    @Autowired
    private ReferenceDictionary referenceDictionary;

    private AlleleGenerator generator;

    @BeforeEach
    void setUp() {
        donorSi = SequenceInterval.builder()
                .interval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 93, 110))
                .sequence("CGTGATGgtaggtgaaa")
                .build();

        acceptorSi = SequenceInterval.builder()
                .interval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 70, 110))
                .sequence("atggcaaacactgttccttctctctttcagGTGGCCCTGC")
                .build();

        anchor = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        generator = new AlleleGenerator(splicingParameters);
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @Test
    void simpleSnp() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGttaggt"));
    }

    @Test
    void shortDeletion() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "gta", "g");
        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    void shortInsertion() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    void insertionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    void deletionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "gtg", "g");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    void deletionOfWholeSite() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 96);
        final GenomeVariant variant = new GenomeVariant(position, "GATGgtaggt", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void deletionSpanningFirstBasesOfDonor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 94);
        final GenomeVariant variant = new GenomeVariant(position, "GTGAT", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("CGGgtaggt"));
    }

    @Test
    void mismatchInContigsForDonor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 2, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void donorRefAllele() {
        final String donorSeq = generator.getDonorSiteSnippet(anchor, donorSi);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is("ATGgtaggt"));
    }

    @Test
    void donorRefAlleleIsNullWhenBadInput() {
        final String donorSeq = generator.getDonorSiteSnippet(new GenomePosition(referenceDictionary, Strand.FWD, 22, 100), donorSi);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is(nullValue()));
    }

    @Test
    void makeDonorInterval() {
        final GenomeInterval donor = generator.makeDonorInterval(anchor);
        assertThat(donor, is(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 97, 106)));
    }

    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @Test
    void simpleSnpInAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagCT"));
    }


    @Test
    void shortDeletionInAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "cag", "c");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gcaaacactgttccttctctctttcGT"));
    }


    @Test
    void shortInsertionInAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "c", "ctt");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcttagGT"));
    }


    @Test
    void insertionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "GCC");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcagGCCT"));
    }


    @Test
    void deletionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "GTGG", "G");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGC"));
    }


    @Test
    void deletionOfTheWholeAcceptorSite() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gcaaacactgttccttctctctttcagGT", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }


    @Test
    void deletionSpanningFirstBasesOfAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gca", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gaacactgttccttctctctttcagGT"));
    }

    @Test
    void mismatchInContigsForAcceptor() {
        final GenomePosition position = new GenomePosition(referenceDictionary, Strand.FWD, 2, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void acceptorRefAllele() {
        final String acceptorSeq = generator.getAcceptorSiteSnippet(anchor, acceptorSi);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is("aaacactgttccttctctctttcagGT"));
    }

    @Test
    void acceptorRefAlleleIsNullWhenBadInput() {
        final String acceptorSeq = generator.getAcceptorSiteSnippet(new GenomePosition(referenceDictionary, Strand.FWD, 22, 100), acceptorSi);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is(nullValue()));
    }

    @Test
    void makeAcceptorInterval() {
        final GenomeInterval acceptor = generator.makeAcceptorInterval(anchor);
        assertThat(acceptor, is(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 75, 102)));
    }
}