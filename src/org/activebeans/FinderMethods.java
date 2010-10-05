package org.activebeans;

import java.util.List;

public interface FinderMethods<T extends Base> {

	T first();

	T last();

	List<T> all();

}
