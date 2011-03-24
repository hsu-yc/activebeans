package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

public class Delegate implements MethodHandler {
	
	private Object target;
	
	public Delegate(Object target){
		this.target = target;
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		return target.getClass().getMethod(method.getName(), method.getParameterTypes())
			.invoke(target, args);
	}

}