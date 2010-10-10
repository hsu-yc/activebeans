package org.activebeans;

public interface BelongsTo<T extends Model> extends SingularAssociation<T> {

	@Override
	BelongsTo<T> set(T associate);

}
