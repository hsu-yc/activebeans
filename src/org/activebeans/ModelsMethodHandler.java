package org.activebeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodHandler;

public class ModelsMethodHandler implements MethodHandler {
	
	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	private Class<?> modelsInterface;
	
	private List<Object> data = new ArrayList<Object>();

	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		this.activeClass = activeClass;
		modelsInterface = new ActiveIntrospector(activeClass).modelsInterface();
	}

	@Override
	public Object invoke(Object self, Method method,
			Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null; 
		if(method.equals(Iterable.class.getMethod("iterator"))){
			onIteration(data);
			rtn = data.iterator();
		} else if(method.equals(modelsInterface.getMethod("add", activeClass)) ||
				method.equals(Models.class.getMethod("add", Model.class))){
			data.add(args[0]);
			rtn = self;
		}else {
			rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
		}
		return rtn;
	}
	
	protected void onIteration(List<Object> data){
		
	}
	
}