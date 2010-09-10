package org.activebeans;

import java.util.Map;


public interface Base<T> {

	Base<T> write(String attr, Object val);

	Object read(String attr);

	boolean present(String attr);

	boolean newBean();

	Object beforeTypeCast(String attr);

	T get();

	boolean save();

	Base<T> update();

	boolean updateAttributes(Map<String, ?> attrs);

}