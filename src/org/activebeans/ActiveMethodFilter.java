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
		for (PropertyMethods propMethods : intro.propertyMethods()) {
			methods.add(propMethods.get());
			methods.add(propMethods.set());
		}
		for (SingularAssociationMethods assocMethods : intro
				.belongsToMethods()) {
			methods.add(assocMethods.get());
			methods.add(assocMethods.set());
		}
		for (CollectionAssociationMethods assocMethods : intro.hasManyMethods()) {
			methods.add(assocMethods.get());
		}
		methods.addAll(Arrays.asList(Model.class.getMethods()));
	}

	@Override
	public boolean isHandled(Method m) {
		return methods.contains(m);
	}

}
