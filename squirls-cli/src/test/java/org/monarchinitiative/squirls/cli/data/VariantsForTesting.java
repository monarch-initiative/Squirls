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
import org.monarchinitiative.squirls.cli.SimpleSplicingPredictionData;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.core.Metadata;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.classifier.StandardPrediction;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.monarchinitiative.vmvt.core.VmvtGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariantsForTesting {

    public static final double FAKE_THRESHOLD = .45;

    private static final VmvtGenerator GENERATOR = new VmvtGenerator();

    private VariantsForTesting() {
        // no-op
    }


    /**
     * Get data for variant <code>chr9:136,224,586G>A</code>. The variant is at -1 position of the canonical acceptor
     * site of the exon 3.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static SplicingVariantAlleleEvaluation SURF2Exon3AcceptorMinus1Evaluation(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("G", true);
        Allele alternateAllele = Allele.create("A", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr("chr9")
                .start(136_224_586)
                .stop(136_224_586)
                .id("rs993")
                .alleles(List.of(referenceAllele, alternateAllele))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, alternateAllele);

        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_586, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "A");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);
        evaluation.setAnnotations(ann);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = Transcripts.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, Sequences.getSurf2Exon3Sequence(rd)))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair("fake", 0.93, FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.builder()
                        .putDonorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, 9, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, 9, 136_224_587, PositionType.ONE_BASED))
                        .putDonorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, 9, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, 9, 136_224_587, PositionType.ONE_BASED))
                        .build()))
                .peek(data -> {
                    data.putFeature("donor_offset", -2.);
                    data.putFeature("canonical_donor", 2.44704789418146);
                    data.putFeature("cryptic_donor", 0.);
                    data.putFeature("acceptor_offset", 143.);
                    data.putFeature("canonical_acceptor", 0.);
                    data.putFeature("cryptic_acceptor", -12.4905210874462);
                    data.putFeature("phylop", 3.5);
                    data.putFeature("hexamer", -1.4957907);
                    data.putFeature("septamer", -0.8844);
                })
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));
        evaluation.putAllPredictionData(predictions);

        return evaluation;
    }


    /**
     * Get data for variant <code>chr9:136,224,694A>T</code>. The variant is located at +4 position of the canonical
     * donor site of the exon 3.
     *
     * @param rd        {@link ReferenceDictionary} to use
     * @param annotator {@link VariantAnnotator} to use to perform functional annotation with respect to genes & transcripts
     * @return evaluation object with all the data
     * @throws Exception bla
     */
    public static SplicingVariantAlleleEvaluation SURF2DonorExon3Plus4Evaluation(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        final String chrom = "chr9";
        final int chr = 9;
        final int pos = 136_224_694;

        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("A", true);
        Allele altAlleleOne = Allele.create("T", false);
        Allele altAlleleTwo = Allele.create("TC", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr(chrom) // on hg19
                .start(pos)
                .stop(pos)
                .id("rs993")
                .alleles(List.of(referenceAllele, altAlleleOne, altAlleleTwo))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, altAlleleOne);
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(chrom), pos, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "A", "T");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);
        evaluation.setAnnotations(ann);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = Transcripts.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, Sequences.getSurf2Exon3Sequence(rd)))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair("fake", 0.94, FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.builder()
                        .putDonorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, chr, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_017503.4", new GenomePosition(rd, Strand.FWD, chr, 136_224_587, PositionType.ONE_BASED))
                        .putDonorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, chr, 136_224_691, PositionType.ONE_BASED))
                        .putAcceptorCoordinate("NM_001278928.1", new GenomePosition(rd, Strand.FWD, chr, 136_224_587, PositionType.ONE_BASED))
                        .build()))
                .peek(data -> {
                    data.putFeature("donor_offset", -2.);
                    data.putFeature("canonical_donor", 2.44704789418146);
                    data.putFeature("cryptic_donor", 0.);
                    data.putFeature("acceptor_offset", 143.);
                    data.putFeature("canonical_acceptor", 0.);
                    data.putFeature("cryptic_acceptor", -12.4905210874462);
                    data.putFeature("phylop", 3.5);
                    data.putFeature("hexamer", -1.4957907);
                    data.putFeature("septamer", -0.8844);
                })
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));
        evaluation.putAllPredictionData(predictions);

        return evaluation;
    }


    private static SplicingVariantAlleleEvaluation makeEvaluation(ReferenceDictionary rd,
                                                                  String chrom,
                                                                  int pos,
                                                                  String variantId,
                                                                  String ref,
                                                                  String alt,
                                                                  VariantAnnotator annotator,
                                                                  Set<String> seqIds,
                                                                  Collection<SplicingTranscript> transcripts,
                                                                  SequenceRegion si,
                                                                  double pathogenicity,
                                                                  Metadata metadata,
                                                                  Map<String, Double> featureMap,
                                                                  String ruler,
                                                                  String primary,
                                                                  String secondary,
                                                                  String title) throws AnnotationException {
        /*
        Assemble data to POJOs
         */
        Allele referenceAllele = Allele.create(ref, true);
        Allele altAlleleOne = Allele.create(alt, false);
        final VariantContext vc = new VariantContextBuilder()
                .chr(chrom) // on hg19
                .start(pos)
                .stop(pos)
                .id(variantId)
                .alleles(List.of(referenceAllele, altAlleleOne))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, altAlleleOne);
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get(chrom), pos, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, ref, alt);

        /*
        Make annotations
         */
        final VariantAnnotations fullAnnotations = annotator.buildAnnotations(variant);
        final VariantAnnotations smallAnnotations = new VariantAnnotations(fullAnnotations.getGenomeVariant(),
                fullAnnotations.getAnnotations().stream()
                        .filter(ann -> seqIds.contains(ann.getTranscript().getAccession()))
                        .collect(Collectors.toList()));
        evaluation.setAnnotations(smallAnnotations);

        /*
        Prepare predictions
         */
        final Map<String, SplicingPredictionData> predictions = transcripts.stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, si))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair("fake", pathogenicity, FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(metadata))
                .peek(data -> featureMap.forEach(data::putFeature))
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));
        evaluation.putAllPredictionData(predictions);

        // add graphics
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
    public static SplicingVariantAlleleEvaluation BRCA2DonorExon15plus2QUID(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getBrca2Exon15Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.brca2Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000059.3", new GenomePosition(rd, Strand.FWD, chr, 32_930_747, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000059.3", new GenomePosition(rd, Strand.FWD, chr, 32_930_565, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", 2.,
                "canonical_donor", 9.94544383637791,
                "cryptic_donor", 1.3473990820467,
                "acceptor_offset", 184.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", -2.52195449384599,
                "phylop", 4.01000022888184,
                "hexamer", 1.8216685,
                "septamer", 2.1036);

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Canonical donor");
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
    public static SplicingVariantAlleleEvaluation ALPLDonorExon7Minus2(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getAlplExon7Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.alplTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000478.4", new GenomePosition(rd, Strand.FWD, chr, 21_894_741, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000478.4", new GenomePosition(rd, Strand.FWD, chr, 21_894_597, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -2.,
                "canonical_donor", 2.44704789418146,
                "cryptic_donor", 0.,
                "acceptor_offset", 143.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", -12.4905210874462,
                "phylop", 3.5,
                "hexamer", -1.4957907,
                "septamer", -0.8844
        );

        // generate graphics using Vmvt
        final String ruler = GENERATOR.getDonorSequenceRuler("AAGgtagcc", "AGGgtagcc");
        final String primary = GENERATOR.getDonorTrekkerSvg("AAGgtagcc", "AGGgtagcc");
        final String secondary = GENERATOR.getDonorDistributionSvg("AAGgtagcc", "AGGgtagcc");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Canonical donor");
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
    public static SplicingVariantAlleleEvaluation HBBcodingExon1UpstreamCrypticInCanonical(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getHbbExon1Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.hbbTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_159, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_029, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -3.,
                "canonical_donor", 1.54532552848381,
                "cryptic_donor", 1.77453922708334,
                "acceptor_offset", -133.,
                "canonical_acceptor", Double.NaN, // the first exon, thus N/A
                "cryptic_acceptor", Double.NaN, // the first exon, thus N/A
                "phylop", 3.1489999294281,
                "hexamer", -2.1175716,
                "septamer", -1.8121
        );

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Cryptic donor");
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
    public static SplicingVariantAlleleEvaluation HBBcodingExon1UpstreamCryptic(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getHbbExon1Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.hbbTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_159, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000518.4", new GenomePosition(rd, Strand.FWD, chr, 5_248_029, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -14.,
                "canonical_donor", 0.,
                "cryptic_donor", 2.23600080829063,
                "acceptor_offset", 129.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", 1.559654824986,
                "phylop", 1.30299997329712,
                "hexamer", -2.4162754,
                "septamer", -1.5387
        );

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Cryptic donor");
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
    public static SplicingVariantAlleleEvaluation VWFAcceptorExon26minus2QUID(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getVwfExon26Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.vwfTranscripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000552.3", new GenomePosition(rd, Strand.FWD, chr, 6_131_905, PositionType.ONE_BASED).withStrand(Strand.REV))
                .putAcceptorCoordinate("NM_000552.3", new GenomePosition(rd, Strand.FWD, chr, 6_132_064, PositionType.ONE_BASED).withStrand(Strand.REV))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", 161.,
                "canonical_donor", 0.,
                "cryptic_donor", -18.6949261480754,
                "acceptor_offset", -2.,
                "canonical_acceptor", 9.96144969439819,
                "cryptic_acceptor", 6.44688781842892,
                "phylop", 7.60500001907349,
                "hexamer", 1.7675138,
                "septamer", 0.6934);

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, logo, primary, secondary, "Canonical acceptor");
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
    public static SplicingVariantAlleleEvaluation TSC2AcceptorExon11Minus3(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getTsc2Exon11Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.tsc2Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000548.3", new GenomePosition(rd, Strand.FWD, chr, 2_110_815, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000548.3", new GenomePosition(rd, Strand.FWD, chr, 2_110_671, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -147.,
                "canonical_donor", 0.,
                "cryptic_donor", -10.1188705692249,
                "acceptor_offset", -3.,
                "canonical_acceptor", 6.74595437739346,
                "cryptic_acceptor", 0.,
                "phylop", 1.12600004673004,
                "hexamer", 2.10609255,
                "septamer", 1.6312
        );

        // generate graphics using Vmvt
        final String logo = GENERATOR.getAcceptorSequenceRuler("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        final String primary = GENERATOR.getAcceptorTrekkerSvg("tgtgctggccgggctcgtgttccagGC", "tgtgctggccgggctcgtgttcgagGC");
        final String secondary = GENERATOR.getAcceptorDistributionSvg("tgtgctggccgggctcgtgttcCagGC", "tgtgctggccgggctcgtgttcGagGC");

        // *************************************************************************************************************

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, logo, primary, secondary, "Cryptic acceptor");
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
    public static SplicingVariantAlleleEvaluation COL4A5AcceptorExon11Minus8(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getCol4a5Exon29Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.col4a5Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000495.4", new GenomePosition(rd, Strand.FWD, chr, 107_850_122, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000495.4", new GenomePosition(rd, Strand.FWD, chr, 107_849_971, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -159.,
                "canonical_donor", 0.,
                "cryptic_donor", -5.19742493336859,
                "acceptor_offset", -8.,
                "canonical_acceptor", 2.06667103964529,
                "cryptic_acceptor", 2.35570268707377,
                "phylop", 3.81599998474121,
                "hexamer", -0.4040078,
                "septamer", -0.9911);

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Cryptic acceptor");
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
    public static SplicingVariantAlleleEvaluation RYR1codingExon102crypticAcceptor(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getRyr1Exon102Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.ryr1Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000540.2", new GenomePosition(rd, Strand.FWD, chr, 39_075_583, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000540.2", new GenomePosition(rd, Strand.FWD, chr, 39_075_740, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -137.,
                "canonical_donor", 0.,
                "cryptic_donor", 1.68046369019604,
                "acceptor_offset", 21.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", 1.92197740139571,
                "phylop", -1.72699999809265,
                "hexamer", -4.2242608,
                "septamer", -3.4074
        );

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Cryptic acceptor");
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
    public static SplicingVariantAlleleEvaluation NF1codingExon9coding_SRE(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {

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

        final SequenceRegion si = Sequences.getNf1Exon9Sequence(rd);
        final Collection<SplicingTranscript> transcripts = Transcripts.nf1Transcripts(rd);
        final Metadata metadata = Metadata.builder()
                .putDonorCoordinate("NM_000267.3", new GenomePosition(rd, Strand.FWD, chr, 29_527_614, PositionType.ONE_BASED))
                .putAcceptorCoordinate("NM_000267.3", new GenomePosition(rd, Strand.FWD, chr, 29_527_440, PositionType.ONE_BASED))
                .build();
        final Map<String, Double> featureMap = Map.of(
                "donor_offset", -153.,
                "canonical_donor", 0.,
                "cryptic_donor", -10.4378653224383,
                "acceptor_offset", 22.,
                "canonical_acceptor", 0.,
                "cryptic_acceptor", -8.01047083607081,
                "phylop", 1.65199995040894,
                "hexamer", -1.2524385,
                "septamer", -1.619
        );

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

        return makeEvaluation(rd, chrom, pos, variantId, ref, alt, annotator, seqIds, transcripts, si, pathogenicity, metadata, featureMap, ruler, primary, secondary, "Splicing regulatory elements");
    }

}
