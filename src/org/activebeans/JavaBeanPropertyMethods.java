package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanPropertyMethods implements PropertyMethods {

	private Property property;
	
	private Method get;
	
	private Method set;
	
	private Method option;
	
	private Method condition;

	public JavaBeanPropertyMethods(Class<?> attrsInterf, Class<?> optionsInterf, 
			Class<?> conditionsInterf, Property prop) {
		property = prop;
		String name = property.name();
		PropertyDescriptor propDesc = null;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(attrsInterf)
					.getPropertyDescriptors()) {
				if (pd.getName().equals(name)) {
					propDesc = pd;
					break;
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (propDesc == null) {
			throw new ActiveBeansException("java bean property methods not found");
		}
		get = propDesc.getReadMethod();
		set = propDesc.getWriteMethod();
		try {
			option = optionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("property option method not found");
		}
		try {
			condition = conditionsInterf.getMethod(name);
		}catch (NoSuchMethodException e) {
			throw new ActiveBeansException("property condition method not found");
		}
	}

	@Override
	public Property property() {
		return property;
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