package org.activebeans;


public interface Model<T extends Model<T, U, V, S>, U, V, S extends Models<T, U, V, S>> {

	boolean save();

	boolean update();

	boolean update(U attrs);

	T attrs(U attrs);

	boolean destroy();

}