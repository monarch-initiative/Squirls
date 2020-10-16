package org.monarchinitiative.squirls.cli.cmd.annotate_pos;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
                .nargs("+")
                .metavar("chr1:1234C>G")
                .help("nucleotide change(s) to annotate");
    }


    @Override
    public void run(Namespace namespace) {
        final List<String> rawChanges = namespace.getList("change");
        LOGGER.info("Analyzing {} changes: `{}`", rawChanges.size(), String.join(", ", rawChanges));

        // parse changes (input)
        final List<VariantChange> changes = rawChanges.stream()
                .map(VariantChange::fromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableList());

        for (VariantChange change : changes) {
            final Map<String, SplicingPredictionData> predictionByTx = variantSplicingEvaluator.evaluate(change.getContig(), change.getPos(), change.getRef(), change.getAlt());
            final String scores = predictionByTx.keySet().stream()
                    .sorted()
                    .map(tx -> String.format("%s=%f", tx, predictionByTx.get(tx).getPrediction().getMaxPathogenicity()))
                    .collect(Collectors.joining(";"));

            System.out.println(String.join(DELIMITER, change.getVariantChange(), scores));
        }
    }
}
