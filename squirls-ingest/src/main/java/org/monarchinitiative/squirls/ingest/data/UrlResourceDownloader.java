package org.monarchinitiative.squirls.ingest.data;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

/**
 * Download a file from {@link URL} to given location.
 */
public class UrlResourceDownloader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlResourceDownloader.class);

    private final URL source;

    private final Path destination;

    private final boolean overwrite;

    /**
     * @param source      url pointing to the resource file
     * @param destination where to store the file
     * @param overwrite   if <code>true</code>, the file is overwritten if it already exists
     */
    public UrlResourceDownloader(URL source, Path destination, boolean overwrite) {
        this.source = source;
        this.destination = destination;
        this.overwrite = overwrite;
    }

    public UrlResourceDownloader(URL source, Path destination) {
        this(source, destination, true);
    }

    @Override
    public void run() {
        if (!overwrite && destination.toFile().exists()) {
            LOGGER.info("The file already exists at `{}`, skipping download", destination);
            return;
        }

        try {
            LOGGER.info("Downloading resource file from `{}`", source.toExternalForm());
            URLConnection connection = source.openConnection();
            try (InputStream is = connection.getInputStream();
                 FileOutputStream os = new FileOutputStream(destination.toFile())) {
                long downloaded = IOUtils.copyLarge(is, os);
                LOGGER.info("Finished the download, transferred {} kB", String.format("%,.3f", (double) downloaded / 1024));
            }
        } catch (IOException e) {
            LOGGER.warn("Error downloading the resource `{}` to `{}`", source.toExternalForm(), destination, e);
        }
    }
}
