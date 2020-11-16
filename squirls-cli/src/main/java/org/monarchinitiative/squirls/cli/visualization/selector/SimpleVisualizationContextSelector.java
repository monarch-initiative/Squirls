package org.monarchinitiative.squirls.cli.visualization.selector;

import org.monarchinitiative.squirls.cli.visualization.MissingFeatureException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleVisualizationContextSelector implements VisualizationContextSelector {


    /**
     * By default, two double precision numbers must be this close in order to consider them to be equal.
     */
    private static final double EQUALITY_TOLERANCE = 1e-9;


    private static boolean notCloseToZero(double first) {
        return !(Math.abs(first - 0.) < EQUALITY_TOLERANCE);
    }

    /**
     * Figure out which figures to create for this variant.
     *
     * @param prediction data regarding variant prediction
     * @return {@link VisualizationContext} for the variant
     */
    @Override
    public VisualizationContext selectContext(Map<String, Double> prediction) throws MissingFeatureException {
        final Set<String> requiredFeatures = Set.of(
                "canonical_donor", "cryptic_donor",
                "canonical_acceptor", "cryptic_acceptor",
                "donor_offset", "acceptor_offset");

        // check that we have all the necessary features
        if (!prediction.keySet().containsAll(requiredFeatures)) {
            final String missingFeatures = requiredFeatures.stream()
                    .filter(f -> !prediction.containsKey(f))
                    .sorted()
                    .collect(Collectors.joining(",", "[", "]"));
            throw new MissingFeatureException(String.format("Missing features for deciding context: %s", missingFeatures));
        }

        final double canonicalDonor = prediction.get("canonical_donor");
        final double crypticDonor = prediction.get("cryptic_donor");
        if (notCloseToZero(canonicalDonor)) {
            // variant overlaps with the canonical donor site
            // decide between canonical vs cryptic
            return crypticDonor > canonicalDonor
                    ? VisualizationContext.CRYPTIC_DONOR
                    : VisualizationContext.CANONICAL_DONOR;
        }

        final double canonicalAcceptor = prediction.get("canonical_acceptor");
        final double crypticAcceptor = prediction.get("cryptic_acceptor");
        if (notCloseToZero(canonicalAcceptor)) {
            // variant overlaps with the canonical acceptor site
            // decide between canonical vs cryptic
            return crypticAcceptor > canonicalAcceptor
                    ? VisualizationContext.CRYPTIC_ACCEPTOR
                    : VisualizationContext.CANONICAL_ACCEPTOR;
        }

        // variant is coding if donor offset is negative AND acceptor offset is positive
        if (prediction.get("donor_offset") < 0
                && prediction.get("acceptor_offset") > 0) {
            // show SRE if both cryptic features are negative
            // (the cryptic site is not better in comparison with the canonical)
            if (crypticDonor < 0. && crypticAcceptor < 0.) {
                return VisualizationContext.SRE;
            }
        }

        return crypticDonor > crypticAcceptor
                ? VisualizationContext.CRYPTIC_DONOR
                : VisualizationContext.CRYPTIC_ACCEPTOR;
    }


}