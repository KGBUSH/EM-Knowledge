package com.emotibot.patternmatching;

public class PatternMatchingResultBean {
	private String answer;
	private int score;
	private boolean empty = true;
	
	public boolean isEmpty(){
		return empty;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		empty = false;
		this.answer = answer;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}

}
