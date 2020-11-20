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
class AlleleGeneratorTest {

    private SequenceInterval sequence;

    private SequenceInterval donorSi;

    private SequenceInterval acceptorSi;

    private GenomePosition anchor;

    @Autowired
    private SplicingParameters splicingParameters;

    @Autowired
    private ReferenceDictionary rd;

    private AlleleGenerator generator;

    @BeforeEach
    void setUp() {
        sequence = SequenceInterval.of(
                new GenomeInterval(rd, Strand.FWD, 1, 0, 60),
                "aaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTT");
        donorSi = SequenceInterval.of(
                new GenomeInterval(rd, Strand.FWD, 1, 93, 110),
                "CGTGATGgtaggtgaaa");

        acceptorSi = SequenceInterval.of(
                new GenomeInterval(rd, Strand.FWD, 1, 70, 110),
                "atggcaaacactgttccttctctctttcagGTGGCCCTGC");

        anchor = new GenomePosition(rd, Strand.FWD, 1, 100);
        generator = new AlleleGenerator(splicingParameters);
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @Test
    void simpleSnp() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
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
    void shortDeletion() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "gta", "g");
        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgggtga"));
    }

    @Test
    void shortInsertion() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgcctag"));
    }

    @Test
    void insertionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "g", "gcc");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtaggc"));
    }

    @Test
    void deletionAcross3PrimeBoundary() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 104);
        final GenomeVariant variant = new GenomeVariant(position, "gtg", "g");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("ATGgtagga"));
    }

    @Test
    void deletionOfWholeSite() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 96);
        final GenomeVariant variant = new GenomeVariant(position, "GATGgtaggt", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is(nullValue()));
    }

    @Test
    void deletionSpanningFirstBasesOfDonor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 94);
        final GenomeVariant variant = new GenomeVariant(position, "GTGAT", "G");

        // reference is CGTGATGgtaggtgaaa
        final String allele = generator.getDonorSiteWithAltAllele(anchor, variant, donorSi);
        assertThat(allele, is("CGGgtaggt"));
    }

    @Test
    void mismatchInContigsForDonor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 2, 100);
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
        final String donorSeq = generator.getDonorSiteSnippet(new GenomePosition(rd, Strand.FWD, 22, 100), donorSi);
        // reference is CGTGATGgtaggtgaaa
        assertThat(donorSeq, is(nullValue()));
    }

    @Test
    void makeDonorInterval() {
        final GenomeInterval donor = generator.makeDonorInterval(anchor);
        assertThat(donor, is(new GenomeInterval(rd, Strand.FWD, 1, 97, 106)));
    }

    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @Test
    void simpleSnpInAcceptor() {
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
    void variantOutsideOfTheSiteInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 103, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGT G GCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGT"));
    }


    @Test
    void shortDeletionInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "cag", "c");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gcaaacactgttccttctctctttcGT"));
    }


    @Test
    void shortInsertionInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 97);
        final GenomeVariant variant = new GenomeVariant(position, "c", "ctt");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcttagGT"));
    }


    @Test
    void insertionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "G", "GCC");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("acactgttccttctctctttcagGCCT"));
    }


    @Test
    void deletionAcross3PrimeBoundaryInAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 100);
        final GenomeVariant variant = new GenomeVariant(position, "GTGG", "G");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("aaacactgttccttctctctttcagGC"));
    }


    @Test
    void deletionOfTheWholeAcceptorSite() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gcaaacactgttccttctctctttcagGT", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is(nullValue()));
    }


    @Test
    void deletionSpanningFirstBasesOfAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 1, 73);
        final GenomeVariant variant = new GenomeVariant(position, "gca", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        final String allele = generator.getAcceptorSiteWithAltAllele(anchor, variant, acceptorSi);
        assertThat(allele, is("gaacactgttccttctctctttcagGT"));
    }

    @Test
    void mismatchInContigsForAcceptor() {
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, 2, 100);
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
        final String acceptorSeq = generator.getAcceptorSiteSnippet(new GenomePosition(rd, Strand.FWD, 22, 100), acceptorSi);
        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(acceptorSeq, is(nullValue()));
    }

    @Test
    void makeAcceptorInterval() {
        final GenomeInterval acceptor = generator.makeAcceptorInterval(anchor);
        assertThat(acceptor, is(new GenomeInterval(rd, Strand.FWD, 1, 75, 102)));
    }

    // --------------------------      OTHER METHODS      -----------------------

    @Test
    void getDonorNeighborSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");
        final String snippet = generator.getDonorNeighborSnippet(variant.getGenomeInterval(), sequence, variant.getAlt());
        assertThat(snippet, is("aaaaCCCC" + "A" + "gggggTTT"));
    }

    @Test
    void getAcceptorNeighborSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 30, PositionType.ONE_BASED),
                "C", "A");
        final String snippet = generator.getAcceptorNeighborSnippet(variant.getGenomeInterval(), sequence, variant.getAlt());
        assertThat(snippet, is("aaCCCCCgggggTTTTTaaaaaCCCC" + "A" + "gggggTTTTTaaaaaCCCCCgggggT"));
    }

    @Test
    void getKmerRefSnippet() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");

        String snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 0);
        assertThat(snippet, is("C"));

        snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), 1);
        assertThat(snippet, is("CCg"));
    }

    @Test
    void getKmerRefSnippet_invalidInput() {
        final GenomeVariant variant = new GenomeVariant(
                new GenomePosition(rd, Strand.FWD, 1, 10, PositionType.ONE_BASED),
                "C", "A");

        // k=-1 should return null
        String snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), sequence, variant.getRef(), -1);
        assertThat(snippet, is(nullValue()));

        // not enough sequence returns null
        final SequenceInterval small = SequenceInterval.of(
                new GenomeInterval(rd, Strand.FWD, 1, 0, 1),
                "C");
        snippet = AlleleGenerator.getPaddedAllele(variant.getGenomeInterval(), small, variant.getRef(), 1);
        assertThat(snippet, is(nullValue()));
    }
}