package org.monarchinitiative.squirls.cli.writers.vcf;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VcfResultWriter implements ResultWriter {

    private static final String SQUIRLS_FLAG_FIELD_NAME = "SQUIRLS";

    private static final VCFInfoHeaderLine FLAG_LINE = new VCFInfoHeaderLine(
            SQUIRLS_FLAG_FIELD_NAME,
            VCFHeaderLineCount.A,
            VCFHeaderLineType.Flag,
            "Variant is considered as pathogenic if the flag is present");

    private static final String SQUIRLS_SCORE_FIELD_NAME = "SQUIRLS_SCORE";

    private static final VCFInfoHeaderLine SCORE_LINE = new VCFInfoHeaderLine(
            SQUIRLS_SCORE_FIELD_NAME,
            VCFHeaderLineCount.A,
            VCFHeaderLineType.String,
            "Squirls pathogenicity score");

    /**
     * Extend the <code>header</code> with INFO fields that are being added in this command.
     *
     * @param header to extend
     * @return the extended header
     */
    private static VCFHeader extendHeaderWithSquirlsFields(VCFHeader header) {
        // SQUIRLS - flag
        header.addMetaDataLine(FLAG_LINE);
        // SQUIRLS_SCORE - float
        header.addMetaDataLine(SCORE_LINE);
        return header;
    }

    /**
     * Store Squirls predictions into VCF INFO fields.
     *
     * @return variant context with populated INFO fields
     */
    private static Function<WritableSplicingAllele, VariantContext> addInfoFields() {
        return ve -> {
            VariantContextBuilder builder = new VariantContextBuilder(ve.variantContext());
            Map<String, SplicingPredictionData> squirlsScores = ve.squirlsPredictions();

            if (squirlsScores.isEmpty()) {
                return builder.make();
            }

            // is the ALT allele pathogenic wrt any overlapping transcript?
            boolean isPathogenic = squirlsScores.values().stream()
                    .anyMatch(spd -> spd.getPrediction().isPositive());

            // prediction string wrt all overlapping transcripts
            String txPredictions = squirlsScores.entrySet().stream()
                    // tx_accession=score
                    .map(entry -> String.format("%s=%f", entry.getKey(), entry.getValue().getPrediction().getMaxPathogenicity()))
                    .collect(Collectors.joining("|", String.format("%s|", ve.allele().getBaseString()), ""));

            return builder.attribute(SQUIRLS_FLAG_FIELD_NAME, isPathogenic)
                    .attribute(SQUIRLS_SCORE_FIELD_NAME, txPredictions)
                    .make();
        };
    }

    @Override
    public void write(AnalysisResults results, OutputSettings outputSettings) throws IOException {
        Path inputVcfPath = Paths.get(results.getSettingsData().getInputPath());
        Path outputVcfPath = Paths.get(outputSettings.outputPrefix() + '.' + OutputFormat.VCF.getFileExtension());

        VCFHeader header;
        try (VCFFileReader reader = new VCFFileReader(inputVcfPath, false)) {
            header = extendHeaderWithSquirlsFields(reader.getFileHeader());
        }

        try (VariantContextWriter writer = new VariantContextWriterBuilder()
                .setOutputPath(outputVcfPath)
                .setReferenceDictionary(header.getSequenceDictionary())
                .setOutputFileType(VariantContextWriterBuilder.OutputType.VCF)
                .unsetOption(Options.INDEX_ON_THE_FLY)
                .build()) {

            writer.writeHeader(header);

            results.getVariants().stream()
                    .map(addInfoFields())
                    .sorted(header.getVCFRecordComparator())
                    .forEach(writer::add);
        }
    }
}
