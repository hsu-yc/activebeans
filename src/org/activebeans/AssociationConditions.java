package org.activebeans;

public interface AssociationConditions<T extends Model, U extends Model> {

	<V> AssociationConditions<T, U> prop(PropertyCondition<U, V> prop);

	<V extends Model> AssociationConditions<T, U> assoc(
			AssociationConditions<U, V> assoc);

}
