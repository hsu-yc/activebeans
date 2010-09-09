package org.activebeans;

import java.util.Map;

public interface SingularAssociation<T extends Base<?>> {

	T get();

	T get(boolean forceReload);

	SingularAssociation<T> set(T associate);

	T build(Class<T> mapper, Map<String, ?> attrs);

	T create(Class<T> mapper, Map<String, ?> attrs);

}
