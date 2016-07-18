package com.emotibot.DB.Import;
//com.emotibot.DB.Import.DBImport
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.emotibot.config.ConfigManager;
import com.emotibot.util.Tool;


public class DBImport {
	
	public static void main(String args[])
	{
		if(args.length!=2) {
		    System.err.println("args.length!=2");
			System.exit(0);
		}
		String fileName=args[0].trim();
		int ThreadNum=Integer.valueOf(args[1].trim());
		Vector<String> vec=Tool.getFileLines(fileName);
		for(int index=0;index<vec.size();index++)
		{
			vec.set(index, vec.get(index).replace("return result;", ";"));
		}
		Queue.SQLS=vec;
		System.err.println(Queue.getSize());
		ConfigManager cf = new ConfigManager();

        ExecutorService exec=Executors.newCachedThreadPool();  
        for(int i=0;i<ThreadNum;i++)  
            exec.execute(new DBThread(cf));  
        exec.shutdown();//并不是终止线程的运行，而是禁止在这个Executor中添加新的任务  

	}

}
