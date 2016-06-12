package com.emotibot.DB.Import;

import java.util.Vector;

	public class Queue {
		public static Vector<String> SQLS = new Vector<String>();
		public static int getSize()
		{
			return SQLS.size();
		}
		public static String getUrl()
		{
			synchronized(SQLS)
			{
				if(SQLS.size()>0)
				{
					String url=SQLS.elementAt(0);
					SQLS.removeElementAt(0);
					return url;
				}
			}
			return null;
		}

	}

