package org.activebeans;

import java.util.HashMap;
import java.util.Map;

public final class ActiveBeansUtils {

	private ActiveBeansUtils() {

	}

	public static Map<String, Class<?>> classNameMap(Class<?>[] classes) {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		for (Class<?> i : classes) {
			map.put(i.getName(), i);
		}
		return map;
	}

}
