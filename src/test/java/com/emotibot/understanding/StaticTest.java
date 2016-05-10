package com.emotibot.understanding;

public class StaticTest {
	protected static String staticString;
	
	public StaticTest(String s){
		changeStaticString(s);
	}
	
	protected void printString(){
		System.out.println(staticString);
	}

	protected static void changeStaticString(String str) {
		staticString = str;
	}
}
