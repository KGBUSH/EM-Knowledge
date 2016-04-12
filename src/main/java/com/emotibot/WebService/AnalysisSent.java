package com.emotibot.WebService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.nlpparser.SimpleKnowledgeGetAnwer;
import com.emotibot.patternmatching.PatternMatchingProcess;
import com.emotibot.util.CUBean;
import com.emotibot.util.Tool;

public class AnalysisSent {
	   public static SimpleKnowledgeGetAnwer simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
//	   public static PatternMatchingProcess patternMatchingProcess = new PatternMatchingProcess();
	   
	   public AnalysisSent()
	   {
		   if(simpleKnowledgeGetAnwer!=null) simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
//		   if(patternMatchingProcess!=null) patternMatchingProcess = new PatternMatchingProcess();
	   }
	   
	   public AnswerBean AnalysisSentence(String str)
	   {
		   CUBean cubean = new CUBean();
		   cubean.setText(str);
		   PatternMatchingProcess patternMatchingProcess = new PatternMatchingProcess(cubean);
		   AnswerBean bean = new AnswerBean();
		   try{
		   if(Tool.isStrEmptyOrNull(str)||!str.contains("姚明")) return bean;
		   else
		   {
			   AnswerRewrite answerRewite = new AnswerRewrite();
			   String ans1=simpleKnowledgeGetAnwer.getAnswer(str).trim();
			   String ans2=patternMatchingProcess.getAnswer().getAnswer().trim();
			   
			   if(ans1.equals(ans2)){
				   bean.setAnswer(answerRewite.rewriteAnswer(ans1));
				   bean.setScore(100.0*(ans1.length()>=1?1:ans1.length()));
			   }
			   else
			   {
				   bean.setAnswer(answerRewite.rewriteAnswer(ans1+";"+ans2));
				   bean.setScore(50*Math.min(1,Math.min(ans1.length(), ans2.length())));
			   }
			   return bean;  
		   }
		   }catch(Exception e)
		   {
			   e.printStackTrace();
		   }
		   return bean;
	   }
	   

	
	public static void main(String args[]) throws IOException
	{
		String testFile = "txt/test";

		final BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
		String line2 = null;
		int i = 1;
		FileWriter f = new FileWriter("a");
		while ((line2 = reader2.readLine()) != null) {
			if (!line2.isEmpty()) {
			  f.write("Line"+i+": "+line2+"\r\n");
			  f.write("Line"+i+": "+new AnalysisSent().AnalysisSentence(line2)+"\r\n");
			  i++;
			}

		}
       f.close();
		reader2.close();

		//String s="";
		//System.err.println(new AnalysisSent().AnalysisSentence(s));
	}

}
