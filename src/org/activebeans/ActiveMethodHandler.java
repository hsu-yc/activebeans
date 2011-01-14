package org.activebeans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

public class ActiveMethodHandler<T extends Model> implements MethodHandler {

	private ActiveIntrospector<T> intro;

	private Map<Method, Property> propGetterMap = new HashMap<Method, Property>();

	private Map<Method, Property> propSetterMap = new HashMap<Method, Property>();

	private Map<Property, Object> propMap = new HashMap<Property, Object>();

	private Map<Method, Association> belongsToGetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> belongsToSetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> belongsToMap = new HashMap<Association, Object>();

	private ActiveMethodHandler(Class<T> activeClass) {
		intro = ActiveIntrospector.of(activeClass);
		for (PropertyAccessors accessor : intro.accessors()) {
			Property prop = accessor.property();
			propGetterMap.put(accessor.get(), prop);
			propSetterMap.put(accessor.set(), prop);
		}
		for (BelongsToAssociationMethods methods : intro.belongsToMethods()) {
			Association assoc = methods.association();
			belongsToGetterMap.put(methods.retrieve(), assoc);
			belongsToSetterMap.put(methods.assign(), assoc);
		}
	}

	public static <U extends Model> ActiveMethodHandler<U> of(
			Class<U> activeClass) {
		return new ActiveMethodHandler<U>(activeClass);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		Object rtn = null;
		if (proceed != null) {
			rtn = proceed.invoke(self, args);
		} else if (propGetterMap.containsKey(method)) {
			rtn = propMap.get(propGetterMap.get(method));
		} else if (propSetterMap.containsKey(method)) {
			propMap.put(propSetterMap.get(method), args[0]);
			rtn = Void.TYPE;
		} else if (belongsToGetterMap.containsKey(method)) {
			rtn = belongsToMap.get(belongsToGetterMap.get(method));
		} else if (belongsToSetterMap.containsKey(method)) {
			belongsToMap.put(belongsToSetterMap.get(method), args[0]);
			rtn = Void.TYPE;
		} else {
			rtn = defaultValue(method.getReturnType());
		}
		return rtn;
	}

	private static Object defaultValue(Class<?> type) {
		Object rtn = null;
		if (Boolean.TYPE.equals(type)) {
			rtn = false;
		} else if (Character.TYPE.equals(type)) {
			rtn = '\u0000';
		} else if (Byte.TYPE.equals(type) || Short.TYPE.equals(type)
				|| Integer.TYPE.equals(type)) {
			rtn = 0;
		} else if (Long.TYPE.equals(type)) {
			rtn = 0L;
		} else if (Float.TYPE.equals(type)) {
			rtn = 0.0f;
		} else if (Double.TYPE.equals(type)) {
			rtn = 0.0d;
		}
		return rtn;
	}

}
