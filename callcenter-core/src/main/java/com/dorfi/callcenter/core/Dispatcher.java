package com.dorfi.callcenter.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dorfi.callcenter.model.Call;
import com.dorfi.callcenter.model.Director;
import com.dorfi.callcenter.model.Employee;
import com.dorfi.callcenter.model.Operator;
import com.dorfi.callcenter.model.Supervisor;

public class Dispatcher {

	private static final int CONCURRENT_CALLS = 10;

	private static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

	private Queue<Operator> availableOperators;
	private Queue<Supervisor> availableSupervisors;
	private Queue<Director> availableDirectors;
	private Queue<Call> pendingCalls = new ConcurrentLinkedQueue<>();
	private List<Call> finishedCalls = Collections.synchronizedList(new ArrayList<>());
	private ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_CALLS);

	public Dispatcher(List<Operator> operators, List<Supervisor> supervisors, List<Director> directors) {
		this.availableOperators = new ConcurrentLinkedQueue<>(operators);
		this.availableSupervisors = new ConcurrentLinkedQueue<>(supervisors);
		this.availableDirectors = new ConcurrentLinkedQueue<>(directors);
	}

	public void dispatchCall(Call call) {
		Optional<Employee> optional = findAvailableCallee();
		if (optional.isPresent()) {
			Employee callee = optional.get();
			dispatchCall(call, callee);
		} else {
			logger.debug("No employee available to answer call {}", call.getId());
			pendingCalls.add(call);
		}
	}

	private void dispatchCall(Call call, Employee callee) {
		Future<?> future = executorService.submit(new CallExecutor(call, callee));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Call {} was interrupted!", call.getId(), e);
			call.setSuccessful(false);
		}
		finishedCalls.add(call);
		pushCallee(callee);

		if (!pendingCalls.isEmpty()) {
			dispatchCall(pendingCalls.poll());
		}
	}

	private void pushCallee(Employee callee) {
		if (callee instanceof Operator) {
			availableOperators.add((Operator) callee);
		} else if (callee instanceof Supervisor) {
			availableSupervisors.add((Supervisor) callee);
		} else if (callee instanceof Director) {
			availableDirectors.add((Director) callee);
		}
	}

	private Optional<Employee> findAvailableCallee() {
		Employee callee = availableOperators.poll();
		if (callee == null) {
			callee = availableSupervisors.poll();
		}
		if (callee == null) {
			callee = availableDirectors.poll();
		}
		return Optional.ofNullable(callee);
	}

	public void shutdown() {
		executorService.shutdown();
	}
}
