package com.dorfi.callcenter.model;

public class Call {

	private long id;
	private Customer caller;
	private Employee callee;
	private long duration; // in miliseconds
	private boolean successful; // true if the call finished successfully

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Customer getCaller() {
		return caller;
	}

	public void setCaller(Customer caller) {
		this.caller = caller;
	}

	public Employee getCallee() {
		return callee;
	}

	public void setCallee(Employee callee) {
		this.callee = callee;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

}
