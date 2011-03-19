package org.activebeans;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

public class MapDelegate implements MethodFilter, MethodHandler {

	private Map<MethodFilter, MethodHandler> map;
	
	public MapDelegate(Map<MethodFilter, MethodHandler> map) {
		this.map = map;
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		for (Entry<MethodFilter, MethodHandler> e : map.entrySet()) {
			if(e.getKey().isHandled(method)){
				rtn = e.getValue().invoke(self, method, proceed, args);
				break;
			}
		}
		return rtn;
	}

	@Override
	public boolean isHandled(Method m) {
		boolean handled = false;
		for (MethodFilter filter : map.keySet()) {
			if(handled = filter.isHandled(m)){
				break;
			}
		}
		return handled;
	}

}
