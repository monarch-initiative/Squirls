package org.monarchinitiative.squirls.initialize;

/**
 * Transcript source to use in the analysis.
 * @deprecated use {@link org.monarchinitiative.squirls.core.config.FeatureSource} instead. Enum will be removed
 */
@Deprecated(since = "1.0.1", forRemoval = true)
// TODO(2.0.0) - remove
public enum TranscriptSource {
    GENCODE,
    REFSEQ
}
