package org.monarchinitiative.squirls.cli.writers;

import java.util.Objects;

public class OutputSettings {

    private final String outputPrefix;
    private final int nVariantsToReport;

    public OutputSettings(String outputPrefix, int nVariantsToReport) {
        this.outputPrefix = outputPrefix;
        this.nVariantsToReport = nVariantsToReport;
    }

    public int nVariantsToReport() {
        return nVariantsToReport;
    }

    public String outputPrefix() {
        return outputPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputSettings that = (OutputSettings) o;
        return nVariantsToReport == that.nVariantsToReport &&
                Objects.equals(outputPrefix, that.outputPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputPrefix, nVariantsToReport);
    }

    @Override
    public String toString() {
        return "OutputSettings{" +
                "outputPrefix='" + outputPrefix + '\'' +
                ", nVariantsToReport=" + nVariantsToReport +
                '}';
    }
}
