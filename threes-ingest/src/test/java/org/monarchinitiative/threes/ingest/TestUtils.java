package org.monarchinitiative.threes.ingest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class TestUtils {

    private TestUtils() {
        // private no-op, static utility class
    }

    public static void deleteFolderAndFiles(Path folder) throws IOException {
        Boolean deletedAll = Arrays.stream(Objects.requireNonNull(folder.toFile().listFiles()))
                .map(File::delete)
                .reduce(Boolean::logicalAnd)
                .orElse(true); // no file is present in the `folder`
        if (!deletedAll) {
            throw new RuntimeException(String.format("Could not delete all the files in '%s'", folder));
        }
        Files.delete(folder);
    }
}
