package org.monarchinitiative.squirls.cli;

import org.monarchinitiative.squirls.cli.cmd.GenerateConfigCommand;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.AnalyzeVcfCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_csv.AnnotateCsvCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_pos.AnnotatePosCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_vcf.AnnotateVcfCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "squirls-cli.jar", mixinStandardHelpOptions = true, version = "squirls v1.0.0-RC3-SNAPSHOT",
        description = "Super-quick Information Content and Random Forest Learning for Splice Variants")
public class Main implements Callable<Integer> {

    // TODO: 10/22/20 fix link
    private static final String EPILOG = "See the full documentation at https://github.com/TheJacksonLaboratory/Squirls/blob/master";

    public static void main(String[] args) {
        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("generate-config", new GenerateConfigCommand())
                .addSubcommand("annotate-pos", new AnnotatePosCommand())
                .addSubcommand("annotate-csv", new AnnotateCsvCommand())
                .addSubcommand("annotate-vcf", new AnnotateVcfCommand())
                .addSubcommand("analyze-vcf", new AnalyzeVcfCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() throws Exception {
        // work done in subcommands
        return 0;
    }
}
