package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * This class calculates <code>wt_ri_donor</code> feature - individual information of the <em>wt/ref</em> allele of the
 * donor site.
 */
public class WtRiDonor extends BaseFeatureCalculator {


    public WtRiDonor(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        super(calculator, generator);
    }

    @Override
    public double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval) {
        // TODO: 14. 7. 2020 implement
        return 0;
    }
}
