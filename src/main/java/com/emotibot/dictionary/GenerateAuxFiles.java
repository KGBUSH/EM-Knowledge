package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.patternmatching.DBProcess;
import com.emotibot.patternmatching.NLPProcess;
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

	private static void getEntityInfoInList() {
		EmotibotNeo4jConnection conn = getDBConnection();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/resources/DYC.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter(Common.UserDir + "/resources/DYC_Info.txt"));
			String line = in.readLine();

			int i = 1;

			while (line != null) {
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty())
					continue;
				// System.out.println("line=" + line + ";");

				String entity = NLPProcess.getEntitySynonymNormal(line).toLowerCase();
				if (entity.isEmpty())
					entity = line;

				String queryCount = "match(n{Name:\"" + entity + "\"}) return n.ParamInfo as " + Common.ResultObj;

				Neo4jResultBean bean = conn.executeCypherSQL(queryCount);
				// System.out.println("in DBProcess, it return " +
				// bean.getResult());
				out.write(i++ + ": " + entity);

				if (!bean.getResult().isEmpty()) {
					if (!line.equals(entity)) {
						out.write(", (" + line + ")");
					}

					String tempLabel = DBProcess.getEntityLabel(entity);
					out.write(", [" + tempLabel + "] ---- " + bean.getResult() + "\n");
				} else {
					out.write(" @@@@ null" + "\n");
					System.out.println("no entity in DB: " + entity);
				}

				line = in.readLine();
			}

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

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
			BufferedWriter multiWriter = new BufferedWriter(new FileWriter(Common.UserDir + "/resources/test/multi.txt"));
			BufferedWriter cypherWriter = new BufferedWriter(new FileWriter(Common.UserDir + "/resources/test/cypher.txt"));
			String line = "";
			int i = 1;
			while ((line = in.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
//				System.out.println("line = " + line);

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
				
				if(partArr[0].contains(String.valueOf((char) 160)) || partArr[1].contains(String.valueOf((char) 160))){
					System.err.println(i+++" line="+line);
				}
				
				if(partArr[0].contains(" ") || partArr[1].contains(" ")){
					System.err.println(i+++" line="+line);
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
					if(tempLabel.isEmpty()){
						exceptionWriter.write(line + "@@@" + tempLabel + "\r\n");
					} else if (!label.equals(tempLabel)){
						multiWriter.write(line + "\t &&& current label in DB:" + tempLabel + "\r\n");
					}
					continue;
				}

				updateWriter.write(line + "\r\n");
//				 match(n{Name:"北京"}) set n:other remove n:tourism
				 String queryCount = "match(n{Name:\"" + dbEntity + "\"}) set n:"+label+" remove n:other";
				 cypherWriter.write(queryCount+"\r\n");
//				 Neo4jResultBean bean = conn.executeCypherSQL(queryCount);

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
	
	

	public static void main(String[] args) {
		hotFixEntityLabel();
		// getEntityInfoInList();
	}

}
