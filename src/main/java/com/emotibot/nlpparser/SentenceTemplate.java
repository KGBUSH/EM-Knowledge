package com.emotibot.nlpparser;

import java.util.List;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.seg.common.Term;

public class SentenceTemplate extends AbstractAIMLEngine {
    
    public SentenceTemplate() {
        super("SentenceBot");
    }
    
    public String getSentenceType(String sentence)
    { 
        String processedQ;
        NLPResult tnode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPos.getValue());
        List<Term> segword = tnode.getWordPos();
       System.out.println(segword);
        StringBuilder ss = new StringBuilder();
        for (Term t : segword) {
        	if (t.nature.toString().equals("nr"))
        		 ss.append("## ");
            ss.append(t.word);
            ss.append(" <pos>");
            ss.append(t.nature.toString());
            ss.append("</pos> "); 
        }
        System.out.println(ss);
        processedQ = insertSpace2Chinese(ss.toString());
        //System.out.println(processedQ);
        String type = removeSpaceFromChinese(chatSession.multisentenceRespond(processedQ));
        System.out.println(type);
        return type;
    }
    public static void main(String[] args){
    	SentenceTemplate sentenceTypeClassifier = new SentenceTemplate();
    	System.out.println(sentenceTypeClassifier.getSentenceType("姚明的昵称"));
    }
}
