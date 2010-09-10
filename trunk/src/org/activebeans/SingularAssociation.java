package org.activebeans;

import java.util.Map;



public interface SingularAssociation<T extends Base<?>> {

	T get();

	T get(boolean forceReload);

	SingularAssociation<T> set(T associate);

	T build(Map<String, ?> attrs);

	T create(Map<String, ?> attrs);

}
