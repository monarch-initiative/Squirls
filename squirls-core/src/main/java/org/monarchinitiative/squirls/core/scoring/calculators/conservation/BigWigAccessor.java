package org.monarchinitiative.squirls.core.scoring.calculators.conservation;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BBFileHeader;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BBFileReader;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.BigWigIterator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.bbfile.WigItem;
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
     * Get list of feature values for given {@link GenomeInterval} object.
     *
     * @param genomeInterval {@link GenomeInterval} object to extract values from.
     * @return list of values for the interval
     */
    public List<Float> getScores(GenomeInterval genomeInterval) {
        // the scores are stored for regions on FWD strand
        final GenomeInterval interval = genomeInterval.withStrand(Strand.FWD);

        String contig = interval.getRefDict().getContigIDToName().get(interval.getChr());
        contig = (contig.startsWith("chr")) ? contig : "chr" + contig;
        int begin = interval.getBeginPos(), end = interval.getEndPos();

        return getScores(contig, begin, end);
    }

    /**
     * Get list of feature values for given {@link GenomeInterval} object.
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
     * Get iterator over given genomeInterval. Iterator contains {@link WigItem} container objects and there is no
     * guarantee, that there exist an object for every position in genomeInterval.
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
