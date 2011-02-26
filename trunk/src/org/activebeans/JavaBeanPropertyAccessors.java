package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanPropertyAccessors implements PropertyAccessors {

	private Property property;

	private PropertyDescriptor propDesc;

	public JavaBeanPropertyAccessors(Class<?> attrsInterf, Property prop) {
		property = prop;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(attrsInterf)
					.getPropertyDescriptors()) {
				if (pd.getName().equals(property.name())) {
					propDesc = pd;
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (propDesc == null) {
			throw new ActiveBeansException(
					"java bean property accessors not found");
		}
	}

	@Override
	public Property property() {
		return property;
	}

	@Override
	public Method get() {
		return propDesc.getReadMethod();
	}

	@Override
	public Method set() {
		return propDesc.getWriteMethod();
	}

}