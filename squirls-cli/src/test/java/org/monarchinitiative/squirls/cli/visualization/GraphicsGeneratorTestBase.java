package org.monarchinitiative.squirls.cli.visualization;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.BeforeEach;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class GraphicsGeneratorTestBase {

    @Autowired
    public JannovarData jannovarData;

    @Autowired
    public SplicingPwmData splicingPwmData;

    protected VmvtGenerator vmvtGenerator = new VmvtGenerator();

    protected VariantAnnotator annotator;

    @BeforeEach
    public void setUp() {
        annotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
    }
}
