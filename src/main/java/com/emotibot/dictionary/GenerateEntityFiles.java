package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emotibot.TCP.TCPClient;
import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.log.LogService;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;

public class GenerateEntityFiles {

	public static void main(String args[]) throws Exception {

		DictionaryBuilder.DictionaryBuilderInit();
		generateEntity();
		// generateEntityAndLabel();

		String tempFileName = Common.UserDir + "/knowledgedata/entityException.txt";
		(new File(tempFileName)).delete();

		checkTemplate();
		modifyEntity();
	}

	private static void modifyEntity() {
		String entityRawFileName = Common.UserDir + "/knowledgedata/entityRaw.txt";
		String entityExceptionFileName = Common.UserDir + "/knowledgedata/entityException.txt";

		try {
			Set<String> entityExceptionSet = new HashSet<>();
			BufferedReader entityExceptionReader = new BufferedReader(new FileReader(entityExceptionFileName));
			String line = null;
			while ((line = entityExceptionReader.readLine()) != null) {
				line = CharUtil.trimAndlower(line);
				if (Tool.isStrEmptyOrNull(line)) {
					continue;
				}
				entityExceptionSet.add(line);
			}

			String tempFileName = Common.UserDir + "/knowledgedata/entity.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));
			
			BufferedReader entityRawReader = new BufferedReader(new FileReader(entityRawFileName));
			line = null;
			while ((line = entityRawReader.readLine()) != null) {
				line = CharUtil.trimAndlower(line);
				if (Tool.isStrEmptyOrNull(line)) {
					continue;
				}
				if(!entityExceptionSet.contains(line)){
					out.write(line + "\r\n");
				}
			}

//			// add entity Synonym
//			for(String s : DictionaryBuilder.getEntitySynonymTable().keySet()){
//				out.write(s + "\r\n");
//			}
			
			entityRawReader.close();
			entityExceptionReader.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// generate the entity name list from DB
	public static void generateEntity() {
		EmotibotNeo4jConnection conn = getDBConnection();

		String query = "match(n) with n return collect(n.Name) as result";
		List<String> list = conn.getArrayListfromCollection(query);

		Set<String> tempSet = new HashSet<>();

		for (String s : list) {
			tempSet.add(s);
		}
		// for (String s : NLPProcess.getEntitySynonymTable().keySet()) {
		// tempSet.add(s);
		// }
		// for (String s : NLPProcess.getEntitySynonymTable().values()) {
		// tempSet.add(s);
		// }

		try {
			String tempFileName = Common.UserDir + "/knowledgedata/entityRaw.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			for (String s : tempSet) {
				// if (s.length() == 1 && !NLPUtil.isEntityPM(s)) {
				if (s.length() == 1) {
					// remove in 5/31, may be added later
					System.out.println(s);
					continue;
				}
				out.write(s + "\r\n");
			}
			out.close();

			// tempFileName = Common.UserDir + "/knowledgedata/entityH.txt";
			// BufferedWriter outH = new BufferedWriter(new
			// FileWriter(tempFileName));
			//
			// for (String s : tempSet) {
			// outH.write(s + " n" + " 2" + "\r\n");
			// }
			// outH.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("entity generation done");

	}

	// generate entity and label
	public static void generateEntityAndLabel() {
		EmotibotNeo4jConnection conn = getDBConnection();

		// String query = "match(n) with n with n.Name as name, labels(n) as l
		// unwind l as domain return collect(name+\"###\"+domain) as result";
		String query = "match(n) with n with n.Name as name, labels(n) as l unwind l as domain return collect(name+\"###\"+domain) as result";
		List<String> list = conn.getArrayListfromCollection(query);

		// System.out.println("list="+list);

		Set<String> tempSet = new HashSet<>();

		for (String s : list) {
			tempSet.add(s);
		}

		try {
			// String tempFileName = Common.UserDir +
			// "/knowledgedata/KnowledgeEntityWithLabel.txt";
			String tempFileName = Common.UserDir + "/knowledgedata/entityTest.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			for (String s : tempSet) {
				if (s.length() == 1) {
					System.out.println(s);
					continue;
				}
				out.write(s + "\r\n");
			}
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("entity and label generation done");

	}

	// removing the characters also existing in the template rules
	private static void checkTemplate() {
		String listFileName = Common.UserDir + "/knowledgedata/domain/domainList.txt";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(listFileName));
			String domain = null;
			int i = 0;
			while ((domain = reader.readLine()) != null) {
				String specFileName = Common.UserDir + "/knowledgedata/template/templateSpec/" + domain + ".txt";
				checkSingleTemplate(specFileName);
			}

			reader.close();

			String specFileName = Common.UserDir + "/knowledgedata/template/questionClassifier.txt";
			checkSingleTemplate(specFileName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void checkSingleTemplate(String inputFile) {
		System.out.println("generateSingleDomainTemplate: inputFile=" + inputFile);

		try {
			// FileWriter newFile = new FileWriter(outputFile);
			// BufferedWriter out = new BufferedWriter(newFile);
			// out.write("<aiml version=\"1.0.1\" encoding=\"UTF-8\">\r\n");

			Set<String> entityExceptionSet = new HashSet<>();

			String tempFileName = Common.UserDir + "/knowledgedata/entityException.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName, true));

			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = null;

			while ((line = in.readLine()) != null) {
				line = CharUtil.trimAndlower(line);
				if (Tool.isStrEmptyOrNull(line)) {
					continue;
				}

				if (!line.contains("#")) {
					continue;
				} else {
					line = line.substring(0, line.indexOf("#"));
				}

				// System.err.println(getMultiPatternMatching(line));

				for (String s : getMultiPatternMatching(line)) {
					entityExceptionSet.add(s);
				}

			}

			for (String s : entityExceptionSet) {
				out.write(s + "\r\n");
			}

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get rs from multipattern matching method
	private static List<String> getMultiPatternMatching(String sentence) {
		List<String> rtList = new ArrayList<>();
		
		if(Tool.isStrEmptyOrNull(sentence)){
			return rtList;
		}
		
		List<String> tmpList = new ArrayList<>();
		for (String s : DictionaryBuilder.getEntityTable()) {
			if (sentence.contains(s.toLowerCase())) {
				tmpList.add(s);
			}
		}
		rtList = tmpList;
		return rtList;
		
		
//		List<String> rtList = new ArrayList<>();
//		TCPClient tcp = new TCPClient();
//		try {
//			String tcpRtn = tcp.TransmitThrowException(sentence);
//			tcpRtn = CharUtil.trimAndlower(tcpRtn);
//			if (!Tool.isStrEmptyOrNull(tcpRtn)) {
//				String[] strArr = tcpRtn.split("&");
//				for (String s : strArr) {
//					if (s.endsWith("=")) {
//						s = s.substring(0, s.length() - 1);
//					}
//					rtList.add(s);
//				}
//			}
//			System.out.println("get from tcp");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.err.println("tcp is broken");
//			LogService.printLog("", "getMultipatternmatching for " + sentence, "tcp is broken");
//
//			List<String> tmpList = new ArrayList<>();
//			for (String s : DictionaryBuilder.getEntityTable()) {
//				if (sentence.contains(s.toLowerCase())) {
//					tmpList.add(s);
//				}
//			}
//			rtList = tmpList;
//		}
//
//		System.out.println("getMultiPatternMatching: rs=" + rtList);
//		return rtList;
	}

	public static EmotibotNeo4jConnection getDBConnection() {
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
		return neo4jDBManager.getConnection();
	}

}
