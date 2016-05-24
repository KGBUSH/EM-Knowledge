package com.emotibot.weka;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.emotibot.common.Common;
import com.emotibot.util.Tool;

import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.core.Instance;
import weka.core.Instances;

public class WekaPrediction {
	  protected Classifier m_Classifier = null;

	  protected String m_TrainingFile = null;
	  protected Instances m_Training = null;
	  protected Evaluation m_Evaluation = null;
	  public WekaPrediction()
	  {
		  super();
	  }
	  public void setClassifier(String name, String[] options) throws Exception 
	  {
		    m_Classifier = AbstractClassifier.forName(name, options);
	  }

      public void setTraining(String name) throws Exception 
      {
		    m_TrainingFile = name;
		    m_Training = new Instances(new BufferedReader(new FileReader(m_TrainingFile)));
		    m_Training.setClassIndex(0);
	  }

		  public void execute() throws Exception {
		   Instances filtered = m_Training;
		    m_Classifier.buildClassifier(filtered);
		    m_Evaluation = new Evaluation(filtered);
		    m_Evaluation.crossValidateModel(m_Classifier, filtered, 10,m_Training.getRandomNumberGenerator(1));
			System.out.println(m_Evaluation.toSummaryString());
	 	    System.out.println(m_Evaluation.toClassDetailsString());
	        System.out.println(m_Evaluation.toMatrixString());
	           
	      /*     int split = (int) (m_Training.numInstances() * 0.8);
	           Instances traindata = new Instances(m_Training, 0, split);
	           Instances testdata = new Instances(m_Training, split, m_Training.numInstances() - split);
	           m_Classifier.buildClassifier(traindata);
	           Evaluation evaluation = new Evaluation(m_Training);
	           evaluation.crossValidateModel(m_Classifier, testdata,10,m_Training.getRandomNumberGenerator(1));
	           System.out.println(evaluation.toSummaryString());//输出总结信息
	 	       System.out.println(evaluation.toClassDetailsString());//输出分类详细信息
	           System.out.println(evaluation.toMatrixString());
	           */
		  }

		  @Override
		  public String toString() {
		    StringBuffer result;
		    result = new StringBuffer();
		    result.append("Weka - Demo\n===========\n\n");
		    try {
		      result.append(m_Evaluation.toMatrixString() + "\n");
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    try {
		      result.append(m_Evaluation.toClassDetailsString() + "\n");
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return result.toString();
		  }

		  public  String usage() {
		    return "\nusage:\n  " + WekaPrediction.class.getName()
		      + "  CLASSIFIER <classname> [options] \n"
		      + "  FILTER <classname> [options]\n" + "  DATASET <trainingfile>\n\n"
		      + "e.g., \n" + "  java -classpath \".:weka.jar\" WekaDemo \n"
		      + "    CLASSIFIER weka.classifiers.trees.J48 -U \n"
		      + "    FILTER weka.filters.unsupervised.instance.Randomize \n"
		      + "    DATASET iris.arff\n";
		  }

		  public  Instances getInstanceByTags(String tags) throws Exception
		  {
			  if(tags==null||tags.trim().length()==0) return null;
			  if(TagCommon.mapIndex.size()==0)
			  {
				  System.err.println("TagCommon.mapIndex.size()==0");
				  return null;
			  }
			  
		      Map<Integer, Integer>  map = new HashMap<>();
			  for(String tag:tags.split(" "))
			  {   
				  if(!Tool.isStrEmptyOrNull(tag)) {
					  if(TagCommon.mapIndex.containsKey(tag)) map.put(TagCommon.mapIndex.get(tag), 1);
				  }
			  }
			   if(map.size()==0)
			   {
				  System.err.println("TagCommon.mapIndex.size()==0");
				  return null;
			   }

			   List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(map.entrySet());  
			   Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {  
			            //降序排序  
			            @Override  
			            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {  
			                return o1.getKey().compareTo(o2.getKey());  
			            }
			        }); 
		        String newLine="{0 "+"novel";
		        for (Map.Entry<Integer, Integer> mapping : list) {  
					newLine+=","+mapping.getKey()+" 1";
		        } 

				newLine+="}";
				String file="tmp/"+DigestUtils.md5Hex(tags)+System.currentTimeMillis();
				FileWriter tmp = new FileWriter(file);
				tmp.write(TagCommon.CommonTarffStr+"\r\n");
				tmp.write(newLine+"\r\n");
				tmp.close();
				Instances instances = new Instances(new BufferedReader(new FileReader(file)));
                return instances;
		  }
         public  String getClassifierTag(String tags) throws Exception
          {
        	  Instances instances=getInstanceByTags(tags);
        	  if(instances==null){
        		  System.err.println("NULL");
        		  return TagCommon.other;
        	  }
        	  if(instances.numInstances()!=1) 
        	  {
        		  System.err.println("!=1");
        		  return TagCommon.other;

        	  }
			  instances.setClassIndex(0);
		      double pred = m_Classifier.classifyInstance(instances.instance(0));
              return  instances.classAttribute().value((int) pred);
          }
		  public static void main2(String[] args) throws Exception {
			  WekaPrediction demo;
		    String classifier = "weka.classifiers.bayes.NaiveBayesUpdateable";
		    String filter = "";
		    String dataset = "tag.arff";
            Vector<String> classifiers = new Vector<String>();
            classifiers.add("");
		    demo = new WekaPrediction();
		    String[] options = new String[0];
		    demo.setClassifier(classifier,options);
		    demo.setTraining(dataset);
		    demo.execute();
		    System.err.println("classifier="+classifier);
		    System.out.println(demo.toString());
		  }
		  
		  public static void main(String args[]) throws Exception
		  {
			    PreProcess.ProduceArffNum(Common.WekaTrainFile);
			    
			    WekaPrediction demo;
			    String classifier = "weka.classifiers.bayes.NaiveBayes";
			    String dataset = "tagNew.arff";
			    demo = new WekaPrediction();
			    String[] options = new String[0];
			    demo.setClassifier(classifier,options);
			    demo.setTraining(dataset);
			    demo.execute();
			    System.err.println("classifier="+classifier);
			    System.out.println(demo.toString());

			    //String tags="人物";
                //System.err.println("Tag="+ demo.getClassifierTag(tags));
			    
			    /*Vector<String> lines = Tool.getFileLines("tags");
			    FileWriter f = new FileWriter("tags_result");
			    for(String tag:lines)
			    {
			    	try{
			    	tag=tag.trim();
	                f.write("Tag="+ tag+"===>"+demo.getClassifierTag(tag)+"\r\n");	                
	                System.err.println("Tag="+ tag+"===>"+demo.getClassifierTag(tag)+"\r\n");
			    	}catch(Exception e)
			    	{
			    		e.printStackTrace();
		                System.err.println("Tag000="+ tag+"===>"+e.getMessage());
			    	}
			    }
			    f.close();*/
			    Vector<String> lines = Tool.getFileLines("l2");
			    FileWriter f = new FileWriter("tags_result");
			    for(String line:lines)
			    {
			    	try{
                   line=line.trim();
                   line=line.replaceAll("Weka:","");
                   String[] arr = line.split("###");
                   String tag=arr[0].trim();
                   String label = arr[1].trim();
                   String url = arr[2].trim();
                   String str = arr[3].trim();

                   String predict =demo.getClassifierTag(tag);
                   if(!label.equals(predict)){
	                System.err.println("Tag="+ tag+"===>"+demo.getClassifierTag(tag)+"\r\n");
	               // System.err.println(tag+"###"+label+"==>"+predict);
                   }
	                f.write("Weka:"+tag+"###"+predict+"###"+url+"###"+str+"\r\n");	                

                   
			    	}catch(Exception e)
			    	{
			    		e.printStackTrace();
			    	}
			    }
			    f.close();
			    System.err.println("End");
			    System.err.println("classifier="+classifier);
			    System.out.println(demo.toString());

		  }

}
