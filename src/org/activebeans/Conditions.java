package org.activebeans;

public interface Conditions<T extends Model> {

	<U> Conditions<T> prop(PropertyCondition<T, U> prop);

	<U extends Model> Conditions<T> assoc(AssociationConditions<T, U> assoc);

}
