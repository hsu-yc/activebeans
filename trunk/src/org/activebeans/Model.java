package org.activebeans;


public interface Model<T extends Model<T, U, V, W>, U, V, W extends Models<T, U, V, W>> {

	boolean save();

	boolean update();

	boolean update(U attrs);

	T attrs(U attrs);

	boolean destroy();

}