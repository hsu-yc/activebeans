package org.activebeans;


public interface Model<T extends Model<T, U, V>, U, V> {

	boolean save();

	boolean update();

	boolean update(U attrs);

	T attrs(U attrs);

	boolean destroy();

}