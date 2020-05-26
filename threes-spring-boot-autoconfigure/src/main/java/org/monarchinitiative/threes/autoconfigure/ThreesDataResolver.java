package org.monarchinitiative.threes.autoconfigure;

import java.nio.file.Path;

/**
 * This class provides paths to resources, such as path to FASTA file, or splicing transcript database.
 * <p>
 * The paths are provided based on {@code threesDataDirectory}, {@code dataVersion}, and {@code genomeAssembly}.
 */
public class ThreesDataResolver {

    private final Path threesDataDirectory;

    private final String dataVersion;

    private final String genomeAssembly;

    public ThreesDataResolver(Path threesDataDirectory, String dataVersion, String genomeAssembly) {
        this.threesDataDirectory = threesDataDirectory.resolve(String.format("%s_%s", dataVersion, genomeAssembly));
        this.dataVersion = dataVersion;
        this.genomeAssembly = genomeAssembly;
    }

    public Path genomeFastaPath() {
        return threesDataDirectory.resolve(String.format("%s_%s.fa", dataVersion, genomeAssembly));
    }

    public Path genomeFastaFaiPath() {
        return threesDataDirectory.resolve(String.format("%s_%s.fa.fai", dataVersion, genomeAssembly));
    }

    public Path genomeFastaDictPath() {
        return threesDataDirectory.resolve(String.format("%s_%s.fa.dict", dataVersion, genomeAssembly));
    }

    public Path getDatasourcePath() {
        // the actual suffix *.mv.db is not being added
        return threesDataDirectory.resolve(String.format("%s_%s_splicing", dataVersion, genomeAssembly))
                .toAbsolutePath();
    }

}
