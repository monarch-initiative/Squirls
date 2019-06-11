package org.monarchinitiative.sss.core.reference.allele;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.TestDataSourceConfig;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class AlleleGeneratorTest {

    @Autowired
    private SplicingParameters splicingParameters;

    private static SequenceInterval donorSi;

    private static SequenceInterval acceptorSi;

    private AlleleGenerator generator;

    @BeforeAll
    static void setUpBefore() {
        donorSi = SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(95)
                        .setEnd(110)
                        .setStrand(true)
                        .build())
                .setSequence("TGATGgtaggtgaaa")
                .build();
        acceptorSi = SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(70)
                        .setEnd(110)
                        .setStrand(true)
                        .build())
                .setSequence("atggcaaacactgttccttctctctttcagGTGGCCCTGC")
                .build();
    }

    @BeforeEach
    void setUp() {
        generator = new AlleleGenerator(splicingParameters);
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @Test
    void simpleSnp() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .setStrand(true)
                        .build())
                .setRef("G")
                .setAlt("T")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("ATGTtaggt"));
    }

    @Test
    void shortDeletion() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(103)
                        .setStrand(true)
                        .build())
                .setRef("gta")
                .setAlt("g")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    void shortInsertion() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .setStrand(true)
                        .build())
                .setRef("g")
                .setAlt("gcc")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    void insertionAcross3PrimeBoundary() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(104)
                        .setEnd(105)
                        .setStrand(true)
                        .build())
                .setRef("g")
                .setAlt("gcc")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    void deletionAcross3PrimeBoundary() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(104)
                        .setEnd(107)
                        .setStrand(true)
                        .build())
                .setRef("gtg")
                .setAlt("g")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    void deletionOfWholeSite() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(96)
                        .setEnd(106)
                        .setStrand(true)
                        .build())
                .setRef("GATGgtaggt")
                .setAlt("G")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void deletionSpanningFirstBasesOfDonor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(96)
                        .setEnd(99)
                        .setStrand(true)
                        .build())
                .setRef("GAT")
                .setAlt("G")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, donorSi);
        assertThat(allele, is("TGGgtaggt"));
    }


    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @Test
    void simpleSnpInAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .setStrand(true)
                        .build())
                .setRef("G")
                .setAlt("C")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagCT"));
    }


    @Test
    void shortDeletionInAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(97)
                        .setEnd(100)
                        .setStrand(true)
                        .build())
                .setRef("cag")
                .setAlt("c")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("gcaaacactgttccttctctctttcGT"));
    }


    @Test
    void shortInsertionInAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(97)
                        .setEnd(98)
                        .setStrand(true)
                        .build())
                .setRef("c")
                .setAlt("ctt")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcttagGT"));
    }


    @Test
    void insertionAcross3PrimeBoundaryInAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .setStrand(true)
                        .build())
                .setRef("G")
                .setAlt("GCC")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcagGCCT"));
    }


    @Test
    void deletionAcross3PrimeBoundaryInAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(104)
                        .setStrand(true)
                        .build())
                .setRef("GTGG")
                .setAlt("G")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGC"));
    }


    @Test
    void deletionOfTheWholeAcceptorSite() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(73)
                        .setEnd(102)
                        .setStrand(true)
                        .build())
                .setRef("gcaaacactgttccttctctctttcagGT")
                .setAlt("g")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }


    @Test
    void deletionSpanningFirstBasesOfAcceptor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(73)
                        .setEnd(76)
                        .build())
                .setRef("gca")
                .setAlt("g")
                .build();
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(100, variant, acceptorSi);
        assertThat(allele, is("gaacactgttccttctctctttcagGT"));
    }

}