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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.common.Common;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;

// input: "movie，^#^放/播^多长/多久;#片长是多少/#时长是多少"
// output: template amil file (4 cases for the above line)
public class TemplateGenerator {
	// String inputFile = Common.UserDir + "/knowledgedata/templateSpec.txt";
	// String outputFile = Common.UserDir +
	// "/bots/Knowledge/aiml/Knowledge.aiml";

	// public TemplateGenerator(String in, String out) {
	// if (!Tool.isStrEmptyOrNull(in))
	// inputFile = in;
	// if (!Tool.isStrEmptyOrNull(out))
	// outputFile = out;
	// System.out.println("file name: inputFile=" + inputFile + "; outputFile="
	// + outputFile);
	// }

	private void generateQuestionClassifierTemplate(String inputFile, String outputFile) {

		try {
			FileWriter newFile = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(newFile);
			out.write("<aiml version=\"1.0.1\" encoding=\"UTF-8\">\r\n");

			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = null;
			while ((line = in.readLine()) != null) {
				line = CharUtil.trim(line);
				if (line.isEmpty() || line.startsWith("####")) {
//					line = in.readLine();
					continue;
				}
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

				if (!line.contains(";")) {
					System.err.println("wrong format 1 in Line=" + line);
					continue;
				}
				
				String templateLine = "";
				if(line.contains("||")){
					String [] tempLineArr = line.split("\\|\\|");
					templateLine = tempLineArr[1];
					line = tempLineArr[0];
				}
				System.out.println("\t seond Line process: line=" + line + ", templateLine=" + templateLine);

				// get the first line and second line
				String[] lineArr = line.split(";");
				if (lineArr.length != 2) {
					System.err.println("wrong format 2 in Line=" + line + ", with number of ; is=" + lineArr.length);
					continue;
				}
				String questionType = lineArr[0];
				String ruleLine = lineArr[1];
				System.out.println("\t questionType=" + questionType + ", patternLine=" + ruleLine);

				// List<String> patternList = new ArrayList<>();
				// patternList.add("");

				String domain = "## * <type>entity</type> ";

				// get each component
				String[] patternArr = ruleLine.split("&");
				List<List<String>> patternList = new ArrayList<>();

				for (int i = 0; i < patternArr.length; i++) {
					String part = patternArr[i];
					List<String> list = new ArrayList<>();
					if (part.startsWith("[")) {
						if (!part.endsWith("]")) {
							System.err.println("wrong format [] in line=" + line);
							continue;
						}
						part = part.substring(1, part.length() - 1);
						list.add("");
					}

					if (part.contains("#")) {
						// System.out.println("##### part = "+part);
						// entity case
						if (!part.equals("#"))
							System.err.println("wrong format: part=" + part);
						list.add(domain);
					} else if (part.contains("/")) {
						// multiple possibility case
						String[] strArr = part.split("/");
						for (String s : strArr) {
							list.add(s);
						}
					} else if (part.contains("^")) {
						if (!part.equals("^")) {
							System.err.println("wrong format of ^");
						}
						list.add("^ ");
					} else {
						// normal case
						list.add(part);
					}
					patternList.add(list);
				}

				List<String> middlePatterList = new ArrayList<>();
				middlePatterList.add("");
				middlePatterList = writePattern(patternList, middlePatterList);
				System.out.print("middlePatterList=" + middlePatterList);

				// second line procedure
//				String secondLineStr = "IntroductionQuestion@:firstParamInfo";
				String secondLineStr = templateLine;
				for (String s : middlePatterList) {
					out.write("    <category>\r\n");
					// s = " " + s.substring(0, s.length() - 2);
					s = " " + s;
					s = s.replace("  ", " ");
					String test = Tool.insertSpace4ChineseCharacter("        <pattern>" + s + "</pattern>\r\n");
					out.write(test);
					out.write("        <template>\r\n");
					out.write("            " + secondLineStr + "\r\n");
					out.write("        </template>\r\n");
					out.write("    </category>\r\n");
				}
//				line = in.readLine();
			}

			out.write("</aiml>\r\n");
			out.close();
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void generateSingleDomainTemplate(String inputFile, String outputFile) {

		try {
			FileWriter newFile = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(newFile);
			out.write("<aiml version=\"1.0.1\" encoding=\"UTF-8\">\r\n");

			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = null;
			while ((line = in.readLine()) != null) {
				line = CharUtil.trim(line);
				if (line.isEmpty() || line.startsWith("####")) {
//					line = in.readLine();
					continue;
				}
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
					System.err.println("wrong format 3 in Line=" + line);
					continue;
				}

				// get the first line and second line
				String[] lineArr = line.split(";");
				if (lineArr.length != 2) {
					System.err.println("wrong format 4 in Line=" + line + ", with number of ; is=" + lineArr.length);
					continue;
				}
				String firstLine = lineArr[0];
				String secondLine = lineArr[1];
				System.out.println("\t firstLine=" + firstLine + ", secondLine=" + secondLine);

				// get the label and pattern
				String[] firstArr = firstLine.split(",");
				if (firstArr.length != 2) {
					System.err.println("wrong format 5 in Line=" + line + ", with number of , is=" + firstArr.length
							+ ", firstLine=" + firstLine);
					continue;
				}

				String[] domainList = firstArr[0].split("&");
				// for each domain
				// case: TV_series & movie,^哪^拍^#^;#的拍摄地点在哪儿？
				for (String domainStr : domainList) {
					String domain = "## * <type>entity</type><label>" + domainStr + "</label> ";
					String pattern = firstArr[1];

					// pattern process
					String[] patternArr = pattern.split("\\^");
					int count = 0;
					int entityPos = 0; // pos of entity, may extend to many
										// entity
					// each part between ^s is a list of words;
					// make the combination of all the parts
					List<List<String>> patternList = new ArrayList<>();

					for (int i = 0; i < patternArr.length; i++) {
						String part = patternArr[i];
						List<String> list = new ArrayList<>();
						if (part.contains("#")) {
							// System.out.println("##### part = "+part);
							// entity case
							if (!part.equals("#") && !part.equals("#~"))
								System.err.println("wrong format: part=" + part);
							count++;
							entityPos = count;
							if(!part.endsWith("~")){
								list.add(domain + "^ ");
							} else {
								list.add(domain + " ");
							}
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

						if (i == patternArr.length - 1 && !pattern.trim().endsWith("^")) {
							for (int j = 0; j < list.size(); j++) {
								String tempS = list.get(j);
								list.set(j, tempS.substring(0, tempS.length() - 2));
							}
						}

						patternList.add(list);
						count++;
					}

					// for (String part : patternArr) {
					// List<String> list = new ArrayList<>();
					// if (part.contains("#")) {
					// // System.out.println("##### part = "+part);
					// // entity case
					// if (!part.equals("#"))
					// System.err.println("wrong format: part=" + part);
					// count++;
					// entityPos = count;
					// list.add(domain + "^ ");
					// } else if (part.contains("/")) {
					// // multiple possibility case
					// String[] strArr = part.split("/");
					// for (String s : strArr) {
					// list.add(s + "^ ");
					// }
					// } else {
					// // normal case
					// list.add(part + "^ ");
					// }
					// patternList.add(list);
					// count++;
					// }

					System.out.println("pattern=" + pattern + ", patternList=" + patternList);
					if (pattern.endsWith("^")) {
						// for(String s : patternList){
						//
						// }
					}

					List<String> middlePatterList = new ArrayList<>();
					middlePatterList.add("");
					middlePatterList = writePattern(patternList, middlePatterList);
					System.out.print("middlePatterList=" + middlePatterList);

					// second line procedure
					String secondLineStr = secondLine;
					secondLineStr = secondLineStr.replace("#", "<star index=\"" + entityPos + "\"/>");
					System.out.println("secondStr=" + secondLineStr);
					for (String s : middlePatterList) {
						out.write("    <category>\r\n");
						// s = " " + s.substring(0, s.length() - 2);
						s = " " + s;
						String test = Tool.insertSpace4ChineseCharacter("        <pattern>" + s + "</pattern>\r\n");
						out.write(test);
						out.write("        <template>\r\n");
						out.write("            " + secondLineStr + "\r\n");
						out.write("        </template>\r\n");
						out.write("    </category>\r\n");
					}
				}
//				line = in.readLine();
			}

			out.write("</aiml>\r\n");
			out.close();
			in.close();

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

	private void generateDomainTemplate() {
		String listFileName = Common.UserDir + "/knowledgedata/domainList.txt";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(listFileName));
			String domain = null;
			int i = 0;
			while ((domain = reader.readLine()) != null) {
				String specFileName = Common.UserDir + "/knowledgedata/template/templateSpec/" + domain + ".txt";
				String aimlFileName = Common.UserDir + "/bots/" + domain + "/aiml/" + domain + ".aiml";

				System.out.println(
						"domain=" + domain + ",\n specFileName=" + specFileName + ";\n aimlFileName=" + aimlFileName);
				generateSingleDomainTemplate(specFileName, aimlFileName);
			}

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void generateQuestionClassifierTemplate() {
		String specFileName = Common.UserDir + "/knowledgedata/template/questionClassifier.txt";
		String aimlFileName = Common.UserDir + "/bots/QuestionClassifier/aiml/QuestionClassifier.aiml";
		generateQuestionClassifierTemplate(specFileName, aimlFileName);
	}

	public static void main(String[] args) {
		TemplateGenerator tg = new TemplateGenerator();
		tg.generateQuestionClassifierTemplate();
		 tg.generateDomainTemplate();
	}

}
