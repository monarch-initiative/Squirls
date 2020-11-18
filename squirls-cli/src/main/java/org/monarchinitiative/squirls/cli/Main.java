package org.monarchinitiative.squirls.cli;

import org.monarchinitiative.squirls.cli.cmd.GenerateConfigCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_csv.AnnotateCsvCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_pos.AnnotatePosCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_vcf.AnnotateVcfCommand;
import picocli.CommandLine;
import picocli.CommandLine.Help.ColorScheme.Builder;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Ansi.Style.*;

@CommandLine.Command(name = "squirls-cli.jar",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        header = "Super-quick Information Content and Random Forest Learning for Splice Variants",
        usageHelpWidth = 120,
        description = "See the full documentation at https://github.com/TheJacksonLaboratory/Squirls/blob/master")
// TODO: 18. 11. 2020 fix documentation link
public class Main implements Callable<Integer> {

    public static final String VERSION = "squirls v1.0.0-RC3-SNAPSHOT";

    private static final CommandLine.Help.ColorScheme COLOR_SCHEME = new Builder()
            .commands(bold, fg_blue, underline)
            .options(fg_yellow)
            .parameters(fg_yellow)
            .optionParams(italic)
            .build();

    public static void main(String[] args) {
        CommandLine cline = new CommandLine(new Main())
                .setColorScheme(COLOR_SCHEME)
                .addSubcommand("generate-config", new GenerateConfigCommand())
                .addSubcommand("annotate-pos", new AnnotatePosCommand())
                .addSubcommand("annotate-csv", new AnnotateCsvCommand())
                .addSubcommand("annotate-vcf", new AnnotateVcfCommand());
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
