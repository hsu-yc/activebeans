package org.activebeans;

public interface Conditions<T extends Model<T, ?>> {

	<U> Conditions<T> prop(PropertyCondition<T, U> prop);

	<U extends Model<U, ?>> Conditions<T> assoc(AssociationConditions<T, U> assoc);

}
