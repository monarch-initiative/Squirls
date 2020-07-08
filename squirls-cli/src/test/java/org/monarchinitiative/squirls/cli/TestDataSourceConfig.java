package org.monarchinitiative.squirls.cli;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestDataSourceConfig {


    /**
     * Small Jannovar cache containing RefSeq transcripts of several genes only:
     * <ul>
     *     <li>SURF1</li>
     *     <li>SURF2</li>
     *     <li>ALPL</li>
     *     <li>TSC2</li>
     *     <li>COL4A5</li>
     *     <li>CYP17A1</li>
     *     <li>HBB</li>
     *     <li>NF1</li>
     *     <li>CFTR</li>
     *     <li>BRCA2</li>
     *     <li>VWF</li>
     *     <li>RYR1</li>
     * </ul>
     *
     * <p>
     *     The small cache was created from Jannovar v0.29 refseq cache using Jannovar Sieve app.
     * </p>
     *
     * @return {@link JannovarData} cache
     */
    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(TestDataSourceConfig.class.getResource("small_refseq.ser").getFile()).load();
    }

    @Bean
    public ReferenceDictionary referenceDictionary(JannovarData jannovarData) {
        return jannovarData.getRefDict();
    }

    @Bean
    public SplicingParameters splicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(6)
                .setAcceptorExonic(2)
                .setAcceptorIntronic(25)
                .build();
    }
}
