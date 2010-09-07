package org.activebeans;

import java.util.List;

public interface Base<T> {

	Base<T> write(String attr, Object val);

	Object read(String attr);

	boolean present(String attr);

	boolean newBean();

	Object beforeTypeCast(String attr);

	T bean();

	Base<T> create(T obj);

	List<Base<T>> create(T... obj);

	List<Base<T>> create(Initialization<T> init, T... obj);

	boolean save();

	Base<T> update();

	boolean updateAttributes(T obj);

}