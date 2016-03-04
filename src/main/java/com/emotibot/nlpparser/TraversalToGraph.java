package com.emotibot.nlpparser;

import java.util.List;

import org.neo4j.cypher.internal.compiler.v2_2.perty.recipe.PrintableDocRecipe.evalUsingStrategy;

public class TraversalToGraph {

	public static String traversal(List<Name_Type> entity,List<Name_Type> attribute){
		String answer= "";
		if(entity.size()==1&&attribute.size() ==0){
			
		}
	    else if(entity.size() ==1 && attribute.size() ==1){
			//单个实体单个属性
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
