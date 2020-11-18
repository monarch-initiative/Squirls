package org.monarchinitiative.squirls.cli;

import org.monarchinitiative.squirls.cli.cmd.GenerateConfigCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_csv.AnnotateCsvCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_pos.AnnotatePosCommand;
import org.monarchinitiative.squirls.cli.cmd.annotate_vcf.AnnotateVcfCommand;
import picocli.CommandLine;
import picocli.CommandLine.Help.ColorScheme.Builder;

import java.util.Locale;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Ansi.Style.*;

@CommandLine.Command(name = "squirls-cli.jar",
        header = "Super-quick Information Content and Random Forest Learning for Splice Variants\n",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
// TODO: 18. 11. 2020 fix documentation link
public class Main implements Callable<Integer> {

    public static final String VERSION = "squirls v1.0.0-RC3-SNAPSHOT";
    public static final int WIDTH = 120;

    public static final String FOOTER = "See the full documentation at https://github.com/TheJacksonLaboratory/Squirls/blob/master";

    private static final CommandLine.Help.ColorScheme COLOR_SCHEME = new Builder()
            .commands(bold, fg_blue, underline)
            .options(fg_yellow)
            .parameters(fg_yellow)
            .optionParams(italic)
            .build();

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        CommandLine cline = new CommandLine(new Main())
                .setColorScheme(COLOR_SCHEME)
                .addSubcommand("generate-config", new GenerateConfigCommand())
                .addSubcommand("annotate-pos", new AnnotatePosCommand())
                .addSubcommand("annotate-csv", new AnnotateCsvCommand())
                .addSubcommand("annotate-vcf", new AnnotateVcfCommand());
        System.exit(cline.execute(args));
    }


    @Override
    public Integer call() throws Exception {
        // work done in subcommands
        return 0;
    }
}
