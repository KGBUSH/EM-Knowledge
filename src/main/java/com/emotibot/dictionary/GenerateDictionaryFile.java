package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import com.emotibot.util.Tool;

public class GenerateDictionaryFile {

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

	public static void changeDB() {
		EmotibotNeo4jConnection conn = getDBConnection();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/txt/temp/tourism.txt.txt"));
			String line = in.readLine();

			while (line != null) {
				line = CharUtil.trim(line);
				System.out.println("enil=" + line + ";");
				String queryCount = "match(n{Name:\"" + line + "\"}) return count(n) as " + Common.ResultObj;

				Neo4jResultBean bean = conn.executeCypherSQL(queryCount);
				System.out.println("in DBProcess, it return " + bean.getResult());
				if (bean.getResult().equals("2")) {
					String queryDel = "match(n:tourism{Name:\"" + line + "\"}) delete n";
					conn.executeCypherSQL(queryDel);
					String queryUpdate = "match(n:other{Name:\"" + line + "\"}) set n:tourism remove n:other";
					conn.executeCypherSQL(queryUpdate);
				}

				line = in.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
			String tempFileName = Common.UserDir + "/knowledgedata/entity.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			for (String s : tempSet) {
				if (s.length() == 1 && !NLPUtil.isEntityPM(s)) {
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

	public static void generateFirstLevelEntity() {
		EmotibotNeo4jConnection conn = getDBConnection();

		String query = "match(n) where not n:other with n return collect(n.Name) as result";
		List<String> list = conn.getArrayListfromCollection(query);

		Set<String> tempSet = new HashSet<>();

		for (String s : list) {
			tempSet.add(s);
		}

		try {
			String tempFileName = Common.UserDir + "/knowledgedata/entityFirstLevel.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			for (String s : tempSet) {
				out.write(s + "\r\n");
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("entity generation done");

	}

	// random check top 1000 entity with the Entity.txt
	public static void checkEntity() {
		EmotibotNeo4jConnection conn = getDBConnection();
		String query = "match(n) with n limit 1000 return collect(n.Name) as result";
		List<String> list = conn.getArrayListfromCollection(query);

		for (String s : list) {
			if (NLPUtil.isEntity(s)) {
				System.out.println(s);
			}

		}
	}

	// generate the entity list entity_ref_PM.txt from domain directory,
	// and check with entity.txt which is used as the entity dictionary.
	public static void generateEntityPMRefFile() {
		String filePath = Common.UserDir + "/knowledgedata/domain";
		List<String> entitySet = new ArrayList<>();
		try {
			String tempFileName = Common.UserDir + "/knowledgedata/entity_ref_PM.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(tempFileName));

			File fileDictoray = new File(filePath);
			File[] allFile = fileDictoray.listFiles();
			for (File f : allFile) {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader read = new InputStreamReader(fis);
				BufferedReader dis = new BufferedReader(read);
				String word = "";
				while ((word = dis.readLine()) != null) {
					if (!CharUtil.trim(word).isEmpty()) {
						entitySet.add(CharUtil.trim(word));
						out.write(CharUtil.trim(word) + "\r\n");
					}
				}
				dis.close();
			}
			out.close();

			String missingFileName = Common.UserDir + "/knowledgedata/entity_missing.txt";
			BufferedWriter outMissing = new BufferedWriter(new FileWriter(missingFileName));
			for (String s : entitySet) {
				if (NLPUtil.isEntity(s)) {
					outMissing.write(s + "\r\n");
					// System.out.println(s);
				}
			}
			outMissing.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// generate the entity list entity_ref_PM.txt from domain directory,
	// and check with entity.txt which is used as the entity dictionary.
	public static void generateEntityPMFile() {
		try {
			// BufferedWriter writer = new BufferedWriter(new
			// FileWriter(Common.UserDir + "/knowledgedata/entityPM.txt",
			// true));
			BufferedWriter writer = new BufferedWriter(new FileWriter(Common.UserDir + "/knowledgedata/entityPM.txt"));

			for (String s : DictionaryBuilder.getEntitySynonymTable().values()) {
				writer.write(s + "\r\n");
				if (s.length() == 1)
					System.out.println(s);
			}

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// generate entity synonynm files from Baidu_PM_Word which is generated by Liutao
	public static void generateDuplicateEntityFile() {
		List<String> entitySet = new ArrayList<>();
		try {

			BufferedReader in = new BufferedReader(
					new FileReader(Common.UserDir + "/knowledgedata/entity/Baidu_PM_Word"));
			String line = in.readLine();

			String outFileName = Common.UserDir + "/knowledgedata/entitySynonym.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));

			while (line != null) {
				line = line.replace(" ", " ");
				if (line.startsWith("小丈夫")) {
					System.out.println("1111=" + line);
				}
				line = CharUtil.trim(line);
				if (line.startsWith("小丈夫")) {
					System.out.println("1111=" + line);
				}
				if (line.isEmpty()) {
					line = in.readLine();
					continue;
				}
				line = line.replace("  ", " ");
				line = line.replace("  ", " ");

				String[] strArr = line.split("##");
				if (strArr.length != 2) {
					System.err.println("wrong format: line=" + line);
					line = in.readLine();
					continue;
				}

				if (!strArr[0].equals(strArr[1])) {
					out.write(line + "\r\n");
				}
				line = in.readLine();
			}

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void findDuplicateEntity() {
		try {
			String tempFileName = Common.UserDir + "/txt/debug/duplicateEntity.txt";
			BufferedWriter duplateOut = new BufferedWriter(new FileWriter(tempFileName));

			BufferedReader in = new BufferedReader(new FileReader(Common.UserDir + "/knowledgedata/entity_ref_PM.txt"));
			List<String> entityList = new ArrayList<>();
			String line = in.readLine();
			while (line != null) {
				line = CharUtil.trim(line);
				entityList.add(line);
				line = in.readLine();
			}

			Iterator it = entityList.iterator();
			while (it.hasNext()) {
				String str = it.next().toString();
				it.remove();
				if (entityList.contains(str)) {
					System.out.println(str);
					duplateOut.write(str + "\r\n");
				}
			}

			duplateOut.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void generatehighFrequentWordFile() {
		List<String> entitySet = new ArrayList<>();
		try {

			BufferedReader in = new BufferedReader(
					new FileReader(Common.UserDir + "/knowledgedata/dictionary/highFrequentWord.txt"));
			String line = in.readLine();

			String outFileName = Common.UserDir + "/knowledgedata/dictionary/highFrequentWordPartOf.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));

			Set<String> strSet = new HashSet<>();

			while (line != null) {
				line = line.replace(" ", " ");
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty()) {
					line = in.readLine();
					continue;
				}
				line = line.replace("  ", " ");
				line = line.replace("  ", " ");

				String[] strArr = line.split("\t");
				if (strArr.length != 3) {
					System.err.println("wrong format: line=" + line + ", length=" + strArr.length);
					line = in.readLine();
					continue;
				}

				int fq = Integer.parseInt(strArr[2]);
				if (fq <= 10000) {
					strSet.add(strArr[0]);
				}

				line = in.readLine();
			}
			
			for (String s : strSet) {
				out.write(s + "\r\n");
			}

			BufferedReader removeIn = new BufferedReader(
					new FileReader(Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequent_Aux.txt"));
			line = null;
			while((line = removeIn.readLine())!=null){
				line = CharUtil.trim(line).toLowerCase();
				if(!Tool.isStrEmptyOrNull(line)){
					out.write(line + "\r\n");
				}
			}
			
			in.close();
			removeIn.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void generateRemoveableHighFrequentWordFile() {
		List<String> entitySet = new ArrayList<>();
		try {

			String outFileName = Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequent.txt";
			BufferedWriter outOther = new BufferedWriter(new FileWriter(outFileName));
			String outFileName2 = Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequentAll.txt";
			BufferedWriter outAll = new BufferedWriter(new FileWriter(outFileName2));

			Set<String> setEntity = DictionaryBuilder.getEntityTable();
			Set<String> setHighWord = DictionaryBuilder.getHighFeqWordTable();
			
			for (String s : setEntity) {
				if (setHighWord.contains(s)) {
					if(!NLPUtil.isEntityPM(s)){
						outOther.write(s + "\r\n");
					}
					outAll.write(s + "\r\n");
//					String tempLabel = DBProcess.getEntityLabel(s);
//					if (tempLabel.endsWith("other")) {
//						out.write(s + "\r\n");
//					}
//					out2.write(s + "\r\n");
				}
			}
			
			// fix the bad case "加油"，"爱你"
			String inFileName = Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequent_Aux.txt";
			BufferedReader in = new BufferedReader(new FileReader(inFileName));
			String line = null;
			while((line = in.readLine())!=null){
				line = CharUtil.trim(line);
				if(Tool.isStrEmptyOrNull(line)){
					continue;
				}
				System.out.println("Aux word: "+line);
				
//				out.write(line + "\r\n");
				outAll.write(line + "\r\n");
			}
			
			in.close();
			outOther.close();
			outAll.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
//		NLPProcess nlp = new NLPProcess();
//		NLPProcess.NLPProcessInit();

		
		
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		DictionaryBuilder.DictionaryBuilderInit();
		
//		generateEntity();
		
		generatehighFrequentWordFile();
//		generateRemoveableHighFrequentWordFile();
		
		

		System.exit(0);
//		generatehighFrequentWordFile();
		// generateEntity();
		// generateFirstLevelEntity();

		// generateEntityPMFile();
	}

}
