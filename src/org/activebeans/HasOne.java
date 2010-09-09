package org.activebeans;

public interface HasOne<T extends Base<?>> extends SingularAssociation<T> {

	HasOne<T> set(T associate);
	
}
