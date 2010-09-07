package org.activebeans;

public interface Base<T> {

	Base<T> write(String attr, Object val);

	Object read(String attr);

	boolean present(String attr);

	boolean newRecord();

	Object beforeTypeCast(String attr);

	T bean();

}