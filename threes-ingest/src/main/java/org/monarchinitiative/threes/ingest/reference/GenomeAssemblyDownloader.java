package org.monarchinitiative.threes.ingest.reference;

import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class downloads tar archive from given url, extracts chromosomes stored as tar entries into a single FASTA file,
 * and creates an index in the end.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public final class GenomeAssemblyDownloader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenomeAssemblyDownloader.class);

    private static final int BUFFER_SIZE = 81920;

    private final URL genomeUrl;

    private final Path whereToSave;

    private final boolean overwrite;

    public GenomeAssemblyDownloader(URL genomeUrl, Path whereToSave, boolean overwrite) {
        this.genomeUrl = genomeUrl;
        this.whereToSave = whereToSave;
        this.overwrite = overwrite;
    }


    /**
     * Download a file from {@link URL} to given location.
     *
     * @param whereToSave {@link File} path where the file will be downloaded
     * @throws IOException if error occurs
     */
    private void download(File whereToSave) throws IOException {
        LOGGER.info("Downloading reference genome file from '{}'", genomeUrl.toExternalForm());

        URLConnection connection = genomeUrl.openConnection();
        try (FileOutputStream writer = new FileOutputStream(whereToSave);
             InputStream reader = connection.getInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
            }
        }
    }


    /**
     * Download the genome tar.gz file, concatenate all chromosomes into a single gzipped file, index the file.
     */
    @Override
    public void run() {
        if (Files.exists(whereToSave) && !overwrite) {
            LOGGER.info("Skipping download since reference genome FASTA already exists at '{}'", whereToSave);
            return;
        }
        try {
            // download genome tar.gz archive into a temporary location
            File genomeTarGz = File.createTempFile("threes-genome-downloader", ".tar.gz");
            genomeTarGz.deleteOnExit();
            download(genomeTarGz);

            // concatenate all the files in the tar.gz archive into a single FASTA file
            try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(genomeTarGz)));
                 OutputStream os = Files.newOutputStream(whereToSave)) {

                LOGGER.info("Concatenating chromosomes from {} into a single FASTA file {}",
                        genomeTarGz.getAbsolutePath(), whereToSave);
                TarArchiveEntry tarEntry = tarInput.getNextTarEntry();

                while (tarEntry != null) {
                    LOGGER.info("Appending chromosome {}", tarEntry.getName());
                    IOUtils.copy(tarInput, os);
                    tarEntry = tarInput.getNextTarEntry();
                }
            }

            // create fasta index for the FASTA file
            LOGGER.info("Indexing FASTA file {}", whereToSave);
            FastaSequenceIndexCreator.create(whereToSave, true);
        } catch (IOException e) {
            LOGGER.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }
}