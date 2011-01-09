package org.activebeans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public static <T> Set<T> arrayToSet(T[] a) {
		return new HashSet<T>(Arrays.asList(a));
	}

}
