package com.emotibot.WebService;

import org.xerial.snappy.SnappyOutputStream;

import com.emotibot.common.Common;
import com.emotibot.util.Tool;

import scala.Predef.any2stringadd;

public class AnswerBean {
	private double score = 0;
	private String answer = "";
	private String property = "";
	private boolean isValid = false;
	private String originalWord = ""; // the word in the sentence
	private String comments = "";

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		AnswerBean rhs;
		if (!(obj instanceof AnswerBean)) {
			return false;
		} else {
			rhs = (AnswerBean) obj;
		}

		if (this.score != rhs.score || !this.answer.equals(rhs.answer) || !this.property.equals(rhs.property)
				|| this.isValid != rhs.isValid || !this.originalWord.equals(rhs.originalWord)) {
			return false;
		}
		
		return true;

	}

	public AnswerBean returnAnswer(AnswerBean bean) {
		if (Tool.isStrEmptyOrNull(bean.getAnswer())) {
			System.out.println("answerBean.returnanswer: bean.answer is null or empty");
			bean.setScore(0);
		}
		return bean;
	}

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

	public void setComments(String s) {
		this.comments += " | " + s;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(property).append("(").append(originalWord).append(") : [").append(score).append("] ")
				.append(answer).append("; ");
		// buffer.append(" | comment:").append(comments);
		return buffer.toString();
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public boolean isValid() {
		if (answer.isEmpty() || score == 0) {
			return isValid;
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

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	
	public static void main(String[] args) {
		AnswerBean a = new AnswerBean();
		AnswerBean b = new AnswerBean();
		
		System.out.println("1="+a.equals(b));
		
		System.out.println("est = "+AnswerBean.class.isInstance(b));
	}
	
	
	
	
	
	
	
	
	

}
