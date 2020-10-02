/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */
package org.monarchinitiative.squirls.ingest.conservation.bbfile;

import org.monarchinitiative.squirls.ingest.conservation.util.SeekableFileStream;
import org.monarchinitiative.squirls.ingest.conservation.util.SeekableStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Broad Institute Interactive Genome Viewer Big Binary File (BBFile) Reader
 * -   File reader for UCSC BigWig and BigBed file types.
 * <p>
 * Notes:   Table entries refer to Jim Kent of UCSC's document description:
 * "BigWig and BigBed: Enabling Browsing of Large Distributed Data Sets",
 * November 2009.
 * <p>
 * The overall binary file layout is defined in Table B of the document.
 * <p>
 * BBFile Reader sequences through this binary file layout:
 * <p>
 * 1) Reads in BBFile Header Table C and determine if file is a valid Big Bed or Big Wig
 * binary file type.
 * <p>
 * 2) Reads in Zoom Header Tables D if zoom data is present, as defined by zoomLevels
 * in Table C, one for each zoom level.
 * <p>
 * 3) Reads in  the AutoSQL block Table B if present, as referenced by autoSqlOffset in Table C.
 * <p>
 * 4) Reads in Total Summary Block Table DD if present, as referenced by
 * TotalSummaryOffset in Table C.
 * <p>
 * 5) Reads in B+ Tree Header Chromosome Index Header Table E, as referenced
 * by chromosomeTreeOffset in Table C.
 * <p>
 * 6)Reads in B+ Tree Nodes indexing mChromosome ID's for mChromosome regions;
 * Table F for node type (leaf/child), Table G for leaf items,
 * Table H for child node items.
 * <p>
 * 7) Reads in R+ Tree Chromosome ID Index Header Table K.
 * <p>
 * 8) Reads in R+ Tree Nodes indexing of data arranged by mChromosome ID's;
 * Table L for node type (leaf or child), Table M for leaf items,
 * Table N for child node items.
 * <p>
 * 9) Verifies Data Count of data records, as referenced by fullDataOffset in Table C
 * <p>
 * 10) References data count records of data size defined in Table M of R+ Tree index
 * for all leaf items in the tree.
 * <p>
 * 11) Reads in zoom level format Table O for each zoom level comprised of
 * zoom record count followed by that many Table P zoom statistics records,
 * followed by an R+ tree of zoom data locations indexed as in Tables L, M, and N.
 * <p>
 * 12) Returns information on chromosome name keys and chromosome data regions.
 * <p>
 * 13) Provides iterators using chromosome names and data regions to extract
 * zoom data, Wig data, and Bed data.
 */
public class BBFileReader implements AutoCloseable {

    public static final long BBFILE_HEADER_OFFSET = 0;

    private static final Logger log = LoggerFactory.getLogger(BBFileReader.class);

    // Defines the Big Binary File (BBFile) access
    private final SeekableStream fis;      // BBFile input stream handle

    private final BBFileHeader fileHeader; // Big Binary file header
    private final boolean isLowToHigh;       // BBFile binary data format: low to high or high to low

    private BPTree chromosomeIDTree;     // Container for the mChromosome index B+ tree

    private RPTree chromosomeDataTree;     // Container for the mChromosome data R+ tree


    public BBFileReader(Path path) throws IOException {
        this(path, new SeekableFileStream(path.toFile()));

    }

    public BBFileReader(Path path, SeekableStream stream) {
        log.debug("Opening BBFile source  " + path);
        fis = stream;

        // read in file header
        // file offset for next item to be read
        long fileOffset = BBFILE_HEADER_OFFSET;
        fileHeader = new BBFileHeader(path, fis, fileOffset);
        //fileHeader.print();

        if (!fileHeader.isHeaderOK()) {
            log.error("BBFile header is unrecognized type, header magic = " +
                    fileHeader.getMagic());
            throw new RuntimeException("Error reading BBFile header for: " + path);
        }

        // get data characteristics
        isLowToHigh = fileHeader.isLowToHigh();
        // buffer byte size for data decompression; 0 for uncompressed
        int uncompressBufSize = fileHeader.getUncompressBuffSize();

        // get Chromosome Data B+ Tree (Table E, F, G, H) : should always exist
        // B+ tree
        // file offset to mChromosome index B+ tree
        long chromIDTreeOffset = fileHeader.getChromosomeTreeOffset();
        if (chromIDTreeOffset != 0) {
            fileOffset = chromIDTreeOffset;
            chromosomeIDTree = new BPTree(fis, fileOffset, isLowToHigh);
        }

        // get R+ chromosome data location tree (Tables K, L, M, N)
        // R+ tree
        // file offset to mChromosome data R+ tree
        long chromDataTreeOffset = fileHeader.getFullIndexOffset();
        if (chromDataTreeOffset != 0) {
            fileOffset = chromDataTreeOffset;
            boolean forceDescend = false;
            chromosomeDataTree = new RPTree(fis, fileOffset, isLowToHigh, uncompressBufSize, forceDescend);
        }
    }


    /*
     *   Method returns the Big Binary File header which identifies
     *   the file type and content.
     *
     *   Returns:
     *       Big Binary File header (Table C)
     * */

    public BBFileHeader getBBFileHeader() {
        return fileHeader;
    }

    /*
     *   Method returns if the Big Binary File is BigWig
     *
     *   Returns:
     *       Boolean identifies if Big Binary File is BigWig
     *       (recognized from magic number in file header Table C)
     * */

    public boolean isBigWigFile() {
        return fileHeader.isBigWig();
    }

    /*
     *   Method returns if the Big Binary File is written with a low to high byte
     *   order for formatted data.
     *
     *   Returns:
     *       Boolean identifies if Big Binary File is low to high byte order
     *       (recognized from magic number in file header Table C); else is
     *       high to low byte order if false.
     */
    public boolean isLowToHigh() {
        return isLowToHigh;
    }


    /**
     * Returns an iterator for BigWig values which occupy the specified startChromosome region.
     * <p/>
     * Note: the BBFile type should be BigWig; else a null iterator is returned.
     * <p/>
     * Parameters:
     * startChromosome  - name of start chromosome
     * startBase    - starting base position for features
     * endChromosome  - name of end chromosome
     * endBase      - ending base position for feature
     * contained    - flag specifies bed features must be contained in the specified
     * base region if true; else can intersect the region if false
     * <p/>
     * Returns:
     * Iterator to provide BedFeature(s) for the requested chromosome region.
     * Error conditions:
     * 1) An empty iterator is returned if region has no data available
     * 2) A null object is returned if the file is not BigWig.(see isBigWigFile method)
     */
    public BigWigIterator getBigWigIterator(String startChromosome, int startBase,
                                            String endChromosome, int endBase, boolean contained) {


        if (!isBigWigFile())
            return null;

        // go from chromosome names to chromosome ID region
        RPChromosomeRegion selectionRegion = getChromosomeBounds(startChromosome, startBase, endChromosome, endBase);

        // check for valid selection region, return empty iterator if null
        if (selectionRegion == null)
            return new BigWigIterator();

        // compose an iterator
        return new BigWigIterator(fis, chromosomeIDTree, chromosomeDataTree, selectionRegion, contained);
    }


    /*
     *   Method generates a chromosome bounds region for the supplied chromosome region name.
     *
     *   Note: No attempt is made to verify the region exists in the file data, nor
     *   which data is being examined.
     *
     *   Parameters:
     *       startChromosome - name of start chromosome
     *       startBase - starting base position for region
     *       endChromosome - name of end chromosome
     *       endBase - ending base position for region
     *
     *   Returns:
     *       Chromosome bounds of a named chromosome region for data extraction;
     *       or null for regions not found in the B+ chromosome index tree.
     * */
    private RPChromosomeRegion getChromosomeBounds(String startChromosome, int startBase,
                                                   String endChromosome, int endBase) {

        // If the chromosome name length is > the key size we can't distinguish it
        if (startChromosome.length() > chromosomeIDTree.getKeySize()) {
            return null;
        }

        // find the chromosome ID's using the name to get a valid name key, then associated ID
        String startChromKey = chromosomeIDTree.getChromosomeKey(startChromosome);
        int startChromID = chromosomeIDTree.getChromosomeID(startChromKey);
        if (startChromID < 0)       // mChromosome not in data?
            return null;

        String endChromKey = chromosomeIDTree.getChromosomeKey(endChromosome);
        int endChromID = chromosomeIDTree.getChromosomeID(endChromKey);
        if (endChromID < 0)       // mChromosome not in data?
            return null;

        // create the bounding mChromosome region
        return new RPChromosomeRegion(startChromID, startBase,
                endChromID, endBase);
    }

    /*
     *   Method reads data count which heads the data section of the BBFile.
     *
     *   Returns:
     *       Data count of the number of data records:
     *          number of Bed features for BigBed
     *          number of Wig sections for BigWig
     * */

    @Override
    public void close() throws Exception {
        fis.close();
    }
} // end of BBFileReader
