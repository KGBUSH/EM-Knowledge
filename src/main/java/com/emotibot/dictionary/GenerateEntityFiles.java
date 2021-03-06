package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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

		
		// generate entity.txt file
		generateEntity();
		String tempFileName = Common.UserDir + "/knowledgedata/entityException.txt";
		File fp = new File(tempFileName);
		if(fp.exists()){
			fp.delete();
		} 
		fp.createNewFile();
//		(new File(tempFileName)).delete();
		
		checkTemplate();
		modifyEntity();
		
//		// generate entity with label
//		generateEntityAndLabel();
//		
//		// generate entity list in first level
//		generateEntityPMFile();
//		
//		// generate entity.txt for multi-pattern matching
//		generateEntity4MultiPatternMatching();
	}
	
	public static void generateEntity4MultiPatternMatching(){
		File sourceFile = new File(Common.UserDir + "/knowledgedata/entity.txt");
		File destFile = new File(Common.UserDir + "/sentiment/entity.txt");
		
		try {
			if(destFile.exists()){
				destFile.delete();
			}
			Files.copy(sourceFile.toPath(), destFile.toPath());
			
			String tempFileName = Common.UserDir + "/sentiment/entity.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName, true));
			for(String s : DictionaryBuilder.getEntitySynonymTable().keySet()){
				out.write(s + "\r\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("generateEntity4MultiPatternMatching done");
		
	}

	public static void modifyEntity() {
		String entityRawFileName = Common.UserDir + "/knowledgedata/entity.txt";
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

			List<String> tempEntitySet = new ArrayList<>();
			BufferedReader entityRawReader = new BufferedReader(new FileReader(entityRawFileName));
			line = null;
			while ((line = entityRawReader.readLine()) != null) {
				line = CharUtil.trimAndlower(line);
				if (Tool.isStrEmptyOrNull(line)) {
					continue;
				}
				if(!entityExceptionSet.contains(line)){
					tempEntitySet.add(line);
				}
			}
			entityRawReader.close();
			entityExceptionReader.close();

			// generate new entity files
			String tempFileName = Common.UserDir + "/knowledgedata/entity.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));
			
			for(String s : tempEntitySet){
				out.write(s + "\r\n");
			}
			out.close();

//			// add entity Synonym
//			for(String s : DictionaryBuilder.getEntitySynonymTable().keySet()){
//				out.write(s + "\r\n");
//			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("modifyEntity done");
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
//			String tempFileName = Common.UserDir + "/knowledgedata/entityRaw.txt";
			String tempFileName = Common.UserDir + "/knowledgedata/entity.txt";
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
		String query = "match(n) with n.Name as name, labels(n) as l order by n.type unwind l as domain return collect(distinct name+\"###\"+domain) as result";
		List<String> list = conn.getArrayListfromCollection(query);

		// System.out.println("list="+list);

//		Set<String> tempSet = new HashSet<>();
//
//		for (String s : list) {
//			tempSet.add(s);
//		}

		try {
			// String tempFileName = Common.UserDir +
			// "/knowledgedata/KnowledgeEntityWithLabel.txt";
			String tempFileName = Common.UserDir + "/knowledgedata/entitywithlabel.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			for (String s : list) {
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
	

	// generate the entity list entity_ref_PM.txt from domain directory,
	// and check with entity.txt which is used as the entity dictionary.
	public static void generateEntityPMFile() {
		EmotibotNeo4jConnection conn = getDBConnection();

		String query = "match(n) where n.type=\"1\" with distinct n.Name as name return collect(name) as result";
		List<String> list = conn.getArrayListfromCollection(query);

//		Set<String> tempSet = new HashSet<>();
//
//		for (String s : list) {
//			tempSet.add(s);
//		}
		
		try {
			// BufferedWriter writer = new BufferedWriter(new
			// FileWriter(Common.UserDir + "/knowledgedata/entityPM.txt",
			// true));
			BufferedWriter writer = new BufferedWriter(new FileWriter(Common.UserDir + "/knowledgedata/entityPM.txt"));

			for(String s : list){
				s = CharUtil.trimAndlower(s);
				writer.write(s + "\r\n");
				if (s.length() == 1)
					System.out.println(s);
			}
			
//			for (String s : DictionaryBuilder.getEntitySynonymTable().values()) {
//				writer.write(s + "\r\n");
//				if (s.length() == 1)
//					System.out.println(s);
//			}

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// removing the characters also existing in the template rules
	public static void checkTemplate() {
		// generate the new entity dictionary 
		DictionaryBuilder.DictionaryBuilderInit();
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
