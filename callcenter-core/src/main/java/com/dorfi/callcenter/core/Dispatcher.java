package com.dorfi.callcenter.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_CALLS);

    public Dispatcher(List<Operator> operators, List<Supervisor> supervisors, List<Director> directors) {
        this.availableOperators = new ConcurrentLinkedQueue<>(operators);
        this.availableSupervisors = new ConcurrentLinkedQueue<>(supervisors);
        this.availableDirectors = new ConcurrentLinkedQueue<>(directors);
    }

    public void dispatchCall(Call call) {
        logger.debug("Received call {}", call.getId());
        Optional<Employee> callee = findAvailableCallee();
        if (callee.isPresent()) {
            dispatchCall(call, callee.get());
        } else {
            logger.debug("No employee available to answer call {}", call.getId());
            call.setOnHold(true);
            pendingCalls.add(call);
        }
    }

    private void dispatchCall(Call call, Employee callee) {
        logger.debug("Call {} assigned to {}", call.getId(), callee.getName());
        CompletableFuture<?> future = CompletableFuture.runAsync(new CallExecutor(call, callee), executorService);
        future.thenRun(() -> checkPendingCalls(callee)).thenRun(() -> finishCall(call));
    }

    private void finishCall(Call call) {
        logger.debug("Call {} has finished", call.getId());
        finishedCalls.add(call);
    }

    private void checkPendingCalls(Employee callee) {
        // push again the callee to the queue to not overload him
        // and look if there is someone which has been idle previously
        pushCallee(callee);
        logger.debug("Employee {} is free", callee.getName());

        if (!pendingCalls.isEmpty()) {
            Call pendingCall = pendingCalls.poll();
            logger.debug("Dispatching pending call {}", pendingCall.getId());
            dispatchCall(pendingCall);
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

    public List<Call> getFinishedCalls() {
        return finishedCalls;
    }
}
