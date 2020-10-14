package org.monarchinitiative.squirls.ingest.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UrlResourceDownloaderTest {

    private File destination;

    @BeforeEach
    public void setUp() {
        final Path parent = Path.of(UrlResourceDownloaderTest.class.getResource(".").getPath());
        this.destination = parent.resolve("copy.txt").toFile();
    }

    @AfterEach
    public void tearDown() {
        if (destination.isFile()) {
            if (!destination.delete()) {
                System.err.println("WHOA!");
            }
        }
    }

    @Test
    public void download() throws Exception {
        URL source = UrlResourceDownloaderTest.class.getResource("funky.txt");
        UrlResourceDownloader downloader = new UrlResourceDownloader(source, destination.toPath());

        assertThat(destination.exists(), is(false));
        downloader.run();
        assertThat(destination.exists(), is(true));
    }
}