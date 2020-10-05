package org.monarchinitiative.squirls.ingest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The purpose of this class is to create an empty database which can be used with IDE to provide SQL autocompletion
 * and other hints.
 * <p>
 * The database is built at the top level
 */
public class MakeAnEmptyDatabase {

    /**
     * Path to top-level Squirls code-base directory.
     */
    private Path appHomeDir;

    @BeforeEach
    public void setUp() {
        appHomeDir = Path.of(MakeAnEmptyDatabase.class.getResource("/").getPath()).getParent().getParent().getParent();
    }

    @Test
    public void makeDatabase() throws Exception {
        final Path databasePath = appHomeDir.resolve("1710_splicing_empty");
        final Path filePath = appHomeDir.resolve("1710_splicing_empty.mv.db");

        if (filePath.toFile().isFile()) {
            System.err.printf("Removing already existing database file at `%s`%n", filePath);
            Files.delete(filePath);
        }

        System.err.printf("Making an empty database at `%s`%n", databasePath);
        final DataSource dataSource = SquirlsDataBuilder.makeDataSource(databasePath);
        int migrations = SquirlsDataBuilder.applyMigrations(dataSource);
        System.err.printf("Applied %d migrations", migrations);
    }
}
