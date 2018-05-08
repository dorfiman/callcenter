package com.dorfi.callcenter.core;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dorfi.callcenter.model.Call;
import com.dorfi.callcenter.model.Employee;

public class CallExecutor implements Runnable {

    private static final int DURATION_FROM = 5000; // in miliseconds
    private static final int DURATION_TO = 10000; // in miliseconds

    private static Logger logger = LoggerFactory.getLogger(CallExecutor.class);

	private Call call;
	private Employee callee;

	public CallExecutor(Call call, Employee callee) {
		this.call = call;
		this.callee = callee;
	}

	@Override
	public void run() {
		call.setCallee(callee);
		int duration = calculateDuration();
		long start = System.currentTimeMillis();
		try {
			MILLISECONDS.sleep(duration);
			call.setDuration(duration);
			call.setSuccessful(true);
			logger.debug("Call {} was answered by {} for {} seconds", call.getId(), callee.getName(),
					SECONDS.convert(duration, MILLISECONDS));
		} catch (InterruptedException e) {
			logger.error("Call {} was interrupted!", call.getId(), e);
			call.setDuration(System.currentTimeMillis() - start);
			call.setSuccessful(false);
		}
		callee.setLastCall(Instant.now());
	}

	private static int calculateDuration() {
		return ThreadLocalRandom.current().nextInt(DURATION_FROM, DURATION_TO + 1);
	}

}
