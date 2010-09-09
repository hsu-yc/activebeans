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

	<U extends Base<V>, V> List<U> find_each(Class<U> mapper, Do<V> block);

	boolean exists(Class<T> mapper, Object... ids);

	boolean exists(Class<T> mapper, String where, Object... params);

	boolean exists(Class<T> mapper, Map<String, ?> params);

	<U extends Base<V>, V> U build(Class<U> mapper, V obj);

	<U extends Base<V>, V> List<U> build(Class<U> mapper, V... objs);

	<U extends Base<V>, V> U create(Class<U> mapper, V obj);

	<U extends Base<V>, V> List<U> create(Class<U> mapper, V... objs);

}
