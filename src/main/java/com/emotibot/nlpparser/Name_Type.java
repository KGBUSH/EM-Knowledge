package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.seg.common.Term;

public class Name_Type {
  Term value;//实体 属性 关系的值
  String type;//实体＝1  属性 ＝2  关系 ＝ 3 关系类型 ＝  4
public Name_Type(Term term , String type){
	this.value =term ;
	this.type = type;
}


public Term getValue() {
	return value;
}


public void setValue(Term value) {
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
	return value.word+"/"+value.nature.toString()+" "+type;
}
  
}
