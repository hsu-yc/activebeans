package org.activebeans;

import java.util.Map;

import javassist.util.proxy.ProxyFactory;

public final class ActiveBeans {

	private ActiveBeans() {

	}

	public static <T extends Model> T build(Class<T> activeClass) {
		ProxyFactory f = new ProxyFactory();
		f.setSuperclass(activeClass);
		f.setFilter(ActiveMethodFilter.of(activeClass));
		try {
			return activeClass.cast(f.create(new Class[0], new Object[0],
					ActiveMethodHandler.of(activeClass)));
		} catch (Exception e) {
			throw new ActiveBeansException(e);
		}
	}

	public static <T extends Model> T build(Class<T> modelClass,
			Map<String, ?> attrs) {
		return null;
	}

	public static <T extends Model> T create(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model> T create(Class<T> modelClass,
			Map<String, ?> attrs) {
		return null;
	}

	public static boolean destroy(Class<? extends Model> modelClass) {
		return false;
	}

	public static boolean update(Class<? extends Model> modelClass,
			Map<String, ?> attrs) {
		return false;
	}

	public static <T extends Model> T get(Class<T> modelClass, Object key,
			Object... keys) {
		return null;
	}

	public static <T extends Model> T first(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model> T first(Class<T> modelClass,
			Map<String, ?> conditions) {
		return null;
	}

	public static <T extends Model> T last(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model> T last(Class<T> modelClass,
			Map<String, ?> conditions) {
		return null;
	}

	public static <T extends Models<U>, U extends Model> T all(
			Class<T> modelsClass) {
		return null;
	}

	public static <T extends Models<U>, U extends Model> T all(
			Class<T> modelsClass, Map<String, ?> conditions) {
		return null;
	}

}