package com.emotibot.weka;

import java.util.HashMap;
import java.util.Map;

public class TagCommon {
	
	public static Map<String,String> DomainNames;
	public static Map<String,Integer> mapIndex = new HashMap<>();
	public static Map<String,Integer> DomainNum = new HashMap<>();
    public static String CommonTarffStr="";
    public final static String other="other";
	static
	{
		DomainNames = new HashMap<>();
		
		DomainNames.put("TV_series","");
		DomainNames.put("anime","");
		DomainNames.put("catchword","");
		DomainNames.put("college","");
		DomainNames.put("computer_game","");
		DomainNames.put("cosmetics","");
		DomainNames.put("delicacy","");
		DomainNames.put("digital_product","");
		DomainNames.put("figure","");
		DomainNames.put("major","");
		DomainNames.put("movie","");
		DomainNames.put("novel","");
		DomainNames.put("pet","");
		DomainNames.put("sports","");
		DomainNames.put("tourism","");
		DomainNames.put("economy","");
		DomainNames.put("medical_treatment","");
		DomainNames.put("job","");
		DomainNames.put("music","");

	}


}
