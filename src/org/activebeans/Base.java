package org.activebeans;

import java.util.Map;

public interface Base {

	boolean isPresent(String attr);

	boolean isNew();

	boolean save();

	boolean update();

	boolean write(String attr, Object val);

	Object read(String attr);

	boolean updateAttributes(Map<String, ?> attrs);

}