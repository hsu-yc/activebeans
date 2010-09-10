package org.activebeans;

public interface BelongsTo<T extends Base<?>> extends SingularAssociation<T> {

	@Override
	BelongsTo<T> set(T associate);

}
