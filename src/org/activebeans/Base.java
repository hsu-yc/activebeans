package org.activebeans;

public interface Base {

	boolean isPresent(String attr);

	boolean isNew();

	boolean save();

	boolean update();

}