package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.SplicingLocationData;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.Annotatable;
import org.monarchinitiative.squirls.core.scoring.SequenceRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param data with variant, transcript, and sequence we calculate the feature for
     * @return <code>1.</code> if variant converts Y to R at -3 position of the canonical acceptor site and
     * <code>0.</code> otherwise. Note that {@link Double#NaN} is returned in case of inconsistent inputs
     * (e.g. insufficient <code>sequence</code>)
     */
    @Override
    public <T extends Annotatable> double score(T data) {
        final GenomeVariant variant = data.getVariant();
        final SplicingTranscript transcript = data.getTranscript();
        final SequenceRegion sequence = data.getTrack(FeatureCalculator.FASTA_TRACK_NAME, SequenceRegion.class);

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

        if (refAcceptorSnippet == null || altAcceptorSnippet == null) {
            // unable to create padded alleles due to insufficient sequence. This should not happen since we fetch
            // the entire sequence of the transcript region +- padding
            return Double.NaN;
        }

        // snippets are converted to upper case to simplify pattern matching
        return RAG.matcher(refAcceptorSnippet.toUpperCase()).matches() ^ RAG.matcher(altAcceptorSnippet.toUpperCase()).matches()
                ? 1.
                : 0.;
    }
}
