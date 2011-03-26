package org.activebeans;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

public class ActiveDelegate implements MethodFilter, MethodHandler {
	
	private ClassMethodFilter filter;

	private AttributeMethodHandler attrHandler;

	private ClassMethodFilter attrFilter;

	private ClassMethodFilter modelFilter;

	private ModelMethodHandler modelHandler;
	
	public ActiveDelegate(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		Class<?> attrInterf = intro.attributesInterface();
		@SuppressWarnings("rawtypes")
		Class<Model> modelClass = Model.class;
		filter = new ClassMethodFilter(attrInterf, modelClass);
		attrFilter = new ClassMethodFilter(attrInterf);
		attrHandler = new AttributeMethodHandler(activeClass);
		modelFilter = new ClassMethodFilter(modelClass);
		modelHandler = new ModelMethodHandler(attrHandler);
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		Object rtn = null;
		if (proceed != null) {
			rtn = proceed.invoke(self, args);
		} else if(attrFilter.isHandled(method)){
			rtn = attrHandler.invoke(self, method, proceed, args);
		} else if(modelFilter.isHandled(method)){
			rtn = modelHandler.invoke(self, method, proceed, args);
		}else {
			rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
		}
		return rtn;
	}
	
	@Override
	public boolean isHandled(Method m) {
		return filter.isHandled(m);
	}
	
	public AttributeMethodHandler attrHandler(){
		return attrHandler;
	}

}
