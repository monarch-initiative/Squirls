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

package org.monarchinitiative.squirls.core.model;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * POJO for transcript data used within 3S codebase.
 */
public class SplicingTranscript {

    public static final String EXON_REGION_CODE = "ex";
    public static final String INTRON_REGION_CODE = "ir";

    private static final SplicingTranscript DEFAULT = SplicingTranscript.builder().build();

    private final GenomeInterval txRegionCoordinates;

    private final String accessionId;

    private final List<SplicingExon> exons;

    private final List<SplicingIntron> introns;

    private SplicingTranscript(Builder builder) {
        txRegionCoordinates = builder.coordinates;
        exons = List.copyOf(builder.exons);
        introns = List.copyOf(builder.introns);
        accessionId = builder.accessionId;
        if (builder.check)
            check();
    }

    public static SplicingTranscript getDefaultInstance() {
        return DEFAULT;
    }

    public static Builder builder() {
        return new Builder();
    }

    private void check() {
        if (!exons.stream().allMatch(e -> e.getInterval().getStrand().equals(txRegionCoordinates.getStrand()))) {
            throw new IllegalArgumentException("All exons are not on transcript's strand");
        }

        if (!introns.stream().allMatch(i -> i.getInterval().getStrand().equals(txRegionCoordinates.getStrand()))) {
            throw new IllegalArgumentException("All introns are not on transcript's strand");
        }

        for (int i = 0; i < exons.size(); i++) {
            GenomeInterval current = exons.get(i).getInterval();
            if (current.getGenomeBeginPos().isGeq(current.getGenomeEndPos())) {
                throw new IllegalArgumentException("Invalid exon " + i + ": begin is not upstream from end");
            }

            if (i > 0) {
                GenomeInterval previous = exons.get(i - 1).getInterval();
                if (previous.getGenomeEndPos().isGeq(current.getGenomeBeginPos())) {
                    throw new IllegalArgumentException("Inconsistent exon " + (i - 1) + ',' + i + " order: " + previous + ", " + current);
                }
            }
        }

        for (int i = 0; i < introns.size(); i++) {
            GenomeInterval current = introns.get(i).getInterval();
            if (current.getGenomeBeginPos().isGeq(current.getGenomeEndPos())) {
                throw new IllegalArgumentException("Invalid intron " + i + ": begin is not upstream from end");
            }

            if (i > 0) {
                GenomeInterval previous = introns.get(i - 1).getInterval();
                if (previous.getGenomeEndPos().isGeq(current.getGenomeBeginPos())) {
                    throw new IllegalArgumentException("Inconsistent exon " + (i - 1) + ',' + i + " order: " + previous + ", " + current);
                }
            }
        }
    }

    public GenomeInterval getTxRegionCoordinates() {
        return txRegionCoordinates;
    }

    public List<SplicingExon> getExons() {
        return exons;
    }

    public List<SplicingIntron> getIntrons() {
        return introns;
    }

    public String getAccessionId() {
        return accessionId;
    }

    public Strand getStrand() {
        return txRegionCoordinates.getStrand();
    }

    public int getChr() {
        return txRegionCoordinates.getChr();
    }

    public String getChrName() {
        return txRegionCoordinates.getRefDict().getContigIDToName().get(txRegionCoordinates.getChr());
    }

    public int getTxBegin() {
        return txRegionCoordinates.getBeginPos();
    }


    public int getTxEnd() {
        return txRegionCoordinates.getEndPos();
    }

    public int getTxLength() {
        return txRegionCoordinates.length();
    }

    @Override
    public String toString() {
        return "SplicingTranscript{" +
                "txRegionCoordinates=" + txRegionCoordinates +
                ", accessionId='" + accessionId + '\'' +
                ", exons=" + exons +
                ", introns=" + introns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplicingTranscript that = (SplicingTranscript) o;
        return Objects.equals(txRegionCoordinates, that.txRegionCoordinates) &&
                Objects.equals(accessionId, that.accessionId) &&
                Objects.equals(exons, that.exons) &&
                Objects.equals(introns, that.introns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txRegionCoordinates, accessionId, exons, introns);
    }

    public static final class Builder {

        private final List<SplicingExon> exons = new ArrayList<>();
        private final List<SplicingIntron> introns = new ArrayList<>();
        private GenomeInterval coordinates;
        private String accessionId = "";
        private boolean check = false;

        private Builder() {
            // private no-op
        }

        public Builder setAccessionId(String accessionId) {
            this.accessionId = accessionId;
            return this;
        }

        public Builder setCoordinates(GenomeInterval coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        public Builder addExon(SplicingExon exon) {
            this.exons.add(exon);
            return this;
        }

        public Builder addAllExons(Collection<SplicingExon> exons) {
            this.exons.addAll(exons);
            return this;
        }

        public Builder addIntron(SplicingIntron intron) {
            this.introns.add(intron);
            return this;
        }

        public Builder addAllIntrons(Collection<SplicingIntron> introns) {
            this.introns.addAll(introns);
            return this;
        }

        public Builder check(boolean check) {
            this.check = check;
            return this;
        }

        public SplicingTranscript build() {
            return new SplicingTranscript(this);
        }
    }
}
