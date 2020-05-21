package org.monarchinitiative.threes.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "classifier")
public class ClassifierProperties {

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
