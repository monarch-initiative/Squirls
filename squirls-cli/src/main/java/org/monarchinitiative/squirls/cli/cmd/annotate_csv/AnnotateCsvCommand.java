package org.monarchinitiative.squirls.cli.cmd.annotate_csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.Prediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "annotate-csv", aliases = {"C"}, mixinStandardHelpOptions = true,
        description = "annotate variants stored in tabular file")
public class AnnotateCsvCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateCsvCommand.class);

    private static final List<String> EXPECTED_HEADER = List.of("CHROM", "POS", "REF", "ALT");

    @CommandLine.Parameters(index = "0", description = "path to the input tabular file")
    public Path inputPath;
    @CommandLine.Parameters(index = "1", description = "where to write the output")
    public Path outputPath;


    @Override
    public Integer call() {
        try (final ConfigurableApplicationContext context = getContext()) {
            LOGGER.info("Reading variants from `{}`", inputPath);
            LOGGER.info("Writing annotated variants to `{}`", outputPath);

            VariantSplicingEvaluator evaluator = context.getBean(VariantSplicingEvaluator.class);

            // make header
            final List<String> header = new ArrayList<>();
            header.addAll(EXPECTED_HEADER);
            header.addAll(List.of("PATHOGENIC", "MAX_SCORE", "SCORES"));

            try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                    .parse(Files.newBufferedReader(inputPath));
                 CSVPrinter printer = CSVFormat.DEFAULT
                         .withHeader(header.toArray(String[]::new))
                         .print(Files.newBufferedWriter(outputPath))) {

                // check
                if (!parser.getHeaderNames().containsAll(EXPECTED_HEADER)) {
                    LOGGER.warn("The input file header does not contain the required columns");
                    return 1;
                }

                // iterate through rows of the tabular file
                for (CSVRecord record : parser) {
                    final String chrom = record.get("CHROM");
                    final int pos;
                    final String ref = record.get("REF");
                    final String alt = record.get("ALT");

                    try {
                        pos = Integer.parseInt(record.get("POS"));
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Invalid pos `{}` in record #{}", record.get("POS"), record.getRecordNumber());
                        continue;
                    }
                    final Map<String, SplicingPredictionData> predictionData = evaluator.evaluate(chrom, pos, ref, alt);

                    // figure out max pathogenicity and whether the variant is a splice variant
                    boolean isSpliceVariant = false;
                    double maxScore = Double.NaN;
                    for (SplicingPredictionData prediction : predictionData.values()) {
                        final Prediction prd = prediction.getPrediction();
                        final double current = prd.getMaxPathogenicity();
                        if (Double.isNaN(maxScore)) {
                            maxScore = current;
                        } else {
                            if (maxScore < current) {
                                maxScore = current;
                            }
                        }
                        isSpliceVariant = isSpliceVariant || prd.isPositive();
                    }

                    printer.printRecord(chrom, pos, ref, alt, isSpliceVariant, maxScore, processScores(predictionData));
                }

            } catch (IOException e) {
                LOGGER.warn("Error reading input", e);
                return 1;
            }
        }
        return 0;
    }
}
