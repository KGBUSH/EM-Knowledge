package com.emotibot.understanding;

import java.util.List;

import com.hankcs.hanlp.seg.common.Term;

public class NERBean {

	private String oldSentence;	// original sentence
	private String sentence;
	private List<Term> segPos;
	private List<String> segWordWithoutStopWord;
	private List<String> entitySet;
	private String uniqueID = "";
	private boolean isQuestion = false;

	public String toString() {
		return "NERBean:\n" + "uniqueID=" + uniqueID + ";\n userSentence=" + sentence + ";\n entitySet=" + entitySet
				+ ";\n segPos=" + segPos + ";\n segWordWithoutStopWord=" + segWordWithoutStopWord;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String userSentence) {
		this.sentence = userSentence;
	}

	public List<Term> getSegPos() {
		return segPos;
	}

	public void setSegPos(List<Term> segPos) {
		this.segPos = segPos;
	}

	public List<String> getSegWordWithoutStopWord() {
		return segWordWithoutStopWord;
	}

	public void setSegWordWithoutStopWord(List<String> segWordWithoutStopWord) {
		this.segWordWithoutStopWord = segWordWithoutStopWord;
	}

	public List<String> getEntitySet() {
		return entitySet;
	}

	public void setEntitySet(List<String> entitySet) {
		this.entitySet = entitySet;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getOldSentence() {
		return oldSentence;
	}

	public void setOldSentence(String oldSentence) {
		this.oldSentence = oldSentence;
	}

	public boolean isQuestion() {
		return isQuestion;
	}

	public void setQuestion(boolean isQuestion) {
		this.isQuestion = isQuestion;
	}

}
