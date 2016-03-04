package com.emotibot.nlpparses;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.emotibot.nlpparser.AnalysisSentence;

public class testExtract {
	public static void testCaseExtract() throws Exception {
		String testFile = "txt/testcase.txt";

		final BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
		String line2 = null;
		while ((line2 = reader2.readLine()) != null) {
			if (!line2.isEmpty()) {
			   System.out.println(line2);
				AnalysisSentence.analysisSentenceToGetAnswer(line2);
			}

		}

		reader2.close();

	}

	public static void main(String[] args) {
		try {
			testExtract.testCaseExtract();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
