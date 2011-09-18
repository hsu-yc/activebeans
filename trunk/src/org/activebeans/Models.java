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

	T first();

	T first(V conds);

	T last();

	T last(V conds);

	Models<T, U, V, W> and(V conds);
	
	Models<T, U, V, W> reverse();

}
