package org.monarchinitiative.threes.core.scoring.dense;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

abstract class BaseScorer {

    protected final SplicingInformationContentCalculator calculator;
    protected final AlleleGenerator generator;

    BaseScorer(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        this.calculator = calculator;
        this.generator = generator;
    }

    abstract String getName();

    abstract double score(GenomePosition anchor, GenomeVariant variant, SequenceInterval sequenceInterval);
}
