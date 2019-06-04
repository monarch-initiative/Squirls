package org.monarchinitiative.sss.ingest.config;

import org.monarchinitiative.sss.ingest.GenomeAssembly;
import org.monarchinitiative.sss.ingest.JannovarTranscriptSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
@Component
@ConfigurationProperties(prefix = "config")
public class IngestProperties {

    private final Environment env;

    private DbConfig dbConfig;

    public IngestProperties(Environment env) {
        this.env = env;
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }


    public Path getJannovarCachePath() {
        String propertyKey = "jannovar-transcript-db-path";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path getFastaPath() {
        String propertyKey = "ref-genome-fasta";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path getFastaIndexPath() {
        String propertyKey = "ref-genome-fasta";
        return Paths.get(getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey)).toString() + ".fai");
    }

    public GenomeAssembly getGenomeAssembly() {
        String ga = env.getProperty("genome-assembly");
        if (ga == null || ga.isEmpty()) {
            throw new IllegalArgumentException("genome-assembly has not been specified");
        }
        switch (ga.toUpperCase()) {
            case "HG19":
                return GenomeAssembly.HG19;
            case "HG38":
                return GenomeAssembly.HG38;
            default:
                throw new IllegalArgumentException("Unknown genome assembly " + ga);
        }
    }

    public JannovarTranscriptSource getJannovarTranscriptSource() {
        String ts = env.getProperty("jannovar-transcript-source");
        if (ts == null || ts.isEmpty()) {
            throw new IllegalArgumentException("jannovar-transcript-source has not been specified");
        }
        switch (ts.toUpperCase()) {
            case "REFSEQ":
                return JannovarTranscriptSource.REFSEQ;
            case "REFSEQ_CURATED":
                return JannovarTranscriptSource.REFSEQ_CURATED;
            case "UCSC":
                return JannovarTranscriptSource.UCSC;
            case "ENSEMBL":
                return JannovarTranscriptSource.ENSEMBL;
            default:
                throw new IllegalArgumentException("Unknown jannovar transcript source " + ts);
        }
    }

    public Path getSplicingInformationContentMatrixPath() {
        String propertyKey = "splicing-information-content-matrix";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path buildDir() {
        String propertyKey = "build-dir";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }


    private Path getPathOrThrow(String propertyKey, String message) {
        String path = env.getProperty(propertyKey, "");

        if (path.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return Paths.get(path);
    }
}
