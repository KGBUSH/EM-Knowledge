package com.emotibot.neo4jprocess;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
import java.util.Map;

import com.emotibot.util.Entity;

public interface CypherSQLParser {
	// build KnowledgeGraph
	
	// insert
	public String InsertEntityNode(String Label, String name, Map<String, String> attr);
	public String InsertRelation(Entity A, Entity B, String relationLabel, Map<String, String> attr);
	
	// update
	// TBD

	// search KnowledgeGraph
	public String getEntity(String label, String entity); 
	public String FindEntityInfo(String Label, String name);
	public String FindEntityAttr(String Label, String name, String attr);
	public String FindEntityAttr(String label, String name, String attr, String key);
	public String Find1stLevelEntityAttr(String Label, String name, String attr);
	public String getPropNamebyEntityName(String label, String ent, String key);
	public String getRelationshipByEntityName(String label, String ent, String key);
	public String getEntityByRelationship(String label, String entity, String relation, String key);
	public String getRelationshipInStraightPath(String labelA, String entityA, String labelB, String entityB, int step);
	public String getRelationshipInConvergePath(String labelA, String entityA, String labelB, String entityB);
	public String getRelationshipInDivergentPath(String labelA, String entityA, String labelB, String entityB);
	public String getLabelByEntity(String ent);
	public String getLabelListByEntity(String ent);
	public String getKeyListByEntity(String ent, String label);
	public String Find1stLevelEntityProperty(String label, String ent);
	public String FindEntityProperty(String label, String ent);
	public String Find1stLevelEntityRelation(String label, String ent);
	public String FindEntityRelation(String label, String ent);
}
