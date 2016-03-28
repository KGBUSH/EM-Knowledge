package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

public class PatternMatchingResultBean implements Cloneable {
	private String answer;
	private double score;
	private boolean valid = false;
	private String orignalWord = "";
	
	public PatternMatchingResultBean clone() {   
		PatternMatchingResultBean clone = null;   
	    try{   
	        clone = (PatternMatchingResultBean) super.clone();   
	    }catch(CloneNotSupportedException e){   
	        throw new RuntimeException(e);  // won't happen   
	    }   
	      
	    return clone;   
	}  

	public boolean isValid() {
		return valid;
	}
	
	public void set2NotValid(){
		valid = false;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		valid = true;
		this.answer = answer;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String toString() {
		return "answer:" + answer + ", score:" + score + ", valid is " + valid;
	}

	public String getOrignalWord() {
		return orignalWord;
	}

	public void setOrignalWord(String orignalWord) {
		this.orignalWord = orignalWord;
	}

}
