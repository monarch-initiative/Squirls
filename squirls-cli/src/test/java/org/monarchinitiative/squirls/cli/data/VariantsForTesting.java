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

import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.squirls.core.PartialPrediction;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.SquirlsTxResult;
import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.squirls.core.reference.TranscriptModel;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.vmvt.core.VmvtGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class VariantsForTesting {

    public static final double FAKE_THRESHOLD = .45;

    private final VmvtGenerator generator;
    private final JannovarData jannovarData;
    private final VariantAnnotator annotator;
    private final GenomicAssembly genomicAssembly;

    public VariantsForTesting(VmvtGenerator generator, JannovarData jannovarData, GenomicAssembly genomicAssembly) {
        this.generator = generator;
        // no-op
        this.jannovarData = jannovarData;
        this.annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        this.genomicAssembly = genomicAssembly;
    }

    /**
     * Put together the figures into a single titled <code>div</code> element.
     *
     * @param title   title for the figures
     * @param figures SVG strings, <code>null</code>s are ignored
     * @return string with titled div containing all figures
     */
    private static String assembleFigures(String title, String... figures) {
        StringBuilder graphics = new StringBuilder();
        // add title
        graphics.append("<div class=\"graphics-container\">")
                .append("<div class=\"graphics-title\">").append(title).append("</div>")
                .append("<div class=\"graphics-content\">");
        // add figures
        for (String figure : figures) {
            if (figure != null) {
                graphics.append("<div>").append(figure).append("</div>");
            }
        }

        // close tags
        return graphics.append("</div>") // graphics-content
                .append("</div>") // graphics-container
                .toString();
    }

    private TestVariant makeEvaluation(Contig contig,
                                       String chrom,
                                       int pos,
                                       String variantId,
                                       String ref,
                                       String alt,
                                       Set<String> seqIds,
                                       Collection<TranscriptModel> transcripts,
                                       StrandedSequence si,
                                       double pathogenicity,
                                       String featurePayload,
                                       String ruler,
                                       String primary,
                                       String secondary,
                                       String title) throws AnnotationException {
        /*
        Assemble data to POJOs
         */
        Allele referenceAllele = Allele.create(ref, true);
        Allele altAlleleOne = Allele.create(alt, false);
        VariantContext vc = new VariantContextBuilder()
                .chr(chrom) // on hg19
                .start(pos)
                .stop(pos)
                .id(variantId)
                .alleles(List.of(referenceAllele, altAlleleOne))
                .make();

        /*
        Prepare features
         */
        Map<String, Double> featureMap = Arrays.stream(featurePayload.split("\n"))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(v -> v[0], v -> Double.parseDouble(v[1])));

        TranscriptModel st = transcripts.stream().min(Comparator.comparing(TranscriptModel::accessionId)).orElseThrow();
        GenomicVariant variant = GenomicVariant.of(contig, "", org.monarchinitiative.svart.Strand.POSITIVE, CoordinateSystem.oneBased(), pos, ref, alt);

        /*
        Prepare test object
         */
        TestVariant evaluation = new TestVariant(vc, variant, st, si, featureMap);


        Set<SquirlsTxResult> txResults = new HashSet<>();
        for (TranscriptModel transcript : transcripts) {
            SquirlsTxResultSimple squirlsTxResult = new SquirlsTxResultSimple(transcript.accessionId(),
                    Prediction.of(PartialPrediction.of("fake", pathogenicity, FAKE_THRESHOLD)),
                    featureMap);
            txResults.add(squirlsTxResult);
        }
        evaluation.setSquirlsResult(new SquirlsResultSimple(txResults));

        /*
         * Make Jannovar annotations.
         */
        int contigId = jannovarData.getRefDict().getContigNameToID().get(variant.contigName());
        GenomePosition position = new GenomePosition(jannovarData.getRefDict(), Strand.FWD, contigId, variant.start());
        GenomeVariant genomeVariant = new GenomeVariant(position, variant.ref(), variant.alt());
        VariantAnnotations fullAnnotations = annotator.buildAnnotations(genomeVariant);
        VariantAnnotations smallAnnotations = new VariantAnnotations(fullAnnotations.getGenomeVariant(),
                fullAnnotations.getAnnotations().stream()
                        .filter(ann -> seqIds.contains(ann.getTranscript().getAccession()))
                        .collect(Collectors.toList()));
        evaluation.setAnnotations(smallAnnotations);


        // Add graphics
        evaluation.setGraphics(assembleFigures(title, ruler, primary, secondary));

        return evaluation;
    }

    /**
     * Get data for variant <code>chr13:32930748T>G</code>. The variant is located 2bp downstream from the canonical
     * donor site of the exon 15 of the <em>BRCA2</em> gene and disrupts the site, leading to exon skipping.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant BRCA2DonorExon15plus2QUID() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "13";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 32_930_748;
        String variantId = "BRCA2_donor_2bp_downstream_exon15_quid";
        String ref = "T", alt = "G";
        Set<String> seqIds = Set.of("NM_000059.3");
        double pathogenicity = 0.95;

        StrandedSequence si = Sequences.getBrca2Exon15Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.brca2Transcripts(contig);
        String featurePayload = "acceptor_offset=184.0\n" +
                "alt_ri_best_window_acceptor=6.24199227902568\n" +
                "alt_ri_best_window_donor=1.6462531025600458\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=9.945443836377912\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=-2.5219544938459935\n" +
                "cryptic_donor=1.3473990820467006\n" +
                "donor_offset=2.0\n" +
                "exon_length=182.0\n" +
                "hexamer=1.8216685\n" +
                "intron_length=41552.0\n" +
                "phylop=4.010000228881836\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=2.1036\n" +
                "wt_ri_acceptor=8.763946772871673\n" +
                "wt_ri_donor=10.244297856891256\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
        String ruler = generator.getDonorSequenceRuler(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");
        String primary = generator.getDonorTrekkerSvg(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");

        String secondary = generator.getDonorDistributionSvg(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Canonical donor");
    }

    /**
     * Get data for variant <code>chr1:21,894,739A>G</code>. The variant is located at -2 position of the canonical
     * donor site of the exon 7.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant ALPLDonorExon7Minus2() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "1";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 21_894_739;
        String variantId = "ALPL_donor_exon7_minus2";
        String ref = "A", alt = "G";
        Set<String> seqIds = Set.of("NM_000478.4");
        double pathogenicity = 0.94;

        StrandedSequence si = Sequences.getAlplExon7Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.alplTranscripts(contig);
        String featurePayload = "acceptor_offset=143.0\n" +
                "alt_ri_best_window_acceptor=-3.06184416990555\n" +
                "alt_ri_best_window_donor=2.4205699538253014\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=2.447047894181465\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=-12.4905210874462\n" +
                "cryptic_donor=0.0\n" +
                "donor_offset=-2.0\n" +
                "exon_length=144.0\n" +
                "hexamer=-1.4957907\n" +
                "intron_length=2057.0\n" +
                "phylop=3.5\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-0.8844000000000001\n" +
                "wt_ri_acceptor=9.42867691754065\n" +
                "wt_ri_donor=4.867617848006766\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
        String ruler = generator.getDonorSequenceRuler("AAGgtagcc", "AGGgtagcc");
        String primary = generator.getDonorTrekkerSvg("AAGgtagcc", "AGGgtagcc");
        String secondary = generator.getDonorDistributionSvg("AAGgtagcc", "AGGgtagcc");

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Canonical donor");
    }

    /**
     * Get data for variant <code>chr11:5,248,162G>A</code>. The variant is located 3bp upstream from the canonical
     * donor site of the exon 1 of the <em>HBB</em> gene and creates a new cryptic donor site.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant HBBcodingExon1UpstreamCrypticInCanonical() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "11";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 5_248_162;
        String variantId = "HBB_donor_3bp_upstream_exon1";
        String ref = "G", alt = "A";
        Set<String> seqIds = Set.of("NM_000518.4");
        double pathogenicity = 0.93;

        StrandedSequence si = Sequences.getHbbExon1Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.hbbTranscripts(contig);
        String featurePayload = "acceptor_offset=-133.0\n" +
                "alt_ri_best_window_acceptor=-1.781069482220321\n" +
                "alt_ri_best_window_donor=6.324680776661294\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=1.5453255284838114\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=0.0\n" +
                "cryptic_donor=1.7745392270833396\n" +
                "donor_offset=-3.0\n" +
                "exon_length=142.0\n" +
                "hexamer=-2.1175715999999998\n" +
                "intron_length=130.0\n" +
                "phylop=3.1489999294281006\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-1.8120999999999998\n" +
                "wt_ri_acceptor=0.0\n" +
                "wt_ri_donor=6.095467078061766\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
//        String ruler = GENERATOR.getDonorSequenceRuler();
        String ruler = ""; // TODO - add ruler here?
        String primary = generator.getDonorTrekkerSvg(
                "TGGg" + "c" + "aggt",  // ref best window snippet
                "TGGg" + "t" + "aggt"); // alt best window snippet

//        String secondary = generator.getDonorCanonicalCryptic(
//                "T" + "AGgttggt",     // alt canonical site snippet
//                "TGGg" + "t" + "aggt"); // alt best window snippet
        String secondary = "";

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Cryptic donor");
    }

    /**
     * Get data for variant <code>chr11:5,248,173C>T</code>. The variant is located 14bp upstream from the canonical
     * donor site of the exon 1 of the <em>HBB</em> gene and creates a new cryptic donor site.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant HBBcodingExon1UpstreamCryptic() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "11";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 5_248_173;
        String variantId = "HBB_donor_14bp_upstream_exon1";
        String ref = "C", alt = "T";
        Set<String> seqIds = Set.of("NM_000518.4");
        double pathogenicity = 0.92;

        StrandedSequence si = Sequences.getHbbExon1Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.hbbTranscripts(contig);
        String featurePayload = "acceptor_offset=-144.0\n" +
                "alt_ri_best_window_acceptor=-1.679406754820472\n" +
                "alt_ri_best_window_donor=8.331467886352396\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=0.0\n" +
                "cryptic_donor=2.2360008082906297\n" +
                "donor_offset=-14.0\n" +
                "exon_length=142.0\n" +
                "hexamer=-2.4162754\n" +
                "intron_length=130.0\n" +
                "phylop=1.3029999732971191\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=-29.198764241874063\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-1.5387\n" +
                "wt_ri_acceptor=0.0\n" +
                "wt_ri_donor=6.095467078061766\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
//        String ruler = GENERATOR.getDonorSequenceRuler();
        String ruler = ""; // TODO - add ruler here?
        String primary = generator.getDonorTrekkerSvg(
                "GTGgt" + "g" + "agg",  // ref best window snippet
                "GTGgt" + "a" + "agg");  // alt best window snippet

//        String secondary = generator.getDonorCanonicalCryptic(
//                "CAGgttggt",  // alt canonical site snippet
//                "GTGgt" + "a" + "agg");  // alt best window snippet
        String secondary = "";

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Cryptic donor");
    }

    /**
     * Get data for variant <code>chr12:6132066T>C</code>. The variant is located 2bp upstream from the canonical
     * acceptor site of the exon 26 of the <em>VWF</em> gene and disrupts the site, leading to exon skipping.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant VWFAcceptorExon26minus2QUID() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "12";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 6_132_066;
        String variantId = "VWF_acceptor_2bp_upstream_exon26_quid";
        String ref = "T", alt = "C";
        Set<String> seqIds = Set.of("NM_000552.3");
        double pathogenicity = 0.91;

        StrandedSequence si = Sequences.getVwfExon26Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.vwfTranscripts(contig);
        String featurePayload = "acceptor_offset=-2.0\n" +
                "alt_ri_best_window_acceptor=3.648124750022908\n" +
                "alt_ri_best_window_donor=-8.032751156095085\n" +
                "canonical_acceptor=9.961449694398194\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=6.446887818428917\n" +
                "cryptic_donor=-18.6949261480754\n" +
                "donor_offset=-161.0\n" +
                "exon_length=159.0\n" +
                "hexamer=1.7675138\n" +
                "intron_length=73536.0\n" +
                "phylop=7.605000019073486\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=0.6933999999999999\n" +
                "wt_ri_acceptor=7.162686625992186\n" +
                "wt_ri_donor=10.662174991980315\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
        String logo = generator.getAcceptorSequenceRuler(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");
        String primary = generator.getAcceptorTrekkerSvg(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");

        String secondary = generator.getAcceptorDistributionSvg(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, logo, primary, secondary, "Canonical acceptor");
    }

    /**
     * Get data for variant <code>chr1:2,110,668C>G</code>. The variant is located at -3 position of the canonical
     * acceptor site of the exon 11 of the <em>TSC2</em> gene.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant TSC2AcceptorExon11Minus3() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "16";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 2_110_668;
        String variantId = "TSC2_acceptor-3_exon11";
        String ref = "C", alt = "G";
        Set<String> seqIds = Set.of("NM_000548.3");
        double pathogenicity = 0.90;

        StrandedSequence si = Sequences.getTsc2Exon11Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.tsc2Transcripts(contig);
        String featurePayload = "acceptor_offset=-3.0\n" +
                "alt_ri_best_window_acceptor=-2.95580052736842\n" +
                "alt_ri_best_window_donor=-1.9417000079717732\n" +
                "canonical_acceptor=6.745954377393462\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=0.0\n" +
                "cryptic_donor=-10.118870569224883\n" +
                "donor_offset=-147.0\n" +
                "exon_length=144.0\n" +
                "hexamer=2.1060925499999996\n" +
                "intron_length=27632.0\n" +
                "phylop=1.1260000467300415\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=1.6312000000000002\n" +
                "wt_ri_acceptor=3.7901538500250416\n" +
                "wt_ri_donor=8.17717056125311\n" +
                "yag_at_acceptor_minus_three=1.0";

        // generate graphics using Vmvt
        String logo = generator.getAcceptorSequenceRuler("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        String primary = generator.getAcceptorTrekkerSvg("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        String secondary = generator.getAcceptorDistributionSvg("tgtgctggccgggctcgtgttcCagGC", "tgtgctggccgggctcgtgttcGagGC");

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, logo, primary, secondary, "Cryptic acceptor");
    }


    /**
     * Get data for variant <code>chrX:107,849,964T>A</code>. The variant is located at -8 position of the canonical
     * acceptor site of the exon 29 of the <em>COL4A5</em> gene.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant COL4A5AcceptorExon11Minus8() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "X";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 107_849_964;
        String variantId = "COL4A5_acceptor-8_exon29";
        String ref = "T", alt = "A";
        Set<String> seqIds = Set.of("NM_000495.4");
        double pathogenicity = 0.89;

        StrandedSequence si = Sequences.getCol4a5Exon29Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.col4a5Transcripts(contig);

        String featurePayload = "acceptor_offset=-8.0\n" +
                "alt_ri_best_window_acceptor=4.2276385348389045\n" +
                "alt_ri_best_window_donor=3.610634341101537\n" +
                "canonical_acceptor=2.066671039645288\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=1.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=2.3557026870737716\n" +
                "cryptic_donor=-5.197424933368591\n" +
                "donor_offset=-159.0\n" +
                "exon_length=151.0\n" +
                "hexamer=-0.40400780000000003\n" +
                "intron_length=89404.0\n" +
                "phylop=3.815999984741211\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-0.9911\n" +
                "wt_ri_acceptor=3.9386068874104208\n" +
                "wt_ri_donor=8.808059274470128\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
//        String ruler = GENERATOR.getAcceptorLogoSvg();
        String ruler = ""; // TODO - add ruler here?
        String primary = generator.getAcceptorTrekkerSvg(
                "tttgttgtgttttgtcatgtgta" + "t" + "gct", // ref best window
                "tttgttgtgttttgtcatgtgta" + "a" + "gct"); // alt best window

//        String secondary = generator.getAcceptorCanonicalCryptic(
//                "gtgttttgtcatgtgta" + "t" + "gctcaagGG", // alt canonical site snippet
//                "tttgttgtgttttgtcatgtgta" + "a" + "gct"); // alt best window snippet
        String secondary = "";

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Cryptic acceptor");
    }

    /**
     * Get data for variant <code>chr19:39,075,603C>G</code>. The variant is located at 21bp downstream from the canonical
     * acceptor site of the exon 102 of the <em>RYR1</em> gene and creates a new cryptic acceptor site.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant RYR1codingExon102crypticAcceptor() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "19";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 39_075_603;
        String variantId = "RYR1_coding_21bp_downstream_exon102";
        String ref = "C", alt = "G";
        Set<String> seqIds = Set.of("NM_000540.2");
        double pathogenicity = 0.88;

        StrandedSequence si = Sequences.getRyr1Exon102Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.ryr1Transcripts(contig);
        String featurePayload = "acceptor_offset=21.0\n" +
                "alt_ri_best_window_acceptor=8.15014683108099\n" +
                "alt_ri_best_window_donor=7.955283012714299\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=1.921977401395714\n" +
                "cryptic_donor=1.9804636901960437\n" +
                "donor_offset=-137.0\n" +
                "exon_length=157.0\n" +
                "hexamer=-4.2242608\n" +
                "intron_length=2225.0\n" +
                "phylop=-1.7269999980926514\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-3.4074\n" +
                "wt_ri_acceptor=6.228169429685276\n" +
                "wt_ri_donor=5.974819322518255\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
//        String ruler = GENERATOR.getAcceptorLogoSvg();
        String ruler = "";// TODO - add ruler here?

        String primary = generator.getAcceptorTrekkerSvg(
                "tcagtgttacctgtttcacatgta" + "c" + "GT",  // ref best window
                "tcagtgttacctgtttcacatgta" + "g" + "GT");  // alt best window

//        String secondary = generator.getAcceptorCanonicalCryptic(
//                "tgaccagtgtgctcccctccctcagTG",  // alt canonical site snippet
//                "tcagtgttacctgtttcacatgta" + "g" + "GT");  // alt best window snippet
        String secondary = "";

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Cryptic acceptor");
    }


    /**
     * Get data for variant <code>chr17:29527461C>T</code>. The variant is located at 22bp downstream from the canonical
     * acceptor site of the exon 9 of the <em>NF1</em> gene and leads to exon skipping due to SRE.
     *
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public TestVariant NF1codingExon9coding_SRE() throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        String chrom = "17";
        Contig contig = genomicAssembly.contigByName(chrom);
        int pos = 29_527_461;
        String variantId = "NF1_coding_22bp_downstream_from_acceptor_exon9";
        String ref = "C", alt = "T";
        Set<String> seqIds = Set.of("NM_000267.3");
        double pathogenicity = 0.87;

        StrandedSequence si = Sequences.getNf1Exon9Sequence(contig);
        Collection<TranscriptModel> transcripts = Transcripts.nf1Transcripts(contig);
        String featurePayload = "acceptor_offset=22.0\n" +
                "alt_ri_best_window_acceptor=-1.0318475740969986\n" +
                "alt_ri_best_window_donor=-2.8493547798837007\n" +
                "canonical_acceptor=0.0\n" +
                "canonical_donor=0.0\n" +
                "creates_ag_in_agez=0.0\n" +
                "creates_yag_in_agez=0.0\n" +
                "cryptic_acceptor=-8.01047083607081\n" +
                "cryptic_donor=-10.437865322438341\n" +
                "donor_offset=-153.0\n" +
                "exon_length=174.0\n" +
                "hexamer=-1.2524385\n" +
                "intron_length=173417.0\n" +
                "phylop=1.6519999504089355\n" +
                "ppt_is_truncated=0.0\n" +
                "s_strength_diff_acceptor=0.0\n" +
                "s_strength_diff_donor=0.0\n" +
                "septamer=-1.6190000000000002\n" +
                "wt_ri_acceptor=6.978623261973812\n" +
                "wt_ri_donor=7.588510542554641\n" +
                "yag_at_acceptor_minus_three=0.0";

        // generate graphics using Vmvt
        String ruler = "";
        int kmerWidth = 450, kmerHeight = 220;
        // hexamers
        String hexamer = generator.getHexamerSvg(
                "GTCTA" + "C" + "GAAAA",
                "GTCTA" + "T" + "GAAAA");
        String primary = String.format(
                "<svg width=\"%d\" height=\"%d\" viewBox=\"0 0 900 400\">" +
                        "%s" +
                        "</svg>", kmerWidth, kmerHeight, hexamer);
        // heptamers
        String heptamerSvg = generator.getHeptamerSvg(
                "AGTCTA" + "C" + "GAAAAG",
                "AGTCTA" + "T" + "GAAAAG");
        String secondary = String.format(
                "<svg width=\"%d\" height=\"%d\" viewBox=\"0 0 900 400\">" +
                        "%s" +
                        "</svg>", kmerWidth, kmerHeight, heptamerSvg);

        // *************************************************************************************************************

        return makeEvaluation(contig, chrom, pos, variantId, ref, alt, seqIds, transcripts, si, pathogenicity, featurePayload, ruler, primary, secondary, "Splicing regulatory elements");
    }

}
