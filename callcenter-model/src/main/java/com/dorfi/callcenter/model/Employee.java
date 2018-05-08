package com.dorfi.callcenter.model;

import java.time.Instant;

public abstract class Employee extends Person {

    private Instant lastCall;

	public Instant getLastCall() {
		return lastCall;
	}

	public void setLastCall(Instant lastCall) {
		this.lastCall = lastCall;
	}

}
