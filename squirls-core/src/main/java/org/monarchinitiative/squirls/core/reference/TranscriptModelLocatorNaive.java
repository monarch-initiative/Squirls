/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.core.reference;

import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicRegion;

/**
 * This class figures out where exactly the variant is located with respect to given <code>transcript</code>.
 * <p>
 * The variant is considered as:
 * <ul>
 * <li>{@link SplicingLocationData.SplicingPosition#DONOR} if the variant overlaps with any <em>donor</em> site of
 * the <code>transcript</code></li>
 * <li>{@link SplicingLocationData.SplicingPosition#ACCEPTOR} if the variant overlaps with any <em>acceptor</em> site
 * of the <code>transcript</code></li>
 * <li>{@link SplicingLocationData.SplicingPosition#EXON} if the variant is located within an <em>exon</em></li>
 * <li>{@link SplicingLocationData.SplicingPosition#INTRON} if ... you get it</li>
 * </ul>
 * </p>
 * @author Daniel Danis
 */
public class TranscriptModelLocatorNaive implements TranscriptModelLocator {

    private final SplicingParameters parameters;


    public TranscriptModelLocatorNaive(SplicingParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public SplicingLocationData locate(GenomicRegion variant, Transcript transcript) {
        // variant and transcript must be present on the same contig
        if (variant.contigId() != transcript.contigId()) {
            return SplicingLocationData.outside();
        }


        // return outside if variant does not intersect with transcript
        if (!transcript.location().overlapsWith(variant)) {
            return SplicingLocationData.outside();
        }

        // adjust variant's strand and the coordinate system
        variant = variant.withCoordinateSystem(transcript.coordinateSystem()).withStrand(transcript.strand());

        int n_introns = transcript.introns().size();
        int n_exons = transcript.exons().size();
        assert n_introns == n_exons - 1;
        SplicingLocationData.Builder locationData = SplicingLocationData.builder();

        // is this a single exon gene?
        if (n_exons == 1) {
            // nothing more to be solved, variant intersects with transcript as checked above.
            // SplicingPosition must be EXON
            return locationData
                    .setExonIndex(0)
                    .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                    .build();
        }

        // we iterate through INTRONS
        for (int i = 0; i < n_introns; i++) {
            Coordinates exon = transcript.exons().get(i);
            Coordinates intron = transcript.introns().get(i);
            Coordinates nextExon = transcript.exons().get(i + 1);

            GenomicRegion donor = parameters.makeDonorRegion(transcript.contig(), transcript.strand(), exon);
            GenomicRegion acceptor = parameters.makeAcceptorRegion(transcript.contig(), transcript.strand(), nextExon);

            // 1 - does the variant overlap with the donor site?
            if (donor.overlapsWith(variant)) {
                if (i != 0)
                    // this is not the first exon, so set the acceptor site of this exon as the acceptor location
                    locationData.setAcceptorRegion(parameters.makeAcceptorRegion(transcript.contig(), transcript.strand(), exon));

                return locationData.setSplicingPosition(SplicingLocationData.SplicingPosition.DONOR)
                        .setDonorRegion(donor)
                        .setIntronIndex(i)
                        .setExonIndex(i)
                        .build();
            }

            // 2 - does the variant overlap with the acceptor site?
            if (acceptor.overlapsWith(variant)) {
                locationData.setSplicingPosition(SplicingLocationData.SplicingPosition.ACCEPTOR)
                        .setAcceptorRegion(acceptor)
                        .setIntronIndex(i)
                        .setExonIndex(i + 1);
                if (i != n_introns - 1) {
                    // we are not processing the last intron. If this is not the acceptor site of the last intron,
                    // then we have a splice donor site
                    locationData.setDonorRegion(parameters.makeDonorRegion(transcript.contig(), transcript.strand(), nextExon));
                }
                // We iterate through introns so the current `donor` defined above is the donor of the previous exon.
                // Let's use the donor of the current exon!
                return locationData.build();
            }

            // 3 - does the variant overlap with the current intron?
            if (intron.overlaps(variant.coordinates())) {
                return locationData
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.INTRON)
                        .setDonorRegion(donor)
                        .setAcceptorRegion(acceptor)
                        .setIntronIndex(i)
                        .build();
            }

            // 4 - does the variant overlap with the current exon?
            if (exon.overlaps(variant.coordinates())) {
                locationData
                        .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                        .setDonorRegion(donor)
                        .setExonIndex(i);
                if (i != 0) {
                    // we're not processing the first exon, so we have the acceptor site
                    locationData.setAcceptorRegion(parameters.makeAcceptorRegion(transcript.contig(), transcript.strand(), exon));
                }
                return locationData.build();
            }
        }

        // For transcript with x exons we processed x-1 exons and x introns above. There is no overlap with previous
        // exons/introns/canonical splice sites if we get here. Since the variant overlaps with the transcript, it
        // has to overlap with the last exon.

        // the last exon does not have the donor site, hence not setting the donor boundary
        int lastExonIdx = n_exons - 1;
        Coordinates lastExon = transcript.exons().get(lastExonIdx);
        return locationData
                .setSplicingPosition(SplicingLocationData.SplicingPosition.EXON)
                .setExonIndex(lastExonIdx)
                .setAcceptorRegion(parameters.makeAcceptorRegion(transcript.contig(), transcript.strand(), lastExon))
                .build();
    }
}
