package org.monarchinitiative.squirls.initialize;

/**
 * Transcript source to use in the analysis.
 * @deprecated use {@link org.monarchinitiative.squirls.core.config.FeatureSource} instead
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public enum TranscriptSource {
    GENCODE,
    REFSEQ
}
