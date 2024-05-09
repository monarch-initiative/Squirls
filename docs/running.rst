.. _rstrunning:

===========
Run Squirls
===========

Squirls is a command-line Java tool that runs with Java version 11 or higher.

Before using Squirls, you must setup Squirls as describe in the :ref:`rstsetup` section.

Squirls provides four commands to annotate variants in different input formats:


* ``annotate-pos`` - quickly annotate a couple of variants, e.g. ``chr9:136224694A>T``
* ``annotate-csv`` - annotate variants stored in a CSV file
* ``annotate-vcf`` - annotate variants in VCF file
* ``precalculate`` - precalculate SQUIRLS scores for provided regions and store the results in a compressed VCF file

In the examples below, we assume that ``$SQUIRLS_DATA`` points to Squirls resource directory obtained
by unzipping the archive, as described in the :ref:`rstsetup` section::

  # e.g.
  SQUIRLS_DATA=path/to/squirls/data

where::

  path/to/squirls/data
   ├── assembly_report.txt
   ├── genome.fa
   ├── genome.fa.dict
   ├── genome.fa.fai
   ├── phylop.bw
   ├── squirls.mv.db
   ├── tx.ensembl.ser
   ├── tx.refseq.ser
   └── tx.ucsc.ser


``annotate-pos`` - Annotate variant positions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The easiest way to quickly calculate Squirls scores for a couple of variants is to use the ``annotate-pos`` command::

  java -jar squirls-cli.jar annotate-pos -d $SQUIRLS_DATA "chr9:136224694A>T" "chr3:52676065CA>C"

.. note::
  Do not forget to surround the variants with double quotes (``"chr9:136224694A>T"`` and *not* ``chr9:136224694A>T``)
  to prevent interpretation of the ``>`` as a shell operator.

.. note::
  Both ``"chr1:12345A>T"`` and ``"chr1:g.12345A>T"`` notations are supported.

The command above generates the following terminal output::

  ...
  2000-01-01 12:34:56.309  INFO 12345 --- [           main] o.m.s.c.c.a.AnnotatePosCommand           : Analyzing 2 change(s): `chr9:136224694A>T, chr3:52676065CA>C`

  chr9:136224694A>T    pathogenic    0.970    NM_001278928.1=0.970115;NM_017503.4=0.970115
  chr3:52676065CA>C    neutral       0.007    NM_018313.4=0.006350;XM_005265275.1=0.006350;XM_005265276.1=0.006350;XM_005265277.1=0.006350;XM_005265278.1=0.006350;XM_005265279.1=0.006350;XM_005265280.1=0.006350;XM_005265281.1=0.006350;XM_005265282.1=0.006350;XM_005265283.1=0.006350;XM_005265284.1=0.006350;XM_005265285.1=0.006350;XM_005265286.1=0.006350;XM_005265287.1=0.006350;XM_005265288.1=0.006350;XM_005265289.1=0.006350;XM_005265290.1=0.006350;XM_005265291.1=0.006350;XM_005265292.1=0.006350

Squirls reports scores in four columns:

- variant position
- variant interpretation, either *pathogenic* or *neutral*
- maximum Squirls pathogenicity prediction rounded up to 3 significant digits
- Squirls pathogenicity predictions calculated for each transcript the variant overlaps with

.. note::
  The options ``--output-format``, ``--n-variants-to-report``, ``--out-dir``, ``--report-features`` are ignored.

``annotate-csv`` - Annotate variant positions stored in a CSV file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To annotate more than just a few variant positions, it may be more convenient to use the ``annotate-csv`` command.

Let's run the ``annotate-csv`` command to annotate four variants stored in the `example.csv`_ file
(an example CSV file with 4 variants stored in Squirls repository)::

  java -jar squirls-cli.jar annotate-csv -d $SQUIRLS_DATA example.csv output

Squirls reads the variants and stores the scores into ``output.html`` file. The *HTML* is the default output format,
see :ref:`rstoutputformats` section for more details.

Mandatory arguments
~~~~~~~~~~~~~~~~~~~

The ``annotate-csv`` command requires three mandatory *arguments*:

* ``-d | --data-directory`` - path to Squirls data directory
* path to CSV file with variants
* output prefix for the generated files

Optional arguments
~~~~~~~~~~~~~~~~~~

In addition to the mandatory arguments, Squirls allows to fine tune the annotation using optional arguments:

* ``--all-transcripts`` - report Squirls scores for all overlapping transcripts. Default: ``false``
* ``--compress`` - compress the output files using ``gzip`` (*tabular*) or ``bgzip`` (*VCF*). The option has no effect
  on *HTML* output format. Default: ``false``
* ``-f | --output-format`` - comma separated list of :ref:`rstoutputformats`. Use ``html,vcf,csv,tsv`` to store results
  in all output formats. Default: ``html``
* ``-n, --n-variants-to-report`` - number of most pathogenic variants to include in *HTML* report. The option has
  no effect on *VCF* output format. Default: ``100``
* ``--out-dir`` - path to folder where to write the output files. Default: current working directory
* ``--report-features`` - include Squirls features into the output. Default: ``false``
* ``-t | --transcript-source`` - transcript source to use. Choose one of ``{REFSEQ, ENSEMBL, UCSC}``. Default: ``REFSEQ``
* ``--threads`` - process variants on *n* threads. Default: ``2``


``annotate-vcf`` - Annotate variants in a VCF file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The aim of this command is to annotate variants in a VCF file and to store the results in one or more :ref:`rstoutputformats`.

To annotate variants in the `example.vcf`_ file (an example VCF file with 6 variants stored in Squirls repository), run::

  $ java -jar squirls-cli.jar annotate-vcf -d $SQUIRLS_DATA example.vcf path/to/output/file

After the annotation, the results are stored at ``path/to/output/file.html``.

Mandatory arguments
~~~~~~~~~~~~~~~~~~~

The ``annotate-vcf`` command requires three mandatory arguments:

* ``-d | --data-directory`` - path to Squirls data directory
* path to the VCF file with variants
* output prefix for the generated files

Optional arguments
~~~~~~~~~~~~~~~~~~

In addition to the mandatory arguments, Squirls allows to fine tune the annotation using optional arguments:

* ``--all-transcripts`` - report Squirls scores for all overlapping transcripts. Default: ``false``
* ``--compress`` - compress the output files using ``gzip`` (*tabular*) or ``bgzip`` (*VCF*). The option has no effect
  on *HTML* output format. Default: ``false``
* ``-f | --output-format`` - comma separated list of :ref:`rstoutputformats`. Use ``html,vcf,csv,tsv`` to store results
  in all output formats. Default: ``html``
* ``-n, --n-variants-to-report`` - number of most pathogenic variants to include in *HTML* report. The option has
  no effect on *VCF* output format. Default: ``100``
* ``--out-dir`` - path to folder where to write the output files. Default: current working directory
* ``--report-features`` - include Squirls features into the output. Default: ``false``
* ``-t | --transcript-source`` - transcript source to use. Choose one of ``{REFSEQ, ENSEMBL, UCSC}``. Default: ``REFSEQ``
* ``--threads`` - process variants on *n* threads. Default: ``2``

``precalculate`` - Precalculate SQUIRLS scores
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

We do not provide a tabular file with precalculated scores for all possible genomic variants. Instead, we provide
a command for precalculating the scores for your genomic regions of interest.
This command precalculates Squirls scores for all possible variants (including INDELs up to specified length)
and stores the scores in a compressed VCF file.

**Example**::

  $ java -jar squirls-cli.jar precalculate -d $SQUIRLS_DATA CM000669.1:44187000-44187600 CM000669.1:44186000-44186500

The command computes scores for two regions, each region encompassing an exons of the *GCK* gene plus some neighboring
intronic sequence. ``SQUIRLS`` recognizes *GenBank*, *RefSeq*, *UCSC*, and *simple*
(``1``, ``2``, ..., ``X``, ``Y``, ``MT``) contigs accessions.

The region coordinates must be provided using *zero-based* coordinates where the start position is *not* part of the region.

By default, SQUIRLS generates all possible SNVs for the bases of the region, including deletion of the base.
For example, a region :math:`r` spanning ``ctg1:3-5`` of a 10bp-long reference contig ``ctg1``::

  >ctg1
  ACGTACGTAC

yields the variants:

.. table::

  ====== =========== ========================== ============ ===================================================
  chrom   pos        SNVs                       DELs         INSs
  ====== =========== ========================== ============ ===================================================
  ctg1        4       ``T>A``, ``T>C``, ``T>G``     ``T>``    N/A
  ctg1        5       ``A>C``, ``A>G``, ``A>T``     ``A>``    N/A
  ====== =========== ========================== ============ ===================================================

the annotated variants are stored in a compressed VCF file named ``squirls-scores.vcf.gz`` that is by default stored in
the current working directory.

Please note that the VCF file *not* sorted. Please sort and index the VCF file yourself, e.g. by running::

  bcftools sort squirls-scores.vcf.gz | bgzip -c > squirls-scores.sorted.vcf.gz
  tabix squirls-scores.sorted.vcf.gz


Mandatory arguments
~~~~~~~~~~~~~~~~~~~

The only mandatory argument for ``precalculate`` is ``-d`` to provide path to Squirls data directory. Following that,
``0..n`` region definitions, e.g. ``CM000669.1:44187000-44187600``, ``CM000669.1:44186000-44186500`` can be provided.

Optional arguments
~~~~~~~~~~~~~~~~~~

There are several options to adjust:

* ``-i | --input`` - path to a BED file with the target regions. Lines starting with ``#`` are ignored. See example `regions.bed`_
* ``--individual`` - if the flag is present, predictions with respect to all overlapping transcripts will be stored within the *INFO* field.
* ``-l | --max-length`` - maximum length of the generated variants on the reference genome, see *Variant generation* below (Default: ``1``)
* ``-o | --output`` - path to VCF file where to write the results. The VCF output is compressed, so we recommend to use ``*.vcf.gz`` suffix. (Default: ``squirls.scores.vcf.gz``)
* ``-t | --transcript-source`` - transcript source to use. Choose one of ``{REFSEQ, ENSEMBL, UCSC}``. Default: ``REFSEQ``
* ``--threads`` - number of threads to use for calculating the scores. (Default: ``2``)


Parallel processing
~~~~~~~~~~~~~~~~~~~

When predicting the scores, each region is handled by a single thread, while at most ``--threads`` threads being used for
prediction at the same time.
Therefore, to fully leverage the parallelism offered by modern multi-core CPUs, we recommend to split large regions
into several smaller ones.


Variant generation
~~~~~~~~~~~~~~~~~~

The default value of the ``-l, --max-length`` parameter is set to ``1``. As explained above, the parameter controls
the length of the generated variants. However, length can be set to any positive integer, leading to calculation
of scores for variants of different lengths.

Using the region :math:`r` and the contig ``ctg1`` defined above, setting ``-l`` to ``2`` will calculate scores for
variants:

.. table:: The variant generation pattern

  ====== =========== ============================== ================= =======================================
  chrom   pos        SNVs                           DELs              INSs
  ====== =========== ============================== ================= =======================================
  ctg1        4       ``T>A``, ``T>C``, ``T>G``     ``T>``, ``TA>T``  ``T>TA``, ``T>TC``, ``T>TG``, ``T>TT``
  ctg1        5       ``A>C``, ``A>G``, ``A>T``     ``A>``            ``A>AA``, ``A>AC``, ``A>AG``, ``A>AT``
  ====== =========== ============================== ================= =======================================

.. note::
  The number of possible variants grows exponentially with increasing of the ``--length`` value. This can lead to
  substantial run times and to extending your computational budget. Use at your own risk ;)


.. _Jannovar: https://pubmed.ncbi.nlm.nih.gov/24677618
.. _example.vcf: https://github.com/monarch-initiative/Squirls/blob/development/squirls-cli/src/examples/example.vcf
.. _example.csv: https://github.com/monarch-initiative/Squirls/blob/development/squirls-cli/src/examples/example.csv
.. _regions.bed: https://github.com/monarch-initiative/Squirls/blob/development/squirls-cli/src/examples/regions.bed
