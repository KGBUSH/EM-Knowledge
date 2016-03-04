package com.emotibot.nlpparser;

import java.util.List;

import org.neo4j.cypher.internal.compiler.v2_2.perty.recipe.PrintableDocRecipe.evalUsingStrategy;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;

public class TraversalToGraph {
	public static EmotibotNeo4jConnection conn = getEmotibotNeo4jConnection();
	public static BuildCypherSQL BuildCypherSQLObj = new BuildCypherSQL();
	public static EmotibotNeo4jConnection getEmotibotNeo4jConnection()
	{
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
	public static String traversal(List<Name_Type> entity,List<Name_Type> attribute){
		
		String answer= "";
		if(entity.size()==1&&attribute.size() ==0){
			answer=BuildCypherSQLObj.FindEntityInfo(Common.PERSONLABEL, entity.get(0).value.word);
		}
	    else if(entity.size() ==1 && attribute.size() ==1){
			//单个实体单个属性
	    	answer = BuildCypherSQLObj.FindEntityAttr(Common.PERSONLABEL, entity.get(0).value.word,attribute.get(0).value.word);
		}else if(entity.size() ==2 && attribute.size() ==1){
			//多个实体单个属性
		}
		else if(entity.size() ==1 && attribute.size() ==2){
			//一个实体多个属性
		}
		else if(entity.size() ==2 && attribute.size() ==2){
			//多个实体多个属性
		}
		return answer;
	}
}
