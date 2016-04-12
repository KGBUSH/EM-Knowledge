package com.emotibot.answerRewrite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emotibot.WebService.AnalysisSent;
import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.util.Tool;

public class AnswerRewrite {
	private static String[] answerRewriteTable = createAnswerRewriteTable();
	private static String[] answerRewriteTableIntro = createAnswerRewriteTableIntro();

	private static String[] createAnswerRewriteTable() {
		String[] rs = null;
		List<String> answerSet = new ArrayList<>();
		String fileName = Common.UserDir + "/knowledgedata/AnswerTemplate.txt";

		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect detect = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[detect.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader readBuffer = new BufferedReader(read);
				String sentence = "";
				while ((sentence = readBuffer.readLine()) != null) {
					answerSet.add(sentence.trim());
				}

				rs = new String[answerSet.size()];
				for (int i = 0; i < answerSet.size(); i++) {
					rs[i] = answerSet.get(i);
				}
				// System.out.println("list is " + answerRewriteTable);
				readBuffer.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return rs;

	}

	private static String[] createAnswerRewriteTableIntro() {
		String[] rs = null;
		List<String> answerSet = new ArrayList<>();
		String fileName = Common.UserDir + "/knowledgedata/AnswerTemplate4Intro.txt";
		
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect detect = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[detect.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader readBuffer = new BufferedReader(read);
				String sentence = "";
				while ((sentence = readBuffer.readLine()) != null) {
					answerSet.add(sentence.trim());
				}
				
				rs = new String[answerSet.size()];
				for (int i = 0; i < answerSet.size(); i++) {
					rs[i] = answerSet.get(i);
				}
				// System.out.println("list is " + answerRewriteTable);
				readBuffer.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return rs;
	}
	
	public String rewriteAnswer(String answer){
		// if answer is null or answer is introduction, then skip rewrite
		// the process logic for introduction may be improved later
		if(Tool.isStrEmptyOrNull(answer) || answer.length()>50){
			return answer;
		}
		
		String rewrite = "";
		int id = (int) Math.round(Math.random()*(answerRewriteTable.length-1));
		System.out.println("id="+id);
		String template = answerRewriteTable[id];
		
		if(!template.contains("__")) {
			return answer;
		} else {
			rewrite = template.substring(0, template.lastIndexOf("__"))+answer+template.substring(template.lastIndexOf("__")+2);
		}
		return rewrite;
	}
	
	public String rewriteAnswer4Intro(String answer){
		
		String rewrite = "";
		int id = (int) Math.round(Math.random()*(answerRewriteTableIntro.length-1));
		System.out.println("id="+id);
		String template = answerRewriteTableIntro[id];
		rewrite = answer + "\r\n" + template;
		return rewrite;
	}
	
	public static void main(String [] args){
		AnswerRewrite answerRewite = new AnswerRewrite();
		String ans = "中锋";
		System.out.println("answer is "+answerRewite.rewriteAnswer4Intro(ans));
//		for(int i=0;i<200;i++){
//			System.out.println("answer is "+answerRewite.rewriteAnswer4Intro("姚明"));
//		}
	}

}
