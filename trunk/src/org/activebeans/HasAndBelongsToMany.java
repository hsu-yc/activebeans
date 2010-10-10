package org.activebeans;

public interface HasAndBelongsToMany<T extends Model> extends
		CollectionAssociation<T> {

	@Override
	HasAndBelongsToMany<T> add(T... obj);

	@Override
	HasAndBelongsToMany<T> delete(T... obj);

	@Override
	HasAndBelongsToMany<T> set(T... obj);

	@Override
	HasAndBelongsToMany<T> setIds(Object... ids);

	@Override
	HasAndBelongsToMany<T> clear();

}
