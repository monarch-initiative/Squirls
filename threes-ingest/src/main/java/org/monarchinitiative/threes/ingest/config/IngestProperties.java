package org.monarchinitiative.threes.ingest.config;

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


    public Path getJannovarDbDir() {
        String propertyKey = "jannovar-transcript-db-dir";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path getSplicingInformationContentMatrixPath() {
        String propertyKey = "splicing-information-content-matrix";
        return getPathOrThrow(propertyKey, String.format("'%s' has not been specified", propertyKey));
    }

    public Path getSeptamersTsvPath() {
        String propertyKey = "sms-tsv-path";
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
