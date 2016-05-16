/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: wenjiewu@emotibot.com.cn
 * Secondary Owner: yunzhou@emotibot.com.cn
 */
package com.emotibot.WebService;

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
import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.nlpparser.SimpleKnowledgeGetAnwer;
//import com.emotibot.patternmatching.PatternMatchingProcess;
import com.emotibot.understanding.KGAgent;
import com.emotibot.util.CUBean;
import com.hankcs.hanlp.seg.common.Term;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 *
 */
public class WebServer {
	public static void main(String[] args) throws Exception {
		// Create a basic jetty server object that will listen on port 8080.
		// Note that if you set this to port 0 then a randomly available port
		// will be assigned that you can either look in the logs for the port,
		// or programmatically obtain it for use in test cases.
		
//		NLPProcess nlpProcess = new NLPProcess();
//		NLPProcess.NLPProcessInit();
		System.out.println("init NLPProcess");
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		DictionaryBuilder.DictionaryBuilderInit();

		int port = 9000;
		if(args.length>1)
		{
			System.out.println("Port args="+args[0].trim());
			port=Integer.valueOf(args[0].trim());
		}
		else{
		ConfigManager cf = new ConfigManager();
		port = cf.getWebServerPort();
		}
		Server server = new Server(port);

		// The ServletHandler is a dead simple way to create a context handler
		// that is backed by an instance of a Servlet.
		// This handler then needs to be registered with the Server object.
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		// Passing in the class for the Servlet allows jetty to instantiate an
		// instance of that Servlet and mount it on a given context path.

		// IMPORTANT:
		// This is a raw Servlet, not a Servlet that has been configured
		// through a web.xml @WebServlet annotation, or anything similar.
		// handler.addServletWithMapping(NlpServlet.class, "/web");
		handler.addServletWithMapping(KGServletJson.class, "/json");

		// Start things up!
		server.start();

		// The use of server.join() the will make the current thread join and
		// wait until the server is done executing.
		// See
		// http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
		server.join();
	}

	@SuppressWarnings("serial")
	public static class NlpServlet extends HttpServlet {
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
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			PrintWriter out = response.getWriter();
			out.println("<html><body>");
			out.println("<form action=\"/\" method=\"POST\">");
			out.println("输入文本: ");
			out.println("<input type=\"text\" name=\"t\">");
			out.println("<input type=\"submit\" name=\"提交\">");
			out.println("</form>");
			int flag = 0;
			String text = request.getParameter("t");
			if (text != null) {
				text = text.trim();
				if (!text.isEmpty()) {
					try {
						flag = Integer.parseInt(request.getParameter("f"));
					} catch (NumberFormatException e) {

					}
					System.out.println(text);
					SimpleKnowledgeGetAnwer simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
					String answer = simpleKnowledgeGetAnwer.getAnswer(text);
					JSONObject result_obj = new JSONObject();
					/*
					 * out.println( "");
					 * 
					 * out.println( ""); out.println( ""); out.println( "");
					 */
					out.println(answer);
					/*
					 * NLPResult tnNode = NLPSevice.ProcessSentence(text, flag);
					 * out.println(tnNode.getWordPos());
					 * out.println(tnNode.getReCoNLLSentence());
					 * out.println(tnNode.getNer());
					 */
					// out.println(tnNode.getSynonyms());
				}
			}

			out.println("</body></html>");
		}
	}

	@SuppressWarnings("serial")
	public static class KGServletJson extends HttpServlet {
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
			try {
				int flag = 0;
				String text = request.getParameter("t");
				String questionType = request.getParameter("questionType");
				String scoreStr = request.getParameter("score");
				String uniqId = request.getParameter("uniqId");
				Debug.printDebug(uniqId, 3, "knowledge", "knowedge doService request=" + request.toString());
				if (text != null) {
					text = text.trim();
					long t = System.currentTimeMillis();
					CUBean cuBean = new CUBean();
					cuBean.setText(text);
					cuBean.setQuestionType(questionType);
					cuBean.setScore(scoreStr);
					cuBean.setUniqueID(uniqId);
					System.out.println("@@@@@@@@@@@@@@@processing: cuBean=" + cuBean + "\n request=" + request);
//					AnswerBean bean = new PatternMatchingProcess(cuBean).getAnswer();
					AnswerBean bean = new KGAgent(cuBean).getAnswer();
					System.out.println("Webserver bean=" + bean);
					JSONObject result_obj = new JSONObject();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String ver = sdf.format(System.currentTimeMillis());

					result_obj.put("ver", ver);

					result_obj.put("score", bean.getScore());
					result_obj.put("topic", "");
					result_obj.put("emotion", "");
					// fix the bad case "加油", there is a encode issue here if without " "
					result_obj.put("answer", bean.getAnswer());
					
//					System.out.println("getAnaswer="+bean.getAnswer());

					if (questionType != null && questionType.equals("debug")) {
						result_obj.put("debug", bean.getComments());
						System.out.println("debug comments=" + bean.getComments());
					}

					long t2 = System.currentTimeMillis();
					result_obj.put("time", (t2 - t) + "ms");

//					System.out.println("result_obj="+result_obj);
					out.println(result_obj);
				}
			} catch (Exception e) {
				JSONObject result_obj = new JSONObject();
				result_obj.put("ver", "");
				result_obj.put("score", 0);
				result_obj.put("topic", "");
				result_obj.put("emotion", "");
				result_obj.put("answer", "");
				result_obj.put("Exception", e.getMessage());
				out.println(result_obj);
			}
		}
	}
}
