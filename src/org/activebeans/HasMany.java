package org.activebeans;

public interface HasMany<T extends Base<?>> extends CollectionAssociation<T> {

	@Override
	HasMany<T> add(T... obj);

	@Override
	HasMany<T> delete(T... obj);

	@Override
	HasMany<T> set(T... obj);

	@Override
	HasMany<T> setIds(Object... ids);

	@Override
	HasMany<T> clear();

}
