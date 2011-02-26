package org.activebeans;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;

public class ActiveMethodFilter implements MethodFilter {

	private Set<Method> methods = new HashSet<Method>();

	public ActiveMethodFilter(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
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

	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
