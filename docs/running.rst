.. _rstrunning:

===============
Running Squirls
===============

Squirls is a command-line Java tool that runs with Java version 11 or higher.

To get help, run Squirls with a command or with the option "-h": ::

  $ java -jar squirls-cli-1.0.0.jar --help
    usage: java -jar squirls-cli.jar
       [-h] {generate-config,run} ...

    Super-quick Information Content and Random Forest Learning for Splice Variants

    positional arguments:
      {generate-config,run}
        generate-config      generate a configuration YAML file
        run                  run a command

    named arguments:
      -h, --help             show this help message and exit

Squirls has two main commands, ``generate-config``, and ``run``. The ``generate-config`` command needs to be run before
anything else to generate a configuration YAML file for Squirls analysis. Squirls can then be used to analyze variants in
a VCF file.


Commands
~~~~~~~~

Squirls command line interface consists of the following commands:

- ``annotate-vcf``,
- ``annotate-pos``,
- ``annotate-csv``, and

All the commands require path to the YAML configuration file. We use ``squirls_config.yml`` in all command examples to
indicate the location of the YAML configuration file.

Annotate variants in a VCF file (``annotate-vcf``)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To annotate variants in a ``variants.vcf`` VCF file and store the annotated variants as ``output.vcf``, run ::

  $ java -jar squirls-cli.jar run -c squirls_config.yml annotate-vcf variants.vcf output.vcf

The annotation adds two ``INFO`` field to each coding variant:

- ``SQUIRLS`` - a flag indicating that the variant is considered to have a deleterious effect on at least a single
  overlapping transcript
- ``SQUIRLS_SCORE`` - a string containing SQUIRLS scores for each variant-transcript combination. For the variant
  ``chr1:1234C>A,G``, the field might look like::

    SQUIRLS_SCORE=A|NM_123456.1=0.988654|ENST00000987654.1=0.988654&G|NM_12356.1=0.330112|ENST00000987654.1=0.330112

  Predictions for the individual ``ALT`` alleles are delimited by the ``&`` symbol and grouped with respect to the
  accession of the affected transcript

When selecting the ``html`` output format option, Squirls generates a HTML report with graphics. See the
:ref:`rstinterpretation` section for getting help with interpretation of the report.

Annotate variant positions (``annotate-pos``)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to quickly calculate Squirls scores for a couple of variants is to use the ``annotate-pos`` command ::

  $java -jar squirls-cli.jar run -c squirls_config.yml annotate-pos -c "chr9:136224694A>T" -c "chr3:52676065CA>C"

An output similar to this is produced ::

  ...
  2000-01-01 12:34:56.309  INFO 12345 --- [           main] o.m.s.c.c.a.AnnotatePosCommand           : Analyzing 2 change(s): `chr9:136224694A>T, chr3:52676065CA>C`

  chr9:136224694A>T	pathogenic	ENST00000371964.4.4=0.966386;ENST00000486887.1.1=0.966386;ENST00000495524.1.1=0.966386;NM_001278928.1=0.966386;NM_017503.4=0.966386;uc004cdi.2=0.966386
  chr3:52676065CA>C	neutral	ENST00000296302.7.7=0.008163;ENST00000337303.4.4=0.008163;ENST00000356770.4.4=0.008163;ENST00000394830.3.3=0.008163;ENST00000409057.1.1=0.008163;ENST00000409114.3.3=0.008163;ENST00000409767.1.1=0.008163;ENST00000410007.1.1=0.008163;ENST00000412587.1.1=0.008163;ENST00000423351.1.1=0.008163;ENST00000446103.1.1=0.008163;NM_018313.4=0.008163;XM_005265275.1=0.008163;XM_005265276.1=0.008163;XM_005265277.1=0.008163;XM_005265278.1=0.008163;XM_005265279.1=0.008163;XM_005265280.1=0.008163;XM_005265281.1=0.008163;XM_005265282.1=0.008163;XM_005265283.1=0.008163;XM_005265284.1=0.008163;XM_005265285.1=0.008163;XM_005265286.1=0.008163;XM_005265287.1=0.008163;XM_005265288.1=0.008163;XM_005265289.1=0.008163;XM_005265290.1=0.008163;XM_005265291.1=0.008163;XM_005265292.1=0.008163;uc003deq.2=0.008163;uc003der.2=0.008163;uc003des.2=0.008163;uc003det.2=0.008163;uc003deu.2=0.008163;uc003dev.2=0.008163;uc003dew.2=0.008163;uc003dex.2=0.008163;uc003dey.2=0.008163;uc003dez.1=0.008163;uc003dfb.1=0.008163;uc010hmk.1=0.008163

  ...

