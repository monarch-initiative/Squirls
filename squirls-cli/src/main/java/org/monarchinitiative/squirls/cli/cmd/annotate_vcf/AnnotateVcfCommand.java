package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

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
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AnnotateVcfCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateVcfCommand.class);

    private static final VCFInfoHeaderLine FLAG_LINE = new VCFInfoHeaderLine(
            "SQUIRLS",
            VCFHeaderLineCount.A,
            VCFHeaderLineType.Flag,
            "Variant is considered as pathogenic if the flag is present");

    private static final VCFInfoHeaderLine SCORE_LINE = new VCFInfoHeaderLine(
            "SQUIRLS_SCORE",
            VCFHeaderLineCount.A,
            VCFHeaderLineType.String,
            "Squirls pathogenicity score");

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
                .help("annotate VCF file with Squirls scores");
        annotateVcfParser.addArgument("input")
                .help("path to VCF file");
        annotateVcfParser.addArgument("output")
                .help("where to write the output VCF file");
    }

    /**
     * Extend the <code>header</code> with INFO fields that are being added in this command.
     *
     * @param header to extend
     * @return the extended header
     */
    private static VCFHeader extendHeader(VCFHeader header) {
        // SQUIRLS - flag
        header.addMetaDataLine(FLAG_LINE);
        // SQUIRLS_SCORE - float
        header.addMetaDataLine(SCORE_LINE);
        return header;
    }

    /**
     * Annotate and return a single {@link VariantContext}.
     *
     * @param evaluator variant splicing evaluator to use
     * @return annotated {@link VariantContext}
     */
    private static UnaryOperator<VariantContext> annotateVariant(VariantSplicingEvaluator evaluator) {
        return vc -> {
            // variant labeled as pathogenic if at least one ALT allele is pathogenic
            boolean isPathogenic = false;

            // get prediction
            final List<String> annotations = new ArrayList<>(vc.getAlternateAlleles().size());
            for (Allele allele : vc.getAlternateAlleles()) {
                final Map<String, SplicingPredictionData> predictionData = evaluator.evaluate(vc.getContig(), vc.getStart(), vc.getReference().getBaseString(), allele.getBaseString());
                if (predictionData.isEmpty()) {
                    continue;
                }
                // is the ALT allele pathogenic wrt any overlapping transcript?
                isPathogenic = isPathogenic || predictionData.values().stream()
                        .anyMatch(spd -> spd.getPrediction().isPositive());

                // prediction string wrt all overlapping transcripts
                String txPredictions = predictionData.entrySet().stream()
                        // tx_accession=score
                        .map(entry -> String.format("%s=%f", entry.getKey(), entry.getValue().getPrediction().getMaxPathogenicity()))
                        .collect(Collectors.joining("|", String.format("%s|", allele.getBaseString()), ""));
                annotations.add(txPredictions);
            }

            // join predictions for individual ALT alleles
            final String annotationLine = String.join("&", annotations);

            return new VariantContextBuilder(vc)
                    .attribute("SQUIRLS", isPathogenic)
                    .attribute("SQUIRLS_SCORE", annotationLine)
                    .make();
        };
    }

    @Override
    public void run(Namespace namespace) {
        final Path inputPath = Paths.get(namespace.getString("input"));
        LOGGER.info("Reading variants from `{}`", inputPath);
        final Path outputPath = Paths.get(namespace.getString("output"));
        LOGGER.info("Writing annotated variants to `{}`", outputPath);

        // TODO: 29. 5. 2020 improve behavior & logging
        // e.g. report progress in % if variant index and thus count is available
        final ProgressReporter<VariantContext> progressReporter = new ProgressReporter<>();
        try (final VCFFileReader reader = new VCFFileReader(inputPath, false);
             final CloseableIterator<VariantContext> variantIterator = reader.iterator();
             final VariantContextWriter writer = new VariantContextWriterBuilder()
                     .setReferenceDictionary(reader.getFileHeader().getSequenceDictionary())
                     .setOutputPath(outputPath)
                     .setOutputFileType(VariantContextWriterBuilder.OutputType.VCF)
                     .setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER)
//                     .unsetOption(Options.INDEX_ON_THE_FLY)
                     .build()) {

            // extend the header from the input VCF and write it out
            final VCFHeader header = reader.getFileHeader();
            final VCFHeader extended = extendHeader(header);
            writer.writeHeader(extended);

            // annotate the variants
            final List<VariantContext> annotated = Collections.synchronizedList(new ArrayList<>());
            try (final Stream<VariantContext> stream = variantIterator.stream()) {
                stream.parallel()
                        .map(annotateVariant(evaluator))
                        .peek(progressReporter::logEntry)
                        .onClose(progressReporter.summarize())
                        .forEach(annotated::add);
            }

            // write out the annotated variants
            annotated.stream()
                    .sorted(header.getVCFRecordComparator())
                    .forEach(writer::add);
        }
    }
}
