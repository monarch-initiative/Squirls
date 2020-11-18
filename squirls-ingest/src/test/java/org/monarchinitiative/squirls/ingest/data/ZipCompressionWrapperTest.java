package org.monarchinitiative.squirls.ingest.data;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ZipCompressionWrapperTest {

    private static final Path TMP_DIR = Paths.get(ZipCompressionWrapperTest.class.getResource("").getPath());

    private File zipPath;

    @BeforeEach
    public void setUp() {
        zipPath = TMP_DIR.resolve("test.zip").toFile();
    }

    @AfterEach
    public void tearDown() {
        if (zipPath.isFile()) {
            if (!zipPath.delete()) {
                System.err.println("What the ...");
            }
        }
    }

    @Test
    public void addResource() throws Exception {
        File funkyFile = new File(ZipCompressionWrapperTest.class.getResource("funky.txt").getFile());

        try (ZipCompressionWrapper compressor = new ZipCompressionWrapper(zipPath)) {
            compressor.addResource(funkyFile, "something.txt");
            compressor.addResource(funkyFile, "anything.txt");
        }

        assertThat(zipPath.isFile(), is(true));

        List<String> names = new ArrayList<>();
        List<Long> sizes = new ArrayList<>();

        try (ArchiveInputStream is = new ZipArchiveInputStream(Files.newInputStream(zipPath.toPath()))) {
            ArchiveEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                names.add(entry.getName());
                sizes.add(entry.getSize());
            }
        }
        assertThat(names, hasSize(2));
        assertThat(names, hasItems("something.txt", "anything.txt"));
        assertThat(sizes, hasSize(2));
        assertThat(sizes, hasItems(55L, 55L));
    }
}