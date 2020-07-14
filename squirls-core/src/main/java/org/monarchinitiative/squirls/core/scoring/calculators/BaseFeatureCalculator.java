package org.monarchinitiative.squirls.core.scoring.calculators;

import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;

abstract class BaseFeatureCalculator implements FeatureCalculator {

    protected final SplicingInformationContentCalculator calculator;
    protected final AlleleGenerator generator;

    BaseFeatureCalculator(SplicingInformationContentCalculator calculator, AlleleGenerator generator) {
        this.calculator = calculator;
        this.generator = generator;
    }
}
