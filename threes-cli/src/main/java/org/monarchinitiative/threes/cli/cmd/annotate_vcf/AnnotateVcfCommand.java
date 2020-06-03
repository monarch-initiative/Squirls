package org.monarchinitiative.threes.cli.cmd.annotate_vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AnnotateVcfCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateVcfCommand.class);

    private static final VCFInfoHeaderLine FLAG_LINE = new VCFInfoHeaderLine(
            "3S",
            VCFHeaderLineCount.A,
            VCFHeaderLineType.Flag,
            "Variant is considered as pathogenic if the flag is present");

    private static final VCFInfoHeaderLine SCORE_LINE = new VCFInfoHeaderLine(
            "3S_SCORE",
            VCFHeaderLineCount.A,
            VCFHeaderLineType.String,
            "3S pathogenicity score");

    private final VariantSplicingEvaluator evaluator;

    public AnnotateVcfCommand(VariantSplicingEvaluator variantSplicingEvaluator) {
        this.evaluator = variantSplicingEvaluator;
    }

    /**
     * Setup subparser for {@code annotate-vcf} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `annotate-vcf` command
        Subparser annotateVcfParser = subparsers.addParser("annotate-vcf")
                .setDefault("cmd", "annotate-vcf")
                .help("annotate VCF file with splicing scores");
        annotateVcfParser.addArgument("input")
                .help("path to VCF file");
        annotateVcfParser.addArgument("output")
                .help("where to write the output VCF file");
        annotateVcfParser.addArgument("-t", "--threshold")
                .type(Double.class)
                .setDefault(.2)
                .help("add `3S` flag to variants with predicted pathogenicity above this value");
    }

    /**
     * Extend the <code>header</code> with INFO fields that are being added in this command.
     *
     * @param header to extend
     * @return the extended header
     */
    private static VCFHeader extendHeader(VCFHeader header) {
        // 3S - flag
        header.addMetaDataLine(FLAG_LINE);
        // 3S_SCORE - float
        header.addMetaDataLine(SCORE_LINE);
        return header;
    }

    /**
     * Annotate and return a single {@link VariantContext}.
     *
     * @param evaluator variant splicing evaluator to use
     * @param threshold flag variants with predicted pathogenicity value above this threshold with `3S` field
     * @return annotated {@link VariantContext}
     */
    private static UnaryOperator<VariantContext> annotateVariant(VariantSplicingEvaluator evaluator, double threshold) {
        return vc -> {
            boolean isPathogenic = false;
            String annotation = null;
            for (Allele allele : vc.getAlternateAlleles()) {
                final Map<String, Prediction> predictionMap = evaluator.evaluate(vc.getContig(), vc.getStart(), vc.getReference().getBaseString(), allele.getBaseString());
                if (predictionMap.isEmpty()) {
                    continue;
                }
                isPathogenic = predictionMap.values().stream()
                        .mapToDouble(Prediction::getMaxPathogenicity)
                        .anyMatch(pathogenicity -> pathogenicity > threshold);
                annotation = predictionMap.entrySet().stream()
                        .map(entry -> String.format("%s=%f", entry.getKey(), entry.getValue().getMaxPathogenicity()))
                        .collect(Collectors.joining("|", String.format("%s|", allele.getBaseString()), ""));
            }

            return new VariantContextBuilder(vc)
                    .attribute("3S", isPathogenic)
                    .attribute("3S_SCORE", annotation)
                    .make();
        };
    }

    @Override
    public void run(Namespace namespace) throws CommandException {
        /*
        - open VCF file
        - extend header
        - iterate
         */
        final Path inputPath = Paths.get(namespace.getString("input"));
        LOGGER.debug("Reading variants from `{}`", inputPath);
        final Path outputPath = Paths.get(namespace.getString("output"));
        LOGGER.debug("Writing annotated variants to `{}`", outputPath);
        final double threshold = namespace.getDouble("threshold");
        LOGGER.debug("Adding `3S` label to variants with predicted pathogenicity value above `{}`", threshold);

        // initialize progress logging
        // TODO: 29. 5. 2020 improve behavior & logging
        // e.g. report progress in % if variant index and thus count is available
        final ProgressReporter<VariantContext> progressReporter = new ProgressReporter<>();
        try (final VCFFileReader reader = new VCFFileReader(inputPath, false);
             final CloseableIterator<VariantContext> variantIterator = reader.iterator();
             final VariantContextWriter writer = new VariantContextWriterBuilder()
                     .setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER)
                     .unsetOption(Options.INDEX_ON_THE_FLY)
                     .setOutputPath(outputPath)
                     .setOutputFileType(VariantContextWriterBuilder.OutputType.VCF)
                     .build()) {
            final VCFHeader header = reader.getFileHeader();
            // extend header and write it out
            final VCFHeader extended = extendHeader(header);

            writer.writeHeader(extended);
            try (final Stream<VariantContext> stream = variantIterator.stream()) {
                stream.map(annotateVariant(evaluator, threshold))
                        .peek(progressReporter::logEntry)
                        .onClose(progressReporter.summarize())
                        .forEach(writer::add);
            }
        }
    }
}
