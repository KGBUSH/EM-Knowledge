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

	public String rewriteAnswer(String answer) {
		// if answer is null or answer is introduction, then skip rewrite
		if (Tool.isStrEmptyOrNull(answer) || answer.length() > 50 || Common.KG_DebugStatus) {
			System.out.println("Debug STATUS = "+Common.KG_DebugStatus);
			return answer;
		}

		String rewrite = "";
		int id = (int) Math.round(Math.random() * (answerRewriteTable.length - 1));
		System.out.println("id=" + id);
		String template = answerRewriteTable[id];

		if (!template.contains("__")) {
			return answer;
		} else {
			rewrite = template.substring(0, template.lastIndexOf("__")) + answer
					+ template.substring(template.lastIndexOf("__") + 2);
		}
		return rewrite;
	}

	// QuestinType: 0-normal, 1-relationship, 2-selective
	public String rewriteAnswer(String answer, int QuestionType) {
		// if answer is null or answer is introduction, then skip rewrite
		if (Tool.isStrEmptyOrNull(answer) || answer.length() > 50 || Common.KG_DebugStatus) {
			System.out.println("Debug STATUS = "+Common.KG_DebugStatus);
			return answer;
		}

		String rewrite = "";
		int id = (int) Math.round(Math.random() * (answerRewriteTable.length - 1));
		System.out.println("id=" + id);
		String template = answerRewriteTable[id];

		if (!template.contains("__")) {
			return answer;
		} else {
			if (QuestionType > 0) {
				// relation type
				System.out.println("template==" + template);
				if (!template.startsWith("__") && template.charAt(template.indexOf("__") - 1) == '是') {
					template = template.substring(0, template.indexOf("__") - 1)
							+ template.substring(template.indexOf("__"));
					System.out.println("template after change ==" + template);
				}
			}
			rewrite = template.substring(0, template.lastIndexOf("__")) + answer
					+ template.substring(template.lastIndexOf("__") + 2);
		}
		return rewrite;
	}

	public String rewriteAnswer4Intro(String answer) {
		if (Tool.isStrEmptyOrNull(answer) || Common.KG_DebugStatus) {
			return answer;
		}

		String rewrite = "";
		int id = (int) Math.round(Math.random() * (answerRewriteTableIntro.length - 1));
		System.out.println("id=" + id);
		String template = answerRewriteTableIntro[id];
		rewrite = "["+answer+"],[" + template+"]";
		return rewrite;
	}

	public static void main(String[] args) {
		AnswerRewrite answerRewite = new AnswerRewrite();
		String ans = "中锋";
		System.out.println("answer is " + answerRewite.rewriteAnswer4Intro(ans));
		// for(int i=0;i<200;i++){
		// System.out.println("answer is
		// "+answerRewite.rewriteAnswer4Intro("姚明"));
		// }
	}

}
