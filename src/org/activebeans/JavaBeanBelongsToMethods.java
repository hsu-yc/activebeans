package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanBelongsToMethods implements BelongsToMethods {

	private Association belongsTo;

	private PropertyDescriptor propDesc;

	public JavaBeanBelongsToMethods(Class<?> activeInterf, Association assoc) {
		belongsTo = assoc;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(activeInterf)
					.getPropertyDescriptors()) {
				if (pd.getPropertyType().equals(belongsTo.with())) {
					propDesc = pd;
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (propDesc == null) {
			throw new ActiveBeansException(
					"java bean property methods not found");
		}
	}

	@Override
	public Method assign() {
		return propDesc.getWriteMethod();
	}

	@Override
	public Method retrieve() {
		return propDesc.getReadMethod();
	}

}
