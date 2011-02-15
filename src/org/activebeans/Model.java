package org.activebeans;


public interface Model<T extends Model<T>> {

	boolean save();

	boolean update();

	boolean update(Conditions<T> attrs);

	T attrs(Conditions<T> attrs);

	boolean destroy();

}