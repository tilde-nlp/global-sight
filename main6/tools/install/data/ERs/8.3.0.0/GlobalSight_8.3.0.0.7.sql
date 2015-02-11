# GBS-2834: GlobalSight SQL Performance Improvements.(#3 & #4)

CREATE INDEX IDX_TERM_TBID_CID_LID ON TB_TERM (TBID, CID, LID);

CREATE INDEX IDX_CONCEPT_TBID_CID ON TB_CONCEPT (TBID, CID);

CREATE INDEX INDEX_REQUEST_BATCHID ON REQUEST(BATCH_ID);