package org.monarchinitiative.threes.cli.cmd.analyze;

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
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.threes.cli.cmd.Command;
import org.monarchinitiative.threes.cli.cmd.CommandException;
import org.monarchinitiative.threes.core.VariantSplicingEvaluator;
import org.monarchinitiative.threes.core.classifier.Prediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This command takes a VCF file, runs the splicing annotation and writes out the results as HTML file.
 */
@Component
public class AnalyzeVcfCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeVcfCommand.class);

    private final VariantSplicingEvaluator evaluator;

    public AnalyzeVcfCommand(VariantSplicingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Setup subparser for {@code analyze-vcf} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `analyze-vcf` command
        Subparser annotateVcfParser = subparsers.addParser("analyze-vcf")
                .setDefault("cmd", "analyze-vcf")
                .help("analyze variants in VCF file and make HTML report");
        annotateVcfParser.addArgument("-d", "--jannovar-database")
                .required(true)
                .help("path to Jannovar transcript database");
        annotateVcfParser.addArgument("-t", "--threshold")
                .type(Double.class)
                .setDefault(.2)
                .help("include variants with predicted pathogenicity above this value into the report");
        annotateVcfParser.addArgument("input")
                .help("path to input VCF file");
        annotateVcfParser.addArgument("-o", "--output")
                .type(String.class)
                .help("where to write the HTML report");
    }

    private static Path resolveOutputPath(String output, Path inputPath) throws CommandException {
        if (output == null) {
            final Path parent = inputPath.getParent();
            final Path fileName = inputPath.getFileName();
            if (fileName.toString().endsWith(".vcf.gz")) {
                return parent.resolve(fileName.toString().replace(".vcf.gz", ".html"));
            } else if (fileName.toString().endsWith(".vcf")) {
                return parent.resolve(fileName.toString().replace(".vcf", ".html"));
            } else {
                throw new CommandException("File name must end with `.vcf` or `.vcf.gz`");
            }
        } else {
            return Paths.get(output);
        }
    }

    @Override
    public void run(Namespace namespace) throws CommandException {
        final Path inputPath = Paths.get(namespace.getString("input"));
        LOGGER.debug("Reading variants from `{}`", inputPath);

        final Path output = resolveOutputPath(namespace.getString("output"), inputPath);
        LOGGER.debug("Writing report to `{}`", output);

        final double threshold = namespace.getDouble("threshold");
        LOGGER.debug("Reporting variants with predicted pathogenicity above `{}`", threshold);

        final Path jannovarDb = Paths.get(namespace.getString("jannovar_database"));
//        LOGGER.info("Using Jannovar database at `{}`", jannovarDb); // jannovar makes the announcement instead


        final JannovarData jannovarData;
        try {
            jannovarData = new JannovarDataSerializer(jannovarDb.toAbsolutePath().toString()).load();
        } catch (SerializationException e) {
            LOGGER.error("Error deserializing Jannovar database at `{}`:", jannovarDb.toAbsolutePath());
            throw new CommandException(e);
        }

        final VariantAnnotator annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());

        final ProgressReporter progressReporter = new ProgressReporter();
        final List<String> sampleNames;
        final List<VariantDataBox> annotated = new ArrayList<>();
        try (final VCFFileReader reader = new VCFFileReader(inputPath, false);
             final CloseableIterator<VariantContext> variantIterator = reader.iterator()) {
            sampleNames = new ArrayList<>(reader.getFileHeader().getSampleNamesInOrder());
            try (final Stream<VariantContext> stream = variantIterator.stream()) {
                stream.peek(progressReporter::logVariant)
                        .flatMap(meltToAltAlleles())
                        .peek(progressReporter::logAltAllele)
                        .map(functionalAnnotation(jannovarData.getRefDict(), annotator))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .peek(progressReporter::logAnnotatedAllele)
                        .map(splicingAnnotation(evaluator))
                        .filter(variant -> !variant.getMaxScore().isNaN() && variant.getMaxScore() > threshold)
                        .peek(progressReporter::logEligibleAllele)
                        .onClose(progressReporter.summarize())
                        .forEach(annotated::add);
            }
        }

        final AnalysisResults results = AnalysisResults.builder()
                .addAllSampleNames(sampleNames)
                .variantData(annotated)
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
            throw new CommandException(e);
        }
    }

    private UnaryOperator<VariantDataBox> splicingAnnotation(VariantSplicingEvaluator evaluator) {
        return vp -> {
            final VariantAnnotations annotations = vp.getAnnotations();
            if (annotations == null || !annotations.hasAnnotation()) {
                // nothing to be done
                return vp;
            }

            /*
            Map of transcript models by their accession ID
             */
            final Map<String, TranscriptModel> txByAccession = annotations.getAnnotations().stream()
                    .map(Annotation::getTranscript)
                    .collect(Collectors.toMap(TranscriptModel::getAccession, Function.identity()));

            final Map<String, Prediction> evaluation = evaluator.evaluate(vp.getBase().getContig(),
                    vp.getBase().getStart(),
                    vp.getBase().getReference().getBaseString(),
                    vp.getAltAllele().getBaseString(),
                    txByAccession.keySet());

            final Map<TranscriptModel, Prediction> predictions = txByAccession.keySet().stream()
                    .collect(Collectors.toMap(txByAccession::get, txId -> evaluation.getOrDefault(txId, Prediction.emptyPrediction())));
            vp.putAllPredictions(predictions);
            return vp;
        };

    }

    /**
     * Variant context might contain multiple alternate alleles. Here we melt the variant context into multiple
     * {@link VariantDataBox}s, one for each <em>ALT</em> allele.
     *
     * @return {@link Stream} of
     */
    private Function<VariantContext, Stream<VariantDataBox>> meltToAltAlleles() {
        return vc -> vc.getAlternateAlleles().stream()
                .map(allele -> new VariantDataBox(vc, allele));
    }

    private Function<VariantDataBox, Optional<VariantDataBox>> functionalAnnotation(ReferenceDictionary rd,
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

    private static class ProgressReporter {

        private final Instant begin;
        private final AtomicInteger allVariantCount = new AtomicInteger();
        private final AtomicInteger altAlleleCount = new AtomicInteger();
        private final AtomicInteger annotatedAltAlleleCount = new AtomicInteger();
        private final AtomicInteger pathogenicAltAlleleCount = new AtomicInteger();

        private ProgressReporter() {
            begin = Instant.now();
            LOGGER.info("Starting the analysis");
        }

        public void logVariant(VariantContext context) {
            final int current = allVariantCount.incrementAndGet();
            if (current % 10_000 == 0) {
                LOGGER.info("Processed {} variants", current);
            }
        }

        public void logAltAllele(VariantDataBox variantDataBox) {
            altAlleleCount.incrementAndGet();
        }

        public void logAnnotatedAllele(VariantDataBox variantDataBox) {
            annotatedAltAlleleCount.incrementAndGet();
        }

        public void logEligibleAllele(VariantDataBox variantDataBox) {
            pathogenicAltAlleleCount.incrementAndGet();
        }

        public Runnable summarize() {
            return () -> {
                Duration duration = Duration.between(begin, Instant.now());
                long ms = duration.toMillis();
                LOGGER.info("Processed {} items in {}m {}s ({} ms)", allVariantCount.get(), (ms / 1000) / 60 % 60, ms / 1000 % 60, ms);
            };
        }

        public AnalysisStats getAnalysisStats() {
            return AnalysisStats.builder()
                    .allVariants(allVariantCount.get())
                    .alleleCount(altAlleleCount.get())
                    .annotatedAlleleCount(annotatedAltAlleleCount.get())
                    .pathogenicAlleleCount(pathogenicAltAlleleCount.get())
                    .build();
        }
    }
}
