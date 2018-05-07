package com.dorfi.callcenter.core;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dorfi.callcenter.model.Employee;
import com.dorfi.callcenter.model.Operator;

public class DispatcherTest {

	private Dispatcher dispatcher;

	@Before
	public void setUp() throws Exception {
		dispatcher = new Dispatcher(build(Operator.class, 10), emptyList(), emptyList());
	}

	private static <T extends Employee> List<T> build(Class<T> clazz, int count) {
		List<T> employees = new ArrayList<>();
		try {
			for (int i = 1; i <= count; i++) {
				T employee = clazz.getConstructor().newInstance();
				employee.setId(i);
				employee.setName(clazz.getName() + i);
				employees.add(employee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employees;
	}

	@After
	public void tearDown() throws Exception {
		dispatcher.shutdown();
	}

	@Test
	public void testDispatchCall() {
		
	}

}
