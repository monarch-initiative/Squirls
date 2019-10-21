package org.monarchinitiative.threes.ingest;

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.monarchinitiative.threes.ingest.config.IngestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@SpringBootApplication
public class Main implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final IngestProperties ingestProperties;

    private final JannovarData jannovarData;

    public Main(IngestProperties ingestProperties, JannovarData jannovarData) {
        this.ingestProperties = ingestProperties;
        this.jannovarData = jannovarData;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
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
            String versionedAssembly = version + "_" + assembly;
            Path genomeBuildDir = Files.createDirectories(ingestProperties.getBuildDir().resolve(versionedAssembly));

            // 1 - parse YAML with splicing matrices
            Path yamlPath = ingestProperties.getSplicingInformationContentMatrixPath();
            SplicingPwmData data;
            try (InputStream is = Files.newInputStream(yamlPath)) {
                InputStreamBasedPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
                data = parser.getSplicingPwmData();
            }

            // 2 - download reference genome FASTA file
            ThreesDataBuilder.downloadReferenceGenome(genomeUrl, genomeBuildDir, assembly, version, false);

            // this is where the reference genome will be downloaded by the command above
            Path genomeFastaPath = genomeBuildDir.resolve(String.format("%s.fa", versionedAssembly));
            Path genomeFastaFaiPath = genomeBuildDir.resolve(String.format("%s.fa.fai", versionedAssembly));

            // 3 - build the database
            try (GenomeSequenceAccessor accessor = new PrefixHandlingGenomeSequenceAccessor(genomeFastaPath, genomeFastaFaiPath)) {
                String jannovarTranscriptSource = ingestProperties.getJannovarTranscriptSource();
                ThreesDataBuilder.buildThreesDatabase(jannovarData, jannovarTranscriptSource, accessor, data, genomeBuildDir, assembly, version);
            }
        } catch (Exception e) {
            LOGGER.error("Error: ", e);
            throw e;
        }
    }
}

