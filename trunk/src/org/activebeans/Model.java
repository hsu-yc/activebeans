package org.activebeans;

import java.util.Map;

public interface Model<T extends Model<T>> {

	boolean save();

	boolean update();

	boolean update(Map<String, ?> attrs);

	T attrs(Conditions<T> attrs);

	boolean destroy();

}