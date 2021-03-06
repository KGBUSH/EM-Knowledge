package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.List;

import org.neo4j.cypher.internal.compiler.v2_2.perty.recipe.PrintableDocRecipe.evalUsingStrategy;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;

public class TraversalToGraph {
	public static EmotibotNeo4jConnection conn = getEmotibotNeo4jConnection(); //connection 
	public static BuildCypherSQL BuildCypherSQLObj = new BuildCypherSQL(); //sql query
	/**
	 * 实例化连接
	 * @return
	 */
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
	public static String getBeanAnswer(Neo4jResultBean bean){
		if(!bean.isStatus()){
			return "";
		}
		else return bean.getResult();
	}
	/**
	 * entity and attribute  traversal knowledgeGraph  to get answer
	 * @param entity
	 * @param attribute
	 * @return
	 */
	public static String traversal(List<Name_Type> entity,List<Name_Type> attribute){
		Neo4jResultBean bean = null;
		String answer= "";
		if(entity.size()> 0 ||attribute.size()>0){
		if(entity.size()==1&&attribute.size() ==0){//单个实体 没有属性
			if(entity.get(0).value.equals("姚明")){
		   String query=BuildCypherSQLObj.FindEntityInfo(Common.PERSONLABEL, entity.get(0).value);
		   bean=conn.executeCypherSQL(query);
			}
		}
	    else if(entity.size() ==1 && attribute.size() ==1){
			//单个实体单个属性
	    	if(entity.get(0).value.equals("姚明")){
	    	String query = BuildCypherSQLObj.FindEntityAttr(Common.PERSONLABEL, entity.get(0).value,attribute.get(0).value);
	    	 bean=conn.executeCypherSQL(query);
	    	}
		}else if(entity.size() ==2 && attribute.size() ==1){
			//多个实体单个属性
		}
		else if(entity.size() ==1 && attribute.size() ==2){
			//一个实体多个属性
		}
		else if(entity.size() ==2 && attribute.size() ==2){
			//多个实体多个属性
		}
		}
		if(bean != null&&getBeanAnswer(bean)!=null){
			answer =getBeanAnswer(bean);
		}
		return answer;
	}
}
