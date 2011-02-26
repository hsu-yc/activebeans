package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;

public class ActiveMethodFilter<T extends Model<T, U, V, W>, U, V, W extends Models<T, U, V, W>> implements MethodFilter {

	private Set<Method> methods = new HashSet<Method>();

	private ActiveMethodFilter(Class<T> activeClass) {
		ActiveIntrospector<T, U, V, W> intro = ActiveIntrospector.of(activeClass);
		for (PropertyAccessors accessor : intro.accessors()) {
			methods.add(accessor.get());
			methods.add(accessor.set());
		}
		for (BelongsToAssociationMethods assocMethods : intro
				.belongsToMethods()) {
			methods.add(assocMethods.retrieve());
			methods.add(assocMethods.assign());
		}
		for (HasManyAssociationMethods assocMethods : intro.hasManyMethods()) {
			methods.add(assocMethods.retrieve());
		}
		methods.addAll(Arrays.asList(Model.class.getMethods()));
	}

	public static <X extends Model<X, Y, Z, A>, Y, Z, A extends Models<X, Y, Z, A>> ActiveMethodFilter<X, Y, Z, A> of(
			Class<X> activeClass) {
		return new ActiveMethodFilter<X, Y, Z, A>(activeClass);
	}

	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
