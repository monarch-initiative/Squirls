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

package org.monarchinitiative.squirls.core.scoring.calculators.conservation;

import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BBFileHeader;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BBFileReader;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BigWigIterator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.WigItem;
import org.monarchinitiative.variant.api.GenomicRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class designed to be used for extraction of features' values from BigWig files such as genomic position conservation
 * scores.
 * <p>
 * The bigwig parser developed at Broad Institute is used under the hood.
 * <p>
 * Created by Daniel Danis on 5/16/17.
 */
public class BigWigAccessor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigWigAccessor.class);

    private final Path bigWigPath;

    /**
     * Reader that will be used to access BigWig data.
     */
    private final BBFileReader reader;

    /**
     * Create Dao for BigWig file on given path. Initialize and perform sanity checks provided by BigWig parser:
     * <ul>
     *     <li>check that the header is OK</li>
     *     <li>check that the file actually is in BigWig format</li>
     * </ul>
     *
     * @param bigWigPath path to file that should be opened.
     * @throws IOException if there is problem during opening of file
     */
    public BigWigAccessor(Path bigWigPath) throws IOException {
        this.bigWigPath = bigWigPath;
        this.reader = new BBFileReader(bigWigPath);
        BBFileHeader header = reader.getBBFileHeader();

        // sanity checks
        if (!header.isHeaderOK()) {
            throw new IOException(String.format("Bad header for %s", bigWigPath));
        }
        if (!header.isBigWig()) {
            throw new IOException(String.format("File %s is not BigWig file!", bigWigPath));
        }
    }

    /**
     * Get list of feature values for given {@link GenomicRegion} object.
     *
     * @param region {@link GenomicRegion} object to extract values from.
     * @return list of values for the interval
     */
    public List<Float> getScores(GenomicRegion region) {
        // the scores are stored for regions on FWD strand
        GenomicRegion interval = region.toPositiveStrand().toZeroBased();

        String contig = interval.contigName();
        contig = (contig.startsWith("chr")) ? contig : "chr" + contig;
        int begin = interval.start(), end = interval.end();

        return getScores(contig, begin, end);
    }

    /**
     * Get list of feature values for given {@link GenomicRegion} object.
     *
     * @param chrom chromosome name, e.g. `chrX`
     * @param begin 0-based (excluded) begin coordinate on FWD strand
     * @param end   0-based (included) end coordinate on FWD strand
     * @return list of values for the interval
     */
    public List<Float> getScores(String chrom, int begin, int end) {
        final int length = end - begin;

        // pre-fill the list with NaNs
        final List<Float> scores = new ArrayList<>(length);
        IntStream.range(0, length).forEach(i -> scores.add(Float.NaN));

        synchronized (this) {
            /*
             We need to make this method synchronized, since the code within `bbfile` sub-package is not thread-safe.
             Ask me how I know.. ;)
             */
            final BigWigIterator iterator = getIterator(chrom, begin, end);
            while (iterator.hasNext()) {
                final WigItem item = iterator.next();
                final int startBase = item.getStartBase();
                final float value = item.getWigValue();
                int idx = startBase - begin;
                scores.set(idx, value);
            }
        }
        return scores;
    }

    /**
     * Get iterator over given GenomicRegion. Iterator contains {@link WigItem} container objects and there is no
     * guarantee, that there exist an object for every position in GenomicRegion.
     *
     * @param chrom chromosome string
     * @param start 0-based (excluded)
     * @param end   0-based (included)
     * @return iterator
     */
    private BigWigIterator getIterator(String chrom, int start, int end) {
        /*
         If true, features must be fully contained by selection region. Frankly, I don't know what this means, it is
         written in library.
         */
        final boolean contained = true;
        return reader.getBigWigIterator(chrom, start, chrom, end, contained);
    }


    /**
     * Close the underlying reader.
     */
    @Override
    public void close() throws Exception {
        try {
            reader.close();
        } catch (Exception e) {
            LOGGER.warn("Error closing the bigWig file at `{}`", bigWigPath);
            throw e;
        }
    }
}
