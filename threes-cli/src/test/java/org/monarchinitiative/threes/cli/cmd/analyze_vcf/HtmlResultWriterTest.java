package org.monarchinitiative.threes.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.cli.TestDataSourceConfig;
import org.monarchinitiative.threes.core.Metadata;
import org.monarchinitiative.threes.core.StandardSplicingPredictionData;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@SpringBootTest(classes = TestDataSourceConfig.class)
class HtmlResultWriterTest {

    // TODO: 3. 6. 2020 remove
    private static final Path OUTPATH = Paths.get("/home/ielis/tmp/3S.html");

    private static final Random RANDOM = new Random(43);

    @Qualifier("referenceDictionary")
    @Autowired
    private ReferenceDictionary rd;

    @Autowired
    private JannovarData jannovarData;

    private VariantAnnotator annotator;

    private HtmlResultWriter writer;

    private static SplicingVariantAlleleEvaluation getFirst(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("A", true);
        Allele altAlleleOne = Allele.create("G", false);
        Allele altAlleleTwo = Allele.create("AG", false);
        //                altAlleleTwo, new GenomeVariant(position, "A", "AG"));
        final VariantContext vc = new VariantContextBuilder()
                .chr("chr9")
                .start(136_224_690)
                .stop(136_224_690)
                .id("rs993")
                .alleles(List.of(referenceAllele, altAlleleOne, altAlleleTwo))
                .make();

        final SplicingVariantAlleleEvaluation box = new SplicingVariantAlleleEvaluation(vc, altAlleleOne);
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_690, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "A", "G");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);

        /*
        Make predictions map
         */
        final Collection<TranscriptModel> annotations = ann.getAnnotations().stream()
                .map(Annotation::getTranscript)
                .collect(Collectors.toList());

        final StandardSplicingPredictionData predictionData = StandardSplicingPredictionData.newBuilder()
                .predictionMap(annotations.stream()
                        .collect(Collectors.toMap(TranscriptModel::getAccession,
                                tx -> StandardPrediction.builder()
                                        .addProbaThresholdPair(RANDOM.nextDouble(), .2)
                                        .build())))
                .metadata(Metadata.empty())
                .build();

        /*
        Add the maps to the container
         */
        box.setAnnotations(ann);
        box.setPredictionData(predictionData);
        return box;
    }

    private static SplicingVariantAlleleEvaluation getSecond(ReferenceDictionary rd, VariantAnnotator annotator) throws Exception {
        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("G", true);
        Allele alternateAllele = Allele.create("GA", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr("chr9")
                .start(136_224_586)
                .stop(136_224_586)
                .id("rs993")
                .alleles(List.of(referenceAllele, alternateAllele))
                .make();

        final SplicingVariantAlleleEvaluation container = new SplicingVariantAlleleEvaluation(vc, alternateAllele);

        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_586, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "GA");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);

        /*
        Make predictions map
         */
        final Collection<TranscriptModel> annotations = ann.getAnnotations().stream()
                .map(Annotation::getTranscript)
                .collect(Collectors.toList());

        final StandardSplicingPredictionData predictionData = StandardSplicingPredictionData.newBuilder()
                .predictionMap(annotations.stream()
                        .collect(Collectors.toMap(TranscriptModel::getAccession,
                                tx -> StandardPrediction.builder()
                                        .addProbaThresholdPair(RANDOM.nextDouble(), .2)
                                        .build())))
                .metadata(Metadata.empty())
                .build();

        /*
        Add the maps to the container
         */
        container.setAnnotations(ann);
        container.setPredictionData(predictionData);
        return container;
    }

    @BeforeEach
    void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        writer = new HtmlResultWriter();
    }

    @Test
    void writeResults() throws Exception {
        final SplicingVariantAlleleEvaluation first = getFirst(jannovarData.getRefDict(), annotator);
        final SplicingVariantAlleleEvaluation second = getSecond(jannovarData.getRefDict(), annotator);

        AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(List.of("FAKE SAMPLE"))
                .analysisStats(AnalysisStats.builder()
                        .allVariants(100)
                        .alleleCount(120)
                        .annotatedAlleleCount(115)
                        .pathogenicAlleleCount(2)
                        .build())
                .settingsData(SettingsData.builder()
                        .inputPath("path to VCF").threshold(.9)
                        .transcriptDb("ENSEMBLEEE")
                        .build())
                .variantData(List.of(first, second))
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }

    }
}