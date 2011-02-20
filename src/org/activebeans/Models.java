package org.activebeans;


public interface Models<T extends Model<T, U, V, S>, U, V, S extends Models<T, U, V, S>> extends Iterable<T> {

	boolean save();

	boolean update();

	boolean update(U opts);

	Models<T, U, V, S> attrs(U opts);

	boolean destroy();

	T build();

	T build(U opts);

	T create();

	T create(U opts);

	T get(Object key, Object... keys);

	T first();

	T first(V cond);

	T last();

	T last(V cond);

	Models<T, U, V, S> add(T model);

	Models<T, U, V, S> all();

	Models<T, U, V, S> all(V cond);

}
