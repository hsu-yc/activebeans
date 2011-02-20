package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;

public class ActiveMethodFilter<T extends Model<T, ?, ?>> implements MethodFilter {

	private Set<Method> methods = new HashSet<Method>();

	private ActiveMethodFilter(Class<T> activeClass) {
		ActiveIntrospector<T> intro = ActiveIntrospector.of(activeClass);
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

	public static <U extends Model<U, ?, ?>> ActiveMethodFilter<U> of(
			Class<U> activeClass) {
		return new ActiveMethodFilter<U>(activeClass);
	}

	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
