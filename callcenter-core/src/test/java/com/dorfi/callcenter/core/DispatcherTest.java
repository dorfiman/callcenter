package com.dorfi.callcenter.core;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.dorfi.callcenter.model.Call;
import com.dorfi.callcenter.model.Customer;
import com.dorfi.callcenter.model.Director;
import com.dorfi.callcenter.model.Employee;
import com.dorfi.callcenter.model.Operator;
import com.dorfi.callcenter.model.Supervisor;

public class DispatcherTest {

    private Dispatcher dispatcher;

    private static Dispatcher buildDispatcher(int operators, int supervisors, int directors) throws Exception {
        return new Dispatcher(build(Operator.class, operators), build(Supervisor.class, supervisors), 
                build(Director.class, directors));
    }

    private static <T extends Employee> List<T> build(Class<T> clazz, int count) throws Exception {
        List<T> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            T employee = clazz.getConstructor().newInstance();
            employee.setId(i);
            employee.setName(clazz.getSimpleName() + "-" + i);
            employees.add(employee);
        }
        return employees;
    }

    private static List<Call> buildCalls(int count) {
        List<Call> calls = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Call call = new Call();
            call.setId(i);
            call.setCaller(new Customer(i, "Customer " + i));
            calls.add(call);
        }
        return calls;
    }

    @After
    public void tearDown() throws Exception {
        dispatcher.shutdown();
    }

    @Test
    public void testDispatchCall_onlyOperators() throws Exception {
        dispatcher = buildDispatcher(10, 5, 2);
        List<Call> calls = buildCalls(10);
        calls.parallelStream().forEach(c -> dispatcher.dispatchCall(c));
        awaitTermination();

        assertThat(dispatcher.getFinishedCalls(), hasSize(10));
        assertThat(calls, everyItem(hasProperty("duration", is(both(greaterThanOrEqualTo(5000l)).and(lessThanOrEqualTo(10000l))))));
        assertFalse(calls.stream().filter(c -> !c.isSuccessful() || c.isOnHold()).findFirst().isPresent());
        assertThat(calls, everyItem(hasProperty("callee", instanceOf(Operator.class))));
    }

    @Test
    public void testDispatchCall_allEmployees() throws Exception {
        dispatcher = buildDispatcher(5, 3, 2);
        List<Call> calls = buildCalls(10);
        calls.parallelStream().forEach(c -> dispatcher.dispatchCall(c));
        awaitTermination();

        assertThat(dispatcher.getFinishedCalls(), hasSize(10));
        assertFalse(calls.stream().filter(c -> !c.isSuccessful() || c.isOnHold()).findFirst().isPresent());
        assertEquals(5, calls.stream().filter(c -> c.getCallee() instanceof Operator).count());
        assertEquals(3, calls.stream().filter(c -> c.getCallee() instanceof Supervisor).count());
        assertEquals(2, calls.stream().filter(c -> c.getCallee() instanceof Director).count());
    }

    @Test
    public void testDispatchCall_allEmployeesWithHoldOn() throws Exception {
        dispatcher = buildDispatcher(2, 1, 1);
        List<Call> calls = buildCalls(10);
        calls.parallelStream().forEach(c -> dispatcher.dispatchCall(c));
        awaitTermination();

        assertThat(dispatcher.getFinishedCalls(), hasSize(10));
        assertEquals(6, calls.stream().filter(c -> c.isOnHold()).count());
    }

    private void awaitTermination() throws InterruptedException {
        dispatcher.executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

}
