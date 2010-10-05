package org.activebeans;

public interface HasOne<T extends Base> extends SingularAssociation<T> {

	@Override
	HasOne<T> set(T associate);

}
