package com.emotibot.dictionary;

import java.io.File;
import java.io.IOException;

import org.netlib.util.booleanW;

import com.emotibot.common.Common;
import com.emotibot.template.TemplateGenerator;

public class KGGenrateFiles {
	
	private static boolean KGFileAll = true;
	
	/*
	 * highFrequentWord
	 */
	private static boolean highFrequentWordPartOf_txt = false;
	private static boolean removeableHighFrequent_txt = false;
	
	/*
	 * entity files generate
	 */
	private static boolean knowledgedata_entity_txt = false;
	private static boolean entitywithlabel_txt = false;
	private static boolean entityPM_txt = false;
	private static boolean entitySynonym_txt = false;	//entity synonym mapping
	private static boolean sentiment_entity_txt = false;
//	private static boolean entityFirstLevel_txt = false;
//	private static boolean duplicateEntity_txt = false;
//	private static boolean entity_ref_PM_txt = false;
	
	/*
	 * property synonym 
	 */
	private static boolean SynonymNoun_txt = false;
	
	
	/*
	 * template file generate 
	 */
	private static boolean domainTemplate = false;
	private static boolean QuestionClassifierTemplate = false;
	private static boolean introductionTemplateByDomain = false;
	
	/*
	 * other 
	 */
//	private static boolean DYC_Default_txt = false;
//	private static boolean DYC_Info_txt = false;
	
	/*
	 * contains:
	 * 		/resources/test/exceptionList.txt
	 *		/resources/test/EntityUpdateList.txt
	 * 		/resources/test/multi.txt
	 * 		/resources/test/cypher.txt
	 */
//	private static boolean d_resources_test = false;
	
	public static void main(String[] args) throws IOException {
//		if(DYC_Info_txt || KGFileAll ){
//			GenerateAuxFiles.getEntityInfoInList();
//			System.out.println("/resources/DYC_Info.txt has generated!");
//		}
//		if(d_resources_test || KGFileAll){
//			GenerateAuxFiles.hotFixEntityLabel();
//			System.out.println("/resources/test/exceptionList.txt && EntityUpdateList.txt && multi.txt && cypher.txt has generated!");
//		}
		
		/**
		 * generate High Freqent related files
		 */
		if(highFrequentWordPartOf_txt || KGFileAll){
			//依赖Dic
			DictionaryBuilder.DictionaryBuilderInit();
			GenerateDictionaryFile.generatehighFrequentWordFile();
			System.out.println("/knowledgedata/dictionary/highFrequentWordPartOf.txt has generated!");
		}
		
		/**
		 * generate entity related files from neo4j 
		 */
		// generate entity.txt
		if(knowledgedata_entity_txt || KGFileAll){
			GenerateEntityFiles.generateEntity();
			String tempFileName = Common.UserDir + "/knowledgedata/entityException.txt";
			File fp = new File(tempFileName);
			if(fp.exists()){
				fp.delete();
			} 
			fp.createNewFile();
			GenerateEntityFiles.checkTemplate();
			GenerateEntityFiles.modifyEntity();
			System.out.println("/knowledgedata/entity.txt has generated!");
		}
		// generate entity synonym mapping file from Tongyici.txt which comes from DB process (maintained by Liutao)
		if(entitySynonym_txt || KGFileAll){
			//依赖Dic
			DictionaryBuilder.DictionaryBuilderInit();
			GenerateAuxFiles.generateSynonymnEntityFile();
			System.out.println("/knowledgedata/entitySynonym.txt has generated!");
		}
		// generate entity.txt for multipattern matching, entity.txt + synonymEntity
		if(sentiment_entity_txt || KGFileAll){
			DictionaryBuilder.DictionaryBuilderInit();
			GenerateEntityFiles.generateEntity4MultiPatternMatching();
			System.out.println("/sentiment/entity.txt has generated!");
		}
		
		if(entitywithlabel_txt || KGFileAll){
			GenerateEntityFiles.generateEntityAndLabel();
			System.out.println("/knowledgedata/entitywithlabel.txt has generated!");
		}
		if(entityPM_txt || KGFileAll){
			GenerateEntityFiles.generateEntityPMFile();
			System.out.println("/knowledgedata/entityPM.txt has generated!");
		}
		
//		if(DYC_Default_txt || KGFileAll){
//			GeneratePMRefFiles.getEntityInfoInList();
//			System.out.println("/txt/temp/DYC_Default.txt has generated!");
//		}
//		if(entityFirstLevel_txt || KGFileAll){
//			GenerateDictionaryFile.generateFirstLevelEntity();
//			System.out.println("/knowledgedata/entityFirstLevel.txt has generated!");
//		}
		
		/**
		 * generate property synonym from SynonymPM.txt which is maintained by Yanjun
		 */
		if(SynonymNoun_txt || KGFileAll){
			SynonymGenerator.generator();
			System.out.println("/knowledgedata/SynonymNoun.txt has generated!");
		}
		
		// it depends highfreqent.txt, entity.txt, and entityFirstLevel.txt
		if(removeableHighFrequent_txt || KGFileAll){
			//依赖Dic
			DictionaryBuilder.DictionaryBuilderInit();
			GenerateDictionaryFile.generateRemoveableHighFrequentWordFile();
			System.out.println("/knowledgedata/dictionary/removeableHighFrequent.txt && removeableHighFrequentAll.txt has generated!");
		}
		
		/**
		 * generate template files from tempalte rule files maintained by Yanjun
		 */
		if(domainTemplate || KGFileAll){
			TemplateGenerator.generateDomainTemplate();
			System.out.println("domainTemplate has generated!");
		}
		if(QuestionClassifierTemplate || KGFileAll){
			TemplateGenerator.generateQuestionClassifierTemplate();
			System.out.println("QuestionClassifierTemplate has generated!");
		}
		
		if(introductionTemplateByDomain || KGFileAll){
			TemplateGenerator.generateIntroductionTemplateByDomain();
			System.out.println("IntroductionTemplateByDomain has generated!");
		}
//		if(entity_ref_PM_txt){
//			GenerateDictionaryFile.generateEntityPMRefFile();
//			System.out.println("/knowledgedata/entity_ref_PM.txt && /entity_missing.txt has generated!");
//		}
//		if(duplicateEntity_txt || KGFileAll){
//			GenerateDictionaryFile.generateEntityPMRefFile();
//			GenerateDictionaryFile.findDuplicateEntity();
//			System.out.println("/txt/debug/duplicateEntity.txt has generated!");
//		}
		
		
	}
}
