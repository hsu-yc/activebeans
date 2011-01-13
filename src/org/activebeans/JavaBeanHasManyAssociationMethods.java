package org.activebeans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class JavaBeanHasManyAssociationMethods implements HasManyAssociationMethods {

	private Association hasMany;

	private Method retrieve;

	public JavaBeanHasManyAssociationMethods(Class<?> activeInterf, Association assoc) {
		hasMany = assoc;
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(activeInterf)
					.getPropertyDescriptors()) {
				Type[] types = pd.getPropertyType().getGenericInterfaces();
				if (types.length == 1 && types[0] instanceof ParameterizedType) {
					ParameterizedType paramType = (ParameterizedType) types[0];
					Type[] typeArgs = paramType.getActualTypeArguments();
					if (typeArgs.length == 1
							&& typeArgs[0].equals(hasMany.with())) {
						retrieve = pd.getReadMethod();
					}
				}
			}
		} catch (IntrospectionException e) {
			throw new ActiveBeansException(e);
		}
		if (retrieve == null) {
			throw new ActiveBeansException(
					"java bean has-many association methods not found");
		}
	}

	@Override
	public Method retrieve() {
		return retrieve;
	}

}
