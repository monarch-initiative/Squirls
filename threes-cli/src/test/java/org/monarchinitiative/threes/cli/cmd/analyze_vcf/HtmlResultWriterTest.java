package org.monarchinitiative.threes.cli.cmd.analyze_vcf;

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
import org.monarchinitiative.threes.cli.PojosForTesting;
import org.monarchinitiative.threes.cli.SimpleSplicingPredictionData;
import org.monarchinitiative.threes.cli.TestDataSourceConfig;
import org.monarchinitiative.threes.core.Metadata;
import org.monarchinitiative.threes.core.SplicingPredictionData;
import org.monarchinitiative.threes.core.classifier.StandardPrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest(classes = TestDataSourceConfig.class)
class HtmlResultWriterTest {

    // TODO: 3. 6. 2020 remove
    private static final Path OUTPATH = Paths.get("/home/ielis/tmp/SQUIRLS.html");

    private static final Random RANDOM = new Random(43);

    private static final double FAKE_THRESHOLD = .45;

    /**
     * A real sequence from interval `>chr9:136224501-136224800` (1-based coordinates) on hg19.
     */
    private static final String SEQUENCE = "TGTCCAGGGATGAGTCCAAGACACAGCCACCAGTCTGAATCCTTGCTGTGAACTGTCCCT" +
            "ACAAATTTGGTCTCTCTGCTCTGTAGGCACCAGTTGTTCTGCAAACTCACCCTGCGGCAC" +
            "ATCAACAAGTGCCCAGAACACGTGCTGAGGCACACCCAGGGCCGGCGGTACCAGCGAGCT" +
            "CTGTGTAAATGTAAGTCCCAGTGGACCCCCATCAGTGCATCGCCATCTGAGTGCATGCCC" +
            "GCCTTGCCCCAGATGGAGCGTGCTTGAAGGCAGGTCGTCCTTCAGCGATCCGTGTTGATG";

    @Qualifier("referenceDictionary")
    @Autowired
    private ReferenceDictionary rd;

    @Autowired
    private JannovarData jannovarData;

    private VariantAnnotator annotator;

    private SequenceInterval sequence;

    private HtmlResultWriter writer;

    private static SplicingVariantAlleleEvaluation getFirst(ReferenceDictionary rd, VariantAnnotator annotator, SequenceInterval sequence) throws Exception {
        /*
        Prepare POJOs
         */
        Allele referenceAllele = Allele.create("A", true);
        Allele altAlleleOne = Allele.create("G", false);
        Allele altAlleleTwo = Allele.create("AG", false);
        final VariantContext vc = new VariantContextBuilder()
                .chr("chr9")
                .start(136_224_690)
                .stop(136_224_690)
                .id("rs993")
                .alleles(List.of(referenceAllele, altAlleleOne, altAlleleTwo))
                .make();

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, altAlleleOne);
        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_690, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "A", "G");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = PojosForTesting.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, sequence))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair(RANDOM.nextDouble(), FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.empty())) // TODO - add metadata
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));

        /*
        Add the maps to the container
         */
        evaluation.setAnnotations(ann);
        evaluation.putAllPredictionData(predictions);
        return evaluation;
    }

    private static SplicingVariantAlleleEvaluation getSecond(ReferenceDictionary rd, VariantAnnotator annotator, SequenceInterval sequence) throws Exception {
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

        final SplicingVariantAlleleEvaluation evaluation = new SplicingVariantAlleleEvaluation(vc, alternateAllele);

        final GenomePosition position = new GenomePosition(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136_224_586, PositionType.ONE_BASED);
        final GenomeVariant variant = new GenomeVariant(position, "G", "GA");

        /*
        Make annotations map
         */
        final VariantAnnotations ann = annotator.buildAnnotations(variant);

        /*
        Make predictions map
         */
        final Map<String, SplicingPredictionData> predictions = PojosForTesting.surf2Transcripts(rd).stream()
                .map(transcript -> new SimpleSplicingPredictionData(variant, transcript, sequence))
                .peek(data -> data.setPrediction(StandardPrediction.builder()
                        .addProbaThresholdPair(RANDOM.nextDouble(), FAKE_THRESHOLD)
                        .build()))
                .peek(data -> data.setMetadata(Metadata.empty())) // TODO - add metadata
                .collect(Collectors.toMap(k -> k.getTranscript().getAccessionId(), Function.identity()));

        /*
        Add the maps to the container
         */
        evaluation.setAnnotations(ann);
        evaluation.putAllPredictionData(predictions);

        return evaluation;
    }

    @BeforeEach
    void setUp() {
        sequence = SequenceInterval.builder()
                .sequence(SEQUENCE)
                .interval(new GenomeInterval(rd, Strand.FWD, rd.getContigNameToID().get("chr9"), 136224501, 136224800, PositionType.ONE_BASED))
                .build();
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        writer = new HtmlResultWriter();
    }

    @Test
    void writeResults() throws Exception {
        final SplicingVariantAlleleEvaluation first = getFirst(jannovarData.getRefDict(), annotator, sequence);
        final SplicingVariantAlleleEvaluation second = getSecond(jannovarData.getRefDict(), annotator, sequence);

        AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(List.of("FAKE SAMPLE"))
                .analysisStats(AnalysisStats.builder()
                        .allVariants(100)
                        .alleleCount(120)
                        .annotatedAlleleCount(115)
                        .pathogenicAlleleCount(2)
                        .build())
                .settingsData(SettingsData.builder()
                        .inputPath("path to VCF").threshold(FAKE_THRESHOLD)
                        .transcriptDb("ENSEMBLah")
                        .build())
                .variantData(List.of(first, second))
                .build();
        try (OutputStream os = Files.newOutputStream(OUTPATH)) {
            writer.writeResults(os, results);
        }

    }
}