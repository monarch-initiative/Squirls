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
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.SquirlsTxResult;
import org.monarchinitiative.squirls.core.classifier.PartialPrediction;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.*;
import java.util.stream.Collectors;

public class VariantsForTesting {

    public static final double FAKE_THRESHOLD = .45;

    private static final VmvtGenerator GENERATOR = new VmvtGenerator();

    private VariantsForTesting() {
        // no-op
    }

    private static TestVariant makeEvaluation(ReferenceDictionary rd,
                                              String chrom,
                                              int pos,
                                              String variantId,
                                              String ref,
                                              String alt,
                                              VariantAnnotator annotator,
                                              Set<String> seqIds,
                                              Collection<SplicingTranscript> transcripts,
                                              SequenceInterval si,
                                              double pathogenicity,
                                              Metadata metadata,
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
        Map<String, Object> featureObjects = Arrays.stream(featurePayload.split("\n"))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(v -> v[0], v -> Double.parseDouble(v[1])));

        SplicingTranscript st = transcripts.stream().min(Comparator.comparing(SplicingTranscript::getAccessionId)).orElseThrow();
        GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(chrom), pos, PositionType.ONE_BASED);
        GenomeVariant variant = new GenomeVariant(position, ref, alt);

        /*
        Prepare test object
         */
        TestVariant evaluation = new TestVariant(vc, altAlleleOne, variant, st, si, featureObjects);


        Set<SquirlsTxResult> txResults = new HashSet<>();
        for (SplicingTranscript transcript : transcripts) {
            SquirlsTxResultSimple squirlsTxResult = new SquirlsTxResultSimple(transcript.getAccessionId(),
                    StandardPrediction.of(PartialPrediction.of("fake", pathogenicity, FAKE_THRESHOLD)),
                    featureMap);
            txResults.add(squirlsTxResult);
        }
        evaluation.setSquirlsResult(new SquirlsResultSimple(txResults));

        /*
         * Make Jannovar annotations.
         */
        VariantAnnotations fullAnnotations = annotator.buildAnnotations(variant);
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
     * Put together the figures into a single titled <code>div</code> element.
     *
     * @param title   title for the figures
     * @param figures SVG strings, <code>null</code>s are ignored
     * @return string with titled div containing all figures
     */
    private static String assembleFigures(String title, String... figures) {
        final StringBuilder graphics = new StringBuilder();
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

    /**
     * Get data for variant <code>chr13:32930748T>G</code>. The variant is located 2bp downstream from the canonical
     * donor site of the exon 15 of the <em>BRCA2</em> gene and disrupts the site, leading to exon skipping.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant BRCA2DonorExon15plus2QUID(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr13";
        final int chr = 13;
        final int pos = 32_930_748;
        final String variantId = "BRCA2_donor_2bp_downstream_exon15_quid";
        final String ref = "T", alt = "G";
        final Set<String> seqIds = Set.of("NM_000059.3");
        final double pathogenicity = 0.95;

        final SequenceInterval si = Sequences.getBrca2Exon15Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.brca2Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000059.3", new GenomePosition(rd, Strand.FWD, chr, 32_930_747, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000059.3", new GenomePosition(rd, Strand.FWD, chr, 32_930_565, PositionType.ONE_BASED))
                .build();
        final String featurePayload = "acceptor_offset=184.0\n" +
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
        final String ruler = GENERATOR.getDonorSequenceRuler(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");
        final String primary = GENERATOR.getDonorTrekkerSvg(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");

        final String secondary = GENERATOR.getDonorDistributionSvg(
                "CAGg" + "t" + "atgt",
                "CAGg" + "g" + "atgt");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Canonical donor");
    }

    /**
     * Get data for variant <code>chr1:21,894,739A>G</code>. The variant is located at -2 position of the canonical
     * donor site of the exon 7.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant ALPLDonorExon7Minus2(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr1";
        final int chr = 1;
        final int pos = 21_894_739;
        final String variantId = "ALPL_donor_exon7_minus2";
        final String ref = "A", alt = "G";
        final Set<String> seqIds = Set.of("NM_000478.4");
        final double pathogenicity = 0.94;

        final SequenceInterval si = Sequences.getAlplExon7Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.alplTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000478.4", new GenomePosition(rd, Strand.FWD, chr, 21_894_741, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000478.4", new GenomePosition(rd, Strand.FWD, chr, 21_894_597, PositionType.ONE_BASED))
                .build();
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
        final String ruler = GENERATOR.getDonorSequenceRuler("AAGgtagcc", "AGGgtagcc");
        final String primary = GENERATOR.getDonorTrekkerSvg("AAGgtagcc", "AGGgtagcc");
        final String secondary = GENERATOR.getDonorDistributionSvg("AAGgtagcc", "AGGgtagcc");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Canonical donor");
    }

    /**
     * Get data for variant <code>chr11:5,248,162G>A</code>. The variant is located 3bp upstream from the canonical
     * donor site of the exon 1 of the <em>HBB</em> gene and creates a new cryptic donor site.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant HBBcodingExon1UpstreamCrypticInCanonical(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr11";
        final int chr = 11;
        final int pos = 5_248_162;
        final String variantId = "HBB_donor_3bp_upstream_exon1";
        final String ref = "G", alt = "A";
        final Set<String> seqIds = Set.of("NM_000518.4");
        final double pathogenicity = 0.93;

        final SequenceInterval si = Sequences.getHbbExon1Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.hbbTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_159, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_029, PositionType.ONE_BASED))
                .build();
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
//        final String ruler = GENERATOR.getDonorSequenceRuler();
        final String ruler = ""; // TODO - add ruler here?
        final String primary = GENERATOR.getDonorTrekkerSvg(
                "TGGg" + "c" + "aggt",  // ref best window snippet
                "TGGg" + "t" + "aggt"); // alt best window snippet

        final String secondary = GENERATOR.getDonorCanonicalCryptic(
                "T" + "AGgttggt",     // alt canonical site snippet
                "TGGg" + "t" + "aggt"); // alt best window snippet

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Cryptic donor");
    }

    /**
     * Get data for variant <code>chr11:5,248,173C>T</code>. The variant is located 14bp upstream from the canonical
     * donor site of the exon 1 of the <em>HBB</em> gene and creates a new cryptic donor site.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant HBBcodingExon1UpstreamCryptic(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr11";
        final int chr = 11;
        final int pos = 5_248_173;
        final String variantId = "HBB_donor_14bp_upstream_exon1";
        final String ref = "C", alt = "T";
        final Set<String> seqIds = Set.of("NM_000518.4");
        final double pathogenicity = 0.92;

        final SequenceInterval si = Sequences.getHbbExon1Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.hbbTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_159, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_029, PositionType.ONE_BASED))
                .build();
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
//        final String ruler = GENERATOR.getDonorSequenceRuler();
        final String ruler = ""; // TODO - add ruler here?
        final String primary = GENERATOR.getDonorTrekkerSvg(
                "GTGgt" + "g" + "agg",  // ref best window snippet
                "GTGgt" + "a" + "agg");  // alt best window snippet

        final String secondary = GENERATOR.getDonorCanonicalCryptic(
                "CAGgttggt",  // alt canonical site snippet
                "GTGgt" + "a" + "agg");  // alt best window snippet

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Cryptic donor");
    }

    /**
     * Get data for variant <code>chr12:6132066T>C</code>. The variant is located 2bp upstream from the canonical
     * acceptor site of the exon 26 of the <em>VWF</em> gene and disrupts the site, leading to exon skipping.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant VWFAcceptorExon26minus2QUID(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr12";
        final int chr = 12;
        final int pos = 6_132_066;
        final String variantId = "VWF_acceptor_2bp_upstream_exon26_quid";
        final String ref = "T", alt = "C";
        final Set<String> seqIds = Set.of("NM_000552.3");
        final double pathogenicity = 0.91;

        final SequenceInterval si = Sequences.getVwfExon26Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.vwfTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000552.3", new GenomePosition(rd, Strand.FWD, chr, 6_131_905, PositionType.ONE_BASED).withStrand(Strand.REV))
                .putAcceptorCoordinate("NM_000552.3", new GenomePosition(rd, Strand.FWD, chr, 6_132_064, PositionType.ONE_BASED).withStrand(Strand.REV))
                .build();
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
        final String logo = GENERATOR.getAcceptorSequenceRuler(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");
        final String primary = GENERATOR.getAcceptorTrekkerSvg(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");

        final String secondary = GENERATOR.getAcceptorDistributionSvg(
                "acagccttgtctcctgtctacac" + "a" + "gCC",
                "acagccttgtctcctgtctacac" + "g" + "gCC");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, logo, primary, secondary, "Canonical acceptor");
    }

    /**
     * Get data for variant <code>chr1:2,110,668C>G</code>. The variant is located at -3 position of the canonical
     * acceptor site of the exon 11 of the <em>TSC2</em> gene.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant TSC2AcceptorExon11Minus3(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr16";
        final int chr = 16;
        final int pos = 2_110_668;
        final String variantId = "TSC2_acceptor-3_exon11";
        final String ref = "C", alt = "G";
        final Set<String> seqIds = Set.of("NM_000548.3");
        final double pathogenicity = 0.90;

        final SequenceInterval si = Sequences.getTsc2Exon11Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.tsc2Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000548.3", new GenomePosition(rd, Strand.FWD, chr, 2_110_815, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000548.3", new GenomePosition(rd, Strand.FWD, chr, 2_110_671, PositionType.ONE_BASED))
                .build();
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
        final String logo = GENERATOR.getAcceptorSequenceRuler("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        final String primary = GENERATOR.getAcceptorTrekkerSvg("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        final String secondary = GENERATOR.getAcceptorDistributionSvg("tgtgctggccgggctcgtgttcCagGC", "tgtgctggccgggctcgtgttcGagGC");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, logo, primary, secondary, "Cryptic acceptor");
    }


    /**
     * Get data for variant <code>chrX:107,849,964T>A</code>. The variant is located at -8 position of the canonical
     * acceptor site of the exon 29 of the <em>COL4A5</em> gene.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant COL4A5AcceptorExon11Minus8(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chrX";
        final int chr = 23;
        final int pos = 107_849_964;
        final String variantId = "COL4A5_acceptor-8_exon29";
        final String ref = "T", alt = "A";
        final Set<String> seqIds = Set.of("NM_000495.4");
        final double pathogenicity = 0.89;

        final SequenceInterval si = Sequences.getCol4a5Exon29Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.col4a5Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000495.4", new GenomePosition(rd, Strand.FWD, chr, 107_850_122, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000495.4", new GenomePosition(rd, Strand.FWD, chr, 107_849_971, PositionType.ONE_BASED))
                .build();

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
//        final String ruler = GENERATOR.getAcceptorLogoSvg();
        final String ruler = ""; // TODO - add ruler here?
        final String primary = GENERATOR.getAcceptorTrekkerSvg(
                "tttgttgtgttttgtcatgtgta" + "t" + "gct", // ref best window
                "tttgttgtgttttgtcatgtgta" + "a" + "gct"); // alt best window

        final String secondary = GENERATOR.getAcceptorCanonicalCryptic(
                "gtgttttgtcatgtgta" + "t" + "gctcaagGG", // alt canonical site snippet
                "tttgttgtgttttgtcatgtgta" + "a" + "gct"); // alt best window snippet

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Cryptic acceptor");
    }

    /**
     * Get data for variant <code>chr19:39,075,603C>G</code>. The variant is located at 21bp downstream from the canonical
     * acceptor site of the exon 102 of the <em>RYR1</em> gene and creates a new cryptic acceptor site.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant RYR1codingExon102crypticAcceptor(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr19";
        final int chr = 19;
        final int pos = 39_075_603;
        final String variantId = "RYR1_coding_21bp_downstream_exon102";
        final String ref = "C", alt = "G";
        final Set<String> seqIds = Set.of("NM_000540.2");
        final double pathogenicity = 0.88;

        final SequenceInterval si = Sequences.getRyr1Exon102Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.ryr1Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000540.2", new GenomePosition(rd, Strand.FWD, chr, 39_075_583, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000540.2", new GenomePosition(rd, Strand.FWD, chr, 39_075_740, PositionType.ONE_BASED))
                .build();
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
//        final String ruler = GENERATOR.getAcceptorLogoSvg();
        final String ruler = "";// TODO - add ruler here?

        final String primary = GENERATOR.getAcceptorTrekkerSvg(
                "tcagtgttacctgtttcacatgta" + "c" + "GT",  // ref best window
                "tcagtgttacctgtttcacatgta" + "g" + "GT");  // alt best window

        final String secondary = GENERATOR.getAcceptorCanonicalCryptic(
                "tgaccagtgtgctcccctccctcagTG",  // alt canonical site snippet
                "tcagtgttacctgtttcacatgta" + "g" + "GT");  // alt best window snippet

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Cryptic acceptor");
    }


    /**
     * Get data for variant <code>chr17:29527461C>T</code>. The variant is located at 22bp downstream from the canonical
     * acceptor site of the exon 9 of the <em>NF1</em> gene and leads to exon skipping due to SRE.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static TestVariant NF1codingExon9coding_SRE(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

        // *********************************** PARAMETRIZE *************************************************************

        /*
        Prepare data
         */
        final String chrom = "chr17";
        final int chr = 17;
        final int pos = 29_527_461;
        final String variantId = "NF1_coding_22bp_downstream_from_acceptor_exon9";
        final String ref = "C", alt = "T";
        final Set<String> seqIds = Set.of("NM_000267.3");
        final double pathogenicity = 0.87;

        final SequenceInterval si = Sequences.getNf1Exon9Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.nf1Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000267.3", new GenomePosition(rd, Strand.FWD, chr, 29_527_614, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000267.3", new GenomePosition(rd, Strand.FWD, chr, 29_527_440, PositionType.ONE_BASED))
                .build();
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
        final String ruler = "";
        final int kmerWidth = 450, kmerHeight = 220;
        // hexamers
        final String hexamer = GENERATOR.getHexamerSvg(
                "GTCTA" + "C" + "GAAAA",
                "GTCTA" + "T" + "GAAAA");
        final String primary = String.format(
                "<svg width=\"%d\" height=\"%d\" viewBox=\"0 0 900 400\">" +
                        "%s" +
                        "</svg>", kmerWidth, kmerHeight, hexamer);
        // heptamers
        final String heptamerSvg = GENERATOR.getHeptamerSvg(
                "AGTCTA" + "C" + "GAAAAG",
                "AGTCTA" + "T" + "GAAAAG");
        final String secondary = String.format(
                "<svg width=\"%d\" height=\"%d\" viewBox=\"0 0 900 400\">" +
                        "%s" +
                        "</svg>", kmerWidth, kmerHeight, heptamerSvg);

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featurePayload, ruler, primary, secondary, "Splicing regulatory elements");
    }

}
