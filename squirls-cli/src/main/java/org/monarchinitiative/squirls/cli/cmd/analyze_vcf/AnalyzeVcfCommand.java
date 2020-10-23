package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommandException;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.AnalysisResults;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SettingsData;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@CommandLine.Command(name = "analyze-vcf", aliases = {"Z"}, mixinStandardHelpOptions = true,
        description = "make HTML report for variants in VCF file")
public class AnalyzeVcfCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeVcfCommand.class);

    @CommandLine.Option(names = {"-d", "--jannovar-database"}, required = true,
            description = "path to Jannovar transcript database")
    public Path jannovarDb;

    @CommandLine.Option(names = {"-t", "--threshold"},
            defaultValue = "0.",
            description = "variants with Squirls score above the threshold are included into the report")
    public double threshold;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "where to write the HTML report")
    public String output;

    @CommandLine.Parameters(index = "0", arity = "0",
            paramLabel = "input.vcf",
            description = "path to input VCF file")
    public Path inputPath;


    private static Path resolveOutputPath(String output, Path inputPath) throws SquirlsCommandException {
        if (output == null) {
            final Path parent = inputPath.getParent();
            final Path fileName = inputPath.getFileName();
            if (fileName.toString().endsWith(".vcf.gz")) {
                return parent.resolve(fileName.toString().replace(".vcf.gz", ".html"));
            } else if (fileName.toString().endsWith(".vcf")) {
                return parent.resolve(fileName.toString().replace(".vcf", ".html"));
            } else {
                throw new SquirlsCommandException("File name must end with `.vcf` or `.vcf.gz`");
            }
        } else {
            return Paths.get(output);
        }
    }

    /**
     * Perform the splicing annotation.
     *
     * @return function for performing the annotation
     */
    private static UnaryOperator<SplicingVariantAlleleEvaluation> splicingAnnotation(VariantSplicingEvaluator evaluator) {
        return variant -> {
            final VariantAnnotations annotations = variant.getAnnotations();
            if (annotations == null || !annotations.hasAnnotation()) {
                // nothing to be done
                return variant;
            }

            /*
            Map of transcript models by their accession ID
             */
            final Map<String, TranscriptModel> txByAccession = annotations.getAnnotations().stream()
                    .map(Annotation::getTranscript)
                    .collect(Collectors.toMap(TranscriptModel::getAccession, Function.identity()));

            final Map<String, SplicingPredictionData> predictionData = evaluator.evaluate(variant.getBase().getContig(),
                    variant.getBase().getStart(),
                    variant.getBase().getReference().getBaseString(),
                    variant.getAltAllele().getBaseString(),
                    txByAccession.keySet());

            variant.putAllPredictionData(predictionData);
            return variant;
        };
    }

    /**
     * Generate SVG graphics for given variant.
     *
     * @param generator use the generator to make the graphics
     * @return variant with graphics
     */
    private static Function<SplicingVariantAlleleEvaluation, PresentableVariant> generatePresentableVariant(SplicingVariantGraphicsGenerator generator) {
        return variant -> PresentableVariant.of(variant.getRepresentation(),
                variant.getAnnotations().getHighestImpactAnnotation().getGeneSymbol(),
                variant.getPrimaryPrediction().getPrediction().getMaxPathogenicity(),
                generator.generateGraphics(variant));
    }

    /**
     * Variant context might contain multiple alternate alleles. Here we melt the variant context into multiple
     * {@link SplicingVariantAlleleEvaluation}s, one for each <em>ALT</em> allele.
     *
     * @return {@link Stream} of
     */
    private static Function<VariantContext, Stream<SplicingVariantAlleleEvaluation>> meltToAltAlleles() {
        return vc -> vc.getAlternateAlleles().stream()
                .map(allele -> new SplicingVariantAlleleEvaluation(vc, allele));
    }

    /**
     * Use Jannovar's {@link VariantAnnotator} to perform functional annotation for a given variant.
     *
     * @return function for performing the annotation, the function returns an empty optional if the annotation fails
     */
    private static Function<SplicingVariantAlleleEvaluation, Optional<SplicingVariantAlleleEvaluation>> functionalAnnotation(ReferenceDictionary rd,
                                                                                                                             VariantAnnotator annotator) {
        return vc -> {
            final String contig = vc.getBase().getContig();
            if (!rd.getContigNameToID().containsKey(contig)) {
                return Optional.empty();
            }
            try {
                final VariantAnnotations annotations = annotator.buildAnnotations(rd.getContigNameToID().get(contig),
                        vc.getBase().getStart(),
                        vc.getBase().getReference().getBaseString(),
                        vc.getAltAllele().getBaseString(), PositionType.ONE_BASED);
                vc.setAnnotations(annotations);
                return Optional.of(vc);
            } catch (AnnotationException e) {
                return Optional.empty();
            }
        };
    }

    @Override
    public Integer call() throws Exception {
        try (final ConfigurableApplicationContext context = getContext()) {
            LOGGER.info("Reading variants from `{}`", inputPath);
            Path output = resolveOutputPath(this.output, inputPath);
            LOGGER.info("Writing report to `{}`", output);
            LOGGER.info("Reporting variants with predicted pathogenicity above `{}`", threshold);

            // no need to make a log announcement for Jannovar, jannovar announces instead

            final VariantSplicingEvaluator evaluator = context.getBean(VariantSplicingEvaluator.class);
            final SplicingVariantGraphicsGenerator graphicsGenerator = context.getBean(SplicingVariantGraphicsGenerator.class);

            final JannovarData jannovarData;
            try {
                jannovarData = new JannovarDataSerializer(jannovarDb.toAbsolutePath().toString()).load();
            } catch (SerializationException e) {
                LOGGER.error("Error deserializing Jannovar database at `{}`:", jannovarDb.toAbsolutePath());
                throw new SquirlsCommandException(e);
            }

            final VariantAnnotator annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());

            final AnalyzeVcfProgressReporter progressReporter = new AnalyzeVcfProgressReporter(5_000);
            final List<String> sampleNames;
            final Collection<PresentableVariant> variants = Collections.synchronizedList(new LinkedList<>());

            try (final VCFFileReader reader = new VCFFileReader(inputPath, false);
                 final Stream<VariantContext> stream = StreamSupport.stream(reader.spliterator(), true)) { // TODO - make true
                sampleNames = new ArrayList<>(reader.getFileHeader().getSampleNamesInOrder());
                stream.peek(progressReporter::logItem)
                        .flatMap(meltToAltAlleles())
                        .peek(progressReporter::logAltAllele)

                        // functional annotation with Jannovar
                        .map(functionalAnnotation(jannovarData.getRefDict(), annotator))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .peek(progressReporter::logAnnotatedAllele)

                        // splicing prediction and removing of non-deleterious alleles
                        .map(splicingAnnotation(evaluator))
                        .filter(variant -> !variant.getMaxScore().isNaN() && variant.getMaxScore() > threshold)
                        .peek(progressReporter::logEligibleAllele)

                        // graphics generation for predicted deleterious variants
                        .map(generatePresentableVariant(graphicsGenerator))

                        .onClose(progressReporter.summarize())
                        .forEach(variants::add);
            }

            final AnalysisResults results = AnalysisResults.builder()
                    .addAllSampleNames(sampleNames)
                    .variants(variants)
                    .analysisStats(progressReporter.getAnalysisStats())
                    .settingsData(SettingsData.builder()
                            .threshold(threshold)
                            .inputPath(inputPath.toString())
                            .transcriptDb(jannovarDb.toString())
                            .build())
                    .build();
            LOGGER.info("Writing the report to {}", output);
            final HtmlResultWriter writer = new HtmlResultWriter();
            try (final OutputStream os = Files.newOutputStream(output)) {
                writer.writeResults(os, results);
            } catch (IOException e) {
                throw new SquirlsCommandException(e);
            }
        }
        return 0;
    }
}
