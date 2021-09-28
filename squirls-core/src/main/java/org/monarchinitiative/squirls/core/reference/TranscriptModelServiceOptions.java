package org.monarchinitiative.squirls.core.reference;

import java.util.Objects;

public class TranscriptModelServiceOptions {

    private static final TranscriptModelServiceOptions DEFAULT = of(5);

    private final int maxTxSupportLevel;

    private TranscriptModelServiceOptions(int maxTxSupportLevel) {
        this.maxTxSupportLevel = maxTxSupportLevel;
    }

    /**
     * @return options that include transcripts supported by all transcript support levels.
     */
    public static TranscriptModelServiceOptions defaultOptions() {
        return DEFAULT;
    }

    public static TranscriptModelServiceOptions of(int maxTxSupportLevel) {
        return new TranscriptModelServiceOptions(maxTxSupportLevel);
    }

    public int maxTxSupportLevel() {
        return maxTxSupportLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptModelServiceOptions that = (TranscriptModelServiceOptions) o;
        return maxTxSupportLevel == that.maxTxSupportLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTxSupportLevel);
    }

    @Override
    public String toString() {
        return "TranscriptModelServiceOptions{" +
                "maxTxSupportLevel=" + maxTxSupportLevel +
                '}';
    }
}
