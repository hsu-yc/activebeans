package org.activebeans;

import java.util.List;
import java.util.Map;

public interface CollectionAssociation<T extends Model> {

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

	T first();

	T last();

	T all();

	T find(Object id);

	List<T> find(Object... ids);

	List<T> find_each(Do<T> block);

	boolean exists(Object... ids);

	boolean exists(String where, Object... params);

	boolean exists(Map<String, ?> params);

	T build(Map<String, ?> attrs);

	T create(Map<String, ?> attrs);

}
