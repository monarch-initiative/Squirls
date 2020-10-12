package org.monarchinitiative.squirls.autoconfigure;

import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for tweaking annotator to use.
 */
@ConfigurationProperties(prefix = "squirls.annotator")
public class AnnotatorProperties {

    /**
     * Which {@link SplicingAnnotator} to use (`dense` by default).
     */
    private String version = "agez";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
