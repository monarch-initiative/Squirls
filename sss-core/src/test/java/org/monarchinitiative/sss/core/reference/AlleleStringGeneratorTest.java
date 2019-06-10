package org.monarchinitiative.sss.core.reference;

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
class AlleleStringGeneratorTest {

    @Autowired
    private SplicingParameters splicingParameters;

    private AlleleStringGenerator generator;

    private SequenceInterval si;

    @BeforeEach
    void setUp() {
        generator = new AlleleStringGenerator(splicingParameters);
        si = SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(95)
                        .setEnd(110)
                        .setStrand(true)
                        .build())
                .setSequence("TGATGgtaggtgaaa")
                .build();
    }


    @Test
    void simpleSnp() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .build())
                .setRef("G")
                .setAlt("T")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("ATGTtaggt"));
    }

    @Test
    void shortDeletion() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(103)
                        .build())
                .setRef("gta")
                .setAlt("g")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    void shortInsertion() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(100)
                        .setEnd(101)
                        .build())
                .setRef("g")
                .setAlt("gcc")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    void insertionAcross3PrimeBoundary() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(104)
                        .setEnd(105)
                        .build())
                .setRef("g")
                .setAlt("gcc")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    void deletionAcross3PrimeBoundary() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(104)
                        .setEnd(107)
                        .build())
                .setRef("gtg")
                .setAlt("g")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    void deletionOfWholeSite() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(96)
                        .setEnd(106)
                        .build())
                .setRef("GATGgtaggt")
                .setAlt("G")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void deletionSpanningFirstBasesOfDonor() {
        final SplicingVariant variant = SplicingVariant.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr1")
                        .setBegin(96)
                        .setEnd(99)
                        .build())
                .setRef("GAT")
                .setAlt("G")
                .build();
        // reference is TGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(100, variant, si);
        assertThat(allele, is("TGGgtaggt"));
    }
}