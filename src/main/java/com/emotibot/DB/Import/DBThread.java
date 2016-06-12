package com.emotibot.DB.Import;

import java.io.FileOutputStream;
import java.io.InputStream;

import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.util.Neo4jResultBean;

public class DBThread implements Runnable {
	public EmotibotNeo4jConnection conn = null;

	public DBThread(ConfigManager configManager) {
		conn = new EmotibotNeo4jConnection(configManager.getNeo4jDriverName(), configManager.getNeo4jServerIp(),
				configManager.getNeo4jServerPort(), configManager.getNeo4jUserName(), configManager.getNeo4jPasswd());

	}

	@Override
	public void run() {
		while (true) {
			String sql = "";// = Queue.getUrl();
			try {
				sql = Queue.getUrl();
				if (sql == null) {
					try {
						Thread.sleep(10 * 1000);
						System.err.println("Queue is Empty");
						System.exit(0);
					} catch (Exception e) {

					}
				} else {
					boolean bean = conn.updateQuery(sql);
					System.err.println(bean);
				}
			} catch (Exception e) {
				System.err.println(sql+"  "+e.toString());
			}
		}

	}

}
