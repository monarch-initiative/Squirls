package org.monarchinitiative.squirls.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeInterval;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.monarchinitiative.squirls.core.scoring.Utils.reverseComplement;

public class SequenceRegion implements TrackRegion<byte[]> {

    public static final Charset FASTA_CHARSET = StandardCharsets.US_ASCII;

    /*
    For now, this is just a dumb data container. However, consider replacing SequenceInterval with this class.
     */
    private final GenomeInterval interval;

    /**
     * Array with ASCII-encoded fasta string
     */
    private final byte[] value;

    private SequenceRegion(GenomeInterval interval, byte[] value) {
        this.interval = interval;
        this.value = value;
    }

    public static SequenceRegion of(GenomeInterval interval, byte[] value) {
        return new SequenceRegion(interval, value);
    }

    public static SequenceRegion of(GenomeInterval interval, String value) {
        return new SequenceRegion(interval, value.getBytes(FASTA_CHARSET));
    }

    @Override
    public GenomeInterval getInterval() {
        return interval;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    public Optional<String> getSubsequence(GenomeInterval sub) {
        if (this.interval.contains(sub)) {
            GenomeInterval onStrand = sub.withStrand(this.interval.getStrand());

            int begin = onStrand.getBeginPos() - interval.getBeginPos();
            int end = onStrand.getEndPos() - interval.getBeginPos();

            String seq = new String(Arrays.copyOfRange(value, begin, end));

            return sub.getStrand().equals(this.interval.getStrand())
                    ? Optional.of(seq)
                    : Optional.of(reverseComplement(seq));
        }
        return Optional.empty();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceRegion that = (SequenceRegion) o;
        return Objects.equals(interval, that.interval) &&
                Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(interval);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return "SequenceRegion{" +
                "interval=" + interval +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
