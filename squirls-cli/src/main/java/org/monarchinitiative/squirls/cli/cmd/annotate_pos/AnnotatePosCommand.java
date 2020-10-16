package org.monarchinitiative.squirls.cli.cmd.annotate_pos;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This command takes a bunch of strings like `chr1:1234C>G` and prints out Squirls predictions.
 */
@Component
public class AnnotatePosCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatePosCommand.class);

    private static final String DELIMITER = "\t";

    private final VariantSplicingEvaluator variantSplicingEvaluator;

    public AnnotatePosCommand(VariantSplicingEvaluator variantSplicingEvaluator) {
        this.variantSplicingEvaluator = variantSplicingEvaluator;
    }

    public static void setupSubparsers(Subparsers subparsers) {
        Subparser ic = subparsers.addParser("annotate-pos")
                .setDefault("cmd", "annotate-pos")
                .help("annotate variants with Squirls");

        ic.addArgument("-c", "--change")
//                .nargs("+")
                .metavar("chr1:1234C>G")
                .action(Arguments.append()).required(true)
                .help("nucleotide change(s) to annotate");
    }


    @Override
    public void run(Namespace namespace) {
        final List<String> rawChanges = namespace.getList("change");
        LOGGER.info("Analyzing {} change(s): `{}`", rawChanges.size(), String.join(", ", rawChanges));

        // parse changes (input)
        final List<VariantChange> changes = rawChanges.stream()
                .map(VariantChange::fromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableList());

        System.out.println();
        for (VariantChange change : changes) {
            final Map<String, SplicingPredictionData> predictionData = variantSplicingEvaluator.evaluate(change.getContig(), change.getPos(), change.getRef(), change.getAlt());
            List<String> columns = new ArrayList<>();

            // variant
            columns.add(change.getVariantChange());

            // is pathogenic
            boolean isPathogenic = predictionData.values().stream()
                    .anyMatch(spd -> spd.getPrediction().isPositive());
            columns.add(isPathogenic ? "pathogenic" : "neutral");

            // max pathogenicity
            double maxScore = predictionData.values().stream()
                    .mapToDouble(e -> e.getPrediction().getMaxPathogenicity())
                    .max()
                    .orElse(Double.NaN);
            columns.add(String.format("%.3f", maxScore));

            // predictions per transcript
            final String scores = processScores(predictionData);
            columns.add(scores);

            System.out.println(String.join(DELIMITER, columns));
        }
        System.out.println();
    }
}
