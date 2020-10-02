package org.monarchinitiative.squirls.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *
 */
@ConfigurationProperties(prefix = "threes.ingest")
public class IngestProperties {


    private String splicingInformationContentMatrix;
    private String jannovarTranscriptDbDir;
    private String hexamerTsvPath;
    private String septamerTsvPath;
    private String fastaUrl;
    private List<ClassifierData> classifiers;
    private String phylopPath;

    public List<ClassifierData> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<ClassifierData> classifiers) {
        this.classifiers = classifiers;
    }

    public String getSplicingInformationContentMatrix() {
        return splicingInformationContentMatrix;
    }

    public void setSplicingInformationContentMatrix(String splicingInformationContentMatrix) {
        this.splicingInformationContentMatrix = splicingInformationContentMatrix;
    }

    public String getJannovarTranscriptDbDir() {
        return jannovarTranscriptDbDir;
    }

    public void setJannovarTranscriptDbDir(String jannovarTranscriptDbDir) {
        this.jannovarTranscriptDbDir = jannovarTranscriptDbDir;
    }

    public String getHexamerTsvPath() {
        return hexamerTsvPath;
    }

    public void setHexamerTsvPath(String hexamerTsvPath) {
        this.hexamerTsvPath = hexamerTsvPath;
    }

    public String getSeptamerTsvPath() {
        return septamerTsvPath;
    }

    public void setSeptamerTsvPath(String septamerTsvPath) {
        this.septamerTsvPath = septamerTsvPath;
    }

    public String getFastaUrl() {
        return fastaUrl;
    }

    public void setFastaUrl(String fastaUrl) {
        this.fastaUrl = fastaUrl;
    }

    public String getPhylopPath() {
        return phylopPath;
    }

    public void setPhylopPath(String phylopPath) {
        this.phylopPath = phylopPath;
    }

    public static class ClassifierData {
        private String version;
        private String classifierPath;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getClassifierPath() {
            return classifierPath;
        }

        public void setClassifierPath(String classifierPath) {
            this.classifierPath = classifierPath;
        }
    }
}
