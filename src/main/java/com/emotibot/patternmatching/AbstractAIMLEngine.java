package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * copy from Yongning's template module (ruleEngine) with small modificaiton by Quan Zu
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import com.emotibot.common.Common;

//import com.emotibot.config.FileConfigManager;


public abstract class AbstractAIMLEngine {
    private String aiml_path;
    private Bot templateBot;
    protected Chat chatSession;
    
    public AbstractAIMLEngine(String botname) {
        configAIML();
        this.templateBot = new Bot(botname, aiml_path);
        this.chatSession = new Chat(templateBot);
    }
    
    private final void configAIML() {
        /*final Properties properties = new Properties();
        final String configFileName = "aiml.property";
        final InputStream inStream = FileConfigManager.class.getClassLoader().getResourceAsStream(configFileName);
        try {
            properties.load(inStream);
        } catch (final IOException e) {
            this.aiml_path = BaseRuleActor.class.getResource("").getPath();
        }
        this.aiml_path = properties.getProperty("aiml_path", BaseRuleActor.class.getResource("").getPath());*/
    	this.aiml_path = Common.UserDir;
    }
    
    
}
