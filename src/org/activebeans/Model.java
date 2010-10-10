package org.activebeans;

import java.util.Map;

public interface Model {

	boolean save();

	boolean update();

	boolean update(Map<String, ?> attrs);

	void attributes(Map<String, ?> attrs);

	boolean destroy();

}