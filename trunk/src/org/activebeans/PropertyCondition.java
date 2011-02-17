package org.activebeans;

public interface PropertyCondition<T extends Model<T, ?>, U> {

	PropertyCondition<T, U> eq(U val);

}
