package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

public class SettingsData {

    /**
     * Path to VCF file with variants.
     */
    private final String inputPath;

    /**
     * Threshold used to determine pathogenicity of the variant.
     */
    private final double threshold;

    /**
     * One of the supported transcript databases: {refseq, ucsc, ensembl}.
     */
    private final String transcriptDb;

    private SettingsData(Builder builder) {
        inputPath = builder.inputPath;
        threshold = builder.threshold;
        transcriptDb = builder.transcriptDb;
    }

    public static Builder builder() {
        return new Builder();
    }


    public String getTranscriptDb() {
        return transcriptDb;
    }


    public String getInputPath() {
        return inputPath;
    }


    public double getThreshold() {
        return threshold;
    }


    public String getYamlRepresentation() {
        return new StringBuilder()
                .append("Analysis settings:").append(System.lineSeparator())
                .append(" - inputPath: ").append(inputPath).append(System.lineSeparator())
                .append(" - transcriptDatabase: ").append(transcriptDb).append(System.lineSeparator())
                .append(" - threshold: ").append(threshold).append(System.lineSeparator())
                .toString();
    }

    public static final class Builder {
        private String inputPath;
        private double threshold;
        private String transcriptDb;

        private Builder() {
        }

        public Builder inputPath(String inputPath) {
            this.inputPath = inputPath;
            return this;
        }

        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder transcriptDb(String transcriptDb) {
            this.transcriptDb = transcriptDb;
            return this;
        }

        public SettingsData build() {
            return new SettingsData(this);
        }
    }
}
