/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
package com.emotibot.util;

import java.util.HashMap;
import java.util.Map;

/*
 * 
 * id as the identity of the entity
 * hashTable to save all the properties of the entity
 * */

public class Entity {
	private static int count = 0;
	private int id;
	private String label;
	private Map<String, String> ht;

	public Entity() {
		this.setID(count++);
		this.ht = new HashMap<>();
	}
	
	public String getProperty(String key){
		return ht.get(key);
	}

	public Map<String, String> getProperties() {
		return ht;
	}

	public void addProperty(String key, String value) {
		ht.put(key, value);
	}

	public int getID() {
		return id;
	}

	private void setID(int i) {
		this.id = i;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
