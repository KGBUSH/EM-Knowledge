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
import java.util.Random;
public class WekaTrain {
	
	
	public static void main(String args[]) throws Exception
	{
		    Instances train = DataSource.read("tag.arff");
		    train.setClassIndex(0);
		    Instances test = DataSource.read("tag.arff");
		    test.setClassIndex(0);
		    if (!train.equalHeaders(test))
		      throw new IllegalArgumentException(
			  "Train and test set are not compatible: " + train.equalHeadersMsg(test));
		    
		    // train classifier
		    NaiveBayesUpdateable cls = new NaiveBayesUpdateable();
		    cls.buildClassifier(train);
            cls.buildClassifier(train);
		    // output predictions
		    int r=0;int all=0;
		    System.out.println("# - actual - predicted - error - distribution");
		    for (int i = 0; i < test.numInstances(); i++) {
		      double pred = cls.classifyInstance(test.instance(i));
		      double[] dist = cls.distributionForInstance(test.instance(i));
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
		     // System.out.print(Utils.arrayToString(dist));
		      System.out.println();
		   }
		      System.out.println("all="+all+"  r="+r+" ratio="+(r*100)/all);
		   /* Evaluation eval=null;
		    System.out.println("==========");
		    for(int i=0;i<1;i++){
		     eval=new Evaluation(test);
		     eval.crossValidateModel(cls, test, 10, new Random(i));//实现交叉验证模型
		    }
		    System.out.println("==========2");
		    System.out.println(eval.toSummaryString());//输出总结信息
		    System.out.println(eval.toClassDetailsString());//输出分类详细信息
		    System.out.println(eval.toMatrixString());//输出分类的混淆矩阵*/
	}
}
