package org.activebeans;

public interface HasOne<T extends Model> extends SingularAssociation<T> {

	@Override
	HasOne<T> set(T associate);

}
