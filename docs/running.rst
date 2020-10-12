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

Annotate variants in a VCF file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To annotate variants in a ``variants.vcf`` VCF file and store the annotated variants as ``output.vcf``, run ::

  $ java -jar squirls-cli.jar run -c squirls_config.yml annotate-vcf variants.vcf output.vcf

The annotation adds two ``INFO`` field to each coding variant:

- ``SQUIRLS`` - a flag indicating that the variant is considered to have a deleterious effect on at least a single
  overlapping transcript
- ``SQUIRLS_SCORE`` - a string containing SQUIRLS scores for each variant-transcript combination. For the variant
  ``chr1:1234C>A,G``, the field might look like::

    SQUIRLS_SCORE=A|NM_123456.1=0.988654|ENST00000987654.1=0.988654&G|NM_12356.1=0.330112|ENST00000987654.1=0.330112.

  Predictions for the individual ``ALT`` alleles are delimited by the ``&`` symbol and grouped with respect to the
  accession of the affected transcript

When selecting the ``html`` output format option, Squirls generates a HTML report with graphics. See the
:ref:`rstinterpretation` section for getting help with interpretation of the report.

