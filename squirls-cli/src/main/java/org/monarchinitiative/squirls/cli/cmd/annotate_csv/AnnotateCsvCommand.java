package org.monarchinitiative.squirls.cli.cmd.annotate_csv;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.squirls.cli.cmd.Command;
import org.monarchinitiative.squirls.cli.cmd.CommandException;
import org.springframework.stereotype.Component;

@Component
public class AnnotateCsvCommand extends Command {


    /**
     * Setup subparser for {@code annotate-csv} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `annotate-csv` command
        Subparser annotateVcfParser = subparsers.addParser("annotate-csv")
                .setDefault("cmd", "annotate-csv")
                .help("annotate variants stored in tabular file with splicing scores");
        annotateVcfParser.addArgument("input")
                .help("path to tabular file");
        annotateVcfParser.addArgument("output")
                .help("where to write the tabular file with annotations");
    }

    @Override
    public void run(Namespace namespace) throws CommandException {
        // TODO: 26. 5. 2020 implement
        throw new CommandException("Not yet implemented");
    }
}
