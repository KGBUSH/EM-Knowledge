package com.emotibot.neo4jprocess;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
import java.util.Map;

public interface CypherSQLParser {
//build KnowledgeGraph
	public String InsertEntityNode(String Label,String name,Map<String,String> attr);
	public String InsertEntityEdge(String LabelA,String nameA,String relation,String LabelB,String nameB);
	
//search KnowledgeGraph
	public String FindEntityInfo(String Label,String name);
	public String FindEntityAttr(String Label,String name,String attr);
}
