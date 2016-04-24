package com.emotibot.weka;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;

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

		  public void setTraining(String name) throws Exception {
		    m_TrainingFile = name;
		    m_Training = new Instances(new BufferedReader(
		      new FileReader(m_TrainingFile)));
		    m_Training.setClassIndex(0);
		  }

		  public void execute() throws Exception {
		    // run filter
		    //m_Filter.setInputFormat(m_Training);
		    Instances filtered = m_Training;

		    // train classifier on complete file for tree
		    m_Classifier.buildClassifier(filtered);

		    // 10fold CV with seed=1
		    m_Evaluation = new Evaluation(filtered);
		    m_Evaluation.crossValidateModel(m_Classifier, filtered, 10,m_Training.getRandomNumberGenerator(1));
		  }

		  @Override
		  public String toString() {
		    StringBuffer result;

		    result = new StringBuffer();
		    result.append("Weka - Demo\n===========\n\n");

		   /* result.append("Classifier...: " + Utils.toCommandLine(m_Classifier) + "\n");

		    result.append("Training file: " + m_TrainingFile + "\n");
		    result.append("\n");

		    result.append(m_Classifier.toString() + "\n");
		    result.append(m_Evaluation.toSummaryString() + "\n");*/
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

		  public static String usage() {
		    return "\nusage:\n  " + WekaPrediction.class.getName()
		      + "  CLASSIFIER <classname> [options] \n"
		      + "  FILTER <classname> [options]\n" + "  DATASET <trainingfile>\n\n"
		      + "e.g., \n" + "  java -classpath \".:weka.jar\" WekaDemo \n"
		      + "    CLASSIFIER weka.classifiers.trees.J48 -U \n"
		      + "    FILTER weka.filters.unsupervised.instance.Randomize \n"
		      + "    DATASET iris.arff\n";
		  }


		  public static void main(String[] args) throws Exception {
			  WekaPrediction demo;

		    // parse command line
		    String classifier = "weka.classifiers.bayes.NaiveBayesUpdateable";
			  //weka.classifiers.functions.Logistic
			 // String classifier = "weka.classifiers.functions.Logistic";
		    String filter = "";
		    String dataset = "tag.arff";
            Vector<String> classifiers = new Vector<String>();
            classifiers.add("");
		    // run
            //for(String classifier:classifiers){
           // Logistic m_classifier=new Logistic();//Logistic用以建立一个逻辑回归分类器

		    demo = new WekaPrediction();
		    String[] options = new String[0];
		    demo.setClassifier(classifier,options);
		    demo.setTraining(dataset);
		    demo.execute();
		    System.err.println("classifier="+classifier);
		    System.out.println(demo.toString());
           // }
		  }
}
