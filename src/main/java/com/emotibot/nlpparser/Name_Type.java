package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.List;



public class Name_Type {
  String value;//实体 属性 关系的值
  String type;//实体＝1  属性 ＝2  关系 ＝ 3 关系类型 ＝  4
public Name_Type(String term , String type){
	this.value =term ;
	this.type = type;
}


public String getValue() {
	return value;
}


public void setValue(String value) {
	this.value = value;
}


public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}

@Override
public String toString(){
	return value+" "+type;
}
  
}
