package org.monarchinitiative.squirls.cli.cmd.annotate_csv;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.cli.cmd.CommandException;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.Prediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AnnotateCsvCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateCsvCommand.class);

    private static final List<String> EXPECTED_HEADER = List.of("CHROM", "POS", "REF", "ALT");

    private final VariantSplicingEvaluator evaluator;

    public AnnotateCsvCommand(VariantSplicingEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Setup subparser for {@code annotate-csv} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `annotate-csv` command
        Subparser annotateVcfParser = subparsers.addParser("annotate-csv")
                .setDefault("cmd", "annotate-csv")
                .help("annotate variants stored in tabular file with Squirls scores");
        annotateVcfParser.addArgument("input")
                .help("path to tabular file");
        annotateVcfParser.addArgument("output")
                .help("where to write the tabular file with annotations");
    }

    @Override
    public void run(Namespace namespace) throws CommandException {
        final Path inputPath = Path.of(namespace.getString("input"));
        final Path outputPath = Paths.get(namespace.getString("output"));

        LOGGER.info("Reading variants from `{}`", inputPath);
        LOGGER.info("Writing annotated variants to `{}`", outputPath);

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
                return;
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
                    LOGGER.warn("Invalid pos `{}` in record #{}: {}", record.get("POS"), record.getRecordNumber(), e.getMessage());
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
            throw new CommandException(e);
        }
    }
}
