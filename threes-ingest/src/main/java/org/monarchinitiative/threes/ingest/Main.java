package org.monarchinitiative.threes.ingest;

import org.monarchinitiative.threes.core.ThreeSException;
import org.monarchinitiative.threes.ingest.config.IngestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Build reference genome fasta file and splicing database for given genome assembly and jannovar caches.
 * <p>
 * Command-line arguments are required:
 *     <ul>
 *         <li>`genome-assembly` - e.g. `hg19`</li>
 *         <li>`version` - e.g. `1910`</li>
 *         <li>`jannovar-transcript-db-dir` - path to directory with Jannovar caches for given genome assembly</li>
 *     </ul>
 * </p>
 */
@SpringBootApplication
public class Main implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final IngestProperties ingestProperties;

    public Main(IngestProperties ingestProperties) {
        this.ingestProperties = ingestProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    private static String normalizeAssemblyString(String assembly) throws ThreeSException {
        switch (assembly.toLowerCase()) {
            case "hg19":
            case "grch37":
                return "hg19";
            case "hg38":
            case "grch38":
                return "hg38";
            default:
                throw new ThreeSException(String.format("Unknown assembly string '%s'", assembly));
        }
    }

    private static String getVersionedAssembly(String assembly, String version) throws ThreeSException {
        assembly = normalizeAssemblyString(assembly);
        // a string like `1902_hg19`
        return version + "_" + assembly;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Running ingest");
        try {
            // 0 - parse command line
            String assembly;
            URL genomeUrl;

            if (args.containsOption("genome-assembly")) {
                String assemblyString = args.getOptionValues("genome-assembly").get(0);
                switch (assemblyString.toLowerCase()) {
                    case "hg19":
                    case "grch37":
                        assembly = "hg19";
                        genomeUrl = new URL(ingestProperties.getHg19FastaUrl());
                        break;
                    case "hg38":
                    case "grch38":
                        assembly = "hg38";
                        genomeUrl = new URL(ingestProperties.getHg38FastaUrl());
                        break;
                    default:
                        LOGGER.error("Unknown assembly '{}', use one of {hg19, hg38}", assemblyString);
                        return;
                }
            } else {
                LOGGER.error("Please specify `--genome-assembly` argument");
                return;
            }

            if (!args.containsOption("version")) {
                LOGGER.error("Missing `--version` argument");
                return;
            }

            String version = args.getOptionValues("version").get(0);
            String versionedAssembly = getVersionedAssembly(assembly, version);
            Path genomeBuildDir = Files.createDirectories(ingestProperties.getBuildDir().resolve(versionedAssembly));

            Path jannovarDbDir;
            if (args.containsOption("jannovar-transcript-db-dir")) {
                jannovarDbDir = Paths.get(args.getOptionValues("jannovar-transcript-db-dir").get(0));
            } else {
                LOGGER.error("Please specify `--jannovar-transcript-db-dir` argument");
                return;
            }

            ThreesDataBuilder.buildDatabase(genomeBuildDir, genomeUrl, jannovarDbDir,
                    ingestProperties.getSplicingInformationContentMatrixPath(),
                    ingestProperties.getHexamersTsvPath(),
                    ingestProperties.getSeptamersTsvPath(),
                    versionedAssembly);

        } catch (ThreeSException e) {
            LOGGER.error("Error: ", e);
            throw e;
        }
    }
}

