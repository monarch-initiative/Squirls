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

package org.monarchinitiative.squirls.ingest;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import org.jblas.DoubleMatrix;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 *
 */
public class PojosForTesting {

    private PojosForTesting() {
        // static utility class
    }

    public static SplicingTranscript makeAlphaTranscript(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("ACC_ALPHA")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 900))
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 300))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 300, 700))
                        .setDonorScore(2.345)
                        .setAcceptorScore(3.210)
                        .build())
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 700, 900))
                        .build())
                .build();
    }

    public static SplicingTranscript makeBetaTranscript(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("ACC_BETA")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 3, 1000, 9000))
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 3, 1000, 3000))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 3, 3000, 7000))
                        .setDonorScore(2.345)
                        .setAcceptorScore(3.210)
                        .build())
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 3, 7000, 9000))
                        .build())
                .build();
    }


    public static TranscriptModel makeThreeExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval first = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 12_000);
        builder.addExonRegion(first);

        GenomeInterval second = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 14_000, 16_000);
        builder.addExonRegion(second);

        GenomeInterval third = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 18_000, 20_000);
        builder.addExonRegion(third);

        return builder.build();
    }

    public static TranscriptModel makeSingleExonTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000);
        builder.setCDSRegion(cdsRegion);

        GenomeInterval exon = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000);
        builder.addExonRegion(exon);
        return builder.build();
    }

    public static TranscriptModel makeSmallTranscriptModel(ReferenceDictionary referenceDictionary) {
        TranscriptModelBuilder builder = new TranscriptModelBuilder();
        builder.setAccession("ACCID");
        builder.setGeneSymbol("GENE");
        builder.setSequence("");

        GenomeInterval txRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 200);
        builder.setTXRegion(txRegion);

        GenomeInterval cdsRegion = new GenomeInterval(referenceDictionary, Strand.FWD, 2, 120, 180);
        builder.setCDSRegion(cdsRegion);

        // the first and the last exons are really small
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 100, 101));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 110, 160));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 170, 190));
        builder.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 199, 200));
        return builder.build();
    }

    public static SequenceInterval getSequenceIntervalForTranscriptWithThreeExons(ReferenceDictionary referenceDictionary) {
        return SequenceInterval.of(
                new GenomeInterval(referenceDictionary, Strand.FWD, 1, 900, 2100),
                "AAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGTTACTAGTAAGCCTGATGCCACT" + // 960
                        "ACACAATTCTAGCTTTTCTCTTTAGGATGATTGTTTCATT" + // 1000
                        "CAGTCTTATCTCTTTTAGAA" + // 1020
                        "AACATAGGaaaaaattatttaataataaaatttaattGGCAAAATGAAGGTATGGCTTAT" + // 1080
                        "AAGAGTGTTTTCCTATTGTTTTCAGTGTAGGACTCACTGTTCTAAATAACTGGGACACCC" + // 1140
                        //                                      v  <- 1200  (end of the 0th exon)
                        "AAGGATTCTGTAAAATGCCATCCAGTTATCATTTATATTC" +
                        "CCTAACTCAAAATTCATTCA" +
                        "CATGTATTCATTTTTTTCTAAACAAATTAGCATGTAGAATTCTGGTTAAAATTTGGCATA" +
                        "GAACACCCGGGTATTTTTTCATAATGCACCCAATAACTGTCATTCACTAATTGAGAATGG" +
                        //                                                          v  <- 1400 (end of the 0th intron)
                        "TGATTTAACAAAGGATAATAAAGTTATGAAACCAATGCCACAAAACATCTGTCTCTAACT" +
                        "GgtgtgtgtgtgtgtgtgtgtgtgtgtgtgtgtAAGAGGGAGAGAGAGAAAATTTCACTC" +
                        "CCTCCATAAATCTCACAGTATTCTTTTCTTtttcctttcctttccttgctcttctttctc" +
                        "tcctattgctttcctttcatttccttCTCATAAAAGAAAAATAACAATATAGAAAATAAC" +
                        "AAAATATAGATGGTCAACCTTTTTAATATTAAGGTTACCTAAAATGCCATTATCCAAAGT" +
                        "GGTTCTCTAGAGATGCTGATGTATATACTTACATATTTTACAGTGTATTCAAATAAAGAG" +
                        "TATATTACATAAGACATATCCTTTTGTAACCAACTTTTGTCATTAACAATTTACTGGACT" +
                        "TGTCAACAAACCTAAATCTGTATCGTCTATAATGGCTACGTTCATTTTGGTATGAATCTT" +
                        "AATTACCCCTTTCTGCATTATTTAATGATTTTCTCATATGTCACTCTTAAATGTACTTCT" +
                        "AATTTTTCACTTTACATCACATAATGAATGGATCCAAATATGTTATGGATAGATATCTTC" +
                        "AAACTTTCTACTTACAAGTAGTGATAATAACAGATGTTCTCTCTAAAGTGTAGTTGGTAT" +
                        "CAATTTTACTGACCTTTAAAAATATCTTAATGGGACAAAGTTCAAATATTTGATGACCAG" +
                        "CTATCGTGACCTTTATCTCTGTGGCTCTGTGGGCCTGTAGTTTTTACGTGCTTTTAGTGT");
    }

    public static SplicingTranscript getTranscriptWithThreeExons(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                // 1st exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200))
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400))
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600))
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800))
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 200))
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithSingleExon(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                // single exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000))
                        .build())
                .build();
    }

    public static SplicingTranscript getTranscriptWithThreeExonsOnRevStrand(ReferenceDictionary referenceDictionary) {
        return SplicingTranscript.builder()
                .setAccessionId("FIRST")
                .setCoordinates(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 2000).withStrand(Strand.REV))

                // 1st exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1800, 2000).withStrand(Strand.REV))
                        .build())
                // 1st intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1600, 1800).withStrand(Strand.REV))
                        .setDonorScore(4.444)
                        .setAcceptorScore(3.333)
                        .build())
                // 2nd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1400, 1600).withStrand(Strand.REV))
                        .build())
                // 2nd intron
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1200, 1400).withStrand(Strand.REV))
                        .setDonorScore(5.555)
                        .setAcceptorScore(6.666)
                        .build())
                // 3rd exon
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(referenceDictionary, Strand.FWD, 1, 1000, 1200).withStrand(Strand.REV))
                        .build())
                .build();
    }

    public static SplicingParameters makeFakeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(2).setDonorIntronic(3)
                .setAcceptorExonic(4).setAcceptorIntronic(5)
                .build();
    }

    /**
     * @return fake donor site represented by 4x5 matrix
     */
    public static DoubleMatrix makeFakeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                {.2, .3, .4, .3, .2}, // A
                {.1, .2, .3, .1, .6}, // C
                {.3, .3, .1, .3, .1}, // G
                {.4, .2, .2, .3, .1}  // T
        });
    }

    /**
     * @return fake acceptor site represented by 4x9 matrix
     */
    public static DoubleMatrix makeFakeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {.2, .2, .4, .6, .1, .3, .4, .3, .2}, // A
                {.1, .2, .3, .2, .5, .2, .1, .6, .9}, // C
                {.3, .3, .1, .3, .1, .1, .2, .8, .2}, // G
                {.4, .2, .2, .3, .1, .8, .2, .1, .8}  // T
        });
    }

    /**
     * This matrix is the same matrix as the one in "spliceSites.yaml" YAML file.
     */
    public static DoubleMatrix makeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                // -3                  +1 (G) +2 (U)                +5     +6
                {0.332, 0.638, 0.097, 0.002, 0.001, 0.597, 0.683, 0.091, 0.179}, // A
                {0.359, 0.107, 0.028, 0.001, 0.012, 0.031, 0.079, 0.060, 0.152}, // C
                {0.186, 0.117, 0.806, 0.996, 0.001, 0.339, 0.122, 0.771, 0.191}, // G
                {0.123, 0.139, 0.069, 0.001, 0.986, 0.032, 0.116, 0.079, 0.478}  // T
        });
    }

    /**
     * This matrix is the same matrix as the one in "spliceSites.yaml" YAML file.
     */
    public static DoubleMatrix makeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {0.248, 0.242, 0.238, 0.228, 0.215, 0.201, 0.181, 0.164, 0.147, 0.136, 0.125, 0.116, 0.106, 0.097, 0.090, 0.091, 0.102, 0.111, 0.117, 0.092, 0.096, 0.240, 0.063, 0.997, 0.001, 0.261, 0.251}, // A
                {0.242, 0.247, 0.250, 0.250, 0.253, 0.255, 0.263, 0.268, 0.271, 0.274, 0.281, 0.278, 0.274, 0.278, 0.259, 0.281, 0.292, 0.319, 0.330, 0.339, 0.293, 0.272, 0.644, 0.001, 0.002, 0.147, 0.191}, // C
                {0.159, 0.155, 0.154, 0.151, 0.151, 0.148, 0.148, 0.142, 0.138, 0.136, 0.128, 0.122, 0.116, 0.107, 0.102, 0.108, 0.114, 0.104, 0.093, 0.066, 0.066, 0.207, 0.006, 0.001, 0.996, 0.478, 0.194}, // G
                {0.350, 0.355, 0.358, 0.371, 0.381, 0.397, 0.409, 0.426, 0.444, 0.454, 0.466, 0.483, 0.504, 0.518, 0.549, 0.519, 0.491, 0.465, 0.460, 0.504, 0.545, 0.281, 0.287, 0.001, 0.001, 0.114, 0.365}  // T
        });
    }

    public static SplicingParameters makeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(6)
                .setAcceptorExonic(2)
                .setAcceptorIntronic(25)
                .build();
    }

}
