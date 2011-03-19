package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

public class Delegate<T> implements MethodFilter, MethodHandler {
	
	private T target;
	
	private List<Method> methods;
	
	private Delegate(T target){
		this.target = target;
		methods = Arrays.asList(target.getClass().getMethods());
	}
	
	public static <U> Delegate<U> of(U target){
		return new Delegate<U>(target);
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		return target.getClass().getMethod(method.getName(), method.getParameterTypes())
			.invoke(target, args);
	}

	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}