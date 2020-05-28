package org.monarchinitiative.threes.ingest.cmd;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class GenerateConfigCommand extends IngestCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateConfigCommand.class);

    /**
     * Setup subparser for {@code generate-config} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `generate-config` command
        final Subparser configParser = subparsers.addParser("generate-config")
                .setDefault("cmd", "generate-config")
                .help("generate a configuration YAML file");
        configParser.addArgument("output")
                .help("configuration file path");
    }

    @Override
    public void run(Namespace namespace) throws Exception {
        final Path output = Paths.get(namespace.getString("output"));
        LOGGER.info("Generating config template to `{}`", output.toAbsolutePath());
        try (InputStream is = GenerateConfigCommand.class.getResourceAsStream("/application-template.yml")) {
            Files.copy(is, output, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Error: ", e);
            throw e;
        }
    }

}
