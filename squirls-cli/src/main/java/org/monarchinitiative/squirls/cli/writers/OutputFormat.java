package org.monarchinitiative.squirls.cli.writers;

/**
 * Squirls reports annotated variants in these formats.
 */
public enum OutputFormat {

    HTML("html"),
    VCF("vcf");

    private final String fileExtension;

    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
