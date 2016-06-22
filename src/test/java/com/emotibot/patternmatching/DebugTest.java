package com.emotibot.patternmatching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.common.Common;
import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.understanding.KGAgent;
import com.emotibot.util.CUBean;

public class DebugTest {
	
	private static long timeCounter = System.currentTimeMillis();

	public static void getFile(String fileReader, String fileWriter){
		try{
			BufferedReader in = new BufferedReader(new FileReader(fileReader));
			BufferedWriter out = new BufferedWriter(new FileWriter(fileWriter));
			
			String line = null;
			int i = 1;
			while((line = in.readLine())!=null){
				if (line.trim().isEmpty() || line.startsWith("#")) continue;
				CUBean cuBean = new CUBean();
				cuBean.setText(line);
				cuBean.setQuestionType("question-info");
				cuBean.setScore("50");
//				AnswerBean bean =new PatternMatchingProcess(cuBean).getAnswer();
				AnswerBean bean =new KGAgent(cuBean).getAnswer();
				System.out.println("input="+line+", answer bean="+bean);
				System.out.println("bean="+bean+", score score ="+bean.getScore());
				
				out.write("input "+i+++": "+line+"-->>> answer: "+bean+"\n"); 
				
				if(Common.KG_DebugStatus){
					Debug.printDebug("123456", 1, "KG", "bean="+bean);
				}
			}
			in.close();
			out.close();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		String debugLevel = "0523All"; 
		//bad_case, debugAll, debug, 0531testcase, 0531DebugPart, 0518Regression, 0523All, 
		// 0630TestCase, 0630TestAll
		String date = "0622";
		String tempFileName = Common.UserDir + "/debug/"+date;
		String reader = Common.UserDir + "/debug/cases/"+debugLevel+".txt";
		String writer = Common.UserDir + "/debug/"+date+"/"+debugLevel+"_cases-"+date+"-21.txt";
		
		File fp = new File(tempFileName);
		if(!fp.exists()){
			fp.mkdir();
		}
	
//		NLPProcess nlpProcess = new NLPProcess();
//		NLPProcess.NLPProcessInit();
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		DictionaryBuilder.DictionaryBuilderInit();
		System.out.println("TIME 1 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		getFile(reader, writer);
		System.out.println("TIME 2 - after get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
	}
	
	
}
