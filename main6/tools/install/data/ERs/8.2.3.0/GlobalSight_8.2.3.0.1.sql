# Change constraint of table IP_TM_INDEX, GBS-2534, by Vincent Yan, 2012/03/01
ALTER TABLE IP_TM_INDEX DROP FOREIGN KEY IP_TM_INDEX_LOCALE_ID;
ALTER TABLE IP_TM_INDEX DROP FOREIGN KEY IP_TM_INDEX_JOB_ID;
ALTER TABLE IP_TM_INDEX DROP FOREIGN KEY IP_TM_INDEX_SRC_ID;

ALTER TABLE IP_TM_INDEX ADD CONSTRAINT FK_IP_TM_LOCALE_ID FOREIGN KEY (LOCALE_ID) REFERENCES LOCALE(ID);
ALTER TABLE IP_TM_INDEX ADD CONSTRAINT FK_IP_TM_JOB_ID FOREIGN KEY (JOB_ID) REFERENCES JOB(ID);
ALTER TABLE IP_TM_INDEX ADD CONSTRAINT FK_IP_TM_SRC_ID FOREIGN KEY (SRC_ID) REFERENCES IP_TM_SRC_T(ID);

# Change constraint of table IP_TM_TRG_T
ALTER TABLE IP_TM_TRG_T DROP FOREIGN KEY IP_TM_SRC_L_LOCALE_ID;
ALTER TABLE IP_TM_TRG_T DROP FOREIGN KEY IP_TM_SRC_L_TU_ID;
ALTER TABLE IP_TM_TRG_T DROP FOREIGN KEY IP_TM_TRG_T_SRC_ID;

ALTER TABLE IP_TM_TRG_T ADD CONSTRAINT FK_IP_TM_TRG_T_LOCALE_ID FOREIGN KEY (LOCALE_ID) REFERENCES LOCALE(ID);
ALTER TABLE IP_TM_TRG_T ADD CONSTRAINT FK_IP_TM_TRG_T_TU_ID FOREIGN KEY (TU_ID) REFERENCES TRANSLATION_UNIT(ID);
ALTER TABLE IP_TM_TRG_T ADD CONSTRAINT FK_IP_TM_TRG_T_SRC_ID FOREIGN KEY (SRC_ID) REFERENCES IP_TM_SRC_T(ID);

# Change constraint of table IP_TM_SRC_T
ALTER TABLE IP_TM_SRC_T DROP FOREIGN KEY IP_TM_SRC_T_JOB;
ALTER TABLE IP_TM_SRC_T DROP FOREIGN KEY IP_TM_SRC_T_LOCALE;
ALTER TABLE IP_TM_SRC_T ADD CONSTRAINT FK_IP_TM_SRC_T_JOB FOREIGN KEY(JOB_ID) REFERENCES JOB(ID);
ALTER TABLE IP_TM_SRC_T ADD CONSTRAINT FK_IP_TM_SRC_T_LOCALE FOREIGN KEY(LOCALE_ID) REFERENCES LOCALE(ID);

# Change constraint of table IP_TM_SRC_L
ALTER TABLE IP_TM_SRC_L DROP FOREIGN KEY IP_TM_SRC_L_JOB;
ALTER TABLE IP_TM_SRC_L DROP FOREIGN KEY IP_TM_SRC_L_LOCALE;
ALTER TABLE IP_TM_SRC_L ADD CONSTRAINT FK_IP_TM_SRC_L_JOB FOREIGN KEY(JOB_ID) REFERENCES JOB(ID);
ALTER TABLE IP_TM_SRC_L ADD CONSTRAINT FK_IP_TM_SRC_L_LOCALE FOREIGN KEY(LOCALE_ID) REFERENCES LOCALE(ID);

# Change constraint of table IP_TM_TRG_L
ALTER TABLE IP_TM_TRG_L DROP FOREIGN KEY IP_TM_TRG_L_SRC_ID;
ALTER TABLE IP_TM_TRG_L DROP FOREIGN KEY IP_TM_TRG_L_LOCALE_ID;
ALTER TABLE IP_TM_TRG_L ADD CONSTRAINT FK_IP_TM_TRG_L_SRC_ID FOREIGN KEY(SRC_ID) REFERENCES IP_TM_SRC_L(ID);
ALTER TABLE IP_TM_TRG_L ADD CONSTRAINT FK_IP_TM_TRG_L_LOCALE_ID FOREIGN KEY(LOCALE_ID) REFERENCES LOCALE(ID);