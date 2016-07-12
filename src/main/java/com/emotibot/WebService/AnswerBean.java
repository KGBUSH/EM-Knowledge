package com.emotibot.WebService;

import com.emotibot.util.Tool;

public class AnswerBean implements Cloneable {
	private double score = 0;
	private String answer = "";
	private String property = "";
	private boolean isValid = false;
	private String originalWord = ""; // the word in the sentence
	private String comments = "";
	private String intent = "";
	private boolean isIntent = false;
	private volatile int hashcode;
	
	@Override 
	public Object clone() {   
		AnswerBean clone = null;   
	    try{   
	        clone = (AnswerBean) super.clone();   
	    }catch(CloneNotSupportedException e){   
	        throw new RuntimeException(e);  // won't happen   
	    }   
	      
	    return clone;   
	}  

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if( obj == this)
			return true;
		if (!(obj instanceof AnswerBean)) {
			return false;
		}
		AnswerBean rhs = (AnswerBean) obj;
		
		return this.score == rhs.score && this.answer.equals(rhs.answer) && this.property.equals(rhs.property)
				&& this.isValid == rhs.isValid && this.originalWord.equals(rhs.originalWord);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int result = hashcode;
		if(result == 0){
			result = 17;
			long f = Double.doubleToLongBits(score);
			int i = (int)(f^(f >>> 32));
			result = 31*result + i;
			result = 31*result + (isValid ? 1:0);
			result = 31*result + answer.hashCode();
			result = 31*result + property.hashCode();
			result = 31*result + originalWord.hashCode();
			hashcode = result;
		}
		return result;
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
	
	
	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public boolean isIntent() {
		return isIntent;
	}

	public void setIntent(boolean isIntent) {
		this.isIntent = isIntent;
	}

	public static void main(String[] args) {
		AnswerBean a = new AnswerBean();
		AnswerBean b = new AnswerBean();
		
		System.out.println("1="+a.equals(b));
		
		System.out.println("est = "+AnswerBean.class.isInstance(b));
	}
	
}
