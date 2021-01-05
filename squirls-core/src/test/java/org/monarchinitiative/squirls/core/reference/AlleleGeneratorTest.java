/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.core.reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.variant.api.*;
import org.monarchinitiative.variant.api.impl.DefaultVariant;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// TODO - parametrize tests
@SpringBootTest(classes = {TestDataSourceConfig.class})
public class AlleleGeneratorTest {

    private static final Contig contig = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 10_000, "", "", "");

    private StrandedSequence sequence;

    private StrandedSequence donorSequence;

    private StrandedSequence acceptorSequence;

    private GenomicPosition anchor;

    private AlleleGenerator generator;

    @BeforeEach
    public void setUp() {
        sequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 0, 60),
                "aaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTT");
        donorSequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 93, 110),
                "CGTGATGgtaggtgaaa");

        acceptorSequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 70, 110),
                "atggcaaacactgttccttctctctttcagGTGGCCCTGC");

        anchor = GenomicPosition.zeroBased(contig, Strand.POSITIVE, Position.of(100));
        generator = new AlleleGenerator(SplicingParameters.of(3, 6, 2, 25));
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @Test
    public void simpleSnp() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGttaggt"));
    }


    /**
     * Check that a sequence of the donor site is returned when variant is outside the site
     */
    @Test
    public void variantOutsideOfTheSite() {
        Variant variant = DefaultVariant.oneBased(contig, 97, "G", "C");

        // reference is CGT G ATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGgtaggt"));
    }


    @Test
    public void shortDeletion() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "gta", "g");
        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    public void shortInsertion() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    public void insertionAcross3PrimeBoundary() {
        Variant variant = DefaultVariant.zeroBased(contig, 104, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    public void deletionAcross3PrimeBoundary() {
        Variant variant = DefaultVariant.zeroBased(contig, 104, "gtg", "g");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    public void deletionOfWholeSite() {
        Variant variant = DefaultVariant.zeroBased(contig, 96, "GATGgtaggt", "G");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void deletionSpanningFirstBasesOfDonor() {
        Variant variant = DefaultVariant.zeroBased(contig, 94, "GTGAT", "G");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is("CGGgtaggt"));
    }

    @Test
    public void mismatchInContigsForDonor() {
        Contig other = Contig.of(44, "44", SequenceRole.ASSEMBLED_MOLECULE, "44", AssignedMoleculeType.CHROMOSOME, 100, "", "", "");
        Variant variant = DefaultVariant.zeroBased(other, 100, "g", "t");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSequence);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void donorRefAllele() {
        String donorSeq = generator.getDonorSiteSnippet(anchor, donorSequence);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is("ATGgtaggt"));
    }

    @Test
    public void donorRefAlleleIsNullWhenBadInput() {
        Contig other = Contig.of(44, "44", SequenceRole.ASSEMBLED_MOLECULE, "44", AssignedMoleculeType.CHROMOSOME, 100, "", "", "");
        String donorSeq = generator.getDonorSiteSnippet(GenomicPosition.zeroBased(other, Strand.POSITIVE, Position.of(22)), donorSequence);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is(nullValue()));
    }

    @Test
    public void makeDonorInterval() {
        GenomicRegion donor = generator.makeDonorInterval(anchor);

        assertThat(donor.contig(), equalTo(contig));
        assertThat(donor.start(), equalTo(97));
        assertThat(donor.end(), equalTo(106));
        assertThat(donor.strand(), equalTo(Strand.POSITIVE));
    }

    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @Test
    public void simpleSnpInAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("aaacactgttccttctctctttcagCT"));
    }

    /**
     * Check that a sequence of the acceptor site is returned when variant is outside the site.
     */
    @Test
    public void variantOutsideOfTheSiteInAcceptor() {
        Variant variant = DefaultVariant.oneBased(contig, 103, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGT G GCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("aaacactgttccttctctctttcagGT"));
    }


    @Test
    public void shortDeletionInAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 97, "cag", "c");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("gcaaacactgttccttctctctttcGT"));
    }


    @Test
    public void shortInsertionInAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 97, "c", "ctt");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("acactgttccttctctctttcttagGT"));
    }


    @Test
    public void insertionAcross3PrimeBoundaryInAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "G", "GCC");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("acactgttccttctctctttcagGCCT"));
    }


    @Test
    public void deletionAcross3PrimeBoundaryInAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 100, "GTGG", "G");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("aaacactgttccttctctctttcagGC"));
    }


    @Test
    public void deletionOfTheWholeAcceptorSite() {
        Variant variant = DefaultVariant.zeroBased(contig, 73, "gcaaacactgttccttctctctttcagGT", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is(nullValue()));
    }


    @Test
    public void deletionSpanningFirstBasesOfAcceptor() {
        Variant variant = DefaultVariant.zeroBased(contig, 73, "gca", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is("gaacactgttccttctctctttcagGT"));
    }

    @Test
    public void mismatchInContigsForAcceptor() {
        Contig other = Contig.of(22, "22", SequenceRole.ASSEMBLED_MOLECULE, "22", AssignedMoleculeType.CHROMOSOME, 100, "", "", "");
        Variant variant = DefaultVariant.zeroBased(other, 100, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSequence);
        assertThat(allele, is(nullValue()));
    }

    @Test
    public void acceptorRefAllele() {
        String acceptorSeq = generator.getAcceptorSiteSnippet(anchor, acceptorSequence);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is("aaacactgttccttctctctttcagGT"));
    }

    @Test
    public void acceptorRefAlleleIsNullWhenBadInput() {
        Contig other = Contig.of(22, "22", SequenceRole.ASSEMBLED_MOLECULE, "22", AssignedMoleculeType.CHROMOSOME, 100, "", "", "");
        String acceptorSeq = generator.getAcceptorSiteSnippet(GenomicPosition.zeroBased(other, Strand.POSITIVE, Position.of(100)), acceptorSequence);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is(nullValue()));
    }

    @Test
    public void makeAcceptorInterval() {
        GenomicRegion acceptor = generator.makeAcceptorInterval(anchor);

        assertThat(acceptor.contig(), equalTo(contig));
        assertThat(acceptor.start(), equalTo(75));
        assertThat(acceptor.end(), equalTo(102));
        assertThat(acceptor.strand(), equalTo(Strand.POSITIVE));
        assertThat(acceptor.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    // --------------------------      OTHER METHODS      -----------------------

    @ParameterizedTest
    @CsvSource({
            "10, C, A,       aaaaCCCCAgggggTTT",
            "10, C, TTT,     aaaaCCCCTTTgggggTTT",
            "10, Cgg, C,     aaaaCCCCCgggTTTTT",
    })
    public void getDonorNeighborSnippet(int pos, String ref, String alt, String expected) {
        Variant variant = DefaultVariant.oneBased(contig, pos, ref, alt);
        assertThat(generator.getDonorNeighborSnippet(variant, sequence, variant.alt()), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "30, C, A,       aaCCCCCgggggTTTTTaaaaaCCCCAgggggTTTTTaaaaaCCCCCgggggT",
            "30, C, TTT,     aaCCCCCgggggTTTTTaaaaaCCCCTTTgggggTTTTTaaaaaCCCCCgggggT",
            "30, Cgg, C,     aaCCCCCgggggTTTTTaaaaaCCCCCgggTTTTTaaaaaCCCCCgggggTTT",
    })
    public void getAcceptorNeighborSnippet(int pos, String ref, String alt, String expected) {
        Variant variant = DefaultVariant.oneBased(contig, pos, ref, alt);
        assertThat(generator.getAcceptorNeighborSnippet(variant, sequence, variant.alt()), equalTo(expected));
    }

    @Test
    public void getKmerRefSnippet() {
        Variant variant = DefaultVariant.oneBased(contig, 10, "C", "A");
        assertThat(AlleleGenerator.getPaddedAllele(variant, sequence, variant.ref(), 1), is("CCg"));
    }

    @Test
    public void getKmerRefSnippet_invalidInput() {
        Variant variant = DefaultVariant.oneBased(contig, 10, "C", "A");

        // k=-1 should return null
        String snippet = AlleleGenerator.getPaddedAllele(variant, sequence, variant.ref(), -1);
        assertThat(snippet, is(nullValue()));

        // not enough sequence returns null
        StrandedSequence small = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, Strand.POSITIVE, Position.of(0), Position.of(1)),
                "C");
        snippet = AlleleGenerator.getPaddedAllele(variant, small, variant.ref(), 1);
        assertThat(snippet, is(nullValue()));
    }
}