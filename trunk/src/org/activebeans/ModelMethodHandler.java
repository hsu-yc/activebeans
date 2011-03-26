package org.activebeans;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

@SuppressWarnings("rawtypes")
public class ModelMethodHandler implements Model, MethodHandler {
	
	private Object self;
	private AttributeMethodHandler attrHandler;
	private Delegate delegate;
	
	public ModelMethodHandler(AttributeMethodHandler attrHandler){
		this.attrHandler = attrHandler;
		delegate = new Delegate(this);
	}
	
	@Override
	public boolean save() {
		return false;
	}

	@Override
	public boolean update() {
		return false;
	}

	@Override
	public boolean update(Object attrs) {
		return false;
	}

	@Override
	public Model attrs(Object attrs) {
		OptionsMethodHandler options = (OptionsMethodHandler) ((ProxyObject)(attrs)).getHandler();
		for (Entry<Property, Object> e : options.properties().entrySet()) {
			attrHandler.set(e.getKey(), e.getValue());
		}
		for (Entry<Association, Object> e : options.associations().entrySet()) {
			attrHandler.set(e.getKey(), e.getValue());
		}
		return (Model) self;
	}

	@Override
	public boolean destroy() {
		return false;
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		this.self = self;
		return delegate.invoke(self, method, proceed, args);
	}

}
