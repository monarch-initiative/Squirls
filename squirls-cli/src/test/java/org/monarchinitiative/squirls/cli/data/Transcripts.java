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

import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.sgenes.model.TranscriptEvidence;
import org.monarchinitiative.sgenes.model.TranscriptIdentifier;
import org.monarchinitiative.sgenes.model.TranscriptMetadata;
import org.monarchinitiative.svart.*;

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
    static List<Transcript> surf2Transcripts(Contig contig) {
        return List.of(surf2_NM_017503_4(contig), surf2_NM_001278928_1(contig));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *     <li>NM_000478.4</li>
     * </ul>
     */
    static List<Transcript> alplTranscripts(Contig contig) {
        return List.of(alpl_NM_000478_4(contig));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *      <li>NM_000548.3</li> - the transcript only contains the first, the 11th and the last exon of the real transcript
     *  </ul>
     */
    static List<Transcript> tsc2Transcripts(Contig contig) {
        return List.of(reduced_tsc2_NM000548_3(contig));
    }

    /**
     * Get the following transcripts of the <em>COL4A5</em> gene:
     * <ul>
     *      <li>NM_000495.4</li> - the transcript only contains the first, the 29th and the last exon of the real transcript
     *  </ul>
     */
    static List<Transcript> col4a5Transcripts(Contig contig) {
        return List.of(reduced_col4a5_NM_000495_4(contig));
    }

    /**
     * Get the following transcripts of the <em>RYR1</em> gene:
     * <ul>
     *      <li>NM_000540.2</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<Transcript> ryr1Transcripts(Contig contig) {
        return List.of(reduced_ryr1_NM_000540_2(contig));
    }


    /**
     * Get the following transcripts of the <em>HBB</em> gene:
     * <ul>
     *      <li>NM_000518.4</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<Transcript> hbbTranscripts(Contig contig) {
        return List.of(hbb_NM_000518_4(contig));
    }


    /**
     * Get the following transcripts of the <em>BRCA2</em> gene:
     * <ul>
     *      <li>NM_000059.3</li> - the transcript only contains the first, the 15th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<Transcript> brca2Transcripts(Contig contig) {
        return List.of(reduced_brca2_NM_000059_3(contig));
    }


    /**
     * Get the following transcripts of the <em>VWF</em> gene:
     * <ul>
     *      <li>NM_000552.3</li> - the transcript only contains the first, the 26th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<Transcript> vwfTranscripts(Contig contig) {
        return List.of(reduced_vwf_NM_000552_3(contig));
    }

    /**
     * Get the following transcripts of the <em>NF1</em> gene:
     * <ul>
     *      <li>NM_000267.3</li> - the transcript only contains the first, the 9th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static List<Transcript> nf1Transcripts(Contig contig) {
        return List.of(reduced_nf1_NM_000267_3(contig));
    }

    /**
     * Get a transcript that contains the first, the last and the 9th exon of the <em>NF1</em> <em>NM_000267.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_nf1_NM_000267_3(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000267.3", "NF1", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 29_421_944, 29_704_695)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 29_421_944, 29_422_387),
                        Coordinates.of(CoordinateSystem.zeroBased(), 29_527_439, 29_527_613),
                        Coordinates.of(CoordinateSystem.zeroBased(), 29_701_030, 29_704_695)),
                Coordinates.of(CoordinateSystem.zeroBased(), 29_421_944, 29_704_695),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_017503.4</em>.
     *
     * @return transcript
     */
    private static Transcript surf2_NM_017503_4(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_017503.4", "SURF2", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_228_034)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_223_546),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_223_788, 136_223_944),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_224_585, 136_224_690),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_226_824, 136_227_005),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_227_139, 136_227_310),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_227_930, 136_228_034)),
                Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_228_034),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
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
    private static Transcript surf2_NM_001278928_1(Contig contig) {
        // this is the same transcript as in the method surf2_NM_017503_4, except for the accession ID
        return Transcript.of(
                TranscriptIdentifier.of("NM_001278928.1", "SURF2", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_228_034)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_223_546),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_223_788, 136_223_944),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_224_585, 136_224_690),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_226_824, 136_227_005),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_227_139, 136_227_310),
                        Coordinates.of(CoordinateSystem.zeroBased(), 136_227_930, 136_228_034)),
                Coordinates.of(CoordinateSystem.zeroBased(), 136_223_424, 136_228_034),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a real transcript corresponding to <em>ALPL</em> <em>NM_000478.4</em> transcript.
     *
     * @return transcript
     */
    private static Transcript alpl_NM_000478_4(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000478.4", "ALPL", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 21_835_915, 21_904_903)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 21_835_915, 21_836_010),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_880_470, 21_880_635),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_887_118, 21_887_238),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_887_589, 21_887_705),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_889_602, 21_889_777),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_890_533, 21_890_709),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_894_596, 21_894_740),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_896_797, 21_896_867),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_900_157, 21_900_292),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_902_225, 21_902_417),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_903_014, 21_903_134),
                        Coordinates.of(CoordinateSystem.zeroBased(), 21_903_875, 21_904_903)
                        ),
                Coordinates.of(CoordinateSystem.zeroBased(), 21_835_915, 21_904_903),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a transcript that contains the first, the last and the 11th exon of the <em>TSC2</em> <em>NM_000548.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_tsc2_NM000548_3(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000548.3", "TSC2", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 2_097_985, 2_139_492)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 2_097_985, 2_098_066),
                        Coordinates.of(CoordinateSystem.zeroBased(), 2_110_670, 2_110_814),
                        Coordinates.of(CoordinateSystem.zeroBased(), 2_138_446, 2_139_492)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 2_097_985, 2_139_492),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a transcript that contains the first, the last and the 29th exon of the <em>COL4A5</em> <em>NM_000495.4</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_col4a5_NM_000495_4(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000495.4", "COL4A5", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 107_683_067, 107_940_775)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 107_683_067, 107_683_436),
                        Coordinates.of(CoordinateSystem.zeroBased(), 107_849_971, 107_850_122),
                        Coordinates.of(CoordinateSystem.zeroBased(), 107_939_526, 107_940_775)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 107_683_067, 107_940_775),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
        }

    /**
     * Get a transcript that contains the first, the 102nd, and the last (106th) exon of the <em>RYR1</em> <em>NM_000540.2</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_ryr1_NM_000540_2(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000540.2", "RYR1", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 38_924_330, 39_078_204)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 38_924_330, 38_924_514),
                        Coordinates.of(CoordinateSystem.zeroBased(), 39_075_582, 39_075_739),
                        Coordinates.of(CoordinateSystem.zeroBased(), 39_077_964, 39_078_204)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 38_924_330, 39_078_204),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>HBB</em> <em>NM_000518.4</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript hbb_NM_000518_4(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000518.4", "HBB", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 5_246_693, 5_248_301)).toOppositeStrand(),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 5_248_159, 5_248_301).invert(contig),
                        Coordinates.of(CoordinateSystem.zeroBased(), 5_247_806, 5_248_029).invert(contig),
                        Coordinates.of(CoordinateSystem.zeroBased(), 5_246_693, 5_246_956).invert(contig)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 5_246_693, 5_248_301).invert(contig),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>BRCA2</em> <em>NM_000059.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_brca2_NM_000059_3(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000059.3", "BRCA2", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 32_889_616, 32_973_809)),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 32_889_616, 32_889_804),
                        Coordinates.of(CoordinateSystem.zeroBased(), 32_930_564, 32_930_746),
                        Coordinates.of(CoordinateSystem.zeroBased(), 32_972_298, 32_973_809)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 32_889_616, 32_973_809),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }


    /**
     * Get a transcript that contains the first, the 26th, and the last exon of the <em>VWF</em> <em>NM_000552.3</em>
     * transcript.
     *
     * @return transcript
     */
    private static Transcript reduced_vwf_NM_000552_3(Contig contig) {
        return Transcript.of(
                TranscriptIdentifier.of("NM_000552.3", "VWF", null),
                GenomicRegion.of(contig, Strand.POSITIVE, Coordinates.of(CoordinateSystem.zeroBased(), 6_058_039, 6_233_841)).toOppositeStrand(),
                List.of(Coordinates.of(CoordinateSystem.zeroBased(), 6_233_586, 6_233_841).invert(contig),
                        Coordinates.of(CoordinateSystem.zeroBased(), 6_131_905, 6_132_064).invert(contig),
                        Coordinates.of(CoordinateSystem.zeroBased(), 6_058_039, 6_058_369).invert(contig)
                ),
                Coordinates.of(CoordinateSystem.zeroBased(), 6_058_039, 6_233_841).invert(contig),
                TranscriptMetadata.of(TranscriptEvidence.KNOWN));
    }
}
