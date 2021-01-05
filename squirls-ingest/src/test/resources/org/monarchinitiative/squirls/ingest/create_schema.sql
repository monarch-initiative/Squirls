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

create schema if not exists SQUIRLS;

-- ------------------------------------------------- classifier ---------------------------------------------------
drop table if exists SQUIRLS.CLASSIFIER;
create table SQUIRLS.CLASSIFIER
(
    VERSION varchar(50) not null,
    DATA    blob        not null
);

create unique index CLASSIFIER_VERSION_UINDEX
    on SQUIRLS.CLASSIFIER (VERSION);


-- ------------------------------------------------- transcripts --------------------------------------------------
drop table if exists SQUIRLS.TRANSCRIPTS;
create table SQUIRLS.TRANSCRIPTS
(
    TX_ID        int         not null auto_increment,

    CONTIG       int         not null, -- contig id which maps to `SQUIRLS.CONTIGS.CONTIG_ID`
    BEGIN        int         not null, -- 0-based (exclusive) begin position of the region
    END          int         not null, -- 0-based (inclusive) end position of the region
    BEGIN_ON_POS int         not null, -- 0-based (exclusive) begin position of the region on FWD strand
    END_ON_POS   int         not null, -- 0-based (inclusive) end position of the region on FWD strand
    STRAND       bool        not null, -- true if POSITIVE, false if NEGATIVE
    TX_ACCESSION char(50)    not null, -- transcript accession, e.g ENST00000123456.1_1, NM_000404.3
    HGVS_SYMBOL  varchar(50) not null,
    CDS_START    int,                  -- zero-based start coordinate of CDS start, null if noncoding transcript
    CDS_END      int                   -- one-based end coordinate of CDS end, null if noncoding transcript
);

create index TRANSCRIPTS__ASSEMBLY_ID_CONTIG_START_ON_POS_END_ON_POS_index
    on SQUIRLS.TRANSCRIPTS (CONTIG, BEGIN_ON_POS, END_ON_POS);
create index TRANSCRIPTS__TX_ACCESSION_index
    on SQUIRLS.TRANSCRIPTS (TX_ACCESSION);

drop table if exists SQUIRLS.EXONS;
create table SQUIRLS.EXONS
(
    TX_ID       int not null,
    BEGIN       int not null, -- 0-based (exclusive) begin position of the exon region
    END         int not null, -- 0-based (inclusive) end position of the exon region
    EXON_NUMBER int not null  -- 0-based number of the region within the transcript, e.g. 0 for the first exon of the transcript
);

create index EXONS__TX_ID_INDEX on SQUIRLS.EXONS (TX_ID);

-- ------------------------------------------------- pwm --------------------------------------------------
DROP TABLE IF EXISTS SQUIRLS.PWM_DATA;
CREATE TABLE SQUIRLS.PWM_DATA
(
    PWM_NAME   VARCHAR(50) NOT NULL,
    ROW_IDX    INTEGER     NOT NULL,
    COL_IDX    INTEGER     NOT NULL,
    CELL_VALUE DOUBLE      NOT NULL
);

-- PWM_METADATA
DROP TABLE IF EXISTS SQUIRLS.PWM_METADATA;
CREATE TABLE SQUIRLS.PWM_METADATA
(
    PWM_NAME  VARCHAR(50)  NOT NULL,
    PWM_KEY   VARCHAR(200) NOT NULL,
    PWM_VALUE VARCHAR(200) NOT NULL
);

-- ------------------------------------------------- k-mers ------------------------------------------------
DROP TABLE IF EXISTS SQUIRLS.HEXAMERS;
CREATE TABLE SQUIRLS.HEXAMERS
(
    SEQUENCE CHAR(6) UNIQUE NOT NULL,
    SCORE    DOUBLE         NOT NULL
);


DROP TABLE IF EXISTS SQUIRLS.SEPTAMERS;
CREATE TABLE SQUIRLS.SEPTAMERS
(
    SEQUENCE CHAR(7) UNIQUE NOT NULL,
    SCORE    DOUBLE         NOT NULL
);
