package org.monarchinitiative.squirls.cli.cmd.annotate_pos;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.cli.cmd.CommandException;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TODO - implement
 */
@Component
public class AnnotatePosCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatePosCommand.class);

    private final VariantSplicingEvaluator variantSplicingEvaluator;

    public AnnotatePosCommand(VariantSplicingEvaluator variantSplicingEvaluator) {
        this.variantSplicingEvaluator = variantSplicingEvaluator;
    }

    public static void setupSubparsers(Subparsers subparsers) {
        Subparser ic = subparsers.addParser("annotate-pos")
                .setDefault("cmd", "annotate-pos")
                .help("annotate variants using 3S");

        ic.addArgument("-c", "--change")
                .nargs("+")
                .metavar("chr1:1234C>G")
                .help("nucleotide change(s) to annotate");
    }


    @Override
    public void run(Namespace namespace) throws CommandException {
        // TODO: 26. 5. 2020 implement
        throw new CommandException("Not yet implemented");

//        final List<String> rawChanges = namespace.getList("change");
//        LOGGER.info("Analyzing {} changes: `{}`", rawChanges.size(), String.join(", ", rawChanges));
//
//        final List<VariantChange> changes = rawChanges.stream()
//                .map(VariantChange::fromString)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toUnmodifiableList());
//
//        for (VariantChange change : changes) {
//            final Map<String, Prediction> dataMap = variantSplicingEvaluator.evaluate(change.getContig(), change.getPos(), change.getRef(), change.getAlt());
//            final List<String> transcripts = dataMap.keySet().stream().sorted().collect(Collectors.toUnmodifiableList());
//            for (String tx : transcripts) {
//                final Prediction data = dataMap.get(tx);
//                final List<String> scoresNames = data.getScoresMap().keySet().stream().sorted().collect(Collectors.toUnmodifiableList());
//                String scores = scoresNames.stream()
//                        .map(name -> name + "=" + data.getOrDefault(name, Double.NaN))
//                        .collect(Collectors.joining(";"));
//                System.out.println(String.join("\t", change.getVariantChange(), tx, scores));
//            }
//        }
    }
}
