package org.activebeans;


public interface Models<T extends Model<T>> extends Iterable<T> {

	boolean save();

	boolean update();

	boolean update(Conditions<T> cond);

	Models<T> attrs(Conditions<T> cond);

	boolean destroy();

	T build();

	T build(Conditions<T> cond);

	T create();

	T create(Conditions<T> cond);

	T get(Object key, Object... keys);

	T first();

	T first(Conditions<T> cond);

	T last();

	T last(Conditions<T> cond);

	Models<T> add(T model);

	Models<T> all();

	Models<T> all(Conditions<T> cond);

}
