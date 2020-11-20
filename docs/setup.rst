.. _rstsetup:

Setting up Squirls
==================

Squirls is a desktop Java application that requires several external files to run. This document explains how to download
these files and prepare to run Squirls.

*Note:*
Squirls is written with Java version 11 and will run and compile under Java 11+.

Squirls downloadable resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are several external files that must be downloaded prior running Squirls.

Prebuilt Squirls executable
~~~~~~~~~~~~~~~~~~~~~~~~~~~

To download the prebuilt Squirls JAR file, go to the
`Releases section <https://github.com/TheJacksonLaboratory/Squirls/releases>`_
on the Squirls GitHub page and download the latest precompiled version of Squirls.

Squirls database files
~~~~~~~~~~~~~~~~~~~~~~

Squirls database files are available for download from:

**hg19/GRCh37**
  `Download 2011_hg19 <https://squirls.s3.amazonaws.com/2011_hg19.zip>`_ (~10.5 GB for download, ~15 GB unpacked)

**hg38/GRCh38**
  `Download 2011_hg38 <https://squirls.s3.amazonaws.com/2011_hg38.zip>`_ (~11.1 GB for download, ~16.5 GB unpacked)

After the download, unzip the archive(s) content into a folder and note the folder path.

.. _download-jannovar-ref:

Jannovar transcript databases
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Functional annotation of variants that is required for some Squirls is performed using `Jannovar`_ library. To run the
annotation, Jannovar transcript database files need to be provided. The Jannovar ``v0.35`` database files were
tested to work with Squirls.

For your convenience, the files containing *UCSC*, *RefSeq*, or *ENSEMBL* transcripts
for *hg19* or *hg38* genome assemblies are available for download (~330 MB for download, ~330 MB unpacked).

`Download Jannovar files from here <https://squirls.s3.amazonaws.com/jannovar_v0.35.zip>`_.


Build Squirls from source
^^^^^^^^^^^^^^^^^^^^^^^^^

As an alternative to using pre-built Squirls JAR file, the Squirls JAR file can also be built from source.

Run the following commands to download Squirls source code from GitHub repository and to build Squirls JAR file::

  $ git https://github.com/TheJacksonLaboratory/Squirls
  $ cd Squirls
  $ ./mvnw package

The JAR file is located at ``squirls-cli/target/squirls-cli-1.0.0.jar``.

To verify that the building process went well, run::

  $ java -jar squirls-cli/target/squirls-cli-1.0.0.jar --help

.. _generate-config-ref:

``generate-config`` - Generate and fill the configuration file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Squirls needs to know about the location of the external files that are required to run the analysis. The location along
with other details are provided in a YAML configuration file. The command ``generate-config`` generates an empty
configuration file. By running ::

  $ java -jar squirls-cli.jar generate-config squirls_config.yml

an empty configuration file ``squirls_config.yml`` is generated in the current working directory.

The configuration file has the following content::

  # Required properties template, the file follows YAML syntax.
  squirls:
    # path to directory with Squirls files
    data-directory:
    # Genome assembly - choose from {hg19, hg38}
    genome-assembly:
    # Exomiser-like data version (1902 in examples above)
    data-version:

    # Variant with longer REF allele will not be evaluated
    #max-variant-length: 100

    #classifier:
    # Which classifier to use
    #version: v0.4.4
    #annotator:
    # Which splicing annotator to use
    #  version: agez

Open the file in your favorite text editor and provide the following three bits of information:

``squirls.data-directory`` - location the the folder with Squirls data. The directory is expected to have a structure like::

  squirls_folder
     |- 1902_hg19:
     |   |- 1902_hg19.fa
     |   |- 1902_hg19.fa.dict
     |   |- 1902_hg19.fa.fai
     |   |- 1902_hg19.phylop.bw
     |   \- 1902_hg19_splicing.mv.db
     \- 1902_hg38
         |- 1902_hg38.fa
         ...

where ``1902_hg19``, ``1902_hg38`` correspond to content of the ZIP files downloaded in the previous section

``squirls.genome-assembly`` - which genome assembly to use, choose from ``{hg19, hg38}``

``squirls.data-version`` - which data version to use, the data version corresponds to ``1902`` in the example above

**Optional parameters:**
``squirls.max-variant-length`` - set the maximal length of the variant to be analyzed, this is ``100`` by default

.. _Jannovar: https://pubmed.ncbi.nlm.nih.gov/24677618