package com.emotibot.util;

public class CUBean {
	private String text;
	private String questionType;
	private String score;
	private String uniqueID;
	private boolean isRewrite;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	
	public boolean isRewrite() {
		return isRewrite;
	}

	public void setRewrite(boolean isRewrite) {
		this.isRewrite = isRewrite;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("CUBean: text=").append(text).append("; questionType=").append(questionType).append("; score=")
				.append(score).append("; uniqueID=").append(uniqueID).append(";isRewrite=").append(isRewrite);
		
		return s.toString();
	}

}
