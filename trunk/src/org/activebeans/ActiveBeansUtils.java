package org.activebeans;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ActiveBeansUtils {

	private ActiveBeansUtils() {

	}

	public static <T> Set<T> arrayToSet(T[] a) {
		return new HashSet<T>(Arrays.asList(a));
	}

}
