package com.emotibot.patternmatching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;

import com.emotibot.common.Common;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.util.CharUtil;
import com.hankcs.hanlp.HanLP;

public class DictTest {
	
	private static void getHighFreqWordInEntityList(){
		Set<String> setHighWord = NLPProcess.getHighFeqWordTable();
		Set<String> setEntity = NLPProcess.getEntityTable();
		
		String outFileName = Common.UserDir + "/txt/temp/highFrequentEntity.txt";
		
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
			for(String s: setEntity){
				if(setHighWord.contains(s)){
					String tempLabel = DBProcess.getEntityLabel(s);
					System.out.println(s +":  "+tempLabel);
					out.write(s +":  "+tempLabel + "\r\n");
				}
			}
			out.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}
	
	

	// based on the entity mapping
	// 甲型病毒性肝炎###甲肝###medicle
	private static void testChar160() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/resources/EntityMapping.txt"));
			String line = "";
			while ((line = in.readLine()) != null) {

				if (line.isEmpty()) {
					continue;
				}
				
				if (line.contains(String.valueOf((char) 160))){
					System.err.println(line);
				}

			}

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	public static void main(String [] args){
		NLPProcess nlp = new NLPProcess();
		NLPProcess.NLPProcessInit();
		
		
		testChar160();
		
		System.exit(0);
		
//		getHighFreqWordInEntityList();
		
		Set<String> entitySet = NLPProcess.getEntityTable();
		
		int i = 1;
		for(String s : entitySet){
			if(NLPProcess.isInSynonymDict(s)){
				System.out.println(i+++": "+s);
			}
		}
		
	}
	
}
