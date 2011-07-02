package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

public class Delegate implements MethodHandler {
	
	private Object self;
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		this.self = self;
		Object rtn;
		try{
			rtn = this.getClass().getMethod(method.getName(), method.getParameterTypes())
				.invoke(this, args);
		}catch(NoSuchMethodException e){
			rtn = methodMissing(self, method, proceed, args);
		}
		return rtn;
	}
	
	public Object self(){
		return self;
	}
	
	protected Object methodMissing(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		return ActiveBeansUtils.defaultValue(method.getReturnType());
	}

}