.. _rstsetup:

Set up Squirls
==============

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

Squirls database files are available for download from our FTP server:

==============  ======================================= ==========================================
 Genome build                     URL                                      Size
==============  ======================================= ==========================================
 hg19/GRCh37     ftp://squirls.ielis.xyz/2103_hg19.zip   ~10.5 GB for download, ~15 GB unpacked
 hg38/GRCh38     ftp://squirls.ielis.xyz/2103_hg38.zip   ~11.1 GB for download, ~16.5 GB unpacked
==============  ======================================= ==========================================

Use ``curl`` or ``wget`` utilities to download the files from command line::

  $ wget ftp://squirls.ielis.xyz/2103_hg38.zip
  or
  $ curl --output 2103_hg38.zip ftp://squirls.ielis.xyz/2103_hg38.zip

Alternatively, use a GUI FTP client such as `FileZilla <https://filezilla-project.org/>`_.

After the download, unzip the archive(s) content into a folder and note the folder path.

.. _download-jannovar-ref:

Jannovar transcript databases
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Functional annotation of variants, which is required for certain Squirls tasks, is performed using `Jannovar`_ library.
To run the annotation, Jannovar transcript database files need to be provided. The Jannovar ``v0.35`` database files were
tested to work with Squirls.

For your convenience, the files containing *UCSC*, *RefSeq*, or *ENSEMBL* transcripts
for *hg19* or *hg38* genome assemblies are available for download (~330 MB for download, ~330 MB unpacked).

Download Jannovar files from ftp://squirls.ielis.xyz/jannovar_v0.35.zip::

  $ wget ftp://squirls.ielis.xyz/jannovar_v0.35.zip
  or
  $ curl --output jannovar_v0.35.zip ftp://squirls.ielis.xyz/jannovar_v0.35.zip

Build Squirls from source
^^^^^^^^^^^^^^^^^^^^^^^^^

As an alternative to using prebuilt Squirls JAR file, the Squirls JAR file can also be built from Java sources.

Run the following commands to download Squirls source code from GitHub repository and to build Squirls JAR file::

  $ git https://github.com/TheJacksonLaboratory/Squirls
  $ cd Squirls
  $ ./mvnw package

.. note::
  To build Squirls from sources, JDK 11 or better must be available in the environment

After the successful build, the JAR file is located at ``squirls-cli/target/squirls-cli-1.0.0.jar``.

To verify that the building process went well, run::

  $ java -jar squirls-cli/target/squirls-cli-1.0.0.jar --help

.. _generate-config-ref:

``generate-config`` - Generate and fill the configuration file
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Squirls needs to know about the locations of the external files. The locations are provided in a YAML configuration file.
The command ``generate-config`` generates an empty configuration file::

  $ java -jar squirls-cli.jar generate-config squirls-config.yml


The command above generates an empty configuration file ``squirls-config.yml`` in the working directory.

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
    #version: v0.4.6
    #annotator:
    # Which splicing annotator to use
    #  version: agez


Mandatory parameters
~~~~~~~~~~~~~~~~~~~~

Open the file in your favorite text editor and provide the following three bits of information:

1. ``squirls.data-directory`` - location the the folder with Squirls data. The directory is expected to have a structure like::

    squirls_folder
       |- 1902_hg19:
       |   |- 1902_hg19.assembly_report.txt
       |   |- 1902_hg19.fa
       |   |- 1902_hg19.fa.dict
       |   |- 1902_hg19.fa.fai
       |   |- 1902_hg19.phylop.bw
       |   |- 1902_hg19.sha256
       |   \- 1902_hg19_splicing.mv.db
       \- 1902_hg38
           |- 1902_hg38.assembly_report.txt
           |- 1902_hg38.fa
           ...

  where ``1902_hg19`` and ``1902_hg38`` correspond to content of the ZIP files downloaded in the previous section

2. ``squirls.genome-assembly`` - which genome assembly to use, choose from ``{hg19, hg38}``

3. ``squirls.data-version`` - which data version to use, the data version corresponds to ``1902`` in the example above

Optional parameters
~~~~~~~~~~~~~~~~~~~

- ``squirls.max-variant-length`` - set the maximal length of the variant to be analyzed (``100 bp`` by default)

Example
~~~~~~~

After extracting contents of the ``1902_hg38`` ZIP file into the folder ``/project/joe/squirls_resources``, the folder has
the following structure::

  squirls_resources
       \- 1902_hg38
           |- 1902_hg38.assembly_report.txt
           |- 1902_hg38.fa
           |- 1902_hg38.fa.dict
           |- 1902_hg38.fa.fai
           |- 1902_hg38.phylop.bw
           |- 1902_hg38.sha256
           \- 1902_hg38_splicing.mv.db

Then, the configuration file should have the following content::

  # Required properties template, the file follows YAML syntax.
  squirls:
    # path to directory with Squirls files
    data-directory: /project/joe/squirls_resources
    # Genome assembly - choose from {hg19, hg38}
    genome-assembly: hg38
    # Exomiser-like data version (1902 in examples above)
    data-version: 1902

    # Variant with longer REF allele will not be evaluated
    #max-variant-length: 100

    #classifier:
    # Which classifier to use
    #version: v0.4.6
    #annotator:
    # Which splicing annotator to use
    #  version: agez

.. note::
  The YAML syntax requires to include a white space between key, value pairs (e.g. ``data-directory: /project/joe/squirls_resources``.

.. _Jannovar: https://pubmed.ncbi.nlm.nih.gov/24677618
