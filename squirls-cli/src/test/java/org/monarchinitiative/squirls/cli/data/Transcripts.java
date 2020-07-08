package org.monarchinitiative.squirls.cli.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.squirls.core.model.SplicingExon;
import org.monarchinitiative.squirls.core.model.SplicingIntron;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.Set;

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
    static Set<SplicingTranscript> surf2Transcripts(ReferenceDictionary rd) {
        return Set.of(surf2_NM_017503_4(rd), surf2_NM_001278928_1(rd));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *     <li>NM_000478.4</li>
     * </ul>
     */
    static Set<SplicingTranscript> alplTranscripts(ReferenceDictionary rd) {
        return Set.of(alpl_NM_000478_4(rd));
    }

    /**
     * Get the following transcripts of the <em>ALPL</em> gene:
     * <ul>
     *      <li>NM_000548.3</li> - the transcript only contains the first, the 11th and the last exon of the real transcript
     *  </ul>
     */
    static Set<SplicingTranscript> tsc2Transcripts(ReferenceDictionary rd) {
        return Set.of(reduced_tsc2_NM000548_3(rd));
    }

    /**
     * Get the following transcripts of the <em>COL4A5</em> gene:
     * <ul>
     *      <li>NM_000495.4</li> - the transcript only contains the first, the 29th and the last exon of the real transcript
     *  </ul>
     */
    static Set<SplicingTranscript> col4a5Transcripts(ReferenceDictionary rd) {
        return Set.of(reduced_col4a5_NM_000495_4(rd));
    }

    /**
     * Get the following transcripts of the <em>RYR1</em> gene:
     * <ul>
     *      <li>NM_000540.2</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static Set<SplicingTranscript> ryr1Transcripts(ReferenceDictionary rd) {
        return Set.of(reduced_ryr1_NM_000540_2(rd));
    }


    /**
     * Get the following transcripts of the <em>HBB</em> gene:
     * <ul>
     *      <li>NM_000518.4</li> - the transcript only contains the first, the 102nd, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static Set<SplicingTranscript> hbbTranscripts(ReferenceDictionary rd) {
        return Set.of(hbb_NM_000518_4(rd));
    }


    /**
     * Get the following transcripts of the <em>BRCA2</em> gene:
     * <ul>
     *      <li>NM_000059.3</li> - the transcript only contains the first, the 15th, and the last exon of the
     *      real transcript
     *  </ul>
     */
    static Set<SplicingTranscript> brca2Transcripts(ReferenceDictionary rd) {
        return Set.of(reduced_brca2_NM_000059_3(rd));
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_017503.4</em>.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript surf2_NM_017503_4(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_017503.4")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
                // 1
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
                        .setDonorScore(3.6156746223715936)
                        .setAcceptorScore(4.277366650982434)
                        .build())
                // 2
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
                        .setDonorScore(2.937332682375464)
                        .setAcceptorScore(10.499414519258275)
                        .build())
                // 3
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
                        .setDonorScore(9.136968204255682)
                        .setAcceptorScore(6.7796902152895875)
                        .build())
                // 4
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
                        .setDonorScore(6.3660441535158965)
                        .setAcceptorScore(8.610070990445257)
                        .build())
                // 5
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
                        .setDonorScore(10.25048397144629)
                        .setAcceptorScore(10.042811633569952)
                        .build())
                // 6
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
                        .build())
                .build();
    }

    /**
     * Get a real transcript corresponding to <em>SURF2</em> <em>NM_001278928.1</em>.
     * <p>
     * NOTE - according to Ensembl genome browser, the transcript
     * <a href="https://grch37.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000148291;r=9:136223428-136228045;t=ENST00000371964">ENST00000371964.4</a> corresponds to both RefSeq transcripts
     * <em>NM_001278928.1</em> and <em>NM_017503.4</em>.
     * </p>
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript surf2_NM_001278928_1(ReferenceDictionary rd) {
        // this is the same transcript as in the method surf2_NM_017503_4, except for the accession ID
        return SplicingTranscript.builder()
                .setAccessionId("NM_001278928.1")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136228034))
                // 1
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223425, 136223546))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223546, 136223789))
                        .setDonorScore(3.6156746223715936)
                        .setAcceptorScore(4.277366650982434)
                        .build())
                // 2
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223789, 136223944))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136223944, 136224586))
                        .setDonorScore(2.937332682375464)
                        .setAcceptorScore(10.499414519258275)
                        .build())
                // 3
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224586, 136224690))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136224690, 136226825))
                        .setDonorScore(9.136968204255682)
                        .setAcceptorScore(6.7796902152895875)
                        .build())
                // 4
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136226825, 136227005))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227005, 136227140))
                        .setDonorScore(6.3660441535158965)
                        .setAcceptorScore(8.610070990445257)
                        .build())
                // 5
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227140, 136227310))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227310, 136227931))
                        .setDonorScore(10.25048397144629)
                        .setAcceptorScore(10.042811633569952)
                        .build())
                // 6
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 9, 136227931, 136228034))
                        .build())
                .build();
    }

    /**
     * Get a real transcript corresponding to <em>ALPL</em> <em>NM_000478.4</em> transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript alpl_NM_000478_4(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000478.4")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 1, 21_835_916, 21_904_903, PositionType.ONE_BASED))
                // 1
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21835916, 21836010, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21836010, 21880470))
                        .build())
                // 2
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21880471, 21880635, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21880635, 21887118))
                        .build())
                // 3
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887119, 21887238, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887238, 21887589))
                        .build())
                // 4
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887590, 21887705, PositionType.ONE_BASED))
                        .build())

                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21887705, 21889602))
                        .build())
                // 5
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21889603, 21889777, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21889777, 21890533))
                        .build())
                // 6
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21890534, 21894740, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21894740, 21894596))
                        .build())
                // 7
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21894597, 21890709, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21890709, 21896797))
                        .build())
                // 8
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21896798, 21896867, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21896867, 21900157))
                        .build())
                // 9
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21900158, 21900292, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21900292, 21902225))
                        .build())
                // 10
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21902226, 21902417, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21902417, 21903014))
                        .build())
                // 11
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903015, 21903134, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903134, 21903875))
                        .build())
                // 12
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 1, 21903876, 21904903, PositionType.ONE_BASED))
                        .build())
                .build();
    }

    /**
     * Get a transcript that contains the first, the last and the 11th exon of the <em>TSC2</em> <em>NM_000548.3</em>
     * transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript reduced_tsc2_NM000548_3(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000548.3")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 16, 2_097_986, 2_139_492, PositionType.ONE_BASED))
                // first
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2097986, 2098066, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_098_066, 2_110_670))
                        .build())
                // 11
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_110_671, 2_110_814, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_110_814, 2_138_446))
                        .build())

                // last
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 16, 2_138_447, 2_139_492, PositionType.ONE_BASED))
                        .build())
                .build();
    }

    /**
     * Get a transcript that contains the first, the last and the 29th exon of the <em>COL4A5</em> <em>NM_000495.4</em>
     * transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript reduced_col4a5_NM_000495_4(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000495.4")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 23, 107_683_068, 107_940_775, PositionType.ONE_BASED))
                // first
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_683_068, 107_683_436, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_683_436, 107_849_971))
                        .build())
                // 29
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_849_972, 107_850_122, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_850_122, 107_939_526))
                        .build())

                // last
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 23, 107_939_527, 107_940_775, PositionType.ONE_BASED))
                        .build())
                .build();
    }

    /**
     * Get a transcript that contains the first, the 102nd, and the last (106th) exon of the <em>RYR1</em> <em>NM_000540.2</em>
     * transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript reduced_ryr1_NM_000540_2(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000540.2")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 19, 38_924_331, 39_078_204, PositionType.ONE_BASED))
                // first
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 38_924_331, 38_924_514, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder() // this intron is not real. It spans all exons between the 1st and the 102th exon
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 38_924_514, 39_075_582))
                        .build())
                // 102
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_075_583, 39_075_739, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder() // this intron is not real. It spans all exons between the 102nd and the last exon
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_075_739, 39_077_964))
                        .build())

                // last (106th)
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 19, 39_077_965, 39_078_204, PositionType.ONE_BASED))
                        .build())
                .build();
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>HBB</em> <em>NM_000518.4</em>
     * transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript hbb_NM_000518_4(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000518.4")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 11, 5_246_694, 5_248_301, PositionType.ONE_BASED).withStrand(Strand.REV))
                // first
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_248_160, 5248301, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_248_029, 5_248_159).withStrand(Strand.REV))
                        .build())
                // second
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_247_807, 5_248_029, PositionType.ONE_BASED).withStrand(Strand.REV))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_246_956, 5_247_806).withStrand(Strand.REV))
                        .build())

                // last (third)
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 11, 5_246_694, 5_246_956, PositionType.ONE_BASED).withStrand(Strand.REV))
                        .build())
                .build();
    }

    /**
     * Get a transcript that contains the first, the second, and the last (third) exon of the <em>BRCA2</em> <em>NM_000059.3</em>
     * transcript.
     *
     * @param rd {@link ReferenceDictionary} to use
     * @return transcript
     */
    private static SplicingTranscript reduced_brca2_NM_000059_3(ReferenceDictionary rd) {
        return SplicingTranscript.builder()
                .setAccessionId("NM_000059.3")
                .setCoordinates(new GenomeInterval(rd, Strand.FWD, 13, 32_889_617, 32_973_809, PositionType.ONE_BASED))
                // first
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_889_617, 32_889_804, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_889_804, 32_930_564))
                        .build())
                // 15
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_930_565, 32_930_746, PositionType.ONE_BASED))
                        .build())
                .addIntron(SplicingIntron.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_930_746, 32_972_298))
                        .build())

                // last
                .addExon(SplicingExon.builder()
                        .setInterval(new GenomeInterval(rd, Strand.FWD, 13, 32_972_299, 32_973_809, PositionType.ONE_BASED))
                        .build())
                .build();
    }
}
