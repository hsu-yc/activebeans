package org.activebeans;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

@SuppressWarnings("rawtypes")
public class ModelMethodHandler implements Model, MethodHandler {
	
	private Model self;
	private Class<Model> selfClass; 
	private AttributeMethodHandler attrHandler;
	private Delegate delegate;
	
	
	public ModelMethodHandler(AttributeMethodHandler attrHandler){
		this.attrHandler = attrHandler;
		@SuppressWarnings("unchecked")
		Class<Model> activeClass = (Class<Model>) attrHandler.activeClass();
		selfClass = activeClass;
		delegate = new Delegate(this);
	}
	
	@Override
	public boolean save() {
		return ActiveBeansUtils.insert(
			ActiveBeans.repository(), 
			selfClass,
			self, 
			new GeneratedKeysMapHandler() {
				@Override
				public void handle(Map<Property, Object> keys) {
					for (Entry<Property, Object> key : keys.entrySet()) {
						attrHandler.set(key.getKey(), key.getValue());
					}
				}
			}
		) == 1;
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
		this.self = (Model) self;
		return delegate.invoke(self, method, proceed, args);
	}

}
