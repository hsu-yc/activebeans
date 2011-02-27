package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanHasManyAssociationMethods implements CollectionAssociationMethods {

	private Association hasMany;

	private Method get;

	private Method option;
	
	private Method condition;
	
	public JavaBeanHasManyAssociationMethods(Class<?> attrsInterf,
			Class<?> optionsInterf, Class<?> conditionsInterf, Association assoc) {
		hasMany = assoc;
		String name = Introspector.decapitalize(hasMany.with().getSimpleName()) + "s";
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(attrsInterf)
					.getPropertyDescriptors()) {
				if (pd.getName().equals(name)) {
					get = pd.getReadMethod();
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (get == null) {
			throw new ActiveBeansException(
					"java bean has-many association methods not found");
		}
		try {
			option = optionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("has-many association option method not found");
		}
		try {
			condition = conditionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("has-many association condition method not found");
		}
	}

	@Override
	public Association association() {
		return hasMany;
	}

	@Override
	public Method get() {
		return get;
	}

	@Override
	public Method option() {
		return option;
	}

	@Override
	public Method condition() {
		return condition;
	}

}
