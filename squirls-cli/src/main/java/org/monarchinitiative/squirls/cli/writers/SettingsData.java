package org.monarchinitiative.squirls.cli.writers;

public class SettingsData {

    /**
     * Path to VCF file with variants.
     */
    private final String inputPath;

    /**
     * N variants to be included in HTML report.
     */
    private final int nReported;

    /**
     * One of the supported transcript databases: {refseq, ucsc, ensembl}.
     */
    private final String transcriptDb;

    private SettingsData(Builder builder) {
        inputPath = builder.inputPath;
        nReported = builder.nReported;
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


    @Deprecated
    public int getNReported() {
        return nReported;
    }


    public String getYamlRepresentation() {
        return new StringBuilder()
                .append("Input VCF path: ").append(inputPath).append(System.lineSeparator())
                .append("Jannovar transcript database: ").append(transcriptDb).append(System.lineSeparator())
                .toString();
    }

    public static final class Builder {
        private String inputPath;
        private int nReported;
        private String transcriptDb;

        private Builder() {
        }

        public Builder inputPath(String inputPath) {
            this.inputPath = inputPath;
            return this;
        }

        public Builder nReported(int nReported) {
            this.nReported = nReported;
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
