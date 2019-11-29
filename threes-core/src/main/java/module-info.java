module org.monarchinitiative.threes.core {
    requires org.slf4j;
    requires jblas;
    requires com.google.common;
    requires java.sql;
    requires snakeyaml;
    requires htsjdk;
    requires jannovar.core;
    requires xyz.ielis.hyperutil.reference;

    exports org.monarchinitiative.threes.core;
    exports org.monarchinitiative.threes.core.calculators.ic;
    exports org.monarchinitiative.threes.core.calculators.sms;

    exports org.monarchinitiative.threes.core.data;
    exports org.monarchinitiative.threes.core.data.ic;
    exports org.monarchinitiative.threes.core.data.sms;

    exports org.monarchinitiative.threes.core.model;

    exports org.monarchinitiative.threes.core.reference;
    exports org.monarchinitiative.threes.core.reference.transcript;

    exports org.monarchinitiative.threes.core.scoring;

    opens org.monarchinitiative.threes.core;

    opens org.monarchinitiative.threes.core.calculators.ic;
    opens org.monarchinitiative.threes.core.calculators.sms;

    opens org.monarchinitiative.threes.core.data;
    opens org.monarchinitiative.threes.core.data.ic;
    opens org.monarchinitiative.threes.core.data.sms;

    opens org.monarchinitiative.threes.core.model;

    opens org.monarchinitiative.threes.core.reference;
    opens org.monarchinitiative.threes.core.reference.allele;
    opens org.monarchinitiative.threes.core.reference.transcript;

    opens org.monarchinitiative.threes.core.scoring;
    opens org.monarchinitiative.threes.core.scoring.scorers;

}