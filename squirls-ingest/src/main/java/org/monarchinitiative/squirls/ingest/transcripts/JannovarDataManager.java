package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JannovarDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JannovarDataManager.class);

    private final Set<JannovarData> jannovarDataSet;

    private JannovarDataManager(Set<JannovarData> jannovarDataSet) {
        this.jannovarDataSet = Set.copyOf(jannovarDataSet);
    }

    /**
     * @param path to directory with `*.ser` files
     * @return jannovar data manager with loaded jannovar data
     */
    public static JannovarDataManager fromDirectory(Path path) {
        final File[] files = path.toFile().listFiles(name -> name.getName().endsWith(".ser"));
        if (files != null) {
            return fromPaths(Arrays.stream(files).map(File::toPath).toArray(Path[]::new));
        } else {
            LOGGER.warn("Path does not point to directory `{}`", path);
            return new JannovarDataManager(Collections.emptySet());
        }
    }

    /**
     * @param paths pointing to jannovar cache `*.ser` files
     * @return instance with loaded Jannovar data
     */
    public static JannovarDataManager fromPaths(Path... paths) {
        /*
        Jannovar data parsing is delegated to Exomiser's `JannovarDataSourceLoader` class.
        This way we handle correctly deserialization of both types of Jannovar cache:
         - Jannovar's native cache
         - Exomiser's improved protobuf-based cache .

        Unfortunately, in order to support this, we have to depend on `exomiser.core` JAR which is not yet
        available in Maven central.
        */
        Set<JannovarData> data = Arrays.stream(paths)
                .map(JannovarDataSourceLoader::loadJannovarData)
                .collect(Collectors.toSet());
        return new JannovarDataManager(data);
    }

    public Collection<TranscriptModel> getAllTranscriptModels() {
        return jannovarDataSet.stream()
                .map(jd -> jd.getTmByAccession().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
