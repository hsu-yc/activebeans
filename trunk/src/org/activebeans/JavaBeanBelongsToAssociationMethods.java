package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class JavaBeanBelongsToAssociationMethods implements
		BelongsToAssociationMethods {

	private Association belongsTo;

	private PropertyDescriptor propDesc;

	public JavaBeanBelongsToAssociationMethods(Class<?> attrsInterf,
			Association assoc) {
		belongsTo = assoc;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(attrsInterf)
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
					"java bean belongs-to association methods not found");
		}
	}

	@Override
	public Association association() {
		return belongsTo;
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
