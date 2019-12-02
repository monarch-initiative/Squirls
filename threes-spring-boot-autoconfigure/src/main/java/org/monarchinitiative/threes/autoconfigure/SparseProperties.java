package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "threes.sparse")
public class SparseProperties {

    /**
     * Choose from {raw, scaling}.
     */
    private String scorerFactoryType = "scaling";

    public String getScorerFactoryType() {
        return scorerFactoryType;
    }

    public void setScorerFactoryType(String scorerFactoryType) {
        this.scorerFactoryType = scorerFactoryType;
    }
}
