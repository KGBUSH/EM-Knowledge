package com.emotibot.weka;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils.Null;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import com.emotibot.Debug.Debug;
import com.emotibot.config.ConfigManager;
import com.emotibot.nlpparser.SimpleKnowledgeGetAnwer;
//import com.emotibot.patternmatching.NLPProcess;
//import com.emotibot.patternmatching.PatternMatchingProcess;
import com.emotibot.util.CUBean;
import com.hankcs.hanlp.seg.common.Term;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

/**
 * 
 *
 */
//com.emotibot.weka.WekaWebserver
public class WekaWebserver {
   public static  WekaPrediction demo;

	public static void main(String[] args) throws Exception {
	    PreProcess.ProduceArffNum("WekaNewWay8");
	   // demo.execute();
	 	String classifier = "weka.classifiers.bayes.NaiveBayes";
	    String dataset = "tagNew.arff";
	    demo = new WekaPrediction();
	    String[] options = new String[0];
	    demo.setClassifier(classifier,options);
	    demo.setTraining(dataset);
	    demo.execute();

		Server server = new Server(7000);
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		handler.addServletWithMapping(Weka.class, "/tag");

		// Start things up!
		server.start();

		// The use of server.join() the will make the current thread join and
		// wait until the server is done executing.
		// See
		// http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
		System.err.println("Start!");
		server.join();
	}

	@SuppressWarnings("serial")
	public static class Weka extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			doService(request, response);
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			doService(request, response);
		}

		/**
		 * @param request
		 * @param response
		 * @throws IOException
		 */
		private void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/json;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			PrintWriter out = response.getWriter();
			try{
			String text = request.getParameter("t");

			if (text != null) {
				text = text.trim();
				String result=demo.getClassifierTag(text);
				System.err.println(text+"==>"+result);
				JSONObject result_obj = new JSONObject();
				result_obj.put("result", result);
				out.println(result_obj);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			JSONObject result_obj = new JSONObject();
			result_obj.put("result", e.getMessage());
			out.println(result_obj);
		}
		}
	}
}
