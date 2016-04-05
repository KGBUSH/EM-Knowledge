package com.emotibot.template;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.emotibot.common.Common;
import com.emotibot.util.Tool;

// input: "movie，^#^放/播^多长/多久;#片长是多少/#时长是多少"
// output: template amil file (4 cases for the above line)
public class TemplateGenerator {
	String inputFile = Common.UserDir + "/knowledgedata/templateSpec.txt";
	String outputFile = Common.UserDir + "/bots/Knowledge/aiml/Knowledge.aiml";

	public TemplateGenerator(String in, String out) {
		if (!Tool.isStrEmptyOrNull(in))
			inputFile = in;
		if (!Tool.isStrEmptyOrNull(out))
			outputFile = out;
		System.out.println("file name: inputFile=" + inputFile + "; outputFile=" + outputFile);
	}

	public void generator() {

		try {
			FileWriter newFile = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(newFile);
			out.write("<aiml version=\"1.0.1\" encoding=\"UTF-8\">\r\n");

			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = in.readLine();
			while (line != null) {
				System.out.println("line=" + line);

				Map<String, String> specialCharacterMap = new HashMap<>();
				specialCharacterMap.put(" ", " ");
				specialCharacterMap.put("；", ";");
				specialCharacterMap.put("，", ",");

				// replace special character in Chinese
				for (String s : specialCharacterMap.keySet()) {
					System.out.println("s=" + s + "; S=" + specialCharacterMap.get(s));
					line = line.replace(s, specialCharacterMap.get(s));
				}
				line = line.replace(" ", ""); // remove blank
				System.out.println("line after special character procedure===" + line);

				if (!line.contains("#") || !line.contains(";")) {
					System.err.println("wrong format in Line=" + line);
					continue;
				}

				// get the first line and second line
				String[] lineArr = line.split(";");
				if (lineArr.length != 2) {
					System.err.println("wrong format in Line=" + line + ", with number of ; is=" + lineArr.length);
					continue;
				}
				String firstLine = lineArr[0];
				String secondLine = lineArr[1];

				// get the label and pattern
				String[] firstArr = firstLine.split(",");
				if (firstArr.length != 2) {
					System.err.println("wrong format in Line=" + line + ", with number of , is=" + firstArr.length);
					continue;
				}
				String domain = "## * <type>entity</type><label>" + firstArr[0] + "</label> ";
				String pattern = firstArr[1];

				// pattern process
				String[] patternArr = pattern.split("\\^");
				int count = 0;
				int entityPos = 0; // pos of entity, may extend to many entity
				// each part between ^s is a list of words;
				// make the combination of all the parts
				List<List<String>> patternList = new ArrayList<>();
				for (String part : patternArr) {
					List<String> list = new ArrayList<>();
					if (part.contains("#")) {
						// System.out.println("##### part = "+part);
						// entity case
						if (!part.equals("#"))
							System.err.println("wrong format: part=" + part);
						count++;
						entityPos = count;
						list.add(domain + "^ ");
					} else if (part.contains("/")) {
						// multiple possibility case
						String[] strArr = part.split("/");
						for (String s : strArr) {
							list.add(s + "^ ");
						}
					} else {
						// normal case
						list.add(part + "^ ");
					}
					patternList.add(list);
					count++;
				}
				System.out.println("patternList=" + patternList);

				List<String> middlePatterList = new ArrayList<>();
				middlePatterList.add("");
				middlePatterList = writePattern(patternList, middlePatterList);
				System.out.print("middlePatterList=" + middlePatterList);

				// second line procedure
				String[] secondLineArr = secondLine.split("/");
				
				for(String secondStr : secondLineArr){
					System.out.println("secondStr="+secondStr);
					secondStr = secondStr.replace("#", "<star index=\"" + entityPos + "\"/>");
					System.out.println("secondStr="+secondStr);
					for (String s : middlePatterList) {
						out.write("    <category>\r\n");
						// s = " " + s.substring(0, s.length() - 2);
						s = " " + s;
						String test = Tool.insertSpace4ChineseCharacter("        <pattern>" + s + "</pattern>\r\n");
						out.write(test);
						out.write("        <template>\r\n");
						out.write("            " + secondStr + "\r\n");
						out.write("        </template>\r\n");
						out.write("    </category>\r\n");
					}
				}


				line = in.readLine();
			}

			out.write("</aiml>\r\n");
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private List<String> writePattern(List<List<String>> patternList, List<String> lineList) {
		System.out.println("\t patternList=" + patternList + "; lineList=" + lineList);
		if (patternList == null || patternList.isEmpty()) {
			return lineList;
		}
		List<String> returnList = new ArrayList<>();
		Iterator<List<String>> it = patternList.iterator();
		if (it.hasNext()) {
			List<String> currentPatList = it.next();
			System.out.println("\t\t currentPatternList=" + patternList + "; lineList=" + lineList);
			for (String part : currentPatList) {
				for (String s : lineList) {
					s = s.concat(part);
					returnList.add(s);
				}
			}
		}
		it.remove();
		return writePattern(patternList, returnList);
	}

	public static void main(String[] args) {
		TemplateGenerator tg = new TemplateGenerator("", "");
		tg.generator();
	}

}