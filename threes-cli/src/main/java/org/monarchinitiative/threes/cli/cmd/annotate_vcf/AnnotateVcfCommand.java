package org.monarchinitiative.threes.cli.cmd.annotate_vcf;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.threes.cli.cmd.Command;
import org.monarchinitiative.threes.cli.cmd.CommandException;
import org.springframework.stereotype.Component;

@Component
public class AnnotateVcfCommand extends Command {

    /**
     * Setup subparser for {@code generate-config} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `generate-config` command
        final Subparser configParser = subparsers.addParser("annotate-vcf")
                .setDefault("cmd", "annotate-vcf")
                .help("annotate VCF file with splicing scores");
        // TODO - implement
//        configParser.addArgument("output")
//                .help("configuration file path");
    }


    @Override
    public void run(Namespace namespace) throws CommandException {
        throw new CommandException("Not implemented");
    }
}
