-- 
--  This creates the database schema for the CXE product.
--  It is dependant on the CAP schema being created.


--  CXE SCHEMA -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

--  dependant on CAP schema being there - insert into SEQUENCE tables

-- CXE sequences
INSERT INTO SEQUENCE VALUES('XML_RULE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('SEGMENTATION_RULE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('FILE_PROFILE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('TEAMSITE_BRANCH_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('EXTENSION_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('CONNECTION_PROFILE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('DB_DISPATCH_PROFILE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('URL_LIST_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('CUSTOMER_DB_ACCESS_PROFILE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('CUSTOMER_COLUMN_DETAIL_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('EXPORT_LOCATION_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('TEAMSITE_SERVER_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('TEAMSITE_BACKING_STORE_SEQ', 1000);
INSERT INTO SEQUENCE VALUES('DOCUMENTUM_USER_SEQ', 1000);
COMMIT;

--  EXTENSION
-- 
DROP TABLE IF EXISTS EXTENSION CASCADE;
CREATE TABLE EXTENSION
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL,
  COMPANY_ID BIGINT
     NOT NULL,
  IS_ACTIVE CHAR(1)
     NOT NULL
     CHECK (IS_ACTIVE IN ('Y', 'N')),
  CONSTRAINT FK_EXTENSION_COMPANY_ID FOREIGN KEY(COMPANY_ID) REFERENCES COMPANY(ID) 
) AUTO_INCREMENT = 1000;

--  KNOWN_FORMAT_TYPE
-- 
DROP TABLE IF EXISTS KNOWN_FORMAT_TYPE CASCADE;
CREATE TABLE KNOWN_FORMAT_TYPE
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(60)
     NOT NULL
     UNIQUE,
  DESCRIPTION VARCHAR(4000),
  FORMAT_TYPE VARCHAR(127)
     NOT NULL,
  PRE_EXTRACT_EVENT VARCHAR(127),
  PRE_MERGE_EVENT VARCHAR(127)
) AUTO_INCREMENT = 1000;

--  XML_RULE
-- 
DROP TABLE IF EXISTS XML_RULE CASCADE;
CREATE TABLE XML_RULE
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL,
  COMPANY_ID BIGINT
     NOT NULL,
  DESCRIPTION VARCHAR(4000),
  RULE_TEXT MEDIUMTEXT,
  UNIQUE(NAME, COMPANY_ID),
  CONSTRAINT FK_XML_RULE_CASCADE_COMPANY_ID FOREIGN KEY(COMPANY_ID) REFERENCES COMPANY(ID) 
) AUTO_INCREMENT = 1000;

--  XML_DTD
-- 
DROP TABLE IF EXISTS XML_DTD CASCADE;
CREATE TABLE XML_DTD
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(40)
     NOT NULL,
  COMPANY_ID BIGINT
     NOT NULL,
  DESCRIPTION VARCHAR(4000),
  ADD_COMMENT CHAR(1)
    NOT NULL
    CHECK(ADD_COMMENT IN ('Y', 'N')),
  SEND_EMAIL CHAR(1)
    NOT NULL
    CHECK(SEND_EMAIL IN ('Y', 'N')),
  CONSTRAINT FK_XML_DTD_CASCADE_COMPANY_ID FOREIGN KEY(COMPANY_ID) REFERENCES COMPANY(ID) 
) AUTO_INCREMENT = 1000;

--  REMOTE_IP
-- 
DROP TABLE IF EXISTS REMOTE_IP CASCADE;
CREATE TABLE REMOTE_IP (
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  IP varchar(40) NOT NULL,                                                                          
  DESCRIPTION varchar(4000)
) AUTO_INCREMENT = 1000;

--  SEGMENTATION_RULE
-- 
DROP TABLE IF EXISTS SEGMENTATION_RULE CASCADE;
CREATE TABLE SEGMENTATION_RULE
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL,
  COMPANY_ID BIGINT
     NOT NULL,
  SR_TYPE INT(4) NOT NULL DEFAULT 0,
  DESCRIPTION VARCHAR(4000),
  RULE_TEXT MEDIUMTEXT,
  IS_ACTIVE CHAR(1)
    NOT NULL
    CHECK(IS_ACTIVE IN ('Y', 'N')),
  IS_DEFAULT CHAR(1)
    NOT NULL DEFAULT 'N'
    CHECK(IS_DEFAULT IN ('Y', 'N')),
  CONSTRAINT FK_SEGMENTATIN_RULE_CASCADE FOREIGN KEY(COMPANY_ID) REFERENCES COMPANY(ID) 
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS SEGMENTATION_RULE_TM_PROFILE CASCADE;
CREATE TABLE SEGMENTATION_RULE_TM_PROFILE
(
 SEGMENTATION_RULE_ID BIGINT,
 TM_PROFILE_ID BIGINT,
 PRIMARY KEY (TM_PROFILE_ID, SEGMENTATION_RULE_ID),
 CONSTRAINT FK_SEGMENTATION_RULE_TM_PROFILE_TM_PROFILE_ID FOREIGN KEY(SEGMENTATION_RULE_ID) REFERENCES SEGMENTATION_RULE(ID) 
);

DROP TABLE IF EXISTS ELOQUA_CONNECTOR CASCADE;
CREATE TABLE ELOQUA_CONNECTOR(
ID BIGINT
    AUTO_INCREMENT
    PRIMARY KEY,
  NAME VARCHAR(300)
    NOT NULL,
  COMPANY_ID bigint(20) unsigned NOT NULL,
  COMPANY VARCHAR(300) NOT NULL,
  USER_NAME VARCHAR(300) NOT NULL,
  PASSWORD VARCHAR(300) NOT NULL,
  DESCRIPTION varchar(4000) NULL,
  URL VARCHAR(300) NOT NULL
) ENGINE=INNODB;

DROP TABLE IF EXISTS CONNECTOR_MINDTOUCH CASCADE;
CREATE TABLE CONNECTOR_MINDTOUCH
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(300) NOT NULL,
  DESCRIPTION VARCHAR(4000) NULL,
  URL VARCHAR(300) NOT NULL,
  USER_NAME VARCHAR(300) NOT NULL,
  PASSWORD VARCHAR(300) NOT NULL,
  IS_ACTIVE CHAR(1) NOT NULL CHECK (IS_ACTIVE IN ('Y', 'N')),
  COMPANY_ID BIGINT(20) UNSIGNED NOT NULL
) ENGINE=INNODB;

DROP TABLE IF EXISTS CONNECTOR_GIT CASCADE;
CREATE TABLE CONNECTOR_GIT(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(300) NOT NULL,
  DESCRIPTION VARCHAR(4000) NULL,
  URL VARCHAR(300) NOT NULL,
  USER_NAME VARCHAR(300) NOT NULL,
  PASSWORD VARCHAR(300) NOT NULL,
  PRIVATE_KEY_FILE VARCHAR(300) NOT NULL,
  BRANCH VARCHAR(100) NULL DEFAULT 'master',
  IS_ACTIVE CHAR(1) NOT NULL CHECK (IS_ACTIVE IN ('Y', 'N')),
  COMPANY_ID BIGINT(20) UNSIGNED NOT NULL
) ENGINE=INNODB;

DROP TABLE IF EXISTS CONNECTOR_GIT_JOB CASCADE;
CREATE TABLE CONNECTOR_GIT_JOB(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  GIT_CONNECTOR_ID BIGINT,
  JOB_ID BIGINT
) ENGINE=INNODB;

DROP TABLE IF EXISTS CONNECTOR_GIT_FILE_MAPPING CASCADE;
CREATE TABLE CONNECTOR_GIT_FILE_MAPPING(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  SOURCE_LOCALE VARCHAR(30) NOT NULL,
  SOURCE_MAPPING_PATH VARCHAR(300) NOT NULL,
  TARGET_LOCALE VARCHAR(30) NOT NULL,
  TARGET_MAPPING_PATH VARCHAR(300) NOT NULL,
  GIT_CONNECTOR_ID BIGINT,
  PARENT_ID BIGINT,
  IS_ACTIVE CHAR(1) NOT NULL CHECK (IS_ACTIVE IN ('Y', 'N')),
  COMPANY_ID BIGINT(20) UNSIGNED NOT NULL
) ENGINE=INNODB;

DROP TABLE IF EXISTS CONNECTOR_GIT_CACHE_FILE CASCADE;
CREATE TABLE CONNECTOR_GIT_CACHE_FILE(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  FILE_PATH VARCHAR(300) NOT NULL,
  GIT_CONNECTOR_ID BIGINT,
  SRC_FILE_PATH VARCHAR(300) NOT NULL,
  DST_FILE_PATH VARCHAR(300) NOT NULL
) ENGINE=INNODB;

--  FILE_PROFILE
-- 
DROP TABLE IF EXISTS FILE_PROFILE CASCADE;
CREATE TABLE FILE_PROFILE
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,
  NAME VARCHAR(60)
	 NOT NULL,
  DESCRIPTION VARCHAR(4000),
  KNOWN_FORMAT_TYPE_ID BIGINT
     NOT NULL,
  CODE_SET VARCHAR(30),
  XML_DTD_ID BIGINT,
  L10N_PROFILE_ID BIGINT
     NOT NULL,
  DEFAULT_EXPORT_STF CHAR(1) 
     NOT NULL
     CHECK (DEFAULT_EXPORT_STF IN ('Y','N')),
  TIMESTAMP DATETIME
	 NOT NULL,
  IS_ACTIVE CHAR(1)
     NOT NULL
     CHECK (IS_ACTIVE IN ('Y', 'N')),
  FILTER_ID INTEGER default -2,
  FILTER_TABLE_NAME VARCHAR(45),
  QA_FILTER_ID BIGINT,
  COMPANYID BIGINT
     NOT NULL , SCRIPT_ON_IMPORT VARCHAR(400), SCRIPT_ON_EXPORT VARCHAR(400),
  NEW_ID BIGINT,
  TERMINOLOGY_APPROVAL int(1) DEFAULT 0,
  XLF_SOURCE_AS_UNTRANSLATED_TARGET int(1) DEFAULT 0,
  REFERENCE_FP bigint(20) DEFAULT 0,
  BOM_TYPE SMALLINT DEFAULT 0,
  CONSTRAINT FK_FILE_PROFILE_KNOWN_FORMAT_ID FOREIGN KEY(KNOWN_FORMAT_TYPE_ID) REFERENCES  KNOWN_FORMAT_TYPE(ID) ,
  CONSTRAINT FK_FILE_PROFILE_CODE_SET FOREIGN KEY(CODE_SET) REFERENCES CODE_SET(CODE_SET),
  CONSTRAINT FK_XML_RULE_COMPANY_ID  FOREIGN KEY(COMPANYID) REFERENCES COMPANY(ID) 
) AUTO_INCREMENT = 1000;

--  FILE_PROFILE_EXTENSION
-- 
DROP TABLE IF EXISTS FILE_PROFILE_EXTENSION CASCADE;
CREATE TABLE FILE_PROFILE_EXTENSION
(
  FILE_PROFILE_ID BIGINT
     NOT NULL,
  EXTENSION_ID BIGINT
     NOT NULL,
  PRIMARY KEY (FILE_PROFILE_ID, EXTENSION_ID),
  CONSTRAINT FK_FILE_PROFILE_EXTENSION_FILE_PROFILE_ID FOREIGN KEY(FILE_PROFILE_ID) REFERENCES  FILE_PROFILE(ID) ,
  CONSTRAINT FK_FILE_PROFILE_EXTENSION_EXTENSION_ID FOREIGN KEY(EXTENSION_ID) REFERENCES EXTENSION(ID) 
);

--  TEAMSITE_SERVER
-- 
DROP TABLE IF EXISTS TEAMSITE_SERVER CASCADE;
CREATE TABLE TEAMSITE_SERVER
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  COMPANY_ID BIGINT
     NOT NULL,
  CONSTRAINT FK_ID_TEAMSITE_SERVER_COMPANY_ID FOREIGN KEY(COMPANY_ID) REFERENCES COMPANY(ID),
  NAME VARCHAR(40)
     NOT NULL,
  DESCRIPTION VARCHAR(4000),
  OPERATING_SYSTEM VARCHAR(40)
     NOT NULL,
  EXPORT_PORT INT(8)
     NOT NULL,
  IMPORT_PORT INT(8)
     NOT NULL,
  PROXY_PORT INT(8)
     NOT NULL,
  HOME VARCHAR(4000)
     NOT NULL,
  USER_NAME VARCHAR(40)
     NOT NULL,
  USER_PASS VARCHAR(40)
     NOT NULL,
  USER_TYPE VARCHAR(40)
     NOT NULL,
  MOUNT_DIR VARCHAR(40)
     NOT NULL,
  ALLOW_REIMPORT CHAR(1)
     NOT NULL
     CHECK (ALLOW_REIMPORT IN ('Y','N')),
  `TIMESTAMP` DATETIME
     NOT NULL
) AUTO_INCREMENT = 1000;

--  TEAMSITE_BACKING_STORE
-- 
DROP TABLE IF EXISTS TEAMSITE_BACKING_STORE CASCADE;
CREATE TABLE TEAMSITE_BACKING_STORE
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(40)
     NOT NULL
) AUTO_INCREMENT = 1000;

--  TEAMSITE_SERVER_BACKING_STORE
-- 
DROP TABLE IF EXISTS TEAMSITE_SERVER_BACKING_STORE CASCADE;
CREATE TABLE TEAMSITE_SERVER_BACKING_STORE
(
  TEAMSITE_SERVER_ID BIGINT
     NOT NULL,
  TEAMSITE_BACKING_STORE_ID BIGINT
     NOT NULL,
   PRIMARY KEY (TEAMSITE_SERVER_ID, TEAMSITE_BACKING_STORE_ID),
  CONSTRAINT FK_TEAMSITE_SERVER_STORE_TEAMSITE_SERVER_ID FOREIGN KEY(TEAMSITE_SERVER_ID)  REFERENCES TEAMSITE_SERVER(ID),
  CONSTRAINT FK_TEAMSITE_SERVER_STORE_TEAMSITE_BACKING_STORE_ID FOREIGN KEY(TEAMSITE_BACKING_STORE_ID) REFERENCES TEAMSITE_BACKING_STORE(ID)
);

--  TEAMSITE_BRANCH_LANGUAGE
-- 
DROP TABLE IF EXISTS TEAMSITE_BRANCH_LANGUAGE CASCADE;
CREATE TABLE TEAMSITE_BRANCH_LANGUAGE
(
	ID BIGINT
	   AUTO_INCREMENT
	   PRIMARY KEY, 
	SOURCE_BRANCH VARCHAR(127)
	   NOT NULL,
	TARGET_BRANCH VARCHAR(127)
	   NOT NULL,	
	TARGET_LOCALE_ID BIGINT
	   NOT NULL,
	TEAMSITE_SERVER_ID BIGINT,
	TEAMSITE_STORE_ID BIGINT,
	CONSTRAINT FK_TEAMSIT_BRANCH_LANGUAGE_TARGET_ID FOREIGN KEY(TARGET_LOCALE_ID) REFERENCES LOCALE(ID),
	CONSTRAINT FK_TEAMSITE_SERVER_ID FOREIGN KEY(TEAMSITE_SERVER_ID) REFERENCES TEAMSITE_SERVER(ID),
	CONSTRAINT FK_TEAMSITE_STORE_ID FOREIGN KEY(TEAMSITE_STORE_ID) REFERENCES TEAMSITE_BACKING_STORE(ID)
) AUTO_INCREMENT = 1000;


--  URL_LIST
-- 
DROP TABLE IF EXISTS URL_LIST CASCADE;
CREATE TABLE URL_LIST
(
	ID BIGINT
	   AUTO_INCREMENT
	   PRIMARY KEY,
	NAME VARCHAR(40)
	   NOT NULL
	   UNIQUE,
    DESCRIPTION VARCHAR(4000),
    XML_URL TEXT
) AUTO_INCREMENT = 1000;


--  CONNECTION_PROFILE
-- 
DROP TABLE IF EXISTS CONNECTION_PROFILE CASCADE;
CREATE TABLE CONNECTION_PROFILE
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL
	 UNIQUE,
  DESCRIPTION VARCHAR(4000),
  DRIVER VARCHAR(127)
     NOT NULL,
  `CONNECTION` VARCHAR(127)
	 NOT NULL,
  USER_NAME VARCHAR(80)
	 NOT NULL,
  PASSWORD VARCHAR(127)
	 NOT NULL
) AUTO_INCREMENT = 1000;


--  CUSTOMER_DB_ACCESS_PROFILE
-- 
DROP TABLE IF EXISTS CUSTOMER_DB_ACCESS_PROFILE CASCADE;
CREATE TABLE CUSTOMER_DB_ACCESS_PROFILE
(
	ID BIGINT
	   AUTO_INCREMENT
	   PRIMARY KEY,
    NAME VARCHAR(40)
       NOT NULL
       UNIQUE,
    DESCRIPTION VARCHAR(4000),
    CHECKOUT_SQL TEXT,
    CHECKOUT_CONNECT_ID BIGINT,
    PREVIEW_INSERT_SQL TEXT,
    PREVIEW_UPDATE_SQL TEXT,
    PREVIEW_CONNECT_ID BIGINT,
    CHECKIN_INSERT_SQL TEXT,
    CHECKIN_UPDATE_SQL TEXT,
    CHECKIN_CONNECT_ID BIGINT,
    PREVIEW_URL_ID INTEGER,
    L10N_PROFILE_ID BIGINT
       NOT NULL,
    CODE_SET VARCHAR(30),
    `TIMESTAMP` DATETIME
       NOT NULL,
	CONSTRAINT FK_CUSTOMER_DB_ACCESS_PROFILE_ID_CHECKOUT_ID FOREIGN	KEY(CHECKOUT_CONNECT_ID) REFERENCES CONNECTION_PROFILE(ID),
	CONSTRAINT FK_CUSTOMER_PREVIEWpID FOREIGN KEY(PREVIEW_CONNECT_ID) REFERENCES CONNECTION_PROFILE(ID),
       	CONSTRAINT FK_CUSTOMER_CHECKiD FOREIGN KEY(CHECKOUT_CONNECT_ID) REFERENCES CONNECTION_PROFILE(ID),
       	CONSTRAINT FK_L10N_PROFILE_ID FOREIGN KEY(L10N_PROFILE_ID) REFERENCES	L10N_PROFILE(ID),
       	CONSTRAINT FK_CUSTOMER_DB_ACCESS_PROFILE_CODE_SET FOREIGN KEY(CODE_SET) REFERENCES CODE_SET(CODE_SET)
) AUTO_INCREMENT = 1000; 

--  CUSTOMER_COLUMN_DETAIL
-- 
DROP TABLE IF EXISTS CUSTOMER_COLUMN_DETAIL CASCADE;
CREATE TABLE CUSTOMER_COLUMN_DETAIL
(
	ID BIGINT
	   AUTO_INCREMENT
	   PRIMARY KEY,
    KNOWN_FORMAT_TYPE BIGINT
       REFERENCES KNOWN_FORMAT_TYPE,
    COLUMN_NUMBER INT(10)
       NOT NULL,
    COLUMN_NAME VARCHAR(127)
       NOT NULL,
    DB_PROFILE_ID BIGINT
       NOT NULL,
    TABLE_NAME VARCHAR(127),
    XML_RULE_ID BIGINT,
    CONTENT_MODE INT(2),
    COLUMN_LABEL VARCHAR(127),
    CONSTRAINT FK_CUSTOMER_COLUMN_DETAL_XML_RULE_ID FOREIGN KEY(XML_RULE_ID) REFERENCES XML_RULE(ID),
    CONSTRAINT FK_CUSTOMER_COLUMN_DETAL_KNOWN_FORMAT_TYPE FOREIGN KEY(KNOWN_FORMAT_TYPE) REFERENCES KNOWN_FORMAT_TYPE(ID),
    CONSTRAINT FK_CUSTOMER_COLUMN_DETAIL_KNOWN_FORMAT FOREIGN KEY(DB_PROFILE_ID) REFERENCES CUSTOMER_DB_ACCESS_PROFILE(ID)
) AUTO_INCREMENT = 1000;

--  PRSXML_STORAGE
-- 
DROP TABLE IF EXISTS PRSXML_STORAGE CASCADE;
CREATE TABLE PRSXML_STORAGE
(
	NAME VARCHAR(127)
	     PRIMARY KEY,
    PRSXML TEXT
);

--  DB_DISPATCH_PROFILE
-- 
DROP TABLE IF EXISTS DB_DISPATCH_PROFILE CASCADE;
CREATE TABLE DB_DISPATCH_PROFILE
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL
	 UNIQUE,
  DESCRIPTION VARCHAR(4000),
  TABLE_NAME VARCHAR(127)
	 NOT NULL,
  CONNECTION_ID BIGINT
     NOT NULL,
  RECORDS_PER_PAGE INT(5),
  PAGES_PER_BATCH INT(5),
  MAX_ELAPSED_MILLIS BIGINT(15),
    CONSTRAINT FK_DB_DISPATCH_PROFILE_CONNECT_ID FOREIGN KEY(CONNECTION_ID) REFERENCES CONNECTION_PROFILE(ID)
) AUTO_INCREMENT = 1000;

--  TASK_CLASSIFIER
-- 
DROP TABLE IF EXISTS TASK_CLASSIFIER CASCADE;
CREATE TABLE TASK_CLASSIFIER
(
	ID BIGINT
	   AUTO_INCREMENT
	   PRIMARY KEY,
    DB_DISPATCH_PROFILE_ID BIGINT,
    SERIALIZATION BLOB,
    CONSTRAINT FK_TASK_CLASSIFIER_DB_DISPATCH_PROFILE_ID FOREIGN
    KEY(DB_DISPATCH_PROFILE_ID) REFERENCES DB_DISPATCH_PROFILE(ID)
) AUTO_INCREMENT = 1000;

--  EXPORT_LOCATION
-- 
DROP TABLE IF EXISTS EXPORT_LOCATION CASCADE;
CREATE TABLE EXPORT_LOCATION
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  NAME VARCHAR(40)
	 NOT NULL,
  DESCRIPTION VARCHAR(4000),
  LOCATION VARCHAR(4000),
  COMPANY_ID BIGINT
     NOT NULL,
  UNIQUE(NAME, COMPANY_ID),
    CONSTRAINT FK_EXPORT_LOCATION_COMPANY_ID FOREIGN KEY(COMPANY_ID) REFERENCES
    COMPANY(ID)
) AUTO_INCREMENT = 1000;

--  DOCUMTNUM_USER
-- 
DROP TABLE IF EXISTS DOCUMENTUM_USER CASCADE;
CREATE TABLE DOCUMENTUM_USER
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,
  DOCUMENTUM_USER_ID VARCHAR(80)
	 NOT NULL,
  DOCUMENTUM_USER_PASSWORD VARCHAR(127)
	 NOT NULL,
  DOCUMENTUM_DOCBASE VARCHAR(80)
) AUTO_INCREMENT = 1000;

--  default segmentation rule
-- 
DELETE FROM SEGMENTATION_RULE WHERE ID = 1;

INSERT INTO SEGMENTATION_RULE(ID, NAME, COMPANY_ID, SR_TYPE, DESCRIPTION, RULE_TEXT, IS_ACTIVE) 
       VALUES (1, 'default', 1, 0, 'Default segmentation rule.', 'default', 'Y');

-- For gbs-799
DROP TABLE IF EXISTS ATTRIBUTE_SET CASCADE;
CREATE TABLE ATTRIBUTE_SET
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(40),
  DESCRIPTION  VARCHAR(4000),
  COMPANY_ID BIGINT NOT NULL,
  CONSTRAINT FK_ATTRIBUTE_SET_COMPANYID FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE CASCADE;
CREATE TABLE ATTRIBUTE
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(40) UNIQUE NOT NULL,
  DISPLAY_NAME VARCHAR(40) NOT NULL,
  DESCRIPTION  VARCHAR(4000),
  COMPANY_ID BIGINT NOT NULL,
  VISIBLE CHAR(1) NOT NULL CHECK(VISIBLE IN ('Y','N')),
  EDITABLE CHAR(1) NOT NULL CHECK(EDITABLE IN ('Y','N')),
  REQUIRED CHAR(1) NOT NULL CHECK(REQUIRED IN ('Y','N')),
  CONDITION_ID BIGINT NOT NULL,
  TYPE VARCHAR(40),
  CONSTRAINT FK_ATTRIBUTE_COMPANYID FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_LIST CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_LIST
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  MULTIPLE CHAR(1) NOT NULL CHECK(MULTIPLE IN ('Y','N'))
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_INT CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_INT
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  MAX BIGINT,
  MIN BIGINT
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_DATE CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_DATE
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_TEXT CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_TEXT
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  LENGTH BIGINT
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_FLOAT CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_FLOAT
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  MAX DOUBLE,
  MIN DOUBLE,
  DEFINITION BIGINT
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_CONDITION_FILE CASCADE;
CREATE TABLE ATTRIBUTE_CONDITION_FILE
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS SELECT_OPTION CASCADE;
CREATE TABLE SELECT_OPTION
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  VALUE VARCHAR(300),
  LIST_CONDITION_ID BIGINT not null
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS ATTRIBUTE_SET_ATTRIBUTE CASCADE;
CREATE TABLE ATTRIBUTE_SET_ATTRIBUTE
(
  ATTRIBUTE_ID BIGINT,
  SET_ID BIGINT,
  PRIMARY KEY (SET_ID, ATTRIBUTE_ID)
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS JOB_ATTRIBUTE CASCADE;
CREATE TABLE JOB_ATTRIBUTE
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  INTEGER_VALUE INTEGER,
  FLOAT_VALUE DOUBLE,
  STRING_VALUE VARCHAR(4000),
  DATE_VALUE DATETIME,
  JOB_ID BIGINT NOT NULL,
  ATTRIBUTE_ID BIGINT NOT NULL
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS PROJECT_TM_ATTRIBUTE CASCADE;
CREATE TABLE `PROJECT_TM_ATTRIBUTE` (
  `ID` bigint AUTO_INCREMENT PRIMARY KEY,
  `TM_ID` bigint NOT NULL,
  `ATT_NAME` VARCHAR(100) NOT NULL,
  `SET_TYPE` VARCHAR(100) NOT NULL
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS TM_PROFILE_ATTRIBUTE CASCADE;
CREATE TABLE `TM_PROFILE_ATTRIBUTE` (
  `ID` bigint AUTO_INCREMENT PRIMARY KEY,
  `TMP_ID` bigint NOT NULL,
  `ATT_NAME` VARCHAR(100) NOT NULL,
  `OPERATOR` VARCHAR(100) NOT NULL,
  `VALUE_TYPE` VARCHAR(100) NOT NULL,
  `VALUE_DATA` VARCHAR(100) NULL,
  `AND_OR` VARCHAR(12) NOT NULL,
  `PRIORITY_ORDER` INT(2) NOT NULL
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS PROJECT_TM_TU_T_PROP CASCADE;
CREATE TABLE `PROJECT_TM_TU_T_PROP` (
  `ID` bigint AUTO_INCREMENT PRIMARY KEY,
  `TU_ID` bigint NOT NULL,
  `PROP_TYPE` VARCHAR(200) NOT NULL,
  `PROP_VALUE` VARCHAR(200) NOT NULL
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS FILE_VALUE_ITEM CASCADE;
CREATE TABLE FILE_VALUE_ITEM
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  PATH VARCHAR(300),
  JOB_ATTRIBUTE_ID BIGINT NOT NULL
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS JOB_ATTRIBUTE_SELECT_OPTION CASCADE;
CREATE TABLE JOB_ATTRIBUTE_SELECT_OPTION
(
  JOB_ATTRIBUTE_ID BIGINT,
  SELECT_OPTION_ID BIGINT,
  PRIMARY KEY (JOB_ATTRIBUTE_ID, SELECT_OPTION_ID)
) AUTO_INCREMENT = 1000;

CREATE TABLE ATTRIBUTE_CLONE
(
  ID BIGINT AUTO_INCREMENT PRIMARY KEY,
  NAME VARCHAR(40) NOT NULL,
  DISPLAY_NAME VARCHAR(40) NOT NULL,
  DESCRIPTION  VARCHAR(4000),
  COMPANY_ID BIGINT NOT NULL,
  VISIBLE CHAR(1) NOT NULL CHECK(VISIBLE IN ('Y','N')),
  EDITABLE CHAR(1) NOT NULL CHECK(EDITABLE IN ('Y','N')),
  REQUIRED CHAR(1) NOT NULL CHECK(REQUIRED IN ('Y','N')),
  CONDITION_ID BIGINT NOT NULL,
  TYPE VARCHAR(40),
  CONSTRAINT FK_ATTRIBUTE_CLONE_COMPANYID FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)
) AUTO_INCREMENT = 1000;

DROP TABLE IF EXISTS REMOVED_TAG CASCADE;
CREATE TABLE REMOVED_TAG
(
  ID BIGINT
     AUTO_INCREMENT
	 PRIMARY KEY,	 
  PREFIX_STRING 
     MEDIUMTEXT
	 NOT NULL,	 
  SUFFIX_STRING 
     MEDIUMTEXT
	 NOT NULL,
  TU_ID BIGINT
     NOT NULL
) AUTO_INCREMENT = 1000;

CREATE INDEX IDX_TU_ID ON REMOVED_TAG(TU_ID);

DROP TABLE IF EXISTS REMOVED_PREFIX_TAG CASCADE;
CREATE TABLE REMOVED_PREFIX_TAG
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,    
  STRING 
     MEDIUMTEXT
     NOT NULL,
  TU_ID BIGINT
     NOT NULL
) AUTO_INCREMENT = 1000;

CREATE INDEX IDX_TU_ID ON REMOVED_PREFIX_TAG(TU_ID);

DROP TABLE IF EXISTS REMOVED_SUFFIX_TAG CASCADE;
CREATE TABLE REMOVED_SUFFIX_TAG
(
  ID BIGINT
     AUTO_INCREMENT
     PRIMARY KEY,    
  STRING 
     MEDIUMTEXT
     NOT NULL,
  TU_ID BIGINT
     NOT NULL
) AUTO_INCREMENT = 1000;

CREATE TABLE `LOGIN_ATTEMPT_CONFIG` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ENABLE` CHAR(1) NOT NULL DEFAULT 'N',
  `BLOCK_TIME` bigint(20) NOT NULL DEFAULT 60,
  `MAX_TIME` int NOT NULL DEFAULT 10,
  `EXEMPT_IPS` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;

CREATE TABLE `LOGIN_ATTEMPT` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `IP` varchar(50) NOT NULL,
  `BLOCK_TIME` timestamp NOT NULL,
  `COUNT` int NOT NULL DEFAULT 10,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8;


CREATE INDEX IDX_TU_ID ON REMOVED_SUFFIX_TAG(TU_ID);

CREATE INDEX IDX_JOB_ATTRIBUTE_JOB_ID ON JOB_ATTRIBUTE(JOB_ID);
CREATE INDEX IDX_JOB_ATTRIBUTE_ATTRIBUTE_ID ON JOB_ATTRIBUTE(ATTRIBUTE_ID);
CREATE INDEX IDX_ATTRIBUTE_COMPANY_ID ON ATTRIBUTE(COMPANY_ID);
CREATE INDEX IDX_ATTRIBUTE_NAME ON ATTRIBUTE(NAME);
CREATE INDEX IDX_ATTRIBUTE_DISPLAY_NAME ON ATTRIBUTE(DISPLAY_NAME);
CREATE INDEX IDX_ATTRIBUTE_SET_COMPANY_ID ON ATTRIBUTE_SET(COMPANY_ID);
CREATE INDEX IDX_ATTRIBUTE_CLONE_DISPLAY_NAME ON ATTRIBUTE_CLONE(DISPLAY_NAME);
CREATE INDEX IDX_ATTRIBUTE_CLONE_COMPANY_ID ON ATTRIBUTE_CLONE(COMPANY_ID);
CREATE INDEX IDX_PROJECT_TM_ATTRIBUTE_TM_ID ON PROJECT_TM_ATTRIBUTE(TM_ID);
CREATE INDEX IDX_TM_PROFILE_ATTRIBUTE_TMP_ID ON TM_PROFILE_ATTRIBUTE(TMP_ID);
CREATE INDEX PROJECT_TM_TU_T_PROP_TU_ID ON PROJECT_TM_TU_T_PROP(TU_ID);

insert into ATTRIBUTE_CONDITION_LIST(MULTIPLE) values('n');
set @LIST_CONDITION_ID = LAST_INSERT_ID();
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('not set',@LIST_CONDITION_ID);
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('yes',@LIST_CONDITION_ID);
insert into SELECT_OPTION(VALUE, LIST_CONDITION_ID) values('no',@LIST_CONDITION_ID);
insert into ATTRIBUTE(name, display_name,description,company_id,visible,editable,required,condition_id,type)
values('protect_international_cost_center', 'international \(non-US\) cost center', 'submitter\'s cost center is an international \(non-US\) cost center or not', 1, 'Y','Y','Y',@LIST_CONDITION_ID,'choice list');
insert into ATTRIBUTE_CONDITION_TEXT(LENGTH) values(null);
set @LIST_CONDITION_ID = LAST_INSERT_ID();
insert into ATTRIBUTE(name, display_name,description,company_id,visible,editable,required,condition_id,type)
values('protect_cost_center', 'cost center', 'The cost center', 1, 'Y','Y','N',@LIST_CONDITION_ID,'text');
