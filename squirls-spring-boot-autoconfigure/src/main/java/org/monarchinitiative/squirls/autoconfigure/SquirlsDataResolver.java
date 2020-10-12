package org.monarchinitiative.squirls.autoconfigure;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class provides paths to resources, such as path to FASTA file, or splicing transcript database.
 * <p>
 * The paths are provided based on {@code squirlsDataDirectory}, {@code dataVersion}, and {@code genomeAssembly}.
 */
public class SquirlsDataResolver {

    private final Path squirlsDataDirectory;

    private final String dataVersion;

    private final String genomeAssembly;

    public SquirlsDataResolver(Path squirlsDataDirectory, String dataVersion, String genomeAssembly) throws MissingSquirlsResourceException {
        this.squirlsDataDirectory = squirlsDataDirectory.resolve(String.format("%s_%s", dataVersion, genomeAssembly));
        this.dataVersion = dataVersion;
        this.genomeAssembly = genomeAssembly;

        // now check that we have all files present
        List<Path> paths = List.of(genomeFastaPath(), genomeFastaFaiPath(), genomeFastaDictPath(), dataSourceFullPath(), phylopPath());
        for (Path path : paths) {
            if (!(Files.isRegularFile(path) && Files.isReadable(path))) {
                throw new MissingSquirlsResourceException(String.format("The file `%s` is missing in SQUIRLS directory", path.toFile().getName()));
            }
        }
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

    public Path dataSourcePath() {
        // the actual suffix *.mv.db is not being added
        return squirlsDataDirectory.resolve(String.format("%s_%s.splicing", dataVersion, genomeAssembly))
                .toAbsolutePath();
    }

    public Path dataSourceFullPath() {
        return squirlsDataDirectory.resolve(String.format("%s_%s.splicing.mv.db", dataVersion, genomeAssembly))
                .toAbsolutePath();
    }

    public Path phylopPath() {
        return squirlsDataDirectory.resolve(String.format("%s_%s.phylop.bw", dataVersion, genomeAssembly))
                .toAbsolutePath();
    }

}
