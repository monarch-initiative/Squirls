.. _rstsetup:

Set up Squirls
==============

Squirls is a desktop Java application that requires several external files to run. This document explains how to download
these files and prepare to run Squirls.

.. note::
	Squirls is written with Java version 11 and will run and compile under Java 11+.

Squirls downloadable resources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are several external files that must be downloaded prior running Squirls.

Prebuilt Squirls executable
~~~~~~~~~~~~~~~~~~~~~~~~~~~

To download the prebuilt Squirls JAR file, go to the
`Releases section <https://github.com/monarch-initiative/Squirls/releases>`_
on the Squirls GitHub page and download the latest precompiled version of Squirls.

Squirls database files
~~~~~~~~~~~~~~~~~~~~~~

Squirls database files are available for download from the following locations:

=========  ==============  =====================================================  ==========================================
 Version    Genome build                           URL                                             Size
=========  ==============  =====================================================  ==========================================
 2103       hg19/GRCh37     https://storage.googleapis.com/squirls/2103_hg19.zip   ~10.5 GB for download, ~15 GB unpacked
 2103       hg38/GRCh38     https://storage.googleapis.com/squirls/2103_hg38.zip   ~11.1 GB for download, ~16.5 GB unpacked
 2203       hg19/GRCh37     https://storage.googleapis.com/squirls/2203_hg19.zip   ~9.5 GB for download, ~11.9 GB unpacked
 2203       hg38/GRCh38     https://storage.googleapis.com/squirls/2203_hg38.zip   ~9.9 GB for download, ~12.2 GB unpacked
=========  ==============  =====================================================  ==========================================

.. note::
	The ``2103`` works with Squirls ``v1.0.0``, the ``2203`` works with Squirls ``v2.0.0``.

Use ``curl`` or ``wget`` utilities to download the files from command line::

  $ wget https://storage.googleapis.com/squirls/2203_hg38.zip
  or
  $ curl --output 2203_hg38.zip https://storage.googleapis.com/squirls/2203_hg38.zip

Alternatively, use a GUI FTP client such as `FileZilla <https://filezilla-project.org/>`_.

After the download, unzip the archive(s) content into a folder and note the folder path.

Build Squirls from source
^^^^^^^^^^^^^^^^^^^^^^^^^

As an alternative to using prebuilt Squirls JAR file, the Squirls JAR file can also be built from Java sources.

Run the following commands to download Squirls source code from GitHub repository and to build Squirls JAR file::

  $ git clone https://github.com/monarch-initiative/Squirls
  $ cd Squirls
  $ ./mvnw package

.. note::
  To build Squirls from sources, JDK 11 or better must be available in the environment

After the successful build, the JAR file is located at ``squirls-cli/target/squirls-cli-2.0.0.jar``.

To verify that the building process went well, run::

  $ java -jar squirls-cli/target/squirls-cli-2.0.0.jar --help

