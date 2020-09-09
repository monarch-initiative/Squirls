package org.monarchinitiative.squirls.core.scoring.calculators;

import de.charite.compbio.jannovar.reference.GenomeVariant;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import xyz.ielis.hyperutil.reference.fasta.SequenceInterval;

/**
 * Calculator that returns <code>1.</code> if the variant removes >1 pyrimidines from the poly-pyrimidine tract (PPT)
 * of the splice acceptor site.
 * <p>
 * To simplify things at the beginning, PPT is actually defined to be identical to equal
 */
public class PptIsTruncated extends BaseAgezCalculator {


    public PptIsTruncated(SplicingTranscriptLocator locator, int agezBegin, int agezEnd) {
        super(locator, agezBegin, agezEnd);
    }

    private static int countPyrimidinesInAllele(String allele) {
        int count = 0;
        for (char base : allele.toCharArray()) {
            switch (base) {
                case 'c':
                case 'C':
                case 't':
                case 'T':
                    count++;
                    break;
                default:
                    break;
            }
        }
        return count;
    }

    @Override
    public double score(GenomeVariant variant, SplicingTranscript transcript, SequenceInterval sequence) {
        switch (variant.getType()) {
            case DELETION:
            case BLOCK_SUBSTITUTION: // represents SVs but MNVs as well, and we want to catch MNVs!
                if (overlapsWithAgezRegion(variant, transcript)) {
                    final int refPyrimidines = countPyrimidinesInAllele(variant.getRef());
                    final int altPyrimidines = countPyrimidinesInAllele(variant.getAlt());
                    return refPyrimidines - altPyrimidines >= 2
                            ? 1.
                            : 0.;
                }
            default:
                return 0.;
        }
    }

}
