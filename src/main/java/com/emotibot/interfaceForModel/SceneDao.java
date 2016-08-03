package com.emotibot.interfaceForModel;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.understanding.DBProcess;
import com.emotibot.understanding.NLPUtil;

public class SceneDao {

	public List<String> getFamousAddressByEntity(String entity,String pro){
		String label = "";
		List<String> result = new ArrayList<String>();
		List<String> labelList = NLPUtil.getLabelListByEntity(entity);
		if(labelList.contains("tourism")){
			label = "tourism";
		}else if (labelList.contains("delicacy")) {
			label = "delicacy";
		}else {
			return result;
		}
		String[] listName = null;
		String name = DBProcess.getPropertyValue(label, entity, pro);
		if(name.contains("，")){
			listName = name.trim().split("，");
		}else if(name.contains("、")) {
			listName = name.trim().split("、");
		}else {
			listName = name.trim().split(" ");
		}
		for(String str : listName){
			result.add(str);
		}
		return result;
	}
	public static void main(String[] args) {
		DictionaryBuilder.DictionaryBuilderInit();
		SceneDao sceneDao = new SceneDao();
		System.out.println(sceneDao.getFamousAddressByEntity("北京","著名景点"));
	}
}
