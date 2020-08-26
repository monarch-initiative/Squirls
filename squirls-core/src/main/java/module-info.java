module org.monarchinitiative.squirls.core {
    exports org.monarchinitiative.squirls.core;
    exports org.monarchinitiative.squirls.core.data.ic to org.yaml.snakeyaml;
    exports org.monarchinitiative.squirls.core.classifier.io to org.yaml.snakeyaml;

    requires jannovar.core;
    requires xyz.ielis.hyperutil.reference;

    requires java.sql;
    requires jblas;
    requires commons.io;
    requires com.google.common;
    requires org.yaml.snakeyaml;
    requires org.slf4j;
}