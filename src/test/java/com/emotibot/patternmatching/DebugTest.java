package com.emotibot.patternmatching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.common.Common;
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
				cuBean.setQuestionType("question");
				cuBean.setScore("50");
				AnswerBean bean =new PatternMatchingProcess(cuBean).getAnswer();
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
		String debugLevel = "bad_case"; //bad_case, debugAll
		String date = "0422";
		String reader = Common.UserDir + "/log/debug/cases/"+debugLevel+".txt";
		String writer = Common.UserDir + "/log/debug/"+debugLevel+"_cases-"+date+"-02.txt";
		NLPProcess nlpProcess = new NLPProcess();
		NLPProcess.NLPProcessInit();
		System.out.println("TIME 1 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		getFile(reader, writer);
		System.out.println("TIME 2 - after get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
	}
	
	
}
