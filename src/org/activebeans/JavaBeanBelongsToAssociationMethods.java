package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanBelongsToAssociationMethods implements SingularAssociationMethods {

	private Association belongsTo;
	
	private Method get;
	
	private Method set;
	
	private Method option;
	
	private Method condition;

	public JavaBeanBelongsToAssociationMethods(Class<?> attrsInterf,
			Class<?> optionsInterf, Class<?> conditionsInterf, Association assoc) {
		belongsTo = assoc;
		String name = Introspector.decapitalize(belongsTo.with().getSimpleName());
		PropertyDescriptor propDesc = null;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(attrsInterf)
					.getPropertyDescriptors()) {
				if (pd.getName().equals(name)) {
					propDesc = pd;
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (propDesc == null) {
			throw new ActiveBeansException("java bean belongs-to association methods not found");
		}
		get = propDesc.getReadMethod();
		set = propDesc.getWriteMethod();
		try {
			option = optionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("belongs-to association option method not found");
		}
		try {
			condition = conditionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("belongs-to association condition method not found");
		}
	}

	@Override
	public Association association() {
		return belongsTo;
	}

	@Override
	public Method get() {
		return get;
	}
	
	@Override
	public Method set() {
		return set;
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
