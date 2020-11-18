.. _rstrunning:

===============
Running Squirls
===============

Squirls is a command-line Java tool that runs with Java version 11 or higher.

To get help, run Squirls with a command or with the option ``-h`` or ``--help``: ::

  $ Super-quick Information Content and Random Forest Learning for Splice Variants

  Usage: squirls-cli.jar [-hV] [COMMAND]
    -h, --help      Show this help message and exit.
    -V, --version   Print version information and exit.
  Commands:
    generate-config, G  Generate a configuration YAML file
    annotate-pos, P     Annotate several variant positions
    annotate-csv, C     Annotate variants stored in tabular file
    annotate-vcf, A     Annotate variants in a VCF file

Before running any command, the ``generate-config`` command needs to be run to generate a configuration YAML file for
Squirls analysis (see :ref:`generate-config-ref`). Then, the other commands can then be used to analyze variants in multiple input formats.

Squirls annotates variants using the following commands:

* ``annotate-vcf``,
* ``annotate-pos``, and
* ``annotate-csv``,

We use ``squirls_config.yml`` placeholder to indicate the location of the YAML configuration file in all command
examples below.

``annotate-vcf`` - Annotate variants in a VCF file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The aim of this command is to annotate variants in a VCF file. The results are then stored in *HTML* and/or
*VCF* format.

To annotate variants in the `example.vcf`_ file (a file with 6 variants stored in Squirls repository), run ::

  $ java -jar squirls-cli.jar annotate-vcf -c squirls_config.yml -d hg19_refseq.ser example.vcf output

Squirls uses `Jannovar`_ library to perform functional annotation for the variant, hence it must be provided with
path to Jannovar transcript database (``-d`` option). See :ref:`download-jannovar-ref` for download instructions.

After the annotation, the results are stored at ``output.html``.

Run ``java -jar squirls-cli.jar annotate-vcf --help`` to see all the available options.

Output formats
##############
The ``annotate-vcf`` command writes results in 2 formats: *HTML* and *VCF*. Use the ``-f`` option to select the output format.

HTML output format
~~~~~~~~~~~~~~~~~~
By default, a *HTML* report with the 100 most deleterious variants is produced. See the :ref:`rstinterpretation`
section for getting help with interpretation of the report.

VCF output format
~~~~~~~~~~~~~~~~~
When using the ``-f vcf`` option, a VCF file with all input variants is created. The annotation adds two novel ``INFO``
fields to each variant that overlaps with at least single transcript region:

* ``SQUIRLS`` - a flag indicating that the variant is considered to have a deleterious effect on >=1 overlapping transcript
* ``SQUIRLS_SCORE`` - a string containing SQUIRLS scores for each variant-transcript combination. For a hypothetical variant
  ``chr1:1234C>A,G``, the field might look like::

    SQUIRLS_SCORE=A|NM_123456.1=0.988654|ENST00000987654.1=0.988654&G|NM_12356.1=0.330112|ENST00000987654.1=0.330112

  Predictions for the individual ``ALT`` alleles are delimited by the ``&`` symbol and grouped with respect to the
  accession ID of the affected transcript

``annotate-pos`` - Annotate variant positions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to quickly calculate Squirls scores for a couple of variants is to use the ``annotate-pos`` command ::

  $java -jar squirls-cli.jar annotate-pos --config squirls_config.yml "chr9:136224694A>T" "chr3:52676065CA>C"

An output similar to this is produced ::

  ...
  2000-01-01 12:34:56.309  INFO 12345 --- [           main] o.m.s.c.c.a.AnnotatePosCommand           : Analyzing 2 change(s): `chr9:136224694A>T, chr3:52676065CA>C`

  chr9:136224694A>T	pathogenic	ENST00000371964.4.4=0.966386;ENST00000486887.1.1=0.966386;ENST00000495524.1.1=0.966386;NM_001278928.1=0.966386;NM_017503.4=0.966386;uc004cdi.2=0.966386
  chr3:52676065CA>C	neutral	ENST00000296302.7.7=0.008163;ENST00000337303.4.4=0.008163;ENST00000356770.4.4=0.008163;ENST00000394830.3.3=0.008163;ENST00000409057.1.1=0.008163;ENST00000409114.3.3=0.008163;ENST00000409767.1.1=0.008163;ENST00000410007.1.1=0.008163;ENST00000412587.1.1=0.008163;ENST00000423351.1.1=0.008163;ENST00000446103.1.1=0.008163;NM_018313.4=0.008163;XM_005265275.1=0.008163;XM_005265276.1=0.008163;XM_005265277.1=0.008163;XM_005265278.1=0.008163;XM_005265279.1=0.008163;XM_005265280.1=0.008163;XM_005265281.1=0.008163;XM_005265282.1=0.008163;XM_005265283.1=0.008163;XM_005265284.1=0.008163;XM_005265285.1=0.008163;XM_005265286.1=0.008163;XM_005265287.1=0.008163;XM_005265288.1=0.008163;XM_005265289.1=0.008163;XM_005265290.1=0.008163;XM_005265291.1=0.008163;XM_005265292.1=0.008163;uc003deq.2=0.008163;uc003der.2=0.008163;uc003des.2=0.008163;uc003det.2=0.008163;uc003deu.2=0.008163;uc003dev.2=0.008163;uc003dew.2=0.008163;uc003dex.2=0.008163;uc003dey.2=0.008163;uc003dez.1=0.008163;uc003dfb.1=0.008163;uc010hmk.1=0.008163

  ...

``annotate-csv`` - Annotate variant positions stored in a CSV file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you want to annotate >10 variant positions, it might be more convenient to do this by using the ``annotate-csv`` command.

Let's consider 4 variants stored in a CSV file ``example.csv``::

  CHROM,POS,REF,ALT
  chr9,136224694,A,T
  chr3,52676065,CA,C
  chr3,165504107,A,C
  chr17,41197805,ACATCTGCC,A

then, by running command the ``annotate-csv`` command ::

  java -jar squirls-cli.jar annotate-csv --config squirls_config.yml example.csv output.csv

Squirls performs the variant classification and predicts pathogenicity wrt. all overlapping transcripts ::

  CHROM,POS,REF,ALT,PATHOGENIC,MAX_SCORE,SCORES
  chr9,136224694,A,T,true,0.9663857211265289,ENST00000371964.4.4=0.966386;ENST00000486887.1.1=0.966386;ENST00000495524.1.1=0.966386;NM_001278928.1=0.966386;NM_017503.4=0.966386;uc004cdi.2=0.966386
  chr3,52676065,CA,C,false,0.008163212387616258,ENST00000296302.7.7=0.008163;ENST00000337303.4.4=0.008163;ENST00000356770.4.4=0.008163;ENST00000394830.3.3=0.008163;ENST00000409057.1.1=0.008163;ENST00000409114.3.3=0.008163;ENST00000409767.1.1=0.008163;ENST00000410007.1.1=0.008163;ENST00000412587.1.1=0.008163;ENST00000423351.1.1=0.008163;ENST00000446103.1.1=0.008163;NM_018313.4=0.008163;XM_005265275.1=0.008163;XM_005265276.1=0.008163;XM_005265277.1=0.008163;XM_005265278.1=0.008163;XM_005265279.1=0.008163;XM_005265280.1=0.008163;XM_005265281.1=0.008163;XM_005265282.1=0.008163;XM_005265283.1=0.008163;XM_005265284.1=0.008163;XM_005265285.1=0.008163;XM_005265286.1=0.008163;XM_005265287.1=0.008163;XM_005265288.1=0.008163;XM_005265289.1=0.008163;XM_005265290.1=0.008163;XM_005265291.1=0.008163;XM_005265292.1=0.008163;uc003deq.2=0.008163;uc003der.2=0.008163;uc003des.2=0.008163;uc003det.2=0.008163;uc003deu.2=0.008163;uc003dev.2=0.008163;uc003dew.2=0.008163;uc003dex.2=0.008163;uc003dey.2=0.008163;uc003dez.1=0.008163;uc003dfb.1=0.008163;uc010hmk.1=0.008163
  chr3,165504107,A,C,true,0.9999720330487433,ENST00000264381.3.3=0.999972;ENST00000479451.1.1=0.999972;ENST00000482958.1.1=0.999972;ENST00000488954.1.1=0.999972;ENST00000497011.1.1=0.999972;ENST00000540653.1.1=0.999972;NM_000055.2=0.999972;XM_005247685.1=0.999972;uc003fem.4=0.999972;uc003fen.4=0.999972
  chr17,41197805,ACATCTGCC,A,false,0.010936742107683193,ENST00000309486.4.4=0.010927;ENST00000346315.3.3=0.010927;ENST00000351666.3.3=0.010927;ENST00000352993.3.3=0.010927;ENST00000354071.3.3=0.010927;ENST00000357654.3.3=0.010927;ENST00000461221.1.1=0.010937;ENST00000468300.1.1=0.010927;ENST00000471181.2.2=0.010930;ENST00000491747.2.2=0.010937;ENST00000493795.1.1=0.010930;ENST00000586385.1.1=0.010929;ENST00000591534.1.1=0.010929;ENST00000591849.1.1=0.010929;NM_007294.3=0.010927;NM_007297.3=0.010927;NM_007298.3=0.010927;NM_007299.3=0.010927;NM_007300.3=0.010927;NR_027676.1=0.010927;uc002icp.4=0.010927;uc002icq.3=0.010927;uc002ict.3=0.010927;uc002icu.3=0.010927;uc010cyx.3=0.010927;uc010whl.2=0.010927;uc010whm.2=0.010927;uc010whn.2=0.010927;uc010who.3=0.010927;uc010whp.2=0.010927

Three columns are added:

* ``PATHOGENIC`` - ``true`` if the variant is predicted to be splicing pathogenic
* ``MAX_SCORE`` - maximum Squirls score of all overlapping transcripts
* ``SCORES`` - Squirls scores calculated wrt. all overlapping transcripts stored in format ``TX1=SCORE1;TX2=SCORE2;...;TXn=SCOREn``

.. _Jannovar: https://pubmed.ncbi.nlm.nih.gov/24677618
.. _example.vcf: https://github.com/TheJacksonLaboratory/Squirls/blob/development/squirls-cli/src/examples/example.vcf