package org.activebeans;

import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class ActiveBeans {

	private static String activeInterfaceName(Class<?> modelClass) {
		return modelClass.getPackage().getName() + ".Active"
				+ modelClass.getSimpleName();
	}

	@SuppressWarnings("unused")
	private static Class<?> activeInterface(Class<?> modelClass) {
		Class<?> interf = null;
		Class<?>[] interfaces = modelClass.getInterfaces();
		String activeInterfaceName = activeInterfaceName(modelClass);
		for (Class<?> i : interfaces) {
			if (i.getCanonicalName().equals(activeInterfaceName)) {
				interf = i;
				break;
			}
		}
		return interf;
	}

	public static <T extends Model> T build(Class<T> modelClass) {
		ProxyFactory f = new ProxyFactory();
		f.setSuperclass(modelClass);
		f.setFilter(new MethodFilter() {
			@Override
			public boolean isHandled(Method m) {
				return true;
			}
		});
		MethodHandler mi = new MethodHandler() {
			public Object invoke(Object self, Method m, Method proceed,
					Object[] args) throws Throwable {
				Object rtn = null;
				if (proceed != null) {
					rtn = proceed.invoke(self, args);
				} else {
					rtn = returnDefault(m.getReturnType());
				}
				return rtn;
			}
		};
		try {
			return modelClass.cast(f.create(new Class[0], new Object[0], mi));
		} catch (Throwable t) {
			throw new ActiveBeansException(t);
		}
	}

	private static Object returnDefault(Class<?> type) {
		Object rtn = null;
		if (type == Boolean.TYPE) {
			rtn = false;
		} else if (type == Character.TYPE) {
			rtn = '\u0000';
		} else if (type == Byte.TYPE || type == Short.TYPE
				|| type == Integer.TYPE) {
			rtn = 0;
		} else if (type == Long.TYPE) {
			rtn = 0L;
		} else if (type == Float.TYPE) {
			rtn = 0.0f;
		} else if (type == Double.TYPE) {
			rtn = 0.0d;
		}
		return rtn;
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