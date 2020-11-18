package org.monarchinitiative.squirls.ingest.cmd;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.ingest.IngestProperties;
import org.monarchinitiative.squirls.ingest.SquirlsDataBuilder;
import org.monarchinitiative.squirls.ingest.data.ZipCompressionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RunIngestCommand extends IngestCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunIngestCommand.class);

    private final IngestProperties ingestProperties;

    public RunIngestCommand(IngestProperties ingestProperties) {
        this.ingestProperties = ingestProperties;
    }

    /**
     * Setup subparser for {@code run-ingest} command.
     *
     * @param subparsers {@link Subparsers}
     */
    public static void setupSubparsers(Subparsers subparsers) {
        // `run-ingest` command
        final Subparser ingestParser = subparsers.addParser("run-ingest")
                .setDefault("cmd", "run-ingest")
                .help("run ingest in order to build resource directory");
        ingestParser.addArgument("build-dir")
                .help("path where to build the database");
        ingestParser.addArgument("version")
                .type(String.class)
                .setDefault("2005")
                .help("exomiser-like version");
        ingestParser.addArgument("assembly")
                .type(String.class)
                .setDefault("hg19")
                .help("which genome assembly to use");
    }

    private static String normalizeAssemblyString(String assembly) throws SquirlsException {
        switch (assembly.toLowerCase()) {
            case "hg19":
            case "grch37":
                return "hg19";
            case "hg38":
            case "grch38":
                return "hg38";
            default:
                throw new SquirlsException(String.format("Unknown assembly string '%s'", assembly));
        }
    }

    private static String getVersionedAssembly(String assembly, String version) throws SquirlsException {
        assembly = normalizeAssemblyString(assembly);
        // a string like `1902_hg19`
        return version + "_" + assembly;
    }

    @Override
    public void run(Namespace args) throws Exception {
        LOGGER.info("Running `run-ingest` command");

        // 0 - parse command line
        Path buildDirPath = Paths.get(args.getString("build_dir"));
        if (!buildDirPath.toFile().isDirectory()) {
            LOGGER.error("Not a directory: {}", buildDirPath);
            return;
        }
        if (!buildDirPath.toFile().canWrite()) {
            LOGGER.error("Directory not writable: {}", buildDirPath);
            return;
        }

        LOGGER.info("Build directory: `{}`", buildDirPath);

        String version = args.getString("version");
        String assembly = args.getString("assembly");
        LOGGER.info("Using version `{}` and genome assembly `{}`", version, assembly);

        // 1 - create build folder
        URL genomeUrl = new URL(ingestProperties.getFastaUrl());
        URL phylopUrl = new URL(ingestProperties.getPhylopUrl());

        String versionedAssembly = getVersionedAssembly(assembly, version);
        Path versionedAssemblyBuildPath = buildDirPath.resolve(versionedAssembly);
        Path genomeBuildDir = Files.createDirectories(versionedAssemblyBuildPath);
        LOGGER.info("Building resources in `{}`", versionedAssemblyBuildPath);

        // 2 - read classifier data
        Map<String, String> classifiers = ingestProperties.getClassifiers().stream()
                .collect(Collectors.toMap(IngestProperties.ClassifierData::getVersion, IngestProperties.ClassifierData::getClassifierPath));

        // 3 - build database
        SquirlsDataBuilder.buildDatabase(genomeBuildDir, genomeUrl, phylopUrl,
                Path.of(ingestProperties.getJannovarTranscriptDbDir()),
                Path.of(ingestProperties.getSplicingInformationContentMatrix()),
                Path.of(ingestProperties.getHexamerTsvPath()),
                Path.of(ingestProperties.getSeptamerTsvPath()),
                classifiers, versionedAssembly);

        // 4 - compress all the files into a single ZIP file
        File[] resources = buildDirPath.toFile().listFiles();
        if (resources == null) {
            LOGGER.warn("Resources are null: {}", buildDirPath);
            return;
        }
        Path zipPath = buildDirPath.getParent().resolve(versionedAssembly + ".zip");
        LOGGER.info("Compressing the resource files into a single ZIP file `{}`", zipPath);
        try (ZipCompressionWrapper wrapper = new ZipCompressionWrapper(zipPath.toFile())) {
            for (File resource : resources) {
                LOGGER.info("Compressing `{}`", resource);
                wrapper.addResource(resource, resource.getName());
            }
        }
    }
}
