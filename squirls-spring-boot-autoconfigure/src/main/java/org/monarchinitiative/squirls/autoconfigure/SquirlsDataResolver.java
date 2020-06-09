package org.monarchinitiative.squirls.autoconfigure;

import java.nio.file.Path;

/**
 * This class provides paths to resources, such as path to FASTA file, or splicing transcript database.
 * <p>
 * The paths are provided based on {@code squirlsDataDirectory}, {@code dataVersion}, and {@code genomeAssembly}.
 */
public class SquirlsDataResolver {

    private final Path squirlsDataDirectory;

    private final String dataVersion;

    private final String genomeAssembly;

    public SquirlsDataResolver(Path squirlsDataDirectory, String dataVersion, String genomeAssembly) {
        this.squirlsDataDirectory = squirlsDataDirectory.resolve(String.format("%s_%s", dataVersion, genomeAssembly));
        this.dataVersion = dataVersion;
        this.genomeAssembly = genomeAssembly;
    }

    public Path genomeFastaPath() {
        return squirlsDataDirectory.resolve(String.format("%s_%s.fa", dataVersion, genomeAssembly));
    }

    public Path genomeFastaFaiPath() {
        return squirlsDataDirectory.resolve(String.format("%s_%s.fa.fai", dataVersion, genomeAssembly));
    }

    public Path genomeFastaDictPath() {
        return squirlsDataDirectory.resolve(String.format("%s_%s.fa.dict", dataVersion, genomeAssembly));
    }

    public Path getDatasourcePath() {
        // the actual suffix *.mv.db is not being added
        return squirlsDataDirectory.resolve(String.format("%s_%s_splicing", dataVersion, genomeAssembly))
                .toAbsolutePath();
    }

}
