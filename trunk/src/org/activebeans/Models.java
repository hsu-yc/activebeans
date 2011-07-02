package org.activebeans;

import java.util.Set;


public interface Models<T extends Model<T, U, V, W>, U, V, W extends Models<T, U, V, W>> 
		extends Set<T> {

	boolean save();

	boolean update();

	boolean update(U opts);

	Models<T, U, V, W> attrs(U opts);

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

	Models<T, U, V, W> all(V cond);

}
