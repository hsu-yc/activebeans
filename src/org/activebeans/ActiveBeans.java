package org.activebeans;

import java.util.List;
import java.util.Map;

public interface ActiveBeans {

	<T extends Base<?>> T first(Class<T> mapper);

	<T extends Base<?>> T last(Class<T> mapper);

	<T extends Base<?>> T all(Class<T> mapper);

	<T extends Base<?>> T find(Class<T> mapper, Object id);

	<T extends Base<?>> List<T> find(Class<T> mapper, Object... ids);

	<T extends Base<?>> List<T> find_each(Class<T> mapper, Do<T> block);

	<T extends Base<U>, U> T of(Class<T> mapper, U obj);

	<T extends Base<U>, U> T of(Class<T> mapper, Do<U> init);

	<T extends Base<?>> T of(Class<T> mapper);
	
	<T extends Base<U>, U> T create(Class<T> mapper, U obj);

	<T extends Base<U>, U> List<T> create(Class<T> mapper, U... objs);

	<T extends Base<U>, U> List<T> create(Class<T> mapper, Do<U> init, U obj);
	
	<T extends Base<U>, U> List<T> create(Class<T> mapper, Do<U> init, U... objs);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper, String where,
			Object... params);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper, String where,
			Map<String, Object> params);

	<T extends Base<?>> FinderMethods<T> where(Class<T> mapper,
			Map<String, ?> params);

	<T extends Base<?>> QueryMethods<T> joins(Class<T> mapper, String attr);

	<T extends Base<?>> boolean exists(Class<T> mapper, Object... ids);

	<T extends Base<?>> boolean exists(Class<T> mapper, String where,
			Object... params);

	<T extends Base<?>> boolean exists(Class<T> mapper, Map<String, ?> params);

}