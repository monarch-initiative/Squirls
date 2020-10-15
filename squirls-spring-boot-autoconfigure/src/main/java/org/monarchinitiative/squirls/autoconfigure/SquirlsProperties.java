package org.monarchinitiative.squirls.autoconfigure;

import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "squirls")
public class SquirlsProperties {

    /**
     * Path to directory with reference genome files and splicing database.
     */
    private String dataDirectory;

    /**
     * Genome assembly version, choose from {hg19, hg38}.
     */
    private String genomeAssembly;

    /**
     * Exomiser-like data version, e.g. `1902`.
     */
    private String dataVersion;

    /**
     * Version of the classifier to use.
     */
    @NestedConfigurationProperty // squirls.classifier
    private ClassifierProperties classifier = new ClassifierProperties();

    @NestedConfigurationProperty // squirls.annotator
    private AnnotatorProperties annotator = new AnnotatorProperties();

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getGenomeAssembly() {
        return genomeAssembly;
    }

    public void setGenomeAssembly(String genomeAssembly) {
        this.genomeAssembly = genomeAssembly;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public ClassifierProperties getClassifier() {
        return classifier;
    }

    public void setClassifier(ClassifierProperties classifier) {
        this.classifier = classifier;
    }

    public AnnotatorProperties getAnnotator() {
        return annotator;
    }

    public void setAnnotator(AnnotatorProperties annotator) {
        this.annotator = annotator;
    }

    /**
     * Properties for specifying which classifier to use.
     */
    @ConfigurationProperties(prefix = "squirls.classifier")
    public static class ClassifierProperties {

        private String version = "v0.4.4";

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

    /**
     * Properties for tweaking the annotator.
     */
    @ConfigurationProperties(prefix = "squirls.annotator")
    public static class AnnotatorProperties {

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
}
