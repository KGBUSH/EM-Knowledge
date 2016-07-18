package com.emotibot.understanding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import net.sf.json.JSONObject;

public class ParseJson {


	//get the json content by url correspond each name 
	public String loadJson (String name) {
		int state = -1;
		String url = "https://way.jd.com/showapi/search_news?title="+name+"&page=1&appkey=69719886345831a185360013e4c39ebe";
        StringBuilder json = new StringBuilder();  
        try {  
            URL urlObject = new URL(url);  
            HttpURLConnection uc = (HttpURLConnection) urlObject.openConnection(); 
            uc.setConnectTimeout(20000);  
            uc.setReadTimeout(20000); 
            state = uc.getResponseCode();
            if(state == 200){
            	 BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));  
                 String inputLine = null;  
                 while ( (inputLine = in.readLine()) != null) {  
                     json.append(inputLine);  
                 }  
                 in.close();  
            }else {
				System.out.println("connect false in loadJson with name = " + name);
				return json.toString();
			}
           
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return json.toString();  
    }  
	
	//parse the json by key contained in json
	@SuppressWarnings("unchecked")
	public String parseJsonNode(String json,String keyName){
		String result = "";
		JSONObject jsObj = JSONObject.fromObject(json);
		Iterator<String> iterator = jsObj.keys();
		while(iterator.hasNext()){
			String key = iterator.next().toString();
			if(key.equals(keyName)){
				result = jsObj.getString(key);
				break;
			}
		}
		return result;
	}
	
	//parse the json 
	//get the value of key "allNum"
	public  String getValueByKeyFromJson(String jsonAll){
		String resultStr = parseJsonNode(jsonAll, "result");
		String showapi_res_bodyStr = parseJsonNode(resultStr, "showapi_res_body");
		String pagebeanStr = parseJsonNode(showapi_res_bodyStr, "pagebean");
		String allNumStr = parseJsonNode(pagebeanStr, "allNum");
		return allNumStr;
	}
	
	//get the value of "allNum" in json by each name
	public boolean isHasNewsOfSomeOne(String name){
		boolean result = false;
		String value = "";
		String jsonAll = loadJson(name);
		if(!jsonAll.isEmpty()){
			value = getValueByKeyFromJson(jsonAll);
		}else {
			return result;
		}
		if(!value.isEmpty()&&Integer.parseInt(value) > 0){
			result = true;
		}
		return result;
	}
	
	public static void main(String[] args) {
		
		ParseJson parseJson = new ParseJson();
		System.out.println(parseJson.isHasNewsOfSomeOne("阿里阿福"));
	}
}
