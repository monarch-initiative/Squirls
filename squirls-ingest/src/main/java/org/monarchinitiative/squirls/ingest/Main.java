package org.monarchinitiative.squirls.ingest;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.monarchinitiative.squirls.ingest.cmd.GenerateConfigCommand;
import org.monarchinitiative.squirls.ingest.cmd.IngestCommand;
import org.monarchinitiative.squirls.ingest.cmd.RunIngestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Command responsible for building resource directory.
 */
@SpringBootApplication
@EnableConfigurationProperties(value = {IngestProperties.class})
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        /*
         1. define CLI interface
         */
        ArgumentParser parser = ArgumentParsers.newFor("java -jar squirls-ingest.jar").build();
        parser.description("Squirls - Ingest module:");
        parser.defaultHelp(true);

        final Subparsers subparsers = parser.addSubparsers();
        // a) - `generate-config`
        GenerateConfigCommand.setupSubparsers(subparsers);

        // b) - `ingest`
        Subparser ingestParser = subparsers.addParser("ingest")
                .setDefault("cmd", "ingest")
                .help("run ingest process");
        final Subparsers ingestSubparsers = ingestParser.addSubparsers();
        RunIngestCommand.setupSubparsers(ingestSubparsers);
        ingestParser.addArgument("-c", "--config")
                .required(true)
                .metavar("path/to/application-template.yml")
                .help("path to configuration file generated by `generate-config` command");

        /*
         2. Parse the command line arguments
         */
        Namespace namespace = null;
        try {
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        /*
         3. run the command
         */
        final IngestCommand command;
        final String cmdName = namespace.get("cmd");
        if (cmdName.equals("generate-config")) {
            command = new GenerateConfigCommand();
        } else {
            String configPath = namespace.getString("config");
            LOGGER.info("Reading ingest configuration from `{}`", configPath);

            // bootstrap Spring application context and add the cli arguments to the properties
            final ConfigurableApplicationContext appContext = new SpringApplicationBuilder(Main.class)
                    .properties(Map.of("spring.config.location", configPath))
                    .run(args);

            // get the selected command and run it
            switch (cmdName) {
                case "run-ingest":
                    command = appContext.getBean(RunIngestCommand.class);
                    break;
                default:
                    LOGGER.warn("Unknown command '{}'", cmdName);
                    System.exit(1);
                    return; // unreachable, but still required
            }
        }
        command.run(namespace);
        LOGGER.info("Done!");
    }


}

