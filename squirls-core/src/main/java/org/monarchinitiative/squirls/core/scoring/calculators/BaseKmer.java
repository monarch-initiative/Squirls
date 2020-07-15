package org.monarchinitiative.squirls.core.scoring.calculators;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.Utils;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.Map;

public abstract class BaseKmer implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseKmer.class);

    protected final ImmutableMap<String, Double> kmerMap;

    protected BaseKmer(Map<String, Double> kmerMap) {
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
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequence) {
        final GenomeInterval variantInterval = variant.getGenomeInterval();

        final String paddedRefAllele = AlleleGenerator.getPaddedAllele(variantInterval, sequence, variant.getRef(), getPadding());
        final String paddedAltAllele = AlleleGenerator.getPaddedAllele(variantInterval, sequence, variant.getAlt(), getPadding());
        if (paddedRefAllele == null || paddedAltAllele == null) {
            LOGGER.debug("Unable to create neighborhood snippet for variant `{}` using sequence `{}`", variant, sequence.getInterval());
            return Double.NaN;
        }

        double ref = scoreSequence(paddedRefAllele);
        double alt = scoreSequence(paddedAltAllele);
        // subtract total alt from total ref
        // the score should be high if the alt allele abolishes ESE element in ref allele
        return ref - alt;
    }
}
