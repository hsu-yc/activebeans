package org.activebeans;

import java.util.List;

public interface FinderMethods<T extends Model> {

	T first();

	T last();

	List<T> all();

}
