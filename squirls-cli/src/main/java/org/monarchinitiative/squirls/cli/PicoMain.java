package org.monarchinitiative.squirls.cli;

import org.monarchinitiative.squirls.cli.picocmd.AnnotatePosCommand;
import org.monarchinitiative.squirls.cli.picocmd.GenerateConfigCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "squirls", mixinStandardHelpOptions = true, version = "squirls v1.0.0-RC3-SNAPSHOT",
        description = "Super-quick Information Content and Random Forest Learning for Splice Variants")
public class PicoMain implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    // TODO: 10/22/20 fix link
    private static final String EPILOG = "See the full documentation at https://github.com/TheJacksonLaboratory/Squirls/blob/master";

    public static void main(String[] args) {
        CommandLine cline = new CommandLine(new PicoMain())
                .addSubcommand("generate-config", new GenerateConfigCommand())
                .addSubcommand("annotate-pos", new AnnotatePosCommand());
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
