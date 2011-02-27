package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;

public class ConditionsMethodFilter implements MethodFilter {

	private ClassMethodFilter filter;
	
	public ConditionsMethodFilter(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		filter = new ClassMethodFilter(new ActiveIntrospector(activeClass).conditionsInterface());
	}
	
	@Override
	public boolean isHandled(Method m) {
		return filter.isHandled(m);
	}

}
