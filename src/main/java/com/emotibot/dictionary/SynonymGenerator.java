package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.emotibot.common.Common;

public class SynonymGenerator {

	public static void generator() {
		int numCount = 10000;
		char charCount = 'a';

		try {
			String tempFileName = Common.UserDir + "/knowledgedata/synonymTemp.txt";

			FileWriter normalFile = new FileWriter(Common.UserDir + "/knowledgedata/SynonymNoun.txt");
			BufferedWriter out = new BufferedWriter(normalFile);
			BufferedWriter outTemp = new BufferedWriter(new FileWriter(tempFileName));

			BufferedReader in = new BufferedReader(
					new FileReader(Common.UserDir + "/knowledgedata/SynonymPM.txt"));
			String line = in.readLine();
			while (line != null) {
				line = line.trim();
				if (line.isEmpty()) {
					line = in.readLine();
					continue;
				}
				line = line.replace("＝", "=");

				if (line.endsWith("=")) {
					System.out.println("omit line=" + line);
					line = in.readLine();
					continue;
				}

				if (line.startsWith("=") || !line.contains("=")) {
					// new added lines
					numCount++;
					if (numCount >= 100000) {
						numCount = 10000;
						charCount++;
					}
					if (charCount == 'A') {
						System.err.println("index float");
					}
					String index = "Z" + charCount + Integer.toString(numCount);

					if (!line.contains("=")) {
						System.out.println("abnormal added line=" + line);
						outTemp.write(index + "= " + line + "\r\n");
					} else {
						if (!line.startsWith("= ")) {
							line = line.replace("=", "= ");
						}
						outTemp.write(index + line + "\r\n");
					}
				} else {
					// original synonyms
					if (!line.split(" ")[0].endsWith("=")) {
						System.err.println("wrong format line=" + line);
						line = in.readLine();
						continue;
					}
					out.write(line + "\r\n");
				}

				line = in.readLine();
			}
			in.close();
			outTemp.close();

			BufferedReader inTemp = new BufferedReader(new FileReader(tempFileName));
			line = inTemp.readLine().trim();
			while (line != null) {
				out.write(line + "\r\n");
				line = inTemp.readLine();
			}
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		generator();
	}

}
