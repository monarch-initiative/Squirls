package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data;

import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.IPresentableVariant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for analysis results, as presented by the `templates/results.html` template.
 */
public class AnalysisResults {

    private final List<String> sampleNames;
    private final SettingsData settingsData;
    private final AnalysisStats analysisStats;
    private final List<? extends IPresentableVariant> variants;

    private AnalysisResults(Builder builder) {
        sampleNames = List.copyOf(builder.sampleNames);
        variants = builder.variants.stream()
                .sorted(Comparator.comparing(IPresentableVariant::getMaxPathogenicity).reversed())
                .collect(Collectors.toList());
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

    public List<? extends IPresentableVariant> getVariants() {
        return variants;
    }

    public static final class Builder {
        private final List<String> sampleNames = new ArrayList<>();
        private Collection<? extends IPresentableVariant> variants;
        private AnalysisStats analysisStats;
        private SettingsData settingsData;

        private Builder() {
        }

        public Builder addAllSampleNames(List<String> sampleIds) {
            this.sampleNames.addAll(sampleIds);
            return this;
        }

        public Builder variants(Collection<? extends IPresentableVariant> variantData) {
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
