package org.activebeans;


public interface Models<T extends Model<T, U>, U> extends Iterable<T> {

	boolean save();

	boolean update();

	boolean update(U opts);

	Models<T, U> attrs(U opts);

	boolean destroy();

	T build();

	T build(U opts);

	T create();

	T create(U opts);

	T get(Object key, Object... keys);

	T first();

	T first(Conditions<T> cond);

	T last();

	T last(Conditions<T> cond);

	Models<T, U> add(T model);

	Models<T, U> all();

	Models<T, U> all(Conditions<T> cond);

}
