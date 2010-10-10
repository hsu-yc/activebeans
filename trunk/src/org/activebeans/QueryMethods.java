package org.activebeans;

import java.util.Map;

public interface QueryMethods<T extends Model> {

	FinderMethods<T> where(String where, Object... params);

	FinderMethods<T> where(String where, Map<String, ?> params);

	FinderMethods<T> where(Map<String, ?> params);

}
