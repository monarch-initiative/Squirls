package org.monarchinitiative.squirls.autoconfigure;

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
     * Path to bigwig file with genome-wide PhyloP scores.
     */
    private String phylopBigwigPath;
    /**
     * Version of the classifier to use.
     */
    @NestedConfigurationProperty // squirls.classifier
    private ClassifierProperties classifier = new ClassifierProperties();

    @NestedConfigurationProperty // squirls.annotator
    private AnnotatorProperties annotator = new AnnotatorProperties();

    public String getPhylopBigwigPath() {
        return phylopBigwigPath;
    }

    public void setPhylopBigwigPath(String phylopBigwigPath) {
        this.phylopBigwigPath = phylopBigwigPath;
    }

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
}
