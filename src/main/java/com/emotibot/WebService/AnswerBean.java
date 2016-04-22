package com.emotibot.WebService;

import com.emotibot.common.Common;

public class AnswerBean {
	private double score = 0;
	private String answer = "";
	private String property = "";
	private boolean isValid = false;
	private String originalWord = ""; // the word in the sentence
	private String comments = "";

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String s){
		this.comments += " | "+s;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(property).append("(").append(originalWord).append(") : [").append(score).append("] ")
				.append(answer).append("; ");
		return buffer.toString();
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public boolean isValid() {
		if(answer.isEmpty() || score == 0){
			return false;
		} else {
			return true;
		}
	}

	// public void setValid(boolean isValid) {
	// this.isValid = isValid;
	// }

	public String getOriginalWord() {
		return originalWord;
	}

	public void setOriginalWord(String originalWord) {
		this.originalWord = originalWord;
	}

}
