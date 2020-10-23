package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.monarchinitiative.squirls.cli.cmd.ProgressReporter;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "annotate-vcf", aliases = {"A"}, mixinStandardHelpOptions = true,
        description = "annotate VCF file")
public class AnnotateVcfCommand extends SquirlsCommand {

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

    @CommandLine.Parameters(index = "0", description = "path to the input VCF file")
    public Path inputPath;
    @CommandLine.Parameters(index = "1", description = "where to write the output VCF file")
    public Path outputPath;

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
    public Integer call() {
        try (final ConfigurableApplicationContext context = getContext()) {
            LOGGER.info("Reading variants from `{}`", inputPath);
            LOGGER.info("Writing annotated variants to `{}`", outputPath);

            final VariantSplicingEvaluator evaluator = context.getBean(VariantSplicingEvaluator.class);

            // TODO: 29. 5. 2020 improve behavior & logging
            // e.g. report progress in % if variant index and thus count is available
            final VCFHeader header;
            final ProgressReporter progressReporter = new ProgressReporter(5_000);
            final List<VariantContext> annotated = Collections.synchronizedList(new ArrayList<>());

            try (final VCFFileReader reader = new VCFFileReader(inputPath, false);
                 final CloseableIterator<VariantContext> variantIterator = reader.iterator()) {

                // extend the header from the input VCF
                header = reader.getFileHeader();

                // annotate the variants
                try (final Stream<VariantContext> stream = variantIterator.stream()) {
                    stream.parallel()
                            .map(annotateVariant(evaluator))
                            .peek(progressReporter::logItem)
                            .onClose(progressReporter.summarize())
                            .forEach(annotated::add);
                }
            }

            // write out the results
            LOGGER.info("Writing out the results");
            try (final VariantContextWriter writer = new VariantContextWriterBuilder()
                    .setReferenceDictionary(header.getSequenceDictionary())
                    .setOutputPath(outputPath)
                    .setOutputFileType(VariantContextWriterBuilder.OutputType.VCF)
                    .setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER)
//                     .unsetOption(Options.INDEX_ON_THE_FLY)
                    .build()) {
                // extend header with Squirls fields and write it out
                writer.writeHeader(extendHeader(header));

                // write out the annotated variants
                annotated.stream()
                        .sorted(header.getVCFRecordComparator())
                        .forEach(writer::add);
            }
        }

        return 0;
    }
}
