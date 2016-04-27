package com.emotibot.weka;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.emotibot.util.Tool;
public class WekaTrain {
	protected static Instances train=null;
	protected static Instances test=null;
	protected static Classifier m_Classifier = null;
	protected static Evaluation eval=null;
	public static String Other="Other";
    public static void Train(String ArrFile) throws Exception
    {
    	if(Tool.isStrEmptyOrNull(ArrFile)) return ;
    	train = DataSource.read(ArrFile);
	    train.setClassIndex(0);
	    m_Classifier = new NaiveBayesUpdateable();
	    m_Classifier.buildClassifier(train);
    }
    
    public static void LoadTest(String ArrFile) throws Exception
    {
    	if(Tool.isStrEmptyOrNull(ArrFile)) return ;
    	test = DataSource.read(ArrFile);
    	test.setClassIndex(0);
    }
	
    public static void CrossValidateModelTest() throws Exception
    {
	    int r=0;int all=0;

	    for (int i = 0; i < test.numInstances(); i++) {
		      double pred = m_Classifier.classifyInstance(test.instance(i));
		     // double[] dist = m_Classifier.distributionForInstance(test.instance(i));
		      System.out.print((i+1));
		      System.out.print(" - ");
		      System.out.print(test.instance(i).toString(test.classIndex()));
		      System.out.print(" - ");
		      System.out.print(test.classAttribute().value((int) pred));
		      System.out.print(" - ");
		      all++;
		      if (pred != test.instance(i).classValue()){
			     System.out.print("Wrong  ");
			     System.err.print(test.instance(i).toString());
		      }
		      else{
			     System.out.print("Right"); r++;
		      }
		      System.out.print(" - ");
		      System.out.println();
		   }
		   for(int i=0;i<1;i++)
		   {
			 eval=new Evaluation(test);
			 eval.crossValidateModel(m_Classifier, test, 10, new Random(i));//实现交叉验证模型
		   }
		   System.out.println(eval.toSummaryString());//输出总结信息
 	       System.out.println(eval.toClassDetailsString());//输出分类详细信息
           System.out.println(eval.toMatrixString());//输出分类的混淆矩阵
     	   System.out.println("all="+all+"  r="+r+" ratio="+(r*100)/all);

    }
    

	public static void main(String args[]) throws Exception
	{
		String file="tagNew.arff";
		Train(file);
		LoadTest(file);
		CrossValidateModelTest();
		//m_Classifier.distributionForInstance(arg0)
       // Instances data=getStructure();  //获取instances框架
	}
}
