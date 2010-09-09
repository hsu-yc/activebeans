package org.activebeans;

public interface HasAndBelongsToMany<T extends Base<?>> extends
		CollectionAssociation<T> {

	HasAndBelongsToMany<T> add(T... obj);

	HasAndBelongsToMany<T> delete(T... obj);

	HasAndBelongsToMany<T> set(T... obj);

	HasAndBelongsToMany<T> setIds(Object... ids);

	HasAndBelongsToMany<T> clear();

}
