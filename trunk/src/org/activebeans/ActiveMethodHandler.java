package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class ActiveMethodHandler implements MethodHandler {

	private Map<Method, Property> propGetterMap = new HashMap<Method, Property>();

	private Map<Method, Property> propSetterMap = new HashMap<Method, Property>();

	private Map<Property, Object> propMap = new HashMap<Property, Object>();

	private Map<Method, Association> belongsToGetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> belongsToSetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> belongsToMap = new HashMap<Association, Object>();

	private Map<Method, Association> hasManyGetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> hasManyMap = new HashMap<Association, Object>();

	public ActiveMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		for (PropertyMethods methods : intro.propertyMethods()) {
			Property prop = methods.property();
			propGetterMap.put(methods.get(), prop);
			propSetterMap.put(methods.set(), prop);
		}
		for (SingularAssociationMethods methods : intro.belongsToMethods()) {
			Association assoc = methods.association();
			belongsToGetterMap.put(methods.get(), assoc);
			belongsToSetterMap.put(methods.set(), assoc);
		}
		for (CollectionAssociationMethods methods : intro.hasManyMethods()) {
			hasManyGetterMap.put(methods.get(), methods.association());
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
								return ActiveBeansUtils.defaultValue(method.getReturnType());
							}
						});
				hasManyMap.put(hasMany, models);
				rtn = models;
			}
		} else {
			rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
		}
		return rtn;
	}

	private static boolean isCovariantReturn(Method m) {
		return m.getDeclaringClass().equals(Models.class)
				&& Arrays.asList(new String[] { "add", "all", "attrs" })
						.contains(m.getName());
	}

}
