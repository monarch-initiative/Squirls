-- reference dictionary specifies chr1, 1, length=10_000
truncate table SPLICING.GENE;
insert into SPLICING.GENE(CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, GENE_ID, SYMBOL)
values (1, 1000, 3000, 1000, 3000, TRUE, 1, 'GENE1'),
       (1, 6000, 8000, 2000, 4000, FALSE, 2, 'GENE2');


truncate table SPLICING.GENE_TRACK;
insert into SPLICING.GENE_TRACK(GENE_ID, CONTIG, BEGIN_POS, END_POS, STRAND, FASTA_SEQUENCE, PHYLOP_VALUES)
values (1, 1, 1000, 1004, TRUE, STRINGTOUTF8('ACGT'),
        '3F800000400000004040000040800000'), -- corresponds to {1., 2., 3., 4.}
       (2, 1, 6000, 6004, FALSE, STRINGTOUTF8('tcga'),
        '4120000041A0000041F0000042200000'); -- corresponds to {10., 20., 30., 40.}


truncate table SPLICING.GENE_TO_TX;
insert into SPLICING.GENE_TO_TX(GENE_ID, TX_ID)
values (1, 1),
       (2, 2),
       (2, 3);


truncate table SPLICING.TRANSCRIPT;
insert into SPLICING.TRANSCRIPT(TX_ID, CONTIG, BEGIN_POS, END_POS, BEGIN_ON_FWD, END_ON_FWD, STRAND, ACCESSION_ID)
values (1, 1, 1000, 3000, 1000, 3000, TRUE, 'TX1'),
       (2, 1, 6000, 8000, 2000, 4000, FALSE, 'TX2'),
       (3, 1, 7000, 8000, 3000, 4000, FALSE, 'TX3');


truncate table SPLICING.TX_FEATURE_REGION;
insert into SPLICING.TX_FEATURE_REGION(TX_ID, CONTIG, BEGIN_POS, END_POS, REGION_TYPE, REGION_NUMBER, PROPERTIES)
values (1, 1, 1000, 1500, 'ex', 0, ''),                           -- TX1 - 1st exon
       (1, 1, 1500, 2500, 'ir', 0, 'DONOR=9.433;ACCEPTOR=7.392'), -- TX1 - 1st intron
       (1, 1, 2500, 3000, 'ex', 1, ''),                           -- TX1 - 2nd exon

       (2, 1, 6000, 8000, 'ex', 0, ''),                           -- TX2 - 1st and only exon

       (3, 1, 7000, 7200, 'ex', 0, ''),                           -- TX3 - 1st exon
       (3, 1, 7200, 7500, 'ir', 0, 'DONOR=4.931;ACCEPTOR=7.832'), -- TX3 - 1st intron
       (3, 1, 7500, 8000, 'ex', 1, ''); -- TX3 - 2nd exon