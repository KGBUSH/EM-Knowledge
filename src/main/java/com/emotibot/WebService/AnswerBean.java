package com.emotibot.WebService;

public class AnswerBean {
	private  double score=0;
	private String answer="";
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
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(answer).append("; ");
		buffer.append(score).append("\r\n");
		return buffer.toString();
	}

}
