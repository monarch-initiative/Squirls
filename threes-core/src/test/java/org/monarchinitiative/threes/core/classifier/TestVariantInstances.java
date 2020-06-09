package org.monarchinitiative.threes.core.classifier;

import org.monarchinitiative.threes.core.SimpleClassifiable;

import java.util.Map;

/**
 * Variant data being used for integration test of {@link SquirlsClassifier} and its parts.
 */
public class TestVariantInstances {

    private TestVariantInstances() {
        // private no-op
    }

    /**
     * @return feature data corresponding to variant `chr10-14977461-C-T`
     */
    public static Classifiable pathogenicDonor() {
        return makeFeature(1., 8.96000193206808, 1.35819994086722,
                103, 0, -12.3472137246951,
                7.32000017166138, 1.6480094, 1.3925);
    }

    /**
     * @return feature data corresponding to variant `chr10-79769277-C-T`
     */
    public static Classifiable donorCryptic() {
        return makeFeature(18., 0., 2.31361478844132,
                157, 0., -10.4416409363208,
                -0.128999993205071, -1.9681822, -1.8941);
    }


    /**
     * @return feature data corresponding to variant `chr10-95399825-A-G`
     */
    public static Classifiable pathogenicAcceptor() {
        return makeFeature(-149., 0., -7.41811803262481,
                -2., 9.9614496943982, 2.44868393330691,
                5.64499998092651, 1.892903, 1.5277);
    }


    /**
     * @return feature data corresponding to variant `chr17-44087661-T-C`
     */
    public static Classifiable acceptorCryptic() {
        return makeFeature(-108, 0., -17.6901254762917,
                -15., 0.729759824433353, 0.,
                .922999978065491, 0.862883, 1.4614);
    }

    private static Classifiable makeFeature(double donorOffset, double canonicalDonor, double crypticDonor,
                                            double acceptorOffset, double canonicalAcceptor, double crypticAcceptor,
                                            double phylop, double hexamer, double septamer) {
        return new SimpleClassifiable(Map.of(
                "donor_offset", donorOffset,
                "canonical_donor", canonicalDonor,
                "cryptic_donor", crypticDonor,
                "acceptor_offset", acceptorOffset,
                "canonical_acceptor", canonicalAcceptor,
                "cryptic_acceptor", crypticAcceptor,
                "phylop", phylop,
                "hexamer", hexamer,
                "septamer", septamer));
    }
}
