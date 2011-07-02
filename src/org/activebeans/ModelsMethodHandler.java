package org.activebeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.util.proxy.MethodHandler;

public class ModelsMethodHandler implements MethodHandler {
	
	@SuppressWarnings("unused")
	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	
	@SuppressWarnings("unused")
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
		if(method.equals(Set.class.getMethod("iterator"))){
			onIteration(data);
			rtn = data.iterator();
		} else if(method.equals(Set.class.getMethod("add", Object.class))){
			data.add(args[0]);
			rtn = true;
		}else {
			rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
		}
		return rtn;
	}
	
	protected void onIteration(List<Object> data){
		
	}
	
}