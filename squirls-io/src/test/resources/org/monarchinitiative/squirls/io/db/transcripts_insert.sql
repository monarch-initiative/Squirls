insert into SQUIRLS.TRANSCRIPTS(TX_ID, CONTIG, BEGIN, END, BEGIN_ON_POS, END_ON_POS, STRAND,
                                TX_ACCESSION, HGVS_SYMBOL, CDS_START, CDS_END, TX_SUPPORT_LEVEL)
values (1, 1, 100, 900, 100, 900, true, 'NM_000001.1', 'JOE', 200, 800, 1),
       (2, 1, 8500, 9500, 500, 1500, false, 'NM_000002.1', 'JAMIE', 8600, 9400, 1),
       (3, 1, 1100, 1900, 1100, 1900, true, 'NM_000003.1', 'JIM', 1200, 1800, 1),
       (4, 2, 100, 900, 100, 900, true, 'NM_000004.1', 'JOHNNY', 200, 800, 1),
       (5, 2, 1000, 2000, 1000, 2000, true, 'NM_000005.1', 'JESSE', null, null, 1);

insert into SQUIRLS.EXONS(TX_ID, BEGIN, END, EXON_NUMBER)
values (1, 100, 300, 0),
       (1, 500, 700, 1),
       (1, 800, 900, 3),

       (2, 8500, 8700, 0),
       (2, 8900, 9100, 1),
       (2, 9300, 9500, 2),

       (3, 1100, 1300, 0),
       (3, 1500, 1600, 1),
       (3, 1700, 1900, 2),

       (4, 100, 300, 0),
       (4, 400, 500, 1),
       (4, 700, 900, 2),

       (5, 1000, 1400, 0),
       (5, 1500, 2000, 1);
