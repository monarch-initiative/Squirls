package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for specifying which classifier to use.
 */
@ConfigurationProperties(prefix = "threes.classifier")
public class ClassifierProperties {

    private String version = "v1";
    private int maxVariantLength = 100;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxVariantLength() {
        return maxVariantLength;
    }

    public void setMaxVariantLength(int maxVariantLength) {
        this.maxVariantLength = maxVariantLength;
    }
}
