package org.activebeans;

import java.util.Map;

public interface ActiveBeans {

	<T extends Base<?>> T create(Class<T> mapper, Map<String, ?> params);

	<T extends Base<U>, U> T create(Class<T> mapper, Initialization<U> init);

	<T extends Base<?>> T create(Class<T> mapper);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper, String where,
			Object... params);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper, String where,
			Map<String, Object> params);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper,
			Map<String, ?> params);

	<T extends Base<?>> QueryMethods<T> joins(Class<T> mapper, String attr);

}