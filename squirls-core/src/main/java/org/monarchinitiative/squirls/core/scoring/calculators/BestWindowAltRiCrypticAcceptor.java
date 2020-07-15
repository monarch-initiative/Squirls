package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * This class calculates <code>alt_ri_acceptor_best_window</code> feature - maximum individual information observed
 * after applying sliding window to the neighboring sequence.
 */
public class BestWindowAltRiCrypticAcceptor extends BaseFeatureCalculator {

    public BestWindowAltRiCrypticAcceptor(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        super(calculator, generator);
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        // TODO: 14. 7. 2020 implement
        return 0;
    }
}
