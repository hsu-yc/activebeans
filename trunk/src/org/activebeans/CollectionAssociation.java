package org.activebeans;

import java.util.List;
import java.util.Map;

public interface CollectionAssociation<T extends Base<?>> {

	List<T> get();

	List<T> get(boolean forceReload);

	CollectionAssociation<T> add(T... obj);

	CollectionAssociation<T> delete(T... obj);

	CollectionAssociation<T> set(T... obj);

	List<?> ids();

	CollectionAssociation<T> setIds(Object... ids);

	CollectionAssociation<T> clear();

	boolean empty();

	int size();

	T first(Class<T> mapper);

	T last(Class<T> mapper);

	T all(Class<T> mapper);

	T find(Class<T> mapper, Object id);

	List<T> find(Class<T> mapper, Object... ids);

	List<T> find_each(Class<T> mapper, Do<T> block);

	boolean exists(Class<T> mapper, Object... ids);

	boolean exists(Class<T> mapper, String where, Object... params);

	boolean exists(Class<T> mapper, Map<String, ?> params);

	T build(Class<T> mapper, Map<String, ?> attrs);

	List<T> build(Class<T> mapper, Map<String, ?>... attrs);

	T create(Class<T> mapper, Map<String, ?> attrs);

	List<T> create(Class<T> mapper, Map<String, ?>... attrs);

}
