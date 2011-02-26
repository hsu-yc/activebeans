package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class ActiveMethodHandler<T extends Model<T, U, V, W>, U, V, W extends Models<T, U, V, W>> implements MethodHandler {

	private ActiveIntrospector<T, U, V, W> intro;

	private Map<Method, Property> propGetterMap = new HashMap<Method, Property>();

	private Map<Method, Property> propSetterMap = new HashMap<Method, Property>();

	private Map<Property, Object> propMap = new HashMap<Property, Object>();

	private Map<Method, Association> belongsToGetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> belongsToSetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> belongsToMap = new HashMap<Association, Object>();

	private Map<Method, Association> hasManyGetterMap = new HashMap<Method, Association>();

	private Map<Association, Models<? extends Model<?, ?, ?, ?>, ?, ?, ?>> hasManyMap = new HashMap<Association, Models<? extends Model<?, ?, ?, ?>, ?, ?, ?>>();

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
		for (HasManyAssociationMethods methods : intro.hasManyMethods()) {
			hasManyGetterMap.put(methods.retrieve(), methods.association());
		}
	}

	public static <X extends Model<X, Y, Z, A>, Y, Z, A extends Models<X, Y, Z, A>> ActiveMethodHandler<X, Y, Z, A> of(
			Class<X> activeClass) {
		return new ActiveMethodHandler<X, Y, Z, A>(activeClass);
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
			Models<?, ?, ?, ?> models = hasManyMap.get(hasMany);
			if (models == null) {
				@SuppressWarnings("rawtypes")
				Class with = hasMany.with();
				@SuppressWarnings("unchecked")
				ActiveIntrospector<? extends Model<?, ?, ?, ?>, ?, ?, 
					? extends Models<?,?,?,?>> intro = ActiveIntrospector.of(with);
				ProxyFactory f = new ProxyFactory();
				Class<? extends Models<?, ?, ?, ?>> collectionInterface = intro.activeCollectionInterface();
				f.setInterfaces(new Class[] { collectionInterface });
				f.setFilter(new MethodFilter() {
					@Override
					public boolean isHandled(Method m) {
						return !isCovariantReturn(m);
					}
				});
				models = collectionInterface.cast(f.create(new Class[0],
						new Object[0], new MethodHandler() {
							@Override
							public Object invoke(Object self, Method method,
									Method proceed, Object[] args)
									throws Throwable {
								return defaultValue(method.getReturnType());
							}
						}));
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
