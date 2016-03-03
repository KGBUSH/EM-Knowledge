package com.emotibot.util;

import java.util.Hashtable;

/*
 * 
 * id as the identity of the entity
 * hashTable to save all the properties of the entity
 * */

public class Entity {
	private int id;
	private Hashtable<String, Object> ht;

	public Entity(int id) {
		this.setID(id);
	}
	
	public Object getProperty(String name){
		return ht.get(name);
	}
	
	public void addProperty(String name, Object value){
		ht.put(name, value);
	}

	public Hashtable<String, Object> getProperties() {
		return ht;
	}

	public void setProperties(Hashtable<String, Object> ht) {
		this.ht = ht;
	}

	public int getID() {
		return id;
	}

	private void setID(int id) {
		this.id = id;
	}

}
