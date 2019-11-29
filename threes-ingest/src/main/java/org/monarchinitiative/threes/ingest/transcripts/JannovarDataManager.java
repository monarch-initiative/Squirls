package org.monarchinitiative.threes.ingest.transcripts;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
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

    public static JannovarDataManager fromPaths(Path... paths) {
        Set<JannovarData> data = new HashSet<>();
        for (Path path : paths) {
            try {
                final JannovarData jd = new JannovarDataSerializer(path.toFile().getAbsolutePath()).load();
                data.add(jd);
            } catch (SerializationException se) {
                LOGGER.warn("Error deserializing Jannovar data at `{}`", path, se);
            }
        }
        return new JannovarDataManager(data);
    }

    public Collection<TranscriptModel> getAllTranscriptModels() {
        return jannovarDataSet.stream()
                .map(jd -> jd.getTmByAccession().values())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
