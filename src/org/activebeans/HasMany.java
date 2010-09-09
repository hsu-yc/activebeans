package org.activebeans;

public interface HasMany<T extends Base<?>> extends CollectionAssociation<T> {

	HasMany<T> add(T... obj);

	HasMany<T> delete(T... obj);

	HasMany<T> set(T... obj);

	HasMany<T> setIds(Object... ids);

	HasMany<T> clear();

}
