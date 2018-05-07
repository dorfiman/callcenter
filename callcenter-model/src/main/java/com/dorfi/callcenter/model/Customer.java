package com.dorfi.callcenter.model;

public class Customer extends Person {

	public static enum Type {
		BASIC, GOLD, PLATINIUM;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
