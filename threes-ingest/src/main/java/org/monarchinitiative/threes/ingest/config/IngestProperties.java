package org.monarchinitiative.threes.ingest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
@Component
@ConfigurationProperties(prefix = "config")
public class IngestProperties {

    private final Environment env;

    private final Path buildDir;

    private String hg19FastaUrl;

    private String hg38FastaUrl;

    public IngestProperties(Environment env) {
        this.env = env;
        String propertyKey = "build-dir";
        this.buildDir = getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
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

    public String getJannovarTranscriptSource() {
        String ts = env.getProperty("jannovar-transcript-source");
        if (ts == null || ts.isEmpty()) {
            throw new IllegalArgumentException("jannovar-transcript-source has not been specified");
        }
        switch (ts.toLowerCase()) {
            case "refseq":
                return "refseq";
            case "ucsc":
                return "ucsc";
            case "ensembl":
                return "ensembl";
            default:
                throw new IllegalArgumentException("Unknown Jannovar transcript source " + ts);
        }
    }

    public Path getSplicingInformationContentMatrixPath() {
        String propertyKey = "splicing-information-content-matrix";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path getSeptamersTsvPath() {
        String propertyKey = "sms-tsv-path";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path resolveBuildDirForGenome(String version, String assembly) throws IOException {
        final Path targetDir = buildDir.resolve(String.format("%s_%s", version, assembly));
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
