package org.activebeans;

import java.util.Map;

public interface Models<T extends Model> extends Model, Iterable<T> {

	T build();

	T build(Map<String, ?> attrs);

	T create();

	T create(Map<String, ?> attrs);

	T get(Object key, Object... keys);

	T first();

	T first(Map<String, ?> conditions);

	T last();

	T last(Map<String, ?> conditions);

	Models<T> add(T model);

	Models<T> all();

	Models<T> all(Map<String, ?> conditions);

}
