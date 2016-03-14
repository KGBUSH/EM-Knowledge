package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

public class PatternMatchingResultBean {
	private String answer;
	private int score;
	private boolean empty = true;

	public boolean isEmpty() {
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

	public String toString() {
		return "answer:" + answer + ", score:" + score + ", empty is " + empty;
	}

}
