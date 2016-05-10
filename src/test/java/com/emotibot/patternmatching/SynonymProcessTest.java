package com.emotibot.patternmatching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.emotibot.nlpparser.SentenceTemplate;
import com.emotibot.understanding.DBProcess;
import com.emotibot.understanding.NLPUtil;
import com.emotibot.understanding.PatternMatchingResultBean;
import com.emotibot.util.Neo4jResultBean;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.lang.reflect.Method;

public class SynonymProcessTest {

	public void changeObject(PatternMatchingResultBean b) {
		b.setAnswer("c");
	}

	public void changeString(String str) {
		str = "c";
	}


	public static void main(String[] args) {
		List<String> arrStr = new ArrayList<>();
		arrStr.add("1 ^ ");
		arrStr.add("2 ^ ");
		
		for(int i = 0; i<arrStr.size(); i++){
			String tempS = arrStr.get(i);
			arrStr.set(i, tempS.substring(0, tempS.length()-2));
		}
		
		for(String s : arrStr){
			System.out.println(s);
		}
		
		String entity = "马德里竞技（马竞）对方的";
		
		System.out.println("fist="+entity.substring(0, entity.indexOf("（"))+", second="+entity.substring(entity.indexOf("（")+1, entity.indexOf("）")));
		 
//		addLabelinEntity();
		
		System.exit(0);
		
		

		System.exit(0);

		Set<String> sss = new HashSet<>();
		sss.add("1");
		sss.add("1");
		System.out.println("sss.length=" + sss.size());


		// // SynonymProcessTest.changeDB();
		// SynonymProcessTest.generateEntityPMFile();
		// SynonymProcessTest.findDuplicateEntity();

		TreeSet<String> tempSet = new TreeSet<String>(new StringLengthComparator());
		tempSet.add("1");
		tempSet.add("2");
		String[] tempArr = tempSet.toArray(new String[0]);
		System.out.println("tempArr=" + tempArr);

		System.out.println("tree set size = " + tempSet.size() + ", lenth=" + tempArr.length);

		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("a");
		System.out.println("list size = " + list.size());

		System.exit(0);
		String strA = "1980年10月";

		System.out.println(strA.indexOf("年"));
		System.out.println(strA.substring(0, strA.indexOf("年")));

		System.out.println("contain#=" + strA.contains("#"));
		System.out.println(strA);
		String[] tetList = strA.split("/");
		for (String s1 : tetList) {
			System.out.println("s=" + s1 + "|");
		}

		System.out.println("什么是姚明".endsWith("姚明"));

		Class<?> demo = null;
		try {
			demo = Class.forName("com.emotibot.patternmatching.Reflect");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("hello");
		try {
			Method method = demo.getMethod("sayHello", String.class, String.class);
			// int year = (int) method.invoke(demo.newInstance());
			method.invoke(demo.newInstance(), "Neil");

		} catch (Exception e) {
			e.printStackTrace();
		}

		// if(list.isEmpty()){
		// System.out.println("1111");
		//// System.out.println(list.size());
		//
		// }
		//
		// SynonymProcessTest test = new SynonymProcessTest();
		//
		// String t = "a";
		// test.changeString(t);
		// System.out.print("t=" + t);
		//
		// List<PatternMatchingResultBean> listPMBean = new ArrayList<>();
		// listPMBean = null;
		//
		// if (listPMBean == null || listPMBean.isEmpty())
		// System.err.println("null");
		//
		// PatternMatchingResultBean bean = new PatternMatchingResultBean();
		// bean.setAnswer("a");
		// System.out.println(bean);
		//
		// test.changeObject(bean);
		// System.out.println(bean);
		//
		// listPMBean.add(bean);
		//
		// // List<PatternMatchingResultBean> copy = new
		// ArrayList<>(listPMBean);
		//
		// List<PatternMatchingResultBean> copy = new
		// ArrayList<PatternMatchingResultBean>(listPMBean.size());
		// Iterator<PatternMatchingResultBean> it = listPMBean.iterator();
		// while (it.hasNext()) {
		// copy.add(it.next().clone());
		// }
		//
		// System.out.println("before chagne: " + copy.get(0).getAnswer());
		// bean.setAnswer("b");
		// System.out.println("after chagne: " + copy.get(0).getAnswer());
		//
		// System.out.println("".length());
		//
		// String s1 = "姚明身#高多#少";
		// String s2 = "姚明";
		// Set<String> ss = new HashSet<>();
		// ss.add("ets");
		//
		// System.out.println("list is " + list.toString());
		//
		// Iterator<String> itList = list.iterator();
		// while (itList.hasNext()) {
		// if (itList.next().equals(s2)) {
		// itList.remove();
		// }
		// }
		//
		// System.out.println("list is " + list.toString());
		//
		// list.add(s2);
		// System.out.println("list.size=" + list.size());
		//
		// List<String> c = new ArrayList<>();
		// c.add("testc");
		//
		// list.addAll(c);
		//
		// System.out.println("after merge is " + list);
		//
		// String[] sa = s1.split("姚明");
		//
		// for (String s : sa) {
		// System.out.println("string arr is: " + s);
		// }
		//
		// String[] arr = { "a", "b", "c" };
		// System.out.println("array.length=" + arr.length);
		// // for (int i = 0; i < arr.length; i++) {
		// // System.out.println(arr[i]);
		// // }
		//
		// String s = "12345";
		// System.out.println("string.length()=" + s.length());
		//
		// for (int i = 0; i < s.length() - 1; i++)
		// System.out.println(s.charAt(i));
		//
		// String str = "姚明多少斤";
		//
		// // SynonymProcessTest spt = new SynonymProcessTest();
		// // spt.testSynTable();
		// // spt.testSplitSentence();
	}

	/*
	 * replace the synonym in a sentence or a word
	 */
	public static String stopWordList(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPosNoStopWords.getValue());
		List<Term> segPos = tnNode.getWordPos();
		System.out.println("test=" + segPos);
		String rs = "";
		for (int i = 0; i < segPos.size(); i++) {
			String word = segPos.get(i).word;
			System.out.println("index " + i + " is " + word + " " + segPos.get(i).nature.toString());
		}
		return rs;
	}

	/*
	 * get the synonym of the word from the syn table
	 */
	private static String getSynonym(String str) {
		NLPResult tnNodeSy = NLPSevice.ProcessSentence(str, NLPFlag.Synonyms.getValue());
		List<List<String>> sy = tnNodeSy.getSynonyms();
		System.out.println("size of synonym is " + sy.size());
		String rs = "";
		if (sy.size() > 0 && sy.get(0).size() > 0) {
			rs = sy.get(0).get(0);
		}
		return rs;
	}

	/*
	 * replace the synonym in a sentence or a word
	 */
	public static String synonymProcessTest(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		String rs = "";
		for (int i = 0; i < segPos.size(); i++) {
			String word = segPos.get(i).word;
			System.out.println("index " + i + " is " + word + " " + segPos.get(i).nature.toString());
			String syn = getSynonym(word);
			if (syn.isEmpty()) {
				rs += word;
				System.out.println("orginal is " + word + "; syn is " + syn);
			} else {
				rs += syn;
				System.out.println("orginal is " + word + "; syn is " + syn);
			}
		}
		return rs;
	}

	/*
	 * replace the synonym in a string.
	 */
	private List<String> getSynonymProcess(String str) {
		List<String> rsSet = new ArrayList<>();

		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		// System.out.println("string is " + str);

		List<Term> segPos = tnNode.getWordPos();
		rsSet.add("");
		boolean flag = false;
		for (int i = 0; i < segPos.size(); i++) {
			String iWord = segPos.get(i).word;
			System.out.println("current word is " + iWord);

			Set<String> iSynSet = NLPUtil.getSynonymWordSet(iWord);
			if (iSynSet.size() > 0) {
				flag = true;
				System.out.println("\t has syn: " + iSynSet);
				// if there are synonyms, combine each of them
				List<String> newRS = new ArrayList<>();
				for (String iSyn : iSynSet) {
					System.out.println("iSyn is " + iSyn);
					List<String> tmpRS = new ArrayList<>();
					tmpRS.addAll(rsSet);
					for (int j = 0; j < tmpRS.size(); j++) {
						tmpRS.set(j, tmpRS.get(j) + iSyn);
					}
					newRS.addAll(tmpRS);
					System.out.println("tempRS is: " + tmpRS + "; newRS is " + newRS);
				}
				rsSet = newRS;
				// System.out.println("after syn is: " + newRS);
			} else {
				// System.out.println("\t No syn: " + iWord);
				for (int j = 0; j < rsSet.size(); j++) {
					rsSet.set(j, rsSet.get(j) + iWord);
				}
			}
		}

		// add the original string
		if (flag) {
			rsSet.add(str);
		}

		return rsSet;
	}

	private void testSplitSentence() {
		String str = "姚明的妻子是谁呀？";
		String out = "";
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		System.out.println("string is " + str);
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			System.out.println(segPos.get(i).word + " " + segPos.get(i).nature.toString());
			out += segPos.get(i).word;
		}

		System.out.println("after is " + str);
	}

	private void testSynTable() {

		String fileName = "data/dictionary/synonym/CoreSynonym.txt";
		String outNounFileName = "txt/SynonymNoun.txt";
		String outNonNounFileName = "txt/SynonymNonNoun.txt";
		StringBuffer buffer = new StringBuffer();
		StringBuffer bufferNotNoun = new StringBuffer();
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";

				FileOutputStream foutNoun = new FileOutputStream(outNounFileName);
				OutputStreamWriter outs = new OutputStreamWriter(foutNoun);
				// outs.write(buffer.toString());

				FileOutputStream foutNonNoun = new FileOutputStream(outNonNounFileName);
				OutputStreamWriter outNonNoun = new OutputStreamWriter(foutNonNoun);
				outNonNoun.write(buffer.toString());

				int i = 0;
				while ((line = dis.readLine()) != null) {
					if (line.lastIndexOf("=") != -1) {
						String[] listS = line.trim().split(" ");
						if (this.isNoun(listS[1]) && this.isNoun(listS[2])) {
							outs.write(line.trim() + '\n');
							// buffer.append(line.trim());
							// System.out.println("line " + i + " is " + line);
						} else {
							outNonNoun.write(line.trim() + '\n');
							// bufferNotNoun.append(line.trim());
						}
					}
				}

				outs.close();
				outNonNoun.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private boolean isNoun(String str) {
		boolean isN = false;
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		if (segPos.size() != 1)
			System.out.println("@@@@@@@@@@ the input string is not a single word @@@@@@@");
		if (segPos.get(0).nature.toString().charAt(0) == 'n')
			isN = true;
		System.out.println("input is " + str + " nature is " + segPos.get(0).nature.toString() + ", isN is " + isN);
		return isN;
	}

	private void testRepeat() {

		String fileName = "txt/SynonymNoun.txt";
		// StringBuffer buffer = new StringBuffer();
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				int i = 0;
				HashSet<String> hm = new HashSet<>();
				while ((line = dis.readLine()) != null) {
					if (line.lastIndexOf("=") != -1) {
						String[] listS = line.trim().split(" ");
						String ws = listS[1];
						if (hm.contains(ws))
							System.out.println("!!!!!!! already contain: " + ws);
						else {
							// System.out.println("add: " + ws);
							hm.add(ws);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	private static void addLabelinEntity(){
		String tempFileName = Common.UserDir + "/txt/temp/multiMean.txt";

		try{
			BufferedReader reader = new BufferedReader(new FileReader(tempFileName));
            String entity = null;
            
            FileWriter writeFile = new FileWriter(Common.UserDir + "/txt/temp/multiMeanUpate.txt");
			BufferedWriter out = new BufferedWriter(writeFile);
		
            
            while ((entity = reader.readLine()) != null) {
            	String label = DBProcess.getEntityLabel(entity);
            	if(Tool.isStrEmptyOrNull(label)){
            		label = DBProcess.getEntityLabel(NLPUtil.getEntitySynonymNormal(entity));
            	}
            	out.write(entity+" "+label+"\r\n");
            }
            
            out.close();
            
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

	private void synonymProcess(String str) {

		// TBD

		// List<Sentence> sentList = new ArrayList<>();
		// List<Term> segpos = Pre_ProcessSentence.getSegPos(str);
		// System.out.println(segpos);
		//
		NLPResult tnNodeSy = NLPSevice.ProcessSentence("姚", NLPFlag.Synonyms.getValue());
		List<List<String>> sy = tnNodeSy.getSynonyms();//
		for (int i = 0; i < sy.size(); i++) {
			List<String> curSy = sy.get(i);// 同义词列表
			for (int j = 0; j < curSy.size(); j++) {
				System.out.println(curSy.get(j));
			}
		}
		NLPResult tnNode = NLPSevice.ProcessSentence("姚明的妻子是谁？", NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			System.out.println(segPos.get(i).word + " " + segPos.get(i).nature.toString());
		}
	}
}
