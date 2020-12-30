/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

-- this data is entered with respect to reference dictionary from `TestDataSourceConfig`, where `chr1` = 1 and it has
-- length of 10_000 bp

truncate table SPLICING.TRANSCRIPTS;
insert into SPLICING.TRANSCRIPTS (contig, begin_pos, end_pos, begin_on_fwd, end_on_fwd, strand, tx_accession)
values (1, 1000, 2000, 1000, 2000, TRUE, 'FIRST'),
       (1, 5000, 6000, 5000, 6000, TRUE, 'SECOND'),
       (1, 8000, 10000, 0, 2000, FALSE, 'THIRD');

truncate table SPLICING.FEATURE_REGIONS;
insert into splicing.FEATURE_REGIONS (CONTIG, BEGIN_POS, END_POS, TX_ACCESSION, REGION_TYPE, PROPERTIES, REGION_NUMBER)
values (1, 1000, 1200, 'FIRST', 'ex', '', 0),                            -- 1st exon
       (1, 1200, 1400, 'FIRST', 'ir', 'DONOR=9.433;ACCEPTOR=7.392', 0),  -- 1st intron
       (1, 1400, 1600, 'FIRST', 'ex', '', 1),                            -- 2nd exon
       (1, 1600, 1800, 'FIRST', 'ir', 'DONOR=4.931;ACCEPTOR=7.832', 1),  -- 2nd intron
       (1, 1800, 2000, 'FIRST', 'ex', '', 2),                            -- 3rd exon

       (1, 5000, 5100, 'SECOND', 'ex', '', 0),                           -- 1st exon
       (1, 5100, 5300, 'SECOND', 'ir', 'DONOR=5.329;ACCEPTOR=3.848', 0), -- 1st intron
       (1, 5300, 5500, 'SECOND', 'ex', '', 1),                           -- 2nd exon
       (1, 5500, 5800, 'SECOND', 'ir', 'DONOR=9.740;ACCEPTOR=6.348', 1), -- 2nd intron
       (1, 5800, 5900, 'SECOND', 'ex', '', 2),                           -- 3rd exon
       (1, 5900, 5950, 'SECOND', 'ir', 'DONOR=5.294;ACCEPTOR=8.239', 2), -- 3rd intron
       (1, 5950, 6000, 'SECOND', 'ex', '', 3),                           -- 4th exon

       (1, 8000, 8200, 'THIRD', 'ex', '', 0),                            -- 1st exon
       (1, 8200, 8300, 'THIRD', 'ir', 'DONOR=8.429;ACCEPTOR=4.541', 0),  -- 1st intron
       (1, 8300, 8500, 'THIRD', 'ex', '', 1),                            -- 2nd exon
       (1, 8500, 8900, 'THIRD', 'ir', 'DONOR=5.249;ACCEPTOR=2.946', 1),  -- 2nd intron
       (1, 8900, 9600, 'THIRD', 'ex', '', 2),                            -- 3rd exon
       (1, 9600, 9800, 'THIRD', 'ir', 'DONOR=4.234;ACCEPTOR=1.493', 2),  -- 3rd intron
       (1, 9800, 10000, 'THIRD', 'ex', '', 3);

