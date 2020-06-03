package org.monarchinitiative.threes.cli;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestDataSourceConfig {


    /**
     * Small Jannovar cache containing RefSeq transcripts of 2 genes only: SURF1 and SURF2.
     *
     * @return {@link JannovarData} cache
     */
    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(TestDataSourceConfig.class.getResource("surf_refseq.ser").getFile()).load();
    }

    @Bean
    public ReferenceDictionary referenceDictionary(JannovarData jannovarData) {
        return jannovarData.getRefDict();
    }
}
