package org.monarchinitiative.squirls.cli.writers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Container for analysis results, as presented by the `templates/results.html` template.
 */
public class AnalysisResults {

    private final List<String> sampleNames;
    private final SettingsData settingsData;
    private final AnalysisStats analysisStats;
    private final List<? extends WritableSplicingAllele> variants;

    private AnalysisResults(Builder builder) {
        sampleNames = List.copyOf(builder.sampleNames);
        variants = List.copyOf(builder.variants);
        analysisStats = Objects.requireNonNull(builder.analysisStats);
        settingsData = Objects.requireNonNull(builder.settingsData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public AnalysisStats getAnalysisStats() {
        return analysisStats;
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    public List<String> getSampleNames() {
        return sampleNames;
    }

    public List<? extends WritableSplicingAllele> getVariants() {
        return variants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResults that = (AnalysisResults) o;
        return Objects.equals(sampleNames, that.sampleNames) &&
                Objects.equals(settingsData, that.settingsData) &&
                Objects.equals(analysisStats, that.analysisStats) &&
                Objects.equals(variants, that.variants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleNames, settingsData, analysisStats, variants);
    }

    @Override
    public String toString() {
        return "AnalysisResults{" +
                "sampleNames=" + sampleNames +
                ", settingsData=" + settingsData +
                ", analysisStats=" + analysisStats +
                ", variants=" + variants +
                '}';
    }

    public static final class Builder {
        private final List<String> sampleNames = new ArrayList<>();
        private Collection<? extends WritableSplicingAllele> variants;
        private AnalysisStats analysisStats;
        private SettingsData settingsData;

        private Builder() {
        }

        public Builder addAllSampleNames(List<String> sampleIds) {
            this.sampleNames.addAll(sampleIds);
            return this;
        }

        public Builder variants(Collection<? extends WritableSplicingAllele> variantData) {
            this.variants = variantData;
            return this;
        }

        public Builder settingsData(SettingsData settingsData) {
            this.settingsData = settingsData;
            return this;
        }

        public Builder analysisStats(AnalysisStats analysisStats) {
            this.analysisStats = analysisStats;
            return this;
        }

        public AnalysisResults build() {
            return new AnalysisResults(this);
        }
    }
}
