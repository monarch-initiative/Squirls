package org.monarchinitiative.threes.core.scoring;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.Utils;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;
import java.util.Optional;

public abstract class KmerFeatureCalculator implements FeatureCalculator {

    protected final ImmutableMap<String, Double> kmerMap;

    protected KmerFeatureCalculator(Map<String, Double> kmerMap) {
        this.kmerMap = ImmutableMap.copyOf(kmerMap);
    }

    /**
     * Calculate score for given nucleotide sequence.
     *
     * @param sequence String with sequence to be scored
     * @return score for the sequence or {@link Double#NaN} if there is invalid nucleotide character present, or if
     * length of the {@code sequence} is less than {@link #getPadding()}+1
     */
    double scoreSequence(String sequence) {
        return Utils.slidingWindow(sequence.toUpperCase(), getPadding() + 1)
                .map(kmer -> kmerMap.getOrDefault(kmer, Double.NaN))
                .reduce(Double::sum)
                .orElse(Double.NaN);
    }

    protected abstract int getPadding();

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        GenomeInterval upstream = new GenomeInterval(variant.getGenomeInterval().getGenomeBeginPos().shifted(-getPadding()), getPadding());
        GenomeInterval downstream = new GenomeInterval(variant.getGenomeInterval().getGenomeEndPos(), getPadding());
        Optional<String> upstreamSequence = sequenceInterval.getSubsequence(upstream);
        Optional<String> downstreamSequence = sequenceInterval.getSubsequence(downstream);
        String paddedRefAllele = upstreamSequence.get() + variant.getRef() + downstreamSequence.get();
        String paddedAltAllele = upstreamSequence.get() + variant.getAlt() + downstreamSequence.get();

        double ref = scoreSequence(paddedRefAllele);
        double alt = scoreSequence(paddedAltAllele);
        // subtract total alt from total ref
        // the score should be high if the alt allele abolishes ESE element in ref allele
        return ref - alt;
    }
}