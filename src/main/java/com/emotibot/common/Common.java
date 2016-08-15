package com.emotibot.common;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
public class Common {
	
	public final static String EMPTY="";
	
	//KnowledgeGraph Node First Param Info :Attribute Name
	public final static String KG_NODE_FIRST_PARAM_ATTRIBUTENAME="ParamInfo";
	public final static String KG_NODE_FIRST_PARAM_MD5="key";
	public final static String KG_NODE_Pic="Pic";

	public final static String PERSONLABEL="Person";
	public final static String KGNODE_NAMEATRR="Name";
	public final static String KGNODE_TYPE="type";
	public final static String KGNODE_KEY="key";
	
	// domain name
	public final static String KGDOMAIN_FIGURE="figure";
	
	
    //sql返回变量的变量名
	public static String ResultObj="result";
	public static String RelationType="relationType";
	public static String RelationName="relationName";
    public static String UserDir=System.getProperty("user.dir");
    
    public static boolean KG_DebugStatus = false;
    
    //static ip need to get 
    public final static String IP_NLP_Provide = "http://192.168.1.126:13101/?q=";
	////////////Page
    
    /////////////Weka Train File
    public static String WekaTrainFile=System.getProperty("user.dir")+"/arff/"+"WekaTrain";

    

}
