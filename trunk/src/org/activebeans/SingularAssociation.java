package org.activebeans;

public interface SingularAssociation<T extends Base<?>> {

	T get();

	T get(boolean forceReload);

	SingularAssociation<T> set(T associate);

	<U extends Base<V>, V> T build(Class<T> mapper, V obj);

	<U extends Base<V>, V> T create(Class<T> mapper, V obj);

}
