package org.activebeans;

public interface PropertyCondition<T extends Model, U> {

	PropertyCondition<T, U> eq(U val);

}
