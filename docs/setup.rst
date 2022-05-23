.. _rstsetup:

Set up Squirls
==============

Squirls is a desktop Java application that requires several external files to run. This document explains how to download
these files and prepare to run Squirls.

Squirls executable
~~~~~~~~~~~~~~~~~~

There are two ways how to get ahold of *"executable"* JAR file. A prebuilt *"executable"* JAR can be downloaded from
the `Releases section <https://github.com/TheJacksonLaboratory/Squirls/releases>`_ of Squirls GitHub page.

Alternatively, Squirls can also be built from Java sources. Run the following commands to check out Squirls source code
from GitHub repository and to build Squirls JAR file::

  $ git clone https://github.com/TheJacksonLaboratory/Squirls
  $ cd Squirls
  $ ./mvnw package

.. note::
  To build Squirls from sources, JDK 11 or better must be available in the environment.

After the successful build, the JAR file is located at ``squirls-cli/target/squirls-cli-2.0.0.jar``. Verify that
the build went well by running::

  $ java -jar squirls-cli/target/squirls-cli-2.0.0.jar --help


Squirls downloadable resources
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Squirls needs several database files to run:

* Squirls database: ``squirls.mv.db``
* transcript databases:

  * ``tx.ensembl.ser``
  * ``tx.refseq.ser``
  * ``tx.ucsc.ser``

* reference genome files:

  * ``genome.fa``
  * ``genome.fa.dict``
  * ``genome.fa.fai``
  * ``assembly_report.txt``

* BigWig file with genome-wide ``phyloP`` scores: ``phylo.bw``

The files must be located in a single data directory. The rest of this document describes how to prepare the data directory.

.. note::
  In versions prior ``2205``, the entire data directory was provided within a single downloadable ZIP file. However,
  starting from ``2205``, bulky reference genome and ``phyloP`` BigWig files are not included in the ZIP archive
  anymore to save network bandwidth, and must be prepared during Squirls setup.


Squirls database files
^^^^^^^^^^^^^^^^^^^^^^

Squirls database and transcript database files are available for download from the following locations:

=========  ==============  =====================================================  ==========================================
 Version    Genome build                           URL                                             Size
=========  ==============  =====================================================  ==========================================
 2103       hg19/GRCh37     https://storage.googleapis.com/squirls/2103_hg19.zip   ~10.5 GB for download, ~15 GB unpacked
 2103       hg38/GRCh38     https://storage.googleapis.com/squirls/2103_hg38.zip   ~11.1 GB for download, ~16.5 GB unpacked
 2203       hg19/GRCh37     https://storage.googleapis.com/squirls/2203_hg19.zip   ~9.5 GB for download, ~11.9 GB unpacked
 2203       hg38/GRCh38     https://storage.googleapis.com/squirls/2203_hg38.zip   ~9.9 GB for download, ~12.2 GB unpacked
 2205       hg19/GRCh37     https://storage.googleapis.com/squirls/2205_hg38.zip  TODO
 2205       hg38/GRCh38     https://storage.googleapis.com/squirls/2205_hg38.zip  TODO
=========  ==============  =====================================================  ==========================================

.. note::
	The ``2103`` works with Squirls ``v1.0.0``, the ``2203`` and onwards works with Squirls ``v2.0.0``.

Use ``curl`` or ``wget`` utilities to download the files from command line::

  $ wget https://storage.googleapis.com/squirls/2203_hg38.zip
  or
  $ curl --output 2203_hg38.zip https://storage.googleapis.com/squirls/2203_hg38.zip

Alternatively, use a GUI FTP client such as `FileZilla <https://filezilla-project.org/>`_.

After the download, unzip the archive(s) content into a folder of your choice and note down the path. We will call
the folder as Squirls data directory (``data-directory``) in the rest of the setup. Squirls data directory should contain
the following files after the download::

  data-directory
   ├── squirls.mv.db
   ├── tx.ensembl.ser
   ├── tx.refseq.ser
   └── tx.ucsc.ser


Genome assembly
^^^^^^^^^^^^^^^

Squirls needs access to reference genome sequence. The sequences of all contigs are stored in a single FASTA file.
Along with the FASTA file, we need an index file, as well as the sequence dictionary file.
We provide a command to download and preprocess the genome assembly files::

  $ java -jar squirls-cli.jar setup ref-genome \
      --data-directory <data-directory> \
      --assembly-report <assembly-report-url> \
      --genome-assembly <genome-url>

The commands requires the following arguments:

* ``-d | --data-directory`` - path to Squirls data directory.
* ``-a | --assembly-report`` - URL pointing to genome assembly report. The following URLs are good candidates to start as of May 2022:

  ==============  ======================================================================================================================================
   Genome build                           URL
  ==============  ======================================================================================================================================
   hg19/GRCh37     https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.25_GRCh37.p13/GCF_000001405.25_GRCh37.p13_assembly_report.txt
   hg38/GRCh38     https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.39_GRCh38.p13/GCF_000001405.39_GRCh38.p13_assembly_report.txt
  ==============  ======================================================================================================================================

* ``-g | --genome-assembly`` - URL pointing to a tarball with FASTA files, one file per contig. The following URLs are good candidates to start as of May 2022:

  ==============  ============================================================================
   Genome build                           URL
  ==============  ============================================================================
   hg19/GRCh37     http://hgdownload.soe.ucsc.edu/goldenPath/hg19/bigZips/chromFa.tar.gz
   hg38/GRCh38     http://hgdownload.soe.ucsc.edu/goldenPath/hg38/bigZips/hg38.chromFa.tar.gz
  ==============  ============================================================================

After running the ``ref-genome`` command, Squirls data directory should contain the following files::

  data-directory
   ├── assembly_report.txt
   ├── genome.fa
   ├── genome.fa.dict
   ├── genome.fa.fai
   ├── squirls.mv.db
   ├── tx.ensembl.ser
   ├── tx.refseq.ser
   └── tx.ucsc.ser


``phyloP`` scores
^^^^^^^^^^^^^^^^^

Last, Squirls requires a BigWig file with genome-wide ``phyloP`` scores. The BigWig files are available for download from
at the following locations:

==============  ============================================================================================
 Genome build                           URL
==============  ============================================================================================
 hg19/GRCh37     http://hgdownload.cse.ucsc.edu/goldenpath/hg19/phyloP100way/hg19.100way.phyloP100way.bw
 hg38/GRCh38     http://hgdownload.soe.ucsc.edu/goldenPath/hg38/phyloP100way/hg38.phyloP100way.bw
==============  ============================================================================================

Run the following commands to download the file with ``phyloP`` scores for *hg38/GRCh38* into the ``data-directory``::

  $ curl --output data-directory/phylop.bw http://hgdownload.soe.ucsc.edu/goldenPath/hg38/phyloP100way/hg38.phyloP100way.bw

After downloading ``phylop.bw`` file, Squirls data directory should contain the following files::

  data-directory
   ├── assembly_report.txt
   ├── genome.fa
   ├── genome.fa.dict
   ├── genome.fa.fai
   ├── phylop.bw
   ├── squirls.mv.db
   ├── tx.ensembl.ser
   ├── tx.refseq.ser
   └── tx.ucsc.ser

The setup is complete!
