package org.activebeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.util.proxy.MethodFilter;

public class ClassMethodFilter implements MethodFilter {
	
	private List<Method> methods = new ArrayList<Method>();
	
	public ClassMethodFilter(Class<?>... classes){
		for (Class<?> c : classes) {
			methods.addAll(Arrays.asList(c.getMethods()));
		}
	}
	
	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
