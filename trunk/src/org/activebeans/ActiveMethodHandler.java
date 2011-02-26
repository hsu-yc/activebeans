package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class ActiveMethodHandler implements MethodHandler {

	private ActiveIntrospector intro;

	private Map<Method, Property> propGetterMap = new HashMap<Method, Property>();

	private Map<Method, Property> propSetterMap = new HashMap<Method, Property>();

	private Map<Property, Object> propMap = new HashMap<Property, Object>();

	private Map<Method, Association> belongsToGetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> belongsToSetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> belongsToMap = new HashMap<Association, Object>();

	private Map<Method, Association> hasManyGetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> hasManyMap = new HashMap<Association, Object>();

	public ActiveMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		intro = new ActiveIntrospector(activeClass);
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
		for (HasManyAssociationMethods methods : intro.hasManyMethods()) {
			hasManyGetterMap.put(methods.retrieve(), methods.association());
		}
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
		} else if (hasManyGetterMap.containsKey(method)) {
			Association hasMany = hasManyGetterMap.get(method);
			Object models = hasManyMap.get(hasMany);
			if (models == null) {
				ActiveIntrospector intro = new ActiveIntrospector(hasMany.with());
				ProxyFactory f = new ProxyFactory();
				Class<?> modelsInterface = intro.modelsInterface();
				f.setInterfaces(new Class[] { modelsInterface });
				f.setFilter(new MethodFilter() {
					@Override
					public boolean isHandled(Method m) {
						return !isCovariantReturn(m);
					}
				});
				models = f.create(new Class[0],
						new Object[0], new MethodHandler() {
							@Override
							public Object invoke(Object self, Method method,
									Method proceed, Object[] args)
									throws Throwable {
								return defaultValue(method.getReturnType());
							}
						});
				hasManyMap.put(hasMany, models);
				rtn = models;
			}
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

	private static boolean isCovariantReturn(Method m) {
		return m.getDeclaringClass().equals(Models.class)
				&& Arrays.asList(new String[] { "add", "all", "attrs" })
						.contains(m.getName());
	}

}
