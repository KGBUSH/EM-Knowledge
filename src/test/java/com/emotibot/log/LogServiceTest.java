package com.emotibot.log;

import org.junit.Test;

import junit.framework.TestCase;
import com.emotibot.log.*;
@SuppressWarnings("unused")
public class LogServiceTest extends TestCase {
    @Test
	public void test()
	{
		CommonLogService logService = CommonLogServiceImpl.getInstance("knowledgeGraph");
		
		logService.log("text");
		
	}

}
