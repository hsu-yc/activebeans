package org.activebeans;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

public class ActiveMethodHandler implements MethodHandler {

	private MapDelegate delegate;

	public ActiveMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		Map<MethodFilter, MethodHandler> map = new LinkedHashMap<MethodFilter, MethodHandler>();
		map.put(new ClassMethodFilter(intro.attributesInterface()), 
			new AttributeMethodHandler(activeClass));
		delegate = new MapDelegate(map);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		Object rtn = null;
		if (proceed != null) {
			rtn = proceed.invoke(self, args);
		} else if(delegate.isHandled(method)){
			rtn = delegate.invoke(self, method, proceed, args);
		} else {
			rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
		}
		return rtn;
	}

}
