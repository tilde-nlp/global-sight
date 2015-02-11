-- 
--  This inserts the known format types
-- 

-- - NOTE that some of these IDs are hardcoded into KnownFormatTypeDescriptorModifier

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   1,'HTML','Hyper-Text Markup Language','html',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   2,'CSS','Cascading Style Sheet','css',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   4,'JavaProperties','Java Properties','javaprop',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   5,'Javascript','Javascript','javascript',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   6,'PlainText','Plain Text','plaintext',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   7,'XML','Extensible Markup Language','xml',
   'XML_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   8,'JHTML','Java HTML','jhtml',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   9, 'ColdFusion','Cold Fusion','cfm',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);
*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   10,'JavaProperties (HTML)','Java Properties(HTML)','javaprop-html',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   11,'JavaProperties (Msg)','Java Properties(Msg)','javaprop-msg',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   12, 'ASP','Active Server Pages','asp',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);
*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   13, 'JSP','Java Server Pages','jsp',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   14, 'Word2007','Microsoft Word 2007','word-html',
   'MSOFFICE_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   15, 'Frame5','Adobe Framemaker5.0+SGML','xml',
   'FRAME_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   16, 'Quark (WIN)','Quark','xml',
   'QUARK_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   17, 'Frame6','Adobe Framemaker 6.0','xml',
   'FRAME_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);*/

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   18, 'C++/C','C++/C','cpp',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   19, 'Excel2007','Microsoft Excel 2007','excel-html',
   'MSOFFICE_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   20, 'PowerPoint2007','Microsoft PowerPoint 2007','powerpoint-html',
   'MSOFFICE_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
    21, 'Java','Java Files','java',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
22, 'PDF','PDF','pdf',
   'PDF_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
); 
*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
23, 'Un-extracted','Content that should not be extracted.','unextracted',
   'UNEXTRACTED_IMPORTED_EVENT',
   'UNEXTRACTED_LOCALIZED_EVENT'
); 

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES(
24, 'Catalyst','Alchemy Catalyst','unextracted',
    'CATALYST_IMPORTED_EVENT',
    'CATALYST_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
25, 'Frame7','Adobe Framemaker 7','xml',
   'FRAME_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
 26, 'RTF','Rich Text Format','rtf',
    'MSOFFICE_IMPORTED_EVENT',
    'HTML_LOCALIZED_EVENT'
 ); 

/* 
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
27, 'SGML','SGML','sgml',
    'HTML_IMPORTED_EVENT',
    'HTML_LOCALIZED_EVENT'
);*/

--  Disabled until further notice
--  INSERT INTO KNOWN_FORMAT_TYPE VALUES (
--  28, 'Man Page','Unix Man Page','troff-man',
--      'HTML_IMPORTED_EVENT',
--      'HTML_LOCALIZED_EVENT'
--  );

/*
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
29, 'XPTag','Quark XPress Tags','xptag',
    'XPTAG_IMPORTED_EVENT',
    'XPTAG_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
30, 'Quark (MAC)','Quark (MAC)','xptag',
    'COPYFLOW_IMPORTED_EVENT',
    'XPTAG_LOCALIZED_EVENT'
);*/

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
31, 'INDD (CS2)','InDesign INDD','indd',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
32, 'Illustrator','Illustrator AI','illustrator',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES(33,'Word2003','Microsoft Word 2003','word-html','MSOFFICE_IMPORTED_EVENT','HTML_LOCALIZED_EVENT');

INSERT INTO KNOWN_FORMAT_TYPE VALUES(34,'Excel2003','Microsoft Excel 2003','excel-html','MSOFFICE_IMPORTED_EVENT','HTML_LOCALIZED_EVENT');

INSERT INTO KNOWN_FORMAT_TYPE VALUES(35,'PowerPoint2003','Microsoft PowerPoint 2003','powerpoint-html','MSOFFICE_IMPORTED_EVENT','HTML_LOCALIZED_EVENT');

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
36, 'INDD (CS3)','InDesign INDD CS3','indd_cs3',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
37, 'INX (CS2)','InDesign CS2 INX','inx',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);


INSERT INTO KNOWN_FORMAT_TYPE VALUES (
38, 'INX (CS3)','InDesign CS3 INX','inx_cs3',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
39, 'Xliff','Xliff file','xlf',
   'XML_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
40, 'INDD (CS4)','InDesign INDD CS4','indd_cs4',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
41, 'OpenOffice document','OpenOffice document(odt, ods, odp)','openoffice-xml',
   'OPENOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
42, 'Portable Object', 'Portable Object File', 'po', 
    'HTML_IMPORTED_EVENT', 'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
43, 'Office2010 document','Microsoft Office 2010 document','office-xml',
   'MSOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   44,'Resource Compiler','Resource Compiler','rc',
   'HTML_IMPORTED_EVENT',
   'HTML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
	45, 'RESX', 'resx', 'xml', 'XML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   46,'InDesign Markup (IDML)','idml','xml',
   'IDML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
47, 'INDD (CS5)','InDesign INDD CS5','indd_cs5',
   'ADOBE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);

-- GBS-1785, Vincent Yan, 2011/01/29
INSERT INTO KNOWN_FORMAT_TYPE VALUES (
  48, 'XLZ', 'XLZ File', 'xlz',
  'XML_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES ( 
49, 'MIF 9','Adobe Framemaker9','mif',
   'MIF_IMPORTED_EVENT',
   'MIF_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
50, 'FrameMaker9','Adobe Framemaker 9','mif',
   'FRAME_IMPORTED_EVENT',
   'MIF_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   51,'Passolo 2011','Passolo 2011','passolo',
   'PASSOLO_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   52,'INDD (CS5.5)','InDesign INDD CS5.5','indd_cs5.5',
   'ADOBE_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
   53,'Windows Portable Executable','Windows Portable Executable','windows_pe',
   'WINPE_IMPORTED_EVENT', 'XML_LOCALIZED_EVENT'
);

INSERT INTO KNOWN_FORMAT_TYPE VALUES (
54, '(Beta) New Office 2010 Filter','Microsoft Office 2010 document','office-xml',
   'MSOFFICE_IMPORTED_EVENT',
   'XML_LOCALIZED_EVENT'
);
-- 
--  NOTE: ids > 500 are reserved for customer-specific file formats.
-- 

COMMIT;
