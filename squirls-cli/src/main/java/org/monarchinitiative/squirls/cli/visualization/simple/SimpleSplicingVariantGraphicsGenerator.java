package org.monarchinitiative.squirls.cli.visualization.simple;

import de.charite.compbio.jannovar.reference.GenomeVariantType;
import org.monarchinitiative.squirls.cli.visualization.AbstractGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.MissingFeatureException;
import org.monarchinitiative.squirls.cli.visualization.VisualizableVariantAllele;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContext;
import org.monarchinitiative.squirls.cli.visualization.selector.VisualizationContextSelector;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The first approach for generating SVG graphics. The class applies a set of rules to generate the best SVG graphics
 * for given variant.
 * <p>
 * The generator only works for SNVs.
 */
public class SimpleSplicingVariantGraphicsGenerator extends AbstractGraphicsGenerator { // TODO: 16. 10. 2020 reimplement

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSplicingVariantGraphicsGenerator.class);

    /**
     * Field used to ensure that we log a missing feature only once.
     */
    private static final AtomicBoolean LOGGED_MISSING_FEATURE = new AtomicBoolean();


    public SimpleSplicingVariantGraphicsGenerator(VmvtGenerator generator,
                                                  SplicingPwmData splicingPwmData,
                                                  VisualizationContextSelector selector,
                                                  GenomeSequenceAccessor genomeSequenceAccessor) {
        super(generator, splicingPwmData, selector, genomeSequenceAccessor);
    }

    @Override
    public String generateGraphics(VisualizableVariantAllele variant) {
        if (!variant.variantAnnotations().getGenomeVariant().getType().equals(GenomeVariantType.SNV)) {
            // this class only supports SNVs
            return EMPTY_SVG_IMAGE;
        }

        /*
        Select the prediction data that we use to create the SVG. This is the data that corresponds to transcript with
        respect to which the variant has the maximum predicted pathogenicity.
         */
        SplicingPredictionData predictionData = variant.getPrimaryPrediction();
        if (predictionData == null) {
            LOGGER.debug("Unable to find transcript with maximum pathogenicity score for variant `{}`",
                    variant.variantAnnotations().getGenomeVariant());
            return EMPTY_SVG_IMAGE;
        }

        try {
            VisualizationContext ctx = contextSelector.selectContext(predictionData.getFeatureMap());
            switch (ctx) {
                case CANONICAL_DONOR:
                    return makeCanonicalDonorContextGraphics(predictionData);
                case CANONICAL_ACCEPTOR:
                    return makeCanonicalAcceptorContextGraphics(predictionData);
                case CRYPTIC_DONOR:
                    return makeCrypticDonorContextGraphics(predictionData);
                case CRYPTIC_ACCEPTOR:
                    return makeCrypticAcceptorContextGraphics(predictionData);
                case SRE:
                    return makeSreContextGraphics(predictionData);
                case UNKNOWN:
                default:
            }
        } catch (MissingFeatureException e) {
            if (LOGGED_MISSING_FEATURE.compareAndSet(false, true)) {
                LOGGER.warn("{} : {}", e.getMessage(), variant.variantAnnotations().getGenomeVariant());
            }
        }

        return EMPTY_SVG_IMAGE;
    }
}
