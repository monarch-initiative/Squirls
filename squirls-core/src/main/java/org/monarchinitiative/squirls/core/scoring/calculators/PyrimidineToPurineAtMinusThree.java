package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

import java.util.regex.Pattern;

public class PyrimidineToPurineAtMinusThree implements FeatureCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PyrimidineToPurineAtMinusThree.class);

    /**
     * Pattern that matches acceptor snippet if there is a purine at -3 position.
     */
    private static final Pattern RAG = Pattern.compile("^[ACGT]+[AG]AG[ACGT]{2}$");

    private final SplicingTranscriptLocator locator;

    private final AlleleGenerator generator;

    public PyrimidineToPurineAtMinusThree(SplicingTranscriptLocator locator, AlleleGenerator generator) {
        this.locator = locator;
        this.generator = generator;
    }


    /**
     * Calculate feature value for given variant and transcript.
     *
     * @param variant    variant we calculate the feature for
     * @param transcript transcript we evaluate the variant against
     * @param sequence   FASTA sequence for the calculation
     * @return <code>1.</code> if variant converts Y to R at -3 position of the canonical acceptor site and
     * <code>0.</code> otherwise. Note that {@link Double#NaN} is returned in case of inconsistent inputs
     * (e.g. insufficient <code>sequence</code>)
     */
    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        final SplicingLocationData locationData = locator.locate(variant, transcript);

        if (locationData.getPosition() != SplicingLocationData.SplicingPosition.ACCEPTOR) {
            // variant does not affect the acceptor site, therefore does not lead to `..YAG..` -> `..RAG..`
            return 0.;
        }

        if (locationData.getAcceptorBoundary().isEmpty()) {
            // this should not happen since the position has been set to ACCEPTOR but let's be sure!
            LOGGER.warn("Inconsistency - position set to acceptor but the boundary is absent: {} - {}",
                    variant, transcript.getAccessionId());
            return Double.NaN;
        }

        final GenomePosition acceptorBoundary = locationData.getAcceptorBoundary().get();
        final String refAcceptorSnippet = generator.getAcceptorSiteSnippet(acceptorBoundary, sequence);
        final String altAcceptorSnippet = generator.getAcceptorSiteWithAltAllele(acceptorBoundary, variant, sequence);

        // snippets are converted to upper case to simplify pattern matching
        return RAG.matcher(refAcceptorSnippet.toUpperCase()).matches() ^ RAG.matcher(altAcceptorSnippet.toUpperCase()).matches()
                ? 1.
                : 0.;

    }
}
