package com.emotibot.weka;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
public class WekaTrain {
	
	
	public static void main(String args[]) throws Exception
	{
		 ArffLoader loader = new ArffLoader();
		    loader.setFile(new File("tag.arff"));
		    Instances structure = loader.getStructure();
		    structure.setClassIndex(structure.numAttributes() - 1);

		    // train NaiveBayes
		    NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
		    nb.buildClassifier(structure);
		    Instance current;
		    while ((current = loader.getNextInstance(structure)) != null)
		      nb.updateClassifier(current);

		    // output generated model
		    System.out.println(nb);
	}

}
