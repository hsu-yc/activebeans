package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

public class NoopMethodHandler implements MethodHandler {

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
			throws Throwable {
		return ActiveBeansUtils.defaultValue(method.getReturnType());
	}

}
