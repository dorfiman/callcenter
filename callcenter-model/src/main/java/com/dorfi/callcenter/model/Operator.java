package com.dorfi.callcenter.model;

public class Operator extends Employee {

	private Supervisor boss;

	public Supervisor getBoss() {
		return boss;
	}

	public void setBoss(Supervisor boss) {
		this.boss = boss;
	}

}
