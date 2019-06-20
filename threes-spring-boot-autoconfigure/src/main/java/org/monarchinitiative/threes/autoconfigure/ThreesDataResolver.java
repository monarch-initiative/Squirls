package org.monarchinitiative.threes.autoconfigure;

import java.nio.file.Path;

/**
 * This class provides paths to resources, such as path to FASTA file, or splicing transcript database.
 * <p>
 * The paths are provided based on {@code threesDataDirectory}, {@code dataVersion}, {@code genomeAssembly}, and
 * {@code transcriptSource}.
 */
public class ThreesDataResolver {

    private final Path threesDataDirectory;

    private final String dataVersion;

    private final String genomeAssembly;

    private final String transcriptSource;

    public ThreesDataResolver(Path threesDataDirectory, String dataVersion, String genomeAssembly, String transcriptSource) {
        this.threesDataDirectory = threesDataDirectory;
        this.dataVersion = dataVersion;
        this.genomeAssembly = genomeAssembly;
        this.transcriptSource = transcriptSource;
    }

    public Path genomeFastaPath() {
        return threesDataDirectory.resolve(String.format("%s_%s.fa", dataVersion, genomeAssembly));
    }

    public Path genomeFastaFaiPath() {
        return threesDataDirectory.resolve(String.format("%s_%s.fa.fai", dataVersion, genomeAssembly));
    }

    public Path getDatasourcePath() {
        // the actual suffix *.mv.db is not being added
        return threesDataDirectory.resolve(String.format("%s_%s_splicing_%s", dataVersion, genomeAssembly, transcriptSource))
                .toAbsolutePath();
    }

}
