package org.monarchinitiative.threes.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.threes.autoconfigure.EnableThrees;
import org.monarchinitiative.threes.cli.cmd.Command;
import org.monarchinitiative.threes.cli.cmd.annotate_pos.AnnotatePosCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 */
@EnableThrees
@SpringBootApplication
public class Main {

    private static final String EPILOG =
            "            ____\n" +
                    "      _,.-'`_ o `;__,\n" +
                    "       _.-'` '---'  '";

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // 1. define CLI interface
        ArgumentParser parser = ArgumentParsers.newFor("java -jar threes-cli.jar").build();
        parser.description("Code for splicing calculations");

        // - we require 3S properties to be provided
        parser.addArgument("-c", "--config")
                .required(true)
                .metavar("/path/to/application.properties")
                .help("path to Spring configuration file");

        Subparsers subparsers = parser.addSubparsers();
        AnnotatePosCommand.setupSubparsers(subparsers);

        parser.defaultHelp(true);
        parser.epilog(EPILOG);

        Namespace namespace = null;
        try {
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get(namespace.getString("config")))) {
            properties.load(is);
        }

        //  2. bootstrap the app
        try (ConfigurableApplicationContext appContext = new SpringApplicationBuilder(Main.class)
                .properties(properties)
                .run()) {
            // 3. get the selected command and run it
            Command command;
            String cmdName = namespace.get("cmd");
            switch (cmdName) {
                case "annotate-pos":
                    command = appContext.getBean(AnnotatePosCommand.class);
                    break;
                default:
                    LOGGER.warn("Unknown command '{}'", cmdName);
                    System.exit(1);
                    return; // unreachable, but still required
            }

            command.run(namespace);
        }
        LOGGER.info("Done!");
    }

}

