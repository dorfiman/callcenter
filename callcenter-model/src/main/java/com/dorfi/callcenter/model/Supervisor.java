package com.dorfi.callcenter.model;

public class Supervisor extends Employee {

	private Director boss;

	public Director getBoss() {
		return boss;
	}

	public void setBoss(Director boss) {
		this.boss = boss;
	}

}
