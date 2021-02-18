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

package org.monarchinitiative.squirls.cli.data;

import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;

class Sequences {

    /**
     * A real sequence from interval `>chr9:136,224,501-136,224,800` (1-based coordinates) on hg19. The interval contains
     * exon 3 of <em>SURF2</em>.
     */
    private static final String SURF2_EXON3 = "TGTCCAGGGATGAGTCCAAGACACAGCCACCAGTCTGAATCCTTGCTGTGAACTGTCCCT" +
            "ACAAATTTGGTCTCTCTGCTCTGTAGGCACCAGTTGTTCTGCAAACTCACCCTGCGGCAC" +
            "ATCAACAAGTGCCCAGAACACGTGCTGAGGCACACCCAGGGCCGGCGGTACCAGCGAGCT" +
            "CTGTGTAAATGTAAGTCCCAGTGGACCCCCATCAGTGCATCGCCATCTGAGTGCATGCCC" +
            "GCCTTGCCCCAGATGGAGCGTGCTTGAAGGCAGGTCGTCCTTCAGCGATCCGTGTTGATG";
    /**
     * A real sequence from interval `>chr1:21,894,401-21,895,000` (1-based coordinates) on hg19. The interval contains
     * exon 7 of <em>ALPL</em>.
     */
    private static final String ALPL_EXON7 = "GTCCCAATAGACTCGTGATTTCATCTCCCCACGCTGGACAAGTAAGGCCCAGAGATGACT" +
            "GAGGCCTGGCTCACCAGGCTGGGAAAGTGTCCACACCATCTCCAGGGACTCCAGGAGTCC" +
            "AGGTTCCAAGCCGAGGTCACTGGGGCTTCTGGGCATCTTGGAACCCTGCAGAAGTGATGG" +
            "CTCCTGTCTCTTTTAGGTGATCATGGGGGGTGGCCGGAAATACATGTACCCCAAGAATAA" +
            "AACTGATGTGGAGTATGAGAGTGACGAGAAAGCCAGGGGCACGAGGCTGGACGGCCTGGA" +
            "CCTCGTTGACACCTGGAAGAGCTTCAAACCGAGATACAAGGTAGCCTGTGCTGGGGCCAT" +
            "GTGGCTGCAGAGGTGGCCTGTGATGGGGAGAGGCTGTGTGACCCCTGCTCTGAAGTTCTG" +
            "TTGTCCTCTGTAGAAAGGGCATCGGAAATCTTTCCTCCATGGGCTCAAATGGCCTAAGAG" +
            "ATATACAAGCAGTAGATAGTCAGTGACTAATCATTTCCTTCCCTTGGAGGGGATACCAGA" +
            "GAGAGGCGGATGGTGAGGAGGAGGCTGGGCCAGGGAATAACCTGCGTGGACATTGAGTCA";

    /**
     * A real sequence from interval `>chr16:2110401-2111000` (1-based coordinates) on hg19. The interval contains
     * exon 11 of <em>TSC2</em>.
     */
    private static final String TSC2_EXON11 = "ACCGGGTGCCCAGGATTCAGTTGCTGGTCTGTCCGAGTCAGGGACTTTGCAGGCAGGCAT" +
            "GGGGGTGGGGCCCGTCTGGGTCCTGACTGTGCTGGAGCATGTAGAAACCCCTCCTGGGCG" +
            "CCCCACCTGCTGTTTCTGCGGCCCCTGATAAACGTGTGGTGGGCACTGCGCGCTCAGGCG" +
            "TGCTACTCTCGGTCCCAAGGGTGACTGGGAGGGCGTCCCACAGCAAGCAAGCAGCTCTGA" +
            "CCCTGTGTGCTGGCCGGGCTCGTGTTCCAGGCCATGGCATGTCCGAACGAGGTGGTGTCC" +
            "TATGAGATCGTCCTGTCCATCACCAGGCTCATCAAGAAGTATAGGAAGGAGCTCCAGGTG" +
            "GTGGCGTGGGACATTCTGCTGAACATCATCGAACGGCTCCTTCAGCAGCTCCAGGTGGGG" +
            "TGGGGGCAGGAGCTCCGGGGAGCACCGGGAACCCAGACAGGCAGGCTCGGCCCACTCAGA" +
            "AGATGGTACCTTGGGCCCCATCTCTGGGGGTCCCGCAGAGACTGCCAGAACCGTGTTCTC" +
            "TGGTGATTCGCAGTGGCGCTCATCCACCTTCCACCGGAGACAGGTCTGATTTTTCCAGAC";

    /**
     * A real sequence from interval `chrX:107849601-107850400` (1-based coordinates) on hg19. The interval contains
     * exon 29 of <em>COL4A5</em>.
     */
    private static final String COL4A5_EXON29 = "tacaaaaaccacaatgttgttatcatacttagtaaaattaaaaataattgtttagtatta" +
            "tctaatactcagcctataatcatatttccCTACAAGGGTAGTCTGTTCTTTAATCTCATG" +
            "CTCTCTCAGTTTTTTTTTTTTATCCTGAAACCAGTCCAAATCATCTAATTTGTTTTACTA" +
            "AACCCTGTTTCCAATCCTTCCATATGTTTTCTCCAACATACCAATTACTTTTTATTTAAT" +
            "TATCTTCTCCTCTCCCCCCATGGAAGGAAAAGTATTGTCTTGTATTTTCGGCATTAAATT" +
            "CTCTGTGGCAAACAATAAGGACAGAAAAGTCATGGGAGTTTTTGTTGTGTTTTGTCATGT" +
            "GTATGCTCAAGGGTGAACCAGGATTTGCATTACCTGGGCCACCTGGGCCACCAGGACTTC" +
            "CAGGTTTCAAAGGAGCACTTGGTCCAAAAGGTGATCGTGGTTTCCCAGGACCTCCGGGTC" +
            "CTCCAGGACGCACTGGCTTAGATGGGCTCCCTGGACCAAAAGGTATGGAGGCTGTCACTG" +
            "CATCTCAACTTGCTTTTAACTTTTATAGAAATTGACACCTTTGGGAAAGTTTATGGGTGA" +
            "TAAGCCCAATTAACTGGAAACCCTATGCTGCCATTCTGTTACTGCTACACATTTTTTTCA" +
            "ATCCTTCATTGATTATTTTTGTCTTTTATTTCATTGAATAGATAATAAAATCATggcccg" +
            "gcacggtggctcactcctgtaatcccagcactttgggaggccaaggtgggcggatcacct" +
            "gaggtctggaattcaagacc";

    /**
     * A real sequence from interval `>chr19:39075401-39075900` (1-based coordinates) on hg19. The interval contains
     * exon 102 of <em>RYR1</em>.
     */
    private static final String RYR1_EXON102 = "cacagccctgaccatttctggctgttggtccctgtctgatgccgtatctgtgagcccttt" +
            "gagggcagggcccagggctgtctcagtcgttaccatgtcttcagccctgcctatcccggg" +
            "gccttggctggtactcagtgaatgtcgaatgaatgagtgaCCAGTGTGCTCCCCTCCCTC" +
            "AGTGTTACCTGTTTCACATGTACGTGGGTGTCCGGGCTGGCGGAGGCATTGGGGACGAGA" +
            "TCGAGGACCCCGCGGGTGACGAATACGAGCTCTACAGGGTGGTCTTCGACATCACCTTCT" +
            "TCTTCTTCGTCATCGTCATCCTGTTGGCCATCATCCAGGGTCAGTGCTGGGAGTGGGCGC" +
            "TCAGGGCCCGGAGGcaggctagctccatggctaagaatgcaggcccaggatccagtcggc" +
            "ctgcattcataccccatctctacctctcgctactgtgagaccttgggcaagtcacctctc" +
            "ggggcctccgtttctccatc";

    /**
     * A real sequence from interval `>chr11:5248001-5248400` (1-based coordinates) on hg19. The interval contains
     * exon 1 of <em>HBB</em>.
     */
    private static final String HBB_EXON1 = "TCTGGGTCCAAGGGTAGACCACCAGCAGCCTAAGGGTGGGAAAATAGACCAATAGGCAGA" +
            "GAGAGTCAGTGCCTATCAGAAACCCAAGAGTCTTCTCTGTCTCCACATGCCCAGTTTCTA" +
            "TTGGTCTCCTTAAACCTGTCTTGTAACCTTGATACCAACCTGCCCAGGGCCTCACCACCA" +
            "ACTTCATCCACGTTCACCTTGCCCCACAGGGCAGTAACGGCAGACTTCTCCTCAGGAGTC" +
            "AGATGCACCATGGTGTCTGTTTGAGGTTGCTAGTGAACACAGTTGTGTCAGAAGCAAATG" +
            "TAAGCAATAGATGGCTCTGCCCTGACTTTTATGCCCAGCCCTGGCTCCTGCCCTCCCTGC" +
            "TCCTGGGAGTAGATTGGCCAACCCTAGGGTGTGGCTCCAC";

    /**
     * A real sequence from interval `>chr13:32930401-32930900` (1-based coordinates) on hg19. The interval contains
     * exon 15 of <em>BRCA2</em>.
     */
    private static final String BRCA2_EXON15 = "ttctccattttggtcaggctggtcttgaactcccgacctcagatgatctgcccgcctcag" +
            "cctcccaaagtgctgggattacaggcgtgagccactgtgcctggccAGGGGTTGTGCTTT" +
            "TTAAATTTCAATTTTATTTTTGCTAAGTATTTATTCTTTGATAGATTTAATTACAAGTCT" +
            "TCAGAATGCCAGAGATATACAGGATATGCGAATTAAGAAGAAACAAAGGCAACGCGTCTT" +
            "TCCACAGCCAGGCAGTCTGTATCTTGCAAAAACATCCACTCTGCCTCGAATCTCTCTGAA" +
            "AGCAGCAGTAGGAGGCCAAGTTCCCTCTGCGTGTTCTCATAAACAGGTATGTGTTTGTCT" +
            "ACAATACTGATGGCTTTTATGACAGAGTGTAATTTTATTTCATTAACTAGTATCTACAAA" +
            "TGGCTTTGTTTAAAGAATGAACACATTAGTGCAGGAATGGATGAATGAAATCATCATATT" +
            "TTCTAATTAGCCTGCAGTGG";

    /**
     * A real sequence from interval `>chr12:6131801-6132200` (1-based coordinates) on hg19. The interval contains
     * exon 26 of <em>VWF</em>.
     */
    private static final String VWF_EXON26 = "TAGGACGTGGCAGGTGGAGGCTGAGATGAAGCAAGACCTAGAAGCACCTTTCCATCCATC" +
            "CCTATCCCATCCCACCAGCCTGACCCCCAGGGATAGAGGCCTCACCTGGAGGGCAGTGGG" +
            "CATGGCAGCCCTCCACACACTGCACAGGGCAGGCCAGTGGCTCAGGGTGCTGACACGTGA" +
            "CTTGACAGGCAGGTGCACAGCTGTTATAGCGCCACTCACACTCATACCCGTTCTCCCGGA" +
            "GATTCCTCTCCTCGCAGCTCTGGGCTGTGTAGACAGGAGACAAGGCTGTGGCCACAACAG" +
            "GCAAAGCCTCCAGGACTGCAGACCCATGTGGTGATGGCCTGCGCCATCTGGAGATAATGT" +
            "TGGGGAACTAGGGGACTATGGGCGTCACCAATATTAGAGA";


    /**
     * A real sequence from interval `>chr17:29527201-29527800` (1-based coordinates) on hg19. The interval contains
     * exon 9 of <em>NF1</em>.
     */
    private static final String NF1_EXON9 = "ACTGTTTTTTGTATTCCTTTAATAATTCAATAAAGAAAATAGAAAAAGGATTTTTTTTAA" +
            "ATTTGAGAAACATTTAAATGATGACCACTACTTAAATTATGAAATTGAAAACCACAAATA" +
            "TAAATTATGCATTCTTTATAGTATGAGTTTTAGAGGCTGTTAATTTGCTATAATATTAGC" +
            "TACATCTGGAATAGAAGAAACTTCATATATTATCTTATCGCTATATTTGAATTCTGTAGA" +
            "AGTTATTTCTGGACAGTCTACGAAAAGCTCTTGCTGGCCATGGAGGAAGTAGGCAGCTGA" +
            "CAGAAAGTGCTGCAATTGCCTGTGTCAAACTGTGTAAAGCAAGTACTTACATCAATTGGG" +
            "AAGATAACTCTGTCATTTTCCTACTTGTTCAGTCCATGGTGGTTGATCTTAAGGTAACAT" +
            "GCTTATTCTTTCTCTACTACAAACTTTAAGAAAATTAAATGAATTTTCTAGCATAAGTAT" +
            "TATGTCAAAGATAATTGCTAACATTAAAGTTCTGACTCTTCGTTGATAAGTTCATAGGAC" +
            "TTGCTTTTGTTGTTACTGTGTTCATCAGCCTAAATGGACTGAGAATATGAAGAAAACACC";

    private Sequences() {
        // no-op
    }

    /**
     * Get sequence corresponding to region `>chr9:136,224,501-136,224,800` (1-based coordinates) on hg19. This is the
     * region containing exon 3 of SURF2 gene.
     *
     * @return the sequence interval
     */
    static StrandedSequence getSurf2Exon3Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(), 136_224_501, 136_224_80), SURF2_EXON3);
    }

    /**
     * Get sequence corresponding to region `>chr1:21,894,401-21,895,000` (1-based coordinates) on hg19. The interval contains
     * exon 7 of <em>ALPL</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getAlplExon7Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(), 21894401, 21895000), ALPL_EXON7);
    }

    /**
     * A real sequence from interval `>chr16:2110401-2111000` (1-based coordinates) on hg19. The interval contains
     * exon 11 of <em>TSC2</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getTsc2Exon11Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),2_110_401, 2_111_000), TSC2_EXON11);
    }

    /**
     * A real sequence from interval `>chrX:107849601-107850400` (1-based coordinates) on hg19. The interval contains
     * exon 29 of <em>COL4A5</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getCol4a5Exon29Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),107_849_601, 107_850_400), COL4A5_EXON29);
    }

    /**
     * A real sequence from interval `>chr19:39075401-39075900` (1-based coordinates) on hg19. The interval contains
     * exon 102 of <em>RYR1</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getRyr1Exon102Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),39_075_401, 39_075_900), RYR1_EXON102);
    }

    /**
     * A real sequence from interval `>chr11:5248001-5248400` (1-based coordinates) on hg19. The interval contains
     * exon 1 of <em>HBB</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getHbbExon1Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),5_248_001, 5_248_400), HBB_EXON1);
    }

    /**
     * A real sequence from interval `>chr13:32930401-32930900` (1-based coordinates) on hg19. The interval contains
     * exon 15 of <em>BRCA2</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getBrca2Exon15Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),32_930_401, 32_930_900), BRCA2_EXON15);
    }

    /**
     * A real sequence from interval `>chr12:6131801-6132200` (1-based coordinates) on hg19. The interval contains
     * exon 26 of <em>VWF</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getVwfExon26Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),6131801, 6132200), VWF_EXON26);
    }

    /**
     * A real sequence from interval `>chr17:29527201-29527800` (1-based coordinates) on hg19. The interval contains
     * exon 9 of <em>NF1</em>.
     *
     * @return the sequence interval
     */
    static StrandedSequence getNf1Exon9Sequence(Contig contig) {
        return StrandedSequence.of(
                GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.oneBased(),29_527_201, 29_527_800), NF1_EXON9);
    }

}
