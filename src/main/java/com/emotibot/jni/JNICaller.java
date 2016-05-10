package com.emotibot.jni;

public class JNICaller {
    static
    {
    }

	public native String getMultiPatternMatching(String sentence, String referenceFileName);
	public static void main(String[] args)
	{
        System.load("/Users/Elaine/Documents/workspace/Git/knowlegegraph/target/com_emotibot_jni_JNICaller.jnilib");
		//System.out.println(System.getProperty("java.library.path"));
	         new JNICaller().getMultiPatternMatching("aaaaa","target/pat1.txt");
	}
	 

}
