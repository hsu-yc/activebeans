package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javassist.util.proxy.MethodFilter;

public class ClassMethodFilter implements MethodFilter {
	
	private List<Method> methods;

	public ClassMethodFilter(Class<?> clazz){
		methods = Arrays.asList(clazz.getMethods());
	}
	
	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
