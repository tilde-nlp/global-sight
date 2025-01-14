## GBS-4140: Native JSON Filter.
INSERT INTO known_format_type (ID, NAME, DESCRIPTION, FORMAT_TYPE, PRE_EXTRACT_EVENT, PRE_MERGE_EVENT)
VALUES (57, 'JSON','JavaScript Object Notation','json','JSON_IMPORTED_EVENT','JSON_LOCALIZED_EVENT');

INSERT INTO EXTENSION (NAME, COMPANY_ID, IS_ACTIVE) (
   SELECT 'json', c.id, 'Y' FROM company c WHERE c.id NOT IN 
     (SELECT DISTINCT COMPANY_ID FROM extension WHERE NAME = 'json'));

CREATE TABLE `filter_json`
(
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILTER_NAME` varchar(255) NOT NULL,
  `FILTER_DESCRIPTION` varchar(400) DEFAULT NULL,
  `ENABLE_SID_SUPPORT` char(1) NOT NULL,
  `ELEMENT_POST_FILTER_ID` bigint(20) DEFAULT NULL,
  `ELEMENT_POST_FILTER_TABLE_NAME` varchar(100) DEFAULT NULL,
  `BASE_FILTER_ID` bigint(20) DEFAULT NULL,
  `COMPANY_ID` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT = 1;
