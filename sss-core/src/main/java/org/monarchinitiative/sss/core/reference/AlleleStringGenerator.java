package org.monarchinitiative.sss.core.reference;

import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.monarchinitiative.sss.core.model.SplicingVariant;
import org.monarchinitiative.sss.core.pwm.SplicingParameters;

/**
 *
 */
public class AlleleStringGenerator {

    private final SplicingParameters splicingParameters;

    public AlleleStringGenerator(SplicingParameters splicingParameters) {
        this.splicingParameters = splicingParameters;
    }

    public String getDonorSiteWithAltAllele(int anchor, SplicingVariant variant, SequenceInterval sequenceInterval) {
        String result; // this method creates the result String in 5' --> 3' direction

        int donorBegin = anchor - splicingParameters.getDonorExonic();
        int donorEnd = anchor + splicingParameters.getDonorIntronic();

        final GenomeCoordinates varCoor = variant.getCoordinates();
        if (donorBegin >= varCoor.getBegin() && varCoor.getEnd() >= donorEnd) {
            // whole donor site is deleted, nothing more to be done here
            return null;
        }

        String alt = variant.getAlt();
        if (varCoor.contains(donorBegin)) {
            // there will be no upstream region, even some nucleotides from before the variantRegion region will be added,
            // if the first positions of donor site are deleted
            int idx = varCoor.getEnd() - donorBegin;
            result = alt.substring(alt.length() - Math.min(idx, alt.length()));
            // there should be already 'idx' nucleotides in 'result' string but there are only 'missing' nucleotides there. Perhaps because the variantRegion is a deletion
            // therefore, we need to add some nucleotides from region before the variantRegion
            int missing = idx - result.length();
            if (missing > 0) {
                int b = varCoor.getBegin() - missing;
                int e = varCoor.getBegin();
                result = sequenceInterval.getSubsequence(b, e) + result;
            }
        } else {
            int upsB = donorBegin;
            int dwnsB = donorBegin + varCoor.getBegin() - donorBegin;
            result = sequenceInterval.getSubsequence(upsB, dwnsB) + alt;
        }
        // add nothing if the sequence is already longer than the SPLICE_DONOR_SITE_LENGTH
        int dwnsBegin = varCoor.getEnd();
        int dwnsEnd = varCoor.getEnd() + Math.max(splicingParameters.getDonorLength() - result.length(), 0);

        result += sequenceInterval.getSubsequence(dwnsBegin, dwnsEnd);
                /* if the variantRegion is a larger insertion, result.length() might be greater than SPLICE_DONOR_SITE_LENGTH after
                   appending 'alt' sequence. We need to make sure only 'SPLICE_DONOR_SITE_LENGTH' nucleotides are returned */
        return result.substring(0, splicingParameters.getDonorLength());
    }
}
