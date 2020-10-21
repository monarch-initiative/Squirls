.. _rstinterpretation:

=====================
Result interpretation
=====================

Interpretability of Squirls scores is derived from the splice features that are being used to make a prediction.
From all possible features, that were ever used in algorithms that assess variant effect on splicing, we deliberately
chose only the informative features with a biological interpretation (see :ref:`rstmodel` section for more details
regarding the features).

Figure types
^^^^^^^^^^^^

After a prediction is made, we use the features to generate the figures that we consider to be the most helpful for
clinical interpretation of the variant. We present the data using the following figure types:


Sequence ruler
##############

.. figure:: _static/acceptorRuler.svg
   :width: 800
   :height: 250

Sequence rulers are SVG graphics that show the sequence of the donor or acceptor site, mark the intron-exon boundary,
and show the position of any alternate bases that diverge from the reference sequence.


Sequence logo
#############

.. figure:: _static/acceptorLogo.svg
   :width: 800
   :height: 400


In 1990, Tom Schneider introduced Sequence logos as a way of graphically displaying consensus sequences.
The characters representing the sequence are stacked on top of each other for each position in the aligned sequences.
The height of each letter is made proportional to its frequency, and the letters are sorted so the most common one
is on top. The height of the entire stack is then adjusted to signify the information content of the sequences at
that position. From these *sequence logos*, one can determine not only the consensus sequence but also the relative
frequency of bases and the information content (measured in bits) at every position in a site or sequence. The logo
displays both significant residues and subtle sequence patterns (`Nucleic Acids Res 1990;18:6097-100`_).


Sequence walker
###############

.. figure:: _static/acceptorWalker.svg
   :width: 800
   :height: 400

Tom Schneider introduced Sequence Walkers in 1995 as a way of graphically displaying how binding proteins and other
macromolecules interact with individual bases of nucleotide sequences. Characters representing the sequence are
either oriented normally and placed above a line indicating favorable contact, or upside-down and placed below the
line indicating unfavorable contact. The positive or negative height of each letter shows the contribution of that
base to the average sequence conservation of the binding site, as represented by a sequence logo
(`Nucleic Acids Res 1997;25:4408-15`_).

In 1998, Peter Rogan introduced the application of individual information content and Sequence Walkers to splicing
variants (`Hum Mutat 1998;12:153-71`_).

Our version of the sequence walker combines the reference and the alternate sequence. The positions in which the
alternate differs from the reference are indicated by a grey box and both nucleotides are shown. In many
disease-associated variants, the reference base will be position upright and the alternate base will be positioned
beneath the line.


Sequence trekker
################

.. figure:: _static/acceptorTrekker.svg
   :width: 800
   :height: 400

We combine the sequence logo (see Sequence Logos) and walker (see Sequence walkers) in a new figure that we call
*sequence trekker* (because a trek goes further than a walk).


:math:`\Delta R_i` score
########################

.. figure:: _static/donorDelta.svg
   :width: 800
   :height: 600

The individual sequence information of a sequence :math:`R_{i\ ref}` and an alternate sequence :math:`R_{i\ alt}` are
presented using the *Sequence walker*. This graphic shows the value of the difference between the reference sequence
and an alternate sequence as well as the distribution of random changes to sequences of the same length. A variant that
reduces the sequence information is associated with a positive :math:`\Delta R_i` score (:math:`\Delta R_i = 8.96` in
this case).


Variant contexts
^^^^^^^^^^^^^^^^

**TODO** - write description regarding the variant reports

Canonical donor
###############

Variant affects the donor site.


Cryptic donor
#############

Variant creates a novel cryptic site.


Canonical acceptor
##################

Variant affects the canonical acceptor site.

Cryptic acceptor
################

Variant creates a novel cryptic site.


.. _Nucleic Acids Res 1990;18:6097-100: https://pubmed.ncbi.nlm.nih.gov/2172928
.. _Nucleic Acids Res 1997;25:4408-15: https://pubmed.ncbi.nlm.nih.gov/9336476
.. _Hum Mutat 1998;12:153-71: https://pubmed.ncbi.nlm.nih.gov/9711873