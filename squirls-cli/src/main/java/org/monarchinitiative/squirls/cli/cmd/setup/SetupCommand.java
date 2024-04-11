package org.monarchinitiative.squirls.cli.cmd.setup;

import org.monarchinitiative.squirls.cli.Main;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "setup",
        aliases = {"S"},
        header = "Setup Squirls resources",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH
)
public class SetupCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }
}
