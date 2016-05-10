package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.understanding.DBProcess;
import com.emotibot.understanding.NLPUtil;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Neo4jResultBean;

public class GenerateAuxFiles {

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

	// from the DYC file generated by Liutao, to catch the introduction info
	// from DB for each entity
	private static void getEntityInfoInList() {
		EmotibotNeo4jConnection conn = getDBConnection();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/resources/DYC.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter(Common.UserDir + "/resources/DYC_Info.txt"));

			Set<String> refSet = new HashSet<>();
			String line = null;
			int i = 1;
			while ((line = in.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty())
					continue;
				if (CharUtil.isPuncuation(line)){
					System.out.println("isPuncuation line=" + line + ";");
					continue;
				}

				String entity = NLPUtil.getEntitySynonymNormal(line).toLowerCase();
				if (entity.isEmpty()){
					entity = line;
				} else {
					System.out.println("Synonym: line="+line+", entity="+entity);
				}
				
				if(refSet.contains(entity)){
					continue;
				} else {
					refSet.add(entity);
				}

//				String queryCount = "match(n{Name:\"" + entity + "\"}) return n.ParamInfo as " + Common.ResultObj;
//				Neo4jResultBean bean = conn.executeCypherSQL(queryCount);
//				
////				Neo4jResultBean bean = DBProcess.getEntityIntroductionInfo("", queryCount);
//				System.out.println("in DBProcess, it return " + bean.getResult());
//
//				out.write(i++ + ": " + entity);
//				if (!bean.getResult().isEmpty()) {
//					if (!line.equals(entity)) {
//						out.write(", (" + line + ")");
//					}
//
//					String tempLabel = DBProcess.getEntityLabel(entity);
//					out.write(", [" + tempLabel + "] ---- " + bean.getResult() + "\n");
//				} else {
//					out.write(" @@@@ null" + "\n");
//					System.err.println("no entity in DB: " + entity);
//				}
			}
			
			System.out.println("size="+refSet.size());

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		conn.close();

	}

	// based on the entity mapping
	// 甲型病毒性肝炎###甲肝###medicle
	private static void hotFixEntityLabel() {
		EmotibotNeo4jConnection conn = getDBConnection();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/resources/EntityMapping.txt"));
			BufferedWriter exceptionWriter = new BufferedWriter(
					new FileWriter(Common.UserDir + "/resources/test/exceptionList.txt"));
			BufferedWriter updateWriter = new BufferedWriter(
					new FileWriter(Common.UserDir + "/resources/test/EntityUpdateList.txt"));
			BufferedWriter logger = new BufferedWriter(new FileWriter(Common.UserDir + "/resources/test/log.txt"));
			BufferedWriter multiWriter = new BufferedWriter(
					new FileWriter(Common.UserDir + "/resources/test/multi.txt"));
			BufferedWriter cypherWriter = new BufferedWriter(
					new FileWriter(Common.UserDir + "/resources/test/cypher.txt"));
			String line = "";
			int i = 1;
			while ((line = in.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
				// System.out.println("line = " + line);

				if (line.isEmpty()) {
					continue;
				}
				// System.out.println("line=" + line + ";");

				if (line.contains("####")) {
					String s = "wrong format: line=" + line;
					System.err.println(s);
					logger.write(s = "\r\n");
					continue;
				}

				String[] partArr = line.split("###");
				if (partArr.length != 3) {
					String s = "wrong format: line=" + line + "; partArr=" + partArr;
					System.err.println(s);
					logger.write(s = "\r\n");
					continue;
				}

				String dbEntity = CharUtil.trim(partArr[0]);
				String pmEntity = CharUtil.trim(partArr[1]);
				String label = CharUtil.trim(partArr[2]);

				if (dbEntity.equals(pmEntity)) {
					if (label.equals("other")) {
						String s = "problem case: line=" + line;
						System.err.println(s);
						logger.write(s = "\r\n");
					}
					continue;
				}
				System.out.println("dbEntity=" + dbEntity + ", pmEntity=" + pmEntity + ", label=" + label);

				if (label.equals("other")) {
					String s = "wrong label: line=" + line;
					System.err.println(s);
					logger.write(s = "\r\n");
					continue;
				}

				String tempLabel = DBProcess.getEntityLabel(dbEntity);
				if (!tempLabel.equals("other")) {
					System.err.println("label not match: line=" + line + ", dbLabel=" + tempLabel);
					if (tempLabel.isEmpty()) {
						exceptionWriter.write(line + "@@@" + tempLabel + "\r\n");
					} else if (!label.equals(tempLabel)) {
						multiWriter.write(line + "\t &&& current label in DB:" + tempLabel + "\r\n");
					}
					continue;
				}

				updateWriter.write(line + "\r\n");
				// match(n{Name:"北京"}) set n:other remove n:tourism
				String queryCount = "match(n{Name:\"" + dbEntity + "\"}) set n:" + label + " remove n:other";
				cypherWriter.write(queryCount + "\r\n");
				Neo4jResultBean bean = conn.executeCypherSQL(queryCount);

			}

			in.close();
			exceptionWriter.close();
			updateWriter.close();
			logger.close();
			multiWriter.close();
			cypherWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// generate entity synonym txt file from the file 'word' that is generated
	// by Liutao from Mapreduce
	public static void generateSynonymnEntityFile() {
		List<String> entitySet = new ArrayList<>();
		try {

			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/resources/liutao/word"));
			String line = "";

			// String outFileName = Common.UserDir +
			// "/knowledgedata/entitySynonym.txt";
			String outFileName = Common.UserDir + "/resources/liutao/entitySynonym.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
			BufferedWriter exceptionLog = new BufferedWriter(new FileWriter(Common.UserDir + "/log/exception.txt"));
			
			int count = 1;

			while ((line = in.readLine()) != null) {
				line = line.replace(" ", " ");
				line = CharUtil.trim(line);
				if (line.isEmpty()) {
					continue;
				}
				line = line.replace("  ", " ");
				line = line.replace("  ", " ");
				
				System.out.println(count++);

				String[] strArr = line.split("###");
				if (strArr.length <= 1) {
					System.err.println("wrong format 1: line=" + line);
					exceptionLog.write(line+"\r\n");
					continue;
				}

				String DBEntity = "";
				List<String> synonymnEntitySet = new ArrayList<>();
				for (int i = 1; i < strArr.length; i++) {
					String s = CharUtil.trim(strArr[i]).toLowerCase();
					if (s.endsWith("tiletags")) {
						DBEntity = s.substring(0, s.lastIndexOf("tiletags"));
					} else {
						synonymnEntitySet.add(s);
					}
				}

				if (DBEntity.isEmpty() || synonymnEntitySet.isEmpty()) {
					System.err.println("wrong format 2: line=" + line);
					exceptionLog.write(line+"\r\n");
					continue;
				}
				
				if(synonymnEntitySet.size() == 1){
					if(synonymnEntitySet.get(0).equals(DBEntity)){
//						System.err.println("wrong format 3: line=" + line);
//						exceptionLog.write(line+"\r\n");
						continue;
					}
				}

				out.write(DBEntity + "##");
				for (String s : synonymnEntitySet) {
					if(!DBEntity.equals(s)){
						out.write(s + "##");
					}
				}
				out.write("\r\n");
			}

			in.close();
			out.close();
			exceptionLog.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		 getEntityInfoInList();
		 
		
		System.exit(0);
//		generateSynonymnEntityFile();
		// hotFixEntityLabel();
	}

}
