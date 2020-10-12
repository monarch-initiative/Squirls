package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
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
                .map(loadJannovarData())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return new JannovarDataManager(data);
    }

    private static Function<Path, Optional<JannovarData>> loadJannovarData() {
        return path -> {
            try {
                return Optional.ofNullable(new JannovarDataSerializer(path.toString()).load());
            } catch (SerializationException e) {
                LOGGER.warn("Error when deserializing jannovar data at `{}`: {}", path, e.getMessage());
                return Optional.empty();
            }
        };
    }

    public Collection<TranscriptModel> getAllTranscriptModels() {
        return jannovarDataSet.stream()
                .map(jd -> jd.getTmByAccession().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
