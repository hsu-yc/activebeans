package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;

public class ActiveMethodFilter implements MethodFilter {

	private ClassMethodFilter filter;

	public ActiveMethodFilter(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		filter = new ClassMethodFilter(intro.attributesInterface(), Model.class);
	}

	@Override
	public boolean isHandled(Method m) {
		return filter.isHandled(m);
	}

}
