package com.emotibot.WebService;

public class AnswerBean {
	private double score = 0;
	private String answer = "";
	private String property = "";
	private boolean isValid = false;
	private String originalWord = "";	// the word in the sentence

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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(property).append("(").append(originalWord).append(") : ").append(answer).append("; ");
		buffer.append(score).append("\r\n");
		return buffer.toString();
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getOriginalWord() {
		return originalWord;
	}

	public void setOriginalWord(String originalWord) {
		this.originalWord = originalWord;
	}

}
