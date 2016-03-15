package com.emotibot.nlpparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;



public abstract class AbstractAIMLEngine {
    private String aiml_path;
    private Bot templateBot;
    protected Chat chatSession;
    
    public AbstractAIMLEngine(String botname) {
        configAIML();
        this.templateBot = new Bot(botname, aiml_path);
        this.chatSession = new Chat(templateBot);
    }
    /**
     * 配置模版路径
     */
    private final void configAIML() {
//        final Properties properties = new Properties();
//        final String configFileName = "aiml.property";
//        final InputStream inStream = AbstractAIMLEngine.class.getClassLoader().getResourceAsStream(configFileName);
//        try {
//            properties.load(inStream);
//        } catch (final IOException e) {
//            this.aiml_path = SentenceTypeClassifier.class.getResource("").getPath();
//        }
        this.aiml_path = Common.UserDir;//properties.getProperty("aiml_path", SentenceTypeClassifier.class.getResource("").getPath());        
    }
    /**
     * 过滤掉非中文字符
     * @param c
     * @return
     */
    protected boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
  /**
   * 
   * @param s
   * @return
   */
    protected String insertSpace2Chinese(String s) {
        int tail = 0;
        char[] temp = new char[s.length() * 2];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isChinese(c)) {
                temp[tail] = c;
                temp[tail + 1] = ' ';
                tail += 2;
            } else {
                temp[tail] = c;
                tail++;
            }
        }
        return new String(temp);
    }
    /**
     * 删除加入的特定字符（＃＃）前的字
     * @param s
     * @return
     */
    protected String removeSpaceFromChinese(String s) {
        Boolean bCn = false;
        int tail = 0;
        char[] temp = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isChinese(c)) bCn = true;
            else if (c != ' ') bCn = false;
            if (c != ' ') {
                temp[tail] = c;
                tail ++;
            } else if (!bCn) {
                temp[tail] = c;
                tail++;
            }
        }
        return new String(temp);
    }
}
