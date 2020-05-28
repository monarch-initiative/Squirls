package org.monarchinitiative.threes.core.scoring;

import org.monarchinitiative.threes.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;

abstract class BaseFeatureCalculator implements FeatureCalculator {

    protected final SplicingInformationContentCalculator calculator;
    protected final AlleleGenerator generator;

    BaseFeatureCalculator(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        this.calculator = calculator;
        this.generator = generator;
    }
}
