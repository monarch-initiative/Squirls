package org.monarchinitiative.threes.core.reference.allele;

import org.monarchinitiative.threes.core.model.*;

/**
 *
 */
public class AlleleGenerator {

    private final SplicingParameters splicingParameters;

    public AlleleGenerator(SplicingParameters splicingParameters) {
        this.splicingParameters = splicingParameters;
    }

    public String getDonorSiteWithAltAllele(int anchor, SplicingVariant variant, SequenceInterval sequenceInterval) {
        String result; // this method creates the result String in 5' --> 3' direction

        final Region donor = makeDonorRegion(anchor);

        final GenomeCoordinates varCoor = variant.getCoordinates();
        if (donor.getBegin() >= varCoor.getBegin() && varCoor.getEnd() >= donor.getEnd()) {
            // whole donor site is deleted, nothing more to be done here
            return null;
        }

        String alt = variant.getAlt();
        if (varCoor.contains(donor.getBegin())) {
            // there will be no upstream region, even some nucleotides from before the variantRegion region will be added,
            // if the first positions of donor site are deleted
            int idx = varCoor.getEnd() - donor.getBegin();
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
            int dwnsB = donor.getBegin() + varCoor.getBegin() - donor.getBegin();
            result = sequenceInterval.getSubsequence(donor.getBegin(), dwnsB) + alt;
        }
        // add nothing if the sequence is already longer than the SPLICE_DONOR_SITE_LENGTH
        int dwnsBegin = varCoor.getEnd();
        int dwnsEnd = varCoor.getEnd() + Math.max(splicingParameters.getDonorLength() - result.length(), 0);

        result += sequenceInterval.getSubsequence(dwnsBegin, dwnsEnd);
                /* if the variantRegion is a larger insertion, result.length() might be greater than SPLICE_DONOR_SITE_LENGTH after
                   appending 'alt' sequence. We need to make sure only 'SPLICE_DONOR_SITE_LENGTH' nucleotides are returned */
        return result.substring(0, splicingParameters.getDonorLength());
    }

    public String getAcceptorSiteWithAltAllele(int anchor, SplicingVariant variant, SequenceInterval sequenceInterval) {
        String result; // this method creates the result String in 3' --> 5' direction

        final Region acceptor = makeAcceptorRegion(anchor);

        final GenomeCoordinates varCoor = variant.getCoordinates();
        if (acceptor.getBegin() >= varCoor.getBegin() && acceptor.getEnd() <= varCoor.getEnd()) {
            // whole acceptor site is deleted, nothing more to be done here
            return null;
        }

        final String alt = variant.getAlt();
        if (varCoor.contains(acceptor.getEnd())) {
            // we should have at least 'idx' nucleotides in result after writing 'alt' but it might be less if there
            // is a deletion. Write 'alt' to result
            int idx = acceptor.getEnd() - varCoor.getBegin();
            result = alt.substring(Math.min(idx, alt.length() - 1));

            int missing = idx - result.length();
            if (missing > 0) {
                result = result + sequenceInterval.getSubsequence(
                        varCoor.getEnd(), varCoor.getEnd() + missing);
            }
        } else {
            result = alt + sequenceInterval.getSubsequence(varCoor.getEnd(), acceptor.getEnd());
        }

        // add nothing if the sequence is already longer than the SPLICE_ACCEPTOR_SITE_LENGTH
        int encore = Math.max(splicingParameters.getAcceptorLength() - result.length(), 0); // running out of English words :)

        result = sequenceInterval.getSubsequence(varCoor.getBegin() - encore, varCoor.getBegin()) + result;
                /* if the variantRegion is a larger insertion, result.length() might be greater than SPLICE_ACCEPTOR_SITE_LENGTH after
                   appending 'alt' sequence. We need to make sure only last 'SPLICE_ACCEPTOR_SITE_LENGTH' nucleotides are returned */
        return result.substring(result.length() - splicingParameters.getAcceptorLength()); // last 'SPLICE_ACCEPTOR_SITE_LENGTH' nucleotides
    }


    public Region makeDonorRegion(SplicingIntron intron) {
        return makeDonorRegion(intron.getBegin());
    }

    public Region makeDonorRegion(SplicingExon exon) {
        return makeDonorRegion(exon.getEnd());
    }

    public Region makeDonorRegion(int anchor) {
        return new Region(anchor - splicingParameters.getDonorExonic(),
                anchor + splicingParameters.getDonorIntronic());
    }

    public Region makeAcceptorRegion(SplicingIntron intron) {
        return makeAcceptorRegion(intron.getEnd());
    }

    public Region makeAcceptorRegion(SplicingExon exon) {
        return makeAcceptorRegion(exon.getBegin());
    }

    public Region makeAcceptorRegion(int anchor) {
        return new Region(anchor - splicingParameters.getAcceptorIntronic(),
                anchor + splicingParameters.getAcceptorExonic());
    }

    public static class Region {

        private final int begin;

        private final int end;

        private Region(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }

        public boolean overlapsWith(int begin, int end) {
            return begin < this.getEnd() && this.getBegin() < end;
        }

        /**
         * Count how many nucleotides are between this region and given <code>begin</code> and <code>end</code> boundaries.
         *
         * @param begin begin boundary
         * @param end   end boundary
         * @return nucleotide count + 1 (regions are adjacent while begin,end is 3'-wise if -1 is returned, or begin,end
         * is 5'wise if 1 is returned
         */
        public int differenceTo(int begin, int end) {
            int bdiff = this.begin - end;
            int ediff = this.end - begin;
            if (bdiff <= 0 && ediff <= 0) {
                return Math.max(bdiff, ediff) - 1;
            } else if (bdiff >= 0 && ediff >= 0) {
                return Math.min(bdiff, ediff) + 1;
            } else {
                // positions overlap
                return 0;
            }
        }

        @Override
        public String toString() {
            return "Region{" + begin +
                    "-" + end +
                    '}';
        }
    }

}
