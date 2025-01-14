/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.workflow;

public class ScorecardData {
	
	private long workflowId;
	private String localeDisplayname;
	private String scoreComment;
	private String avgScore;
	
	public void setWorkflowId(long workflowId) {
		this.workflowId = workflowId;
	}
	public long getWorkflowId() {
		return workflowId;
	}
	public void setScoreComment(String scoreComment) {
		this.scoreComment = scoreComment;
	}
	public String getScoreComment() {
		return scoreComment;
	}
	public void setLocaleDisplayname(String localeDisplayname) {
		this.localeDisplayname = localeDisplayname;
	}
	public String getLocaleDisplayname() {
		return localeDisplayname;
	}
	public void setAvgScore(String avgScore) {
		this.avgScore = avgScore;
	}
	public String getAvgScore() {
		return avgScore;
	}
}
