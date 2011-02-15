package org.activebeans;

public interface AssociationConditions<T extends Model<T>, U extends Model<U>> {

	<V> AssociationConditions<T, U> prop(PropertyCondition<U, V> prop);

	<V extends Model<V>> AssociationConditions<T, U> assoc(
			AssociationConditions<U, V> assoc);

}
