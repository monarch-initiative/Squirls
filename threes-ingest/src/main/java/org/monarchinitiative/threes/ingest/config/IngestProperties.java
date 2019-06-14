package org.monarchinitiative.threes.ingest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.threes.ingest.GenomeAssembly;
import org.monarchinitiative.threes.ingest.JannovarTranscriptSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 */
@Component
@ConfigurationProperties(prefix = "config")
public class IngestProperties {

    private final Environment env;

    private final String propertyKey = "build-dir";

    private final Path buildDir;

    private String hg19FastaUrl;

    private String hg38FastaUrl;

    public IngestProperties(Environment env) {
        this.env = env;
        String buildDirPath = Objects.requireNonNull(env.getProperty(propertyKey), String.format("'%s' has not been specified", propertyKey));
        this.buildDir = Paths.get(buildDirPath);
    }

    public Path getBuildDir() {
        return buildDir;
    }

    public String getHg19FastaUrl() {
        return hg19FastaUrl;
    }

    public void setHg19FastaUrl(String hg19FastaUrl) {
        this.hg19FastaUrl = hg19FastaUrl;
    }

    public String getHg38FastaUrl() {
        return hg38FastaUrl;
    }

    public void setHg38FastaUrl(String hg38FastaUrl) {
        this.hg38FastaUrl = hg38FastaUrl;
    }


    public Path getJannovarCachePath() {
        String propertyKey = "jannovar-transcript-db-path";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
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

    public Path resolveBuildDirForGenome(String version, GenomeAssembly genomeAssembly) throws IOException {
        final Path targetDir = buildDir.resolve(genomeAssembly.getValue());
        return Files.createDirectories(targetDir);
    }

    public DataSource makeDatasourceForGenome(Path databasePath) {
        String jdbcUrl = String.format("jdbc:h2:file:%s", databasePath.toString());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    private Path getPathOrThrow(String propertyKey, String message) {
        String path = env.getProperty(propertyKey, "");

        if (path.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return Paths.get(path);
    }
}
