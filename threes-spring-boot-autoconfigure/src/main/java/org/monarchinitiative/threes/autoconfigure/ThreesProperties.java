package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "threes")
public class ThreesProperties {

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
    private String classifierVersion = "v1";

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

    public String getClassifierVersion() {
        return classifierVersion;
    }

    public void setClassifierVersion(String classifierVersion) {
        this.classifierVersion = classifierVersion;
    }
}
