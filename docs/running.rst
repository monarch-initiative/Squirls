.. _rstrunning:

===========
Run Squirls
===========

Squirls is a command-line Java tool that runs with Java version 11 or higher.

Before using Squirls, you must setup Squirls as describe in the :ref:`rstsetup` section.

Squirls provides three commands to annotate variants in different input formats:


* ``annotate-pos`` - quickly annotate a couple of variants, e.g. ``chr9:136224694A>T``
* ``annotate-csv`` - annotate variants stored in a CSV file
* ``annotate-vcf`` - annotate variants in VCF file

In the examples below, we assume that ``squirls-config.yml`` points to a configuration file with correct locations of
Squirls resources.


``annotate-pos`` - Annotate variant positions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to quickly calculate Squirls scores for a couple of variants is to use the ``annotate-pos`` command::

  java -jar squirls-cli.jar annotate-pos squirls-config.yml "chr9:136224694A>T" "chr3:52676065CA>C"

The command above generates the following output::

  ...
  2000-01-01 12:34:56.309  INFO 12345 --- [           main] o.m.s.c.c.a.AnnotatePosCommand           : Analyzing 2 change(s): `chr9:136224694A>T, chr3:52676065CA>C`

  chr9:136224694A>T    pathogenic    0.970    ENST00000371964.4=0.970115;ENST00000486887.1=0.970115;ENST00000495524.1=0.970115;NM_001278928.1=0.970115;NM_017503.4=0.970115;uc004cdi.2=0.970115
  chr3:52676065CA>C    neutral       0.007    ENST00000296302.7=0.007040;ENST00000337303.4=0.007040;ENST00000356770.4=0.007040;ENST00000394830.3=0.007040;ENST00000409057.1=0.007040;ENST00000409114.3=0.007040;ENST00000409767.1=0.007040;ENST00000410007.1=0.007040;ENST00000412587.1=0.007040;ENST00000423351.1=0.007040;ENST00000446103.1=0.007040;NM_018313.4=0.007040;XM_005265275.1=0.007040;XM_005265276.1=0.007040;XM_005265277.1=0.007040;XM_005265278.1=0.007040;XM_005265279.1=0.007040;XM_005265280.1=0.007040;XM_005265281.1=0.007040;XM_005265282.1=0.007040;XM_005265283.1=0.007040;XM_005265284.1=0.007040;XM_005265285.1=0.007040;XM_005265286.1=0.007040;XM_005265287.1=0.007040;XM_005265288.1=0.007040;XM_005265289.1=0.007040;XM_005265290.1=0.007040;XM_005265291.1=0.007040;XM_005265292.1=0.007040;uc003deq.2=0.007040;uc003der.2=0.007040;uc003des.2=0.007040;uc003det.2=0.007040;uc003deu.2=0.007040;uc003dev.2=0.007040;uc003dew.2=0.007040;uc003dex.2=0.007040;uc003dey.2=0.007040;uc003dez.1=0.007040;uc003dfb.1=0.007040;uc010hmk.1=0.007040

Squirls reports scores in four columns:

- variant position
- variant interpretation, either *pathogenic* or *neutral*
- maximum Squirls pathogenicity prediction
- Squirls pathogenicity predictions calculated for each transcript the variant overlaps with


``annotate-csv`` - Annotate variant positions stored in a CSV file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To annotate more than just a few variant positions, it may be more convenient to use the ``annotate-csv`` command.

Let's run the ``annotate-csv`` command to annotate four variants stored in the `example.csv`_ file
(an example CSV file with 4 variants stored in Squirls repository)::

  java -jar squirls-cli.jar annotate-csv squirls-config.yml example.csv output.csv

Squirls will write the scores into ``output.csv`` file::

  CHROM,POS,REF,ALT,INTERPRETATION,MAX_SCORE,SCORES
  chr9,136224694,A,T,pathogenic,0.9663857211265289,ENST00000371964.4.4=0.966386;ENST00000486887.1.1=0.966386;ENST00000495524.1.1=0.966386;NM_001278928.1=0.966386;NM_017503.4=0.966386;uc004cdi.2=0.966386
  chr3,52676065,CA,C,neutral,0.008163212387616258,ENST00000296302.7.7=0.008163;ENST00000337303.4.4=0.008163;ENST00000356770.4.4=0.008163;ENST00000394830.3.3=0.008163;ENST00000409057.1.1=0.008163;ENST00000409114.3.3=0.008163;ENST00000409767.1.1=0.008163;ENST00000410007.1.1=0.008163;ENST00000412587.1.1=0.008163;ENST00000423351.1.1=0.008163;ENST00000446103.1.1=0.008163;NM_018313.4=0.008163;XM_005265275.1=0.008163;XM_005265276.1=0.008163;XM_005265277.1=0.008163;XM_005265278.1=0.008163;XM_005265279.1=0.008163;XM_005265280.1=0.008163;XM_005265281.1=0.008163;XM_005265282.1=0.008163;XM_005265283.1=0.008163;XM_005265284.1=0.008163;XM_005265285.1=0.008163;XM_005265286.1=0.008163;XM_005265287.1=0.008163;XM_005265288.1=0.008163;XM_005265289.1=0.008163;XM_005265290.1=0.008163;XM_005265291.1=0.008163;XM_005265292.1=0.008163;uc003deq.2=0.008163;uc003der.2=0.008163;uc003des.2=0.008163;uc003det.2=0.008163;uc003deu.2=0.008163;uc003dev.2=0.008163;uc003dew.2=0.008163;uc003dex.2=0.008163;uc003dey.2=0.008163;uc003dez.1=0.008163;uc003dfb.1=0.008163;uc010hmk.1=0.008163
  chr3,165504107,A,C,pathogenic,0.9999720330487433,ENST00000264381.3.3=0.999972;ENST00000479451.1.1=0.999972;ENST00000482958.1.1=0.999972;ENST00000488954.1.1=0.999972;ENST00000497011.1.1=0.999972;ENST00000540653.1.1=0.999972;NM_000055.2=0.999972;XM_005247685.1=0.999972;uc003fem.4=0.999972;uc003fen.4=0.999972
  chr17,41197805,ACATCTGCC,A,neutral,0.010936742107683193,ENST00000309486.4.4=0.010927;ENST00000346315.3.3=0.010927;ENST00000351666.3.3=0.010927;ENST00000352993.3.3=0.010927;ENST00000354071.3.3=0.010927;ENST00000357654.3.3=0.010927;ENST00000461221.1.1=0.010937;ENST00000468300.1.1=0.010927;ENST00000471181.2.2=0.010930;ENST00000491747.2.2=0.010937;ENST00000493795.1.1=0.010930;ENST00000586385.1.1=0.010929;ENST00000591534.1.1=0.010929;ENST00000591849.1.1=0.010929;NM_007294.3=0.010927;NM_007297.3=0.010927;NM_007298.3=0.010927;NM_007299.3=0.010927;NM_007300.3=0.010927;NR_027676.1=0.010927;uc002icp.4=0.010927;uc002icq.3=0.010927;uc002ict.3=0.010927;uc002icu.3=0.010927;uc010cyx.3=0.010927;uc010whl.2=0.010927;uc010whm.2=0.010927;uc010whn.2=0.010927;uc010who.3=0.010927;uc010whp.2=0.010927

Three columns are added into the newly generated ``output.csv`` file:

- ``INTERPRETATION`` - variant interpretation, either *pathogenic* or *neutral*
- ``MAX_SCORE`` - maximum Squirls pathogenicity prediction
- ``SCORES`` - Squirls pathogenicity predictions calculated with respect to all overlapping transcripts,
  stored in format ``TX1=SCORE1;TX2=SCORE2;...;TXn=SCOREn``

``annotate-vcf`` - Annotate variants in a VCF file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The aim of this command is to annotate variants in a VCF file and to store the results in one or more output formats.

.. note::
  Squirls uses `Jannovar`_ library under the hood to perform functional variant annotation. Therefore, you must provide
  a location of the Jannovar transcript database in your system
  As a convenience, we prepared the databases for *UCSC*, *ENSEMBL*, and *RefSeq* transcripts for download, see
  :ref:`download-jannovar-ref`.

To annotate variants in the `example.vcf`_ file (an example VCF file with 6 variants stored in Squirls repository), run::

  $ java -jar squirls-cli.jar annotate-vcf squirls-config.yml hg19_refseq.ser example.vcf output

After the annotation, the results are stored at ``output.html``.

Parameters
~~~~~~~~~~

The ``annotate-vcf`` command requires four positional *parameters*:

* path to Squirls configuration file
* path to Jannovar transcript database (indicated as ``hg19_refseq.ser`` in the example above)
* path to VCF file with variants
* output prefix for the generated files

Options
~~~~~~~
In addition to parameters, Squirls allows to fine tune the annotation using the following *options* (optional):

* ``-f, --output-format`` - comma separated list of output format descriptors (see below). Use ``html,vcf,vcfgz,csv,tsv`` to store results
  in all output formats. Default: ``html``
* ``-n, --n-variants-to-report`` - number of most pathogenic variants to include in *HTML* report. Default: ``100``
* ``-t, --n-threads`` - number of threads to use for variant processing. Default: ``4``

.. note::
  Please note that the options must be specified *before* the positional parameters

Output formats
##############
The ``annotate-vcf`` command writes results in 4 output formats: *HTML*, *VCF* (compressed and uncompressed), *CSV*, and *TSV*. Use the ``-f`` option
to select one or more of the desired output formats (e.g. ``-f html,vcf``).

HTML output format
~~~~~~~~~~~~~~~~~~
Without specifying the ``-f`` option, a *HTML* report containing the 100 most deleterious variants is produced.
The number of the reported variants is adjusted by the ``-n`` option.

See the :ref:`rstinterpretation` section for getting more help.


VCF output format
~~~~~~~~~~~~~~~~~
When including ``vcf`` into the ``-f`` option, a VCF file with all input variants is created. The annotation process
adds a novel *FILTER* and *INFO* field to each variant that overlaps with at least single transcript region:

* ``SQUIRLS`` - a *FILTER* flag indicating that the variant is considered to have a deleterious effect on >=1 overlapping transcript
* ``SQUIRLS_SCORE`` - an *INFO* string containing SQUIRLS scores for each variant-transcript combination. For an example variant
  ``chr1:1234C>A,G``, the field might look like::

    SQUIRLS_SCORE=A|NM_123456.1=0.988654|ENST00000987654.1=0.988654
    SQUIRLS_SCORE=G|NM_12356.1=0.330112|ENST00000987654.1=0.330112

Multiallelic variants are broken down into separate records and processed individually. Predictions with respect to
the overlapping transcripts are separated by a pipe (``|``) symbol.

The ``-n`` option has no effect for the *VCF* output format.

Use ``vcfgz`` instead of ``vcf`` to **compress** the VCF output (``bgzip``) on the fly.

CSV/TSV output format
~~~~~~~~~~~~~~~~~~~~~
To write *n* most deleterious variants into a *CSV* (or *TSV*) file, use ``csv`` (``tsv``) in the ``-f`` option.

In result, the tabular files with the following columns are created:

.. table:: Tabular output

  ====== =========== ===== ===== ============= ============== ================ ================
  chrom   pos        ref   alt   gene_symbol   tx_accession    interpretation   squirls_score
  ====== =========== ===== ===== ============= ============== ================ ================
  chr3    165504107   A     C     *BCHE*        NM_000055.2    pathogenic       0.99997203304
  ...     ...         ...   ...   ...           ...            ...              ...
  ====== =========== ===== ===== ============= ============== ================ ================


.. _Jannovar: https://pubmed.ncbi.nlm.nih.gov/24677618
.. _example.vcf: https://github.com/TheJacksonLaboratory/Squirls/blob/development/squirls-cli/src/examples/example.vcf
.. _example.csv: https://github.com/TheJacksonLaboratory/Squirls/blob/development/squirls-cli/src/examples/example.csv