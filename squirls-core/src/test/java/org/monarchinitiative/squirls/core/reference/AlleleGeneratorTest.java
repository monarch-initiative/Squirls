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
import org.monarchinitiative.squirls.core.TestContig;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.svart.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class AlleleGeneratorTest {

    private static final Contig contig = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, "1", AssignedMoleculeType.CHROMOSOME, 10_000, "", "", "");

    private StrandedSequence sequence;

    private StrandedSequence donorSequence;

    private StrandedSequence acceptorSequence;

    private GenomicRegion exon;

    private AlleleGenerator generator;

    @BeforeEach
    public void setUp() {
        exon = GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(80), Position.of(100));
        sequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 0, 60),
                "aaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTTaaaaaCCCCCgggggTTTTT");
        donorSequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 93, 110),
                "CGTGATGgtaggtgaaa");

        acceptorSequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 50, 90),
                "atggcaaacactgttccttctctctttcagGTGGCCCTGC");

        generator = new AlleleGenerator(SplicingParameters.of(3, 6, 2, 25));
    }

    // --------------------------      DONOR ALLELE     ---------------------------

    @ParameterizedTest
    @CsvSource({
            " 97,     G,   C,   ATGgtaggt",

            "101,     g,   t,   ATGttaggt",
            "101,   gta,   g,   ATGgggtga",
            "101,     g, gcc,   ATGgcctag",
            "105,     g, gcc,   ATGgtaggc",
            "105,   gtg,   g,   ATGgtagga",
            " 95, GTGAT,   G,   CGGgtaggt",
    })
    public void getDonorSiteWithAltAllele(int pos, String ref, String alt, String expected) {
        Contig contig = TestContig.of(1, 200);

        Variant variant = Variant.of(contig,"", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(pos), ref, alt);
        StrandedSequence sequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 93, 110),
                "CGTG" + "ATGgtaggt" + "gaaa");
        GenomicRegion donor = GenomicRegion.zeroBased(contig, 97, 106);

        String allele = generator.getDonorSiteWithAltAllele(donor, variant, sequence);

        assertThat(allele, equalTo(expected));
    }

    @Test
    public void deletionOfWholeSite() {
        Variant variant = Variant.of(contig,"", Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(96), "GATGgtaggt", "G");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(exon, variant, donorSequence);

        assertThat(allele, is(nullValue()));
    }

    @Test
    public void mismatchInContigsForDonor() {
        Contig other = Contig.of(44, "44", SequenceRole.ASSEMBLED_MOLECULE, "44", AssignedMoleculeType.CHROMOSOME, 100, "", "", "");
        Variant variant = Variant.of(other, "", Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(50), "g", "t");

        // reference is CGTGATGgtaggtgaaa
        String allele = generator.getDonorSiteWithAltAllele(exon, variant, donorSequence);
        assertThat(allele, is(nullValue()));
    }

    // --------------------------      ACCEPTOR ALLELE      -----------------------


    @ParameterizedTest
    @CsvSource({
            " 83,    G,   C,     aaacactgttccttctctctttcagGT",

            " 81,    G,   C,     aaacactgttccttctctctttcagCT",
            " 78,  cag,   c,     gcaaacactgttccttctctctttcGT",
            " 78,    c, ctt,     acactgttccttctctctttcttagGT",
            " 81,    G, GCC,     acactgttccttctctctttcagGCCT",
            " 81, GTGG,   G,     aaacactgttccttctctctttcagGC",
            " 54,  gca,   g,     gaacactgttccttctctctttcagGT",
    })
    public void getAcceptorSiteWithAltAllele(int pos, String ref, String alt, String expected) {
        Variant variant = Variant.of(contig, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(pos), ref, alt);

        StrandedSequence sequence = StrandedSequence.of(
                GenomicRegion.zeroBased(contig, 50, 90),
                "atggc" + "aaacactgttccttctctctttcagGT" + "GGCCCTGC");
        GenomicRegion acceptor = GenomicRegion.zeroBased(contig, 55, 82);

        assertThat(generator.getAcceptorSiteWithAltAllele(acceptor, variant, sequence), is(expected));
    }

    @Test
    public void deletionOfTheWholeAcceptorSite() {
        Variant variant = Variant.of(contig, "", Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(73), "gcaaacactgttccttctctctttcagGT", "g");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(generator.getAcceptorSiteWithAltAllele(exon, variant, acceptorSequence), is(nullValue()));
    }

    @Test
    public void mismatchInContigsForAcceptor() {
        Contig other = TestContig.of(22, 100);
        Variant variant = Variant.of(other, "", Strand.POSITIVE, CoordinateSystem.zeroBased(), Position.of(50), "G", "C");

        // reference is atggcaaacactgttccttctctctttcagGTGGCCCTGC
        assertThat(generator.getAcceptorSiteWithAltAllele(exon, variant, acceptorSequence), is(nullValue()));
    }

    // --------------------------      OTHER METHODS      -----------------------

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, 5, 10,   7, 16",
            "POSITIVE, 5,  9,   6, 15",
            "NEGATIVE, 2,  5,   2, 11"
    })
    public void makeDonorInterval(Strand strand, int exonStart, int exonEnd,
                                  int start, int end) {
        Contig contig = TestContig.of(1, 20);
        GenomicRegion exon = GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(), Position.of(exonStart), Position.of(exonEnd));

        GenomicRegion donor = generator.makeDonorInterval(exon);

        assertThat(donor.contig(), equalTo(contig));
        assertThat(donor.start(), equalTo(start));
        assertThat(donor.end(), equalTo(end));
        assertThat(donor.strand(), equalTo(strand));
        assertThat(donor.coordinateSystem(), equalTo(CoordinateSystem.zeroBased())); // always
    }


    @ParameterizedTest
    @CsvSource({
            "POSITIVE, 30, 40,    5, 32",
            "POSITIVE, 31, 40,    6, 33",
            "NEGATIVE, 40, 50,   15, 42"
    })
    public void makeAcceptorInterval(Strand strand, int exonStart, int exonEnd,
                                     int start, int end) {
        Contig contig = TestContig.of(1, 50);
        GenomicRegion exon = GenomicRegion.of(contig, strand, CoordinateSystem.zeroBased(), Position.of(exonStart), Position.of(exonEnd));

        GenomicRegion acceptor = generator.makeAcceptorInterval(exon);

        assertThat(acceptor.contig(), equalTo(contig));
        assertThat(acceptor.start(), equalTo(start));
        assertThat(acceptor.end(), equalTo(end));
        assertThat(acceptor.strand(), equalTo(strand));
        assertThat(acceptor.coordinateSystem(), equalTo(CoordinateSystem.zeroBased()));  // always
    }

    @ParameterizedTest
    @CsvSource({
            "10,   C,   A, POSITIVE,     aaaaCCCCAgggggTTT",
            "10,   C,   A, NEGATIVE,     AAAcccccTGGGGtttt",
            "10,   C, TTT, POSITIVE,     aaaaCCCCTTTgggggTTT",
            "10, Cgg,   C, POSITIVE,     aaaaCCCCCgggTTTTT",
    })
    public void getDonorNeighborSnippet(int pos, String ref, String alt, Strand strand, String expected) {
        Variant variant = Variant.of(contig, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(pos), ref, alt).withStrand(strand);
        assertThat(generator.getDonorNeighborSnippet(variant, sequence, variant.alt()), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "30,   C,   A, POSITIVE,      aaCCCCCgggggTTTTTaaaaaCCCCAgggggTTTTTaaaaaCCCCCgggggT",
            "30,   C,   A, NEGATIVE,      AcccccGGGGGtttttAAAAAcccccTGGGGtttttAAAAAcccccGGGGGtt",
            "30,   C, TTT, POSITIVE,      aaCCCCCgggggTTTTTaaaaaCCCCTTTgggggTTTTTaaaaaCCCCCgggggT",
            "30, Cgg,   C, POSITIVE,      aaCCCCCgggggTTTTTaaaaaCCCCCgggTTTTTaaaaaCCCCCgggggTTT",
    })
    public void getAcceptorNeighborSnippet(int pos, String ref, String alt, Strand strand, String expected) {
        Variant variant = Variant.of(contig,"", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(pos), ref, alt).withStrand(strand);
        assertThat(generator.getAcceptorNeighborSnippet(variant, sequence, variant.alt()), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2, POSITIVE, C, A, 0,     C,     A",
            "2, POSITIVE, C, A, 1,    aCg,   aAg",
            "3, POSITIVE, G, T, 2,    acGtA, acTtA",

            "4, NEGATIVE, C, A, 0,     C,     A",
            "4, NEGATIVE, C, A, 1,    ACG,   AAG",
            "5, NEGATIVE, G, A, 2,    ACGTa, ACATa",
    })
    public void getKmerRefSnippet(int pos, Strand strand, String ref, String alt, int padding, String refAllele, String altAllele) {
        Contig contig = TestContig.of(1, 10);
        StrandedSequence seq = StrandedSequence.of(GenomicRegion.zeroBased(contig, 0, 10), "acgtACGTac");
        Variant variant = Variant.of(contig,"", strand, CoordinateSystem.oneBased(), Position.of(pos), ref, alt);

        assertThat(AlleleGenerator.getPaddedAllele(variant, seq, variant.ref(), padding), equalTo(refAllele));
        assertThat(AlleleGenerator.getPaddedAllele(variant, seq, variant.alt(), padding), equalTo(altAllele));
    }

    @Test
    public void getKmerRefSnippet_invalidInput() {
        Variant variant = Variant.of(contig,"", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(10), "C", "A");

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