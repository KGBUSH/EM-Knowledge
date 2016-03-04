package com.emotibot.nlpparses;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.internal.compiler.v2_2.perty.recipe.Pretty.nest;

import com.emotibot.nlp.GetSegFun;
import com.hankcs.hanlp.seg.common.Term;

public class test {
	public static  void getAttribute() throws Exception {
		String testFile = "txt/attribute.txt";

		final BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
		String line2 = null;
		while ((line2 = reader2.readLine()) != null) {
            if (!line2.isEmpty()) {
				String[] array = line2.split("\t");
				if(array.length== 2){
					String[] attribute = array[0].split("=");
					if(attribute.length == 2){
						String attri = attribute[1];
						System.out.println(attri+" "+"ude2"+" "+"1024");
					}
				}
			}
          
		}

		reader2.close();
		

	}
	
	public static void main(String[] args){
		try {
			test.getAttribute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
