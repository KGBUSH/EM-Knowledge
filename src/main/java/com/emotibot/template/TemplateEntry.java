package com.emotibot.template;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emotibot.Debug.Debug;
import com.emotibot.common.Common;
import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.log.LogService;
import com.emotibot.understanding.DBProcess;
import com.emotibot.understanding.NLPUtil;
import com.emotibot.util.Tool;

public class TemplateEntry {
//	static TemplateProcessor sentenceTemplate = new TemplateProcessor("Knowledge");
	static TemplateProcessor questionClassifier;
	// static TemplateProcessor = new TemplateProcessor("QuestionClassifier");
	static TemplateProcessor figure_introductionTemplate;
//	static TemplateProcessor TV_seriesTemplate = new TemplateProcessor("TV_series");
//	static TemplateProcessor animeTemplate = new TemplateProcessor("anime");
//	static TemplateProcessor catchwordTemplate = new TemplateProcessor("catchword");
//	static TemplateProcessor collegeTemplate = new TemplateProcessor("college");
//	static TemplateProcessor computer_gameTemplate = new TemplateProcessor("computer_game");
//	static TemplateProcessor cosmeticsTemplate = new TemplateProcessor("cosmetics");
//	static TemplateProcessor delicacyTemplate = new TemplateProcessor("delicacy");
//	static TemplateProcessor digital_productTemplate = new TemplateProcessor("digital_product");
//	static TemplateProcessor economyTemplate = new TemplateProcessor("economy");
//	static TemplateProcessor figureTemplate = new TemplateProcessor("figure");
//	static TemplateProcessor majorTemplate = new TemplateProcessor("major");
//	static TemplateProcessor medical_treatmentTemplate = new TemplateProcessor("medical_treatment");
//	static TemplateProcessor movieTemplate = new TemplateProcessor("movie");
//	static TemplateProcessor novelTemplate = new TemplateProcessor("novel");
//	static TemplateProcessor sportsTemplate = new TemplateProcessor("sports");
//	static TemplateProcessor sports_organizationTemplate = new TemplateProcessor("sports_organization");
//	static TemplateProcessor tourismTemplate = new TemplateProcessor("tourism");
//	static TemplateProcessor varity_showTemplate = new TemplateProcessor("varity_show");
	
	static TemplateProcessor [] staticTemplateArr;
	static TemplateProcessor [] staticTemplateByDomain;
	static Map<String, TemplateProcessor> templateMap;
	static Map<String, TemplateProcessor> templateByIntroductonMap;
	
	public static void TemplateEntryInit(){
		questionClassifier = new TemplateProcessor("QuestionClassifier");
		figure_introductionTemplate = new TemplateProcessor("figure_introduction");
		staticTemplateArr = createTemplateArrary();
		staticTemplateByDomain = createTemplateByDomain();
		templateMap = buildTemplateMap();
		templateByIntroductonMap = buildTemplateByDomainMap();
	}
	
	private static TemplateProcessor [] createTemplateArrary(){
		if(DictionaryBuilder.getDomainAllListTable().isEmpty()){
			System.err.println("domain list is empty");
			LogService.printLog("", "TemplateEntry", "domain List is empty");
			return null;
		}
		
		TemplateProcessor [] templateArr = new TemplateProcessor [DictionaryBuilder.getDomainAllListTable().size()];
		int i = 0;
		for(String domainname : DictionaryBuilder.getDomainAllListTable()){
//			System.out.println(domainname);
			templateArr[i++] = new TemplateProcessor(domainname);
		}
		
		return templateArr;
	}
	
	private static TemplateProcessor [] createTemplateByDomain(){
		if(DictionaryBuilder.getDomainAllListTable().isEmpty()){
			System.err.println("domain list is empty");
			LogService.printLog("", "TemplateEntry", "domain List is empty");
			return null;
		}
		
		TemplateProcessor [] templateDomain = new TemplateProcessor [DictionaryBuilder.getDomainAllListTable().size()];
		int i = 0;
		for(String domainname : DictionaryBuilder.getDomainAllListTable()){
//			System.out.println(domainname);
			String name = "introduction_"+domainname;
			templateDomain[i++] = new TemplateProcessor(name);
		}
		
		return templateDomain;
	}
	
	private static Map<String, TemplateProcessor> buildTemplateMap() {
		Map<String, TemplateProcessor> map = new HashMap<>();
		
		int i = 0;
		for(String domainname : DictionaryBuilder.getDomainAllListTable()){
			map.put(domainname, staticTemplateArr[i++]);
		}

//		map.put("tv_series", TV_seriesTemplate);
//		map.put("anime", animeTemplate);
//		map.put("catchword", catchwordTemplate);
//		map.put("college", collegeTemplate);
//		map.put("computer_game", computer_gameTemplate);
//		map.put("cosmetics", cosmeticsTemplate);
//		map.put("delicacy", delicacyTemplate);
//		map.put("digital_product", digital_productTemplate);
//		map.put("economy", economyTemplate);
//		map.put("figure", figureTemplate);
//		map.put("major", majorTemplate);
//		map.put("medical_treatment", medical_treatmentTemplate);
//		map.put("movie", movieTemplate);
//		map.put("novel", novelTemplate);
//		map.put("sports", sportsTemplate);
//		map.put("sports_organization", sports_organizationTemplate);
//		map.put("tourism", tourismTemplate);
//		map.put("varity_show", varity_showTemplate);

		return map;
	}
	
	private static Map<String, TemplateProcessor> buildTemplateByDomainMap() {
		Map<String, TemplateProcessor> map = new HashMap<>();
		
		int i = 0;
		for(String domainname : DictionaryBuilder.getDomainAllListTable()){
			String name = "introduction_"+domainname;
			map.put(name, staticTemplateByDomain[i++]);
		}
		return map;
	}

	public static TemplateProcessor getDomainTemplate(String domain) {
		if (Tool.isStrEmptyOrNull(domain) || !templateMap.keySet().contains(domain)) {
			System.err.println("wrong input, domain=" + domain);
			return new TemplateProcessor("");
		}
		return templateMap.get(domain);
	}

	public static TemplateProcessor getDomainTemplateByIntroduction(String domain) {
		if (Tool.isStrEmptyOrNull(domain) || !templateByIntroductonMap.keySet().contains(domain)) {
			System.err.println("wrong input, domain=" + domain);
			return new TemplateProcessor("");
		}
		return templateByIntroductonMap.get(domain);
	}
	
	// template process, change the exception cases
	// input: entity and sentence, "姚明", "姚明多高"
	// output: the sentence changed by template, "姚明身高多少"
	public static String templateProcess(String label, String entity, String sentence, String uniqueID) {
		Debug.printDebug(uniqueID, 3, "knowledge", "entity=" + entity + ", setnence=" + sentence);
		System.out.println("TEMPLATE: entity=" + entity + ", setnence=" + sentence);
		if (sentence.lastIndexOf(entity) == -1 || sentence.equals(entity)) {
			Debug.printDebug(uniqueID, 4, "knowledge", "special case: return setnence=" + sentence);
			return sentence;
		}

		// TBC: remove the possible mood word
//		sentence = NLPUtil.removeMoodWord(entity, sentence);
		
		String[] strArr = sentence.split(entity);
		if (strArr.length == 0) {
			return "";
		}

		String tempStr = strArr[0];
		for (int i = 1; i < strArr.length; i++) {
			tempStr += "## " + entity + " <type>entity</type>" + "<label>" + label + "</label> ";
			tempStr += strArr[i];
		}
		// if entity appear in the last
		if (sentence.endsWith(entity)) {
			tempStr += "## " + entity + " <type>entity</type>" + "<label>" + label + "</label> ";
		}

		System.out.println("\t templateProcess: label=" + label);
		String templateRS = getDomainTemplate(label.toLowerCase()).process(tempStr);

		// String templateRS = sentenceTemplate.process(tempStr);
		if (templateRS.isEmpty()) {
			templateRS = sentence;
		}

		System.out.println("\t templateProcess: tempStr=" + tempStr + ", templateRS=" + templateRS);
		return templateRS;
	}

	private static void generateTemplateName() {
		String tempFileName = Common.UserDir + "/knowledgedata/domain/domainList.txt";

		try {
			BufferedReader reader = new BufferedReader(new FileReader(tempFileName));
			String domain = null;
			int i = 0;
			while ((domain = reader.readLine()) != null) {
				// System.out.println("static TemplateProcessor
				// "+domain+"Template"+" = new
				// TemplateProcessor(\""+domain+"\");");
				// System.out.println(domain+"Template("+i+++"), ");
				System.out.println("map.put(\"" + domain + "\", " + domain + "Template);");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		System.out.println("99999");
		String string = "姚明身高";
		String string2 = "姚明";
		System.out.println(getDomainTemplate("movie").toString());
//		generateTemplateName();

	}

}
