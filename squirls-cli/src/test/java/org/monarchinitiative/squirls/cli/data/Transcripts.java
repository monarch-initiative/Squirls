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

import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.variant.api.Contig;
import org.monarchinitiative.variant.api.CoordinateSystem;
import org.monarchinitiative.variant.api.GenomicRegion;
import org.monarchinitiative.variant.api.Strand;

import java.util.List;

// TODO - remove old transcript definitions
class Transcripts {

    private Transcripts() {
        // private no-op
    }


    /**
     * Get the following transcripts of the <em>SURF2</em> gene:
     * <ul>
     *     <li>NM_017503.4</li>
     *     <li>NM_001278928.1</li>
     * </ul>
     */
    static List<TranscriptModel> surf2Transcripts(Contig contig) {
        return List.of(surf2_NM_017503_4(contig), surf2_NM_001278928_1(contig));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *     <li>NM_000478.4</li>
     * </ul>
     */
    static List<TranscriptModel> alplTranscripts(Contig contig) {
        return List.of(alpl_NM_000478_4(contig));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *      <li>NM_000548.3</li> - the transcript only contains the first, the 11th and the last exon of the real transcript
     *  </ul>
     */
    static List<TranscriptModel> tsc2Transcripts(Contig contig) {
        return List.of(reduced_tsc2_NM000548_3(contig));
    }

    /**
     * Get the following transcripts of the <em>COL4A5</em> gene:
     * <ul>
     *      <li>NM_000495.4</li> - the transcript only contains the first, the 29th and the last exon of the real transcript
     *  </ul>
     */
    static List<TranscriptModel> col4a5Transcripts(Contig contig) {
        return List.of(reduced_col4a5_NM_000495_4(contig));
    }

    /**
     * Get the following transcripts of the <em>RYR1</em> gene:
     * <ul>
     *      <li>NM_000540.2</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<TranscriptModel> ryr1Transcripts(Contig contig) {
        return List.of(reduced_ryr1_NM_000540_2(contig));
    }


    /**
     * Get the following transcripts of the <em>HBB</em> gene:
     * <ul>
     *      <li>NM_000518.4</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<TranscriptModel> hbbTranscripts(Contig contig) {
        return List.of(hbb_NM_000518_4(contig));
    }


    /**
     * Get the following transcripts of the <em>BRCA2</em> gene:
     * <ul>
     *      <li>NM_000059.3</li> - the transcript only contains the first, the 15th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<TranscriptModel> brca2Transcripts(Contig contig) {
        return List.of(reduced_brca2_NM_000059_3(contig));
    }


    /**
     * Get the following transcripts of the <em>VWF</em> gene:
     * <ul>
     *      <li>NM_000552.3</li> - the transcript only contains the first, the 26th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<TranscriptModel> vwfTranscripts(Contig contig) {
        return List.of(reduced_vwf_NM_000552_3(contig));
    }

    /**
     * Get the following transcripts of the <em>NF1</em> gene:
     * <ul>
     *      <li>NM_000267.3</li> - the transcript only contains the first, the 9th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<TranscriptModel> nf1Transcripts(Contig contig) {
        return List.of(reduced_nf1_NM_000267_3(contig));
    }

    /**
     * Get a transcript that contains the first, the last and the 9th exon of the <em>NF1</em> <em>NM_000267.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_nf1_NM_000267_3(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                29_421_944, 29_704_695, 29_421_944, 29_704_695, "NM_000267.3", "NF1",
                List.of(GenomicRegion.zeroBased(contig, 29_421_944, 29_422_387),
                        GenomicRegion.zeroBased(contig, 29_527_439, 29_527_613),
                        GenomicRegion.zeroBased(contig, 29_701_030, 29_704_695)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000267.3")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 17, 29_421_945, 29_704_695, PositionType.ONE_BASED))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 17, 29_421_945, 29422387, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 17, 29422387, 29_527_439))
//                        .build())
//                // 9th
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 17, 29_527_440, 29_527_613, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 17, 29_527_613, 29_701_030))
//                        .build())
//
//                // last
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 17, 29_701_031, 29_704_695, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_017503.4</em>.
     *
     * @return transcript
     */
    private static TranscriptModel surf2_NM_017503_4(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                136_223_424, 136_228_034, 136_223_424, 136_228_034, "NM_017503.4", "SURF2",
                List.of(GenomicRegion.zeroBased(contig, 136_223_424, 136_223_546),
                        GenomicRegion.zeroBased(contig, 136_223_788, 136_223_944),
                        GenomicRegion.zeroBased(contig, 136_224_585, 136_224_690),
                        GenomicRegion.zeroBased(contig, 136_226_824, 136_227_005),
                        GenomicRegion.zeroBased(contig, 136_227_139, 136_227_310),
                        GenomicRegion.zeroBased(contig, 136_227_930, 136_228_034)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_017503.4")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
//                // 1
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
//                        .setDonorScore(3.6156746223715936)
//                        .setAcceptorScore(4.277366650982434)
//                        .build())
//                // 2
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
//                        .setDonorScore(2.937332682375464)
//                        .setAcceptorScore(10.499414519258275)
//                        .build())
//                // 3
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
//                        .setDonorScore(9.136968204255682)
//                        .setAcceptorScore(6.7796902152895875)
//                        .build())
//                // 4
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
//                        .setDonorScore(6.3660441535158965)
//                        .setAcceptorScore(8.610070990445257)
//                        .build())
//                // 5
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
//                        .setDonorScore(10.25048397144629)
//                        .setAcceptorScore(10.042811633569952)
//                        .build())
//                // 6
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_001278928.1</em>.
     * <p>
     * NOTE - according to Ensembl genome browser, the transcript
     * <a href="https://grch37.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000148291;r=9:136223428-136228045;t=ENST00000371964">ENST00000371964.4</a> corresponds to both RefSeq transcripts
     * <em>NM_001278928.1</em> and <em>NM_017503.4</em>.
     * </p>
     *
     * @return transcript
     */
    private static TranscriptModel surf2_NM_001278928_1(Contig contig) {
        // this is the same transcript as in the method surf2_NM_017503_4, except for the accession ID
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                136_223_424, 136_228_034, 136_223_424, 136_228_034, "NM_001278928.1", "SURF2",
                List.of(GenomicRegion.zeroBased(contig, 136_223_424, 136_223_546),
                        GenomicRegion.zeroBased(contig, 136_223_788, 136_223_944),
                        GenomicRegion.zeroBased(contig, 136_224_585, 136_224_690),
                        GenomicRegion.zeroBased(contig, 136_226_824, 136_227_005),
                        GenomicRegion.zeroBased(contig, 136_227_139, 136_227_310),
                        GenomicRegion.zeroBased(contig, 136_227_930, 136_228_034)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_001278928.1")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
//                // 1
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
//                        .setDonorScore(3.6156746223715936)
//                        .setAcceptorScore(4.277366650982434)
//                        .build())
//                // 2
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
//                        .setDonorScore(2.937332682375464)
//                        .setAcceptorScore(10.499414519258275)
//                        .build())
//                // 3
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
//                        .setDonorScore(9.136968204255682)
//                        .setAcceptorScore(6.7796902152895875)
//                        .build())
//                // 4
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
//                        .setDonorScore(6.3660441535158965)
//                        .setAcceptorScore(8.610070990445257)
//                        .build())
//                // 5
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
//                        .setDonorScore(10.25048397144629)
//                        .setAcceptorScore(10.042811633569952)
//                        .build())
//                // 6
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a real transcript corresponding to <em>ALPL</em> <em>NM_000478.4</em> transcript.
     *
     * @return transcript
     */
    private static TranscriptModel alpl_NM_000478_4(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                21_835_915, 21_904_903, 21_835_915, 21_904_903, "NM_000478.4", "ALPL",
                List.of(GenomicRegion.zeroBased(contig, 21_835_915, 21_836_010),
                        GenomicRegion.zeroBased(contig, 21_880_470, 21_880_635),
                        GenomicRegion.zeroBased(contig, 21_887_118, 21_887_238),
                        GenomicRegion.zeroBased(contig, 21_887_589, 21_887_705),
                        GenomicRegion.zeroBased(contig, 21_889_602, 21_889_777),
                        GenomicRegion.zeroBased(contig, 21_890_533, 21_890_709),
                        GenomicRegion.zeroBased(contig, 21_894_596, 21_894_740),
                        GenomicRegion.zeroBased(contig, 21_896_797, 21_896_867),
                        GenomicRegion.zeroBased(contig, 21_900_157, 21_900_292),
                        GenomicRegion.zeroBased(contig, 21_902_225, 21_902_417),
                        GenomicRegion.zeroBased(contig, 21_903_014, 21_903_134),
                        GenomicRegion.zeroBased(contig, 21_903_875, 21_904_903)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000478.4")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 1, 21_835_916, 21_904_903, PositionType.ONE_BASED))
//                // 1
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21835916, 21836010, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21836010, 21880470))
//                        .build())
//                // 2
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21880471, 21880635, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21880635, 21887118))
//                        .build())
//                // 3
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887119, 21887238, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887238, 21887589))
//                        .build())
//                // 4
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887590, 21887705, PositionType.ONE_BASED))
//                        .build())
//
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887705, 21889602))
//                        .build())
//                // 5
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_889_603, 21_889_777, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_889_777, 21_890_533))
//                        .build())
//                // 6
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_890_534, 21_890_709, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_890_709, 21_894_596))
//                        .build())
//                // 7
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_894_597, 21_894_740, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21_894_740, 21_896_797))
//                        .build())
//                // 8
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21896798, 21896867, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21896867, 21900157))
//                        .build())
//                // 9
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21900158, 21900292, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21900292, 21902225))
//                        .build())
//                // 10
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21902226, 21902417, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21902417, 21903014))
//                        .build())
//                // 11
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903015, 21903134, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903134, 21903875))
//                        .build())
//                // 12
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903876, 21904903, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a transcript that contains the first, the last and the 11th exon of the <em>TSC2</em> <em>NM_000548.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_tsc2_NM000548_3(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                2_097_985, 2_139_492, 2_097_985, 2_139_492, "NM_000548.3", "TSC2",
                List.of(GenomicRegion.zeroBased(contig, 2_097_985, 2_098_066),
                        GenomicRegion.zeroBased(contig, 2_110_670, 2_110_814),
                        GenomicRegion.zeroBased(contig, 2_138_446, 2_139_492)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000548.3")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 16, 2_097_986, 2_139_492, PositionType.ONE_BASED))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2097986, 2098066, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_098_066, 2_110_670))
//                        .build())
//                // 11
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_110_671, 2_110_814, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_110_814, 2_138_446))
//                        .build())
//
//                // last
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_138_447, 2_139_492, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a transcript that contains the first, the last and the 29th exon of the <em>COL4A5</em> <em>NM_000495.4</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_col4a5_NM_000495_4(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                107_683_067, 107_940_775, 107_683_067, 107_940_775, "NM_000495.4", "COL4A5",
                List.of(GenomicRegion.zeroBased(contig, 107_683_067, 107_683_436),
                        GenomicRegion.zeroBased(contig, 107_849_971, 107_850_122),
                        GenomicRegion.zeroBased(contig, 107_939_526, 107_940_775)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000495.4")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 23, 107_683_068, 107_940_775, PositionType.ONE_BASED))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_683_068, 107_683_436, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_683_436, 107_849_971))
//                        .build())
//                // 29
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_849_972, 107_850_122, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_850_122, 107_939_526))
//                        .build())
//
//                // last
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_939_527, 107_940_775, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a transcript that contains the first, the 102nd, and the last (106th) exon of the <em>RYR1</em> <em>NM_000540.2</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_ryr1_NM_000540_2(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                38_924_330, 39_078_204, 38_924_330, 39_078_204, "NM_000540.2", "RYR1",
                List.of(GenomicRegion.zeroBased(contig, 38_924_330, 38_924_514),
                        GenomicRegion.zeroBased(contig, 39_075_582, 39_075_739),
                        GenomicRegion.zeroBased(contig, 39_077_964, 39_078_204)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000540.2")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 19, 38_924_331, 39_078_204, PositionType.ONE_BASED))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 38_924_331, 38_924_514, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder() // this intron is not real. It spans all exons between the 1st and the 102th exon
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 38_924_514, 39_075_582))
//                        .build())
//                // 102
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_075_583, 39_075_739, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder() // this intron is not real. It spans all exons between the 102nd and the last exon
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_075_739, 39_077_964))
//                        .build())
//
//                // last (106th)
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_077_965, 39_078_204, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>HBB</em> <em>NM_000518.4</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel hbb_NM_000518_4(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                5_246_693, 5_248_301, 5_246_693, 5_248_301, "NM_000518.4", "HBB",
                List.of(GenomicRegion.zeroBased(contig, 5_246_693, 5_246_956),
                        GenomicRegion.zeroBased(contig, 5_247_806, 5_248_029),
                        GenomicRegion.zeroBased(contig, 5_248_159, 5_248_301))).withStrand(Strand.NEGATIVE);
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000518.4")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 11, 5_246_694, 5_248_301, PositionType.ONE_BASED).withStrand(Strand.REV))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_248_160, 5248301, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_248_029, 5_248_159).withStrand(Strand.REV).withStrand(Strand.REV))
//                        .build())
//                // second
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_247_807, 5_248_029, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_246_956, 5_247_806).withStrand(Strand.REV).withStrand(Strand.REV))
//                        .build())
//
//                // last (third)
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_246_694, 5_246_956, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .check(true)
//                .build();
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>BRCA2</em> <em>NM_000059.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_brca2_NM_000059_3(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                32_889_616, 32_973_809, 32_889_616, 32_973_809, "NM_000059.3", "BRCA2",
                List.of(GenomicRegion.zeroBased(contig, 32_889_616, 32_889_804),
                        GenomicRegion.zeroBased(contig, 32_930_564, 32_930_746),
                        GenomicRegion.zeroBased(contig, 32_972_298, 32_973_809)));
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000059.3")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 13, 32_889_617, 32_973_809, PositionType.ONE_BASED))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_889_617, 32_889_804, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_889_804, 32_930_564))
//                        .build())
//                // 15
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_930_565, 32_930_746, PositionType.ONE_BASED))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_930_746, 32_972_298))
//                        .build())
//
//                // last
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_972_299, 32_973_809, PositionType.ONE_BASED))
//                        .build())
//                .check(true)
//                .build();
    }


    /**
     * Get a transcript that contains the first, the 26th, and the last exon of the <em>VWF</em> <em>NM_000552.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static TranscriptModel reduced_vwf_NM_000552_3(Contig contig) {
        return TranscriptModel.coding(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED,
                6_058_039, 6_233_841, 6_058_039, 6_233_841, "NM_000552.3", "VWF",
                List.of(GenomicRegion.zeroBased(contig, 6_058_039, 6_058_369),
                        GenomicRegion.zeroBased(contig, 6_131_905, 6_132_064),
                        GenomicRegion.zeroBased(contig, 6_233_586, 6_233_841)))
                .withStrand(Strand.NEGATIVE);
//        return SplicingTranscript.builder()
//                .setAccessionId("NM_000552.3")
//                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 12, 6_058_040, 6_233_841, PositionType.ONE_BASED).withStrand(Strand.REV))
//                // first
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 12, 6_233_587, 6_233_841, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 12, 6_132_064, 6_233_586).withStrand(Strand.REV))
//                        .build())
//                // 26
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 12, 6_131_906, 6_132_064, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .addIntron(SplicingIntron.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 12, 6_058_369, 6_131_905).withStrand(Strand.REV))
//                        .build())
//
//                // last (52nd)
//                .addExon(SplicingExon.builder()
//                        .setInterval(new GenomeInterval(rd, Strand.FWD, 12, 6_058_040, 6_058_369, PositionType.ONE_BASED).withStrand(Strand.REV))
//                        .build())
//                .check(true)
//                .build();
    }
}
