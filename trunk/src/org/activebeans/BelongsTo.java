package org.activebeans;

public interface BelongsTo<T extends Base<?>> extends SingularAssociation<T> {

	BelongsTo<T> set(T associate);

}
