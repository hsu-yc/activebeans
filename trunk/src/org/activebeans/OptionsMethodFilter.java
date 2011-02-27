package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;

public class OptionsMethodFilter implements MethodFilter {

	private ClassMethodFilter filter;
	
	public OptionsMethodFilter(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		filter = new ClassMethodFilter(new ActiveIntrospector(activeClass).optionsInterface());
	}
	
	@Override
	public boolean isHandled(Method m) {
		return filter.isHandled(m);
	}

}
