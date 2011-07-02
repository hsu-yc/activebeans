package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

public class Delegate implements MethodHandler {
	
	private Object self;
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		this.self = self;
		return this.getClass().getMethod(method.getName(), method.getParameterTypes())
			.invoke(this, args);
	}
	
	public Object self(){
		return self;
	}

}