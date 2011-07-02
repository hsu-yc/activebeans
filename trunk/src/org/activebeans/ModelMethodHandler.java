package org.activebeans;

import java.util.Map.Entry;

import javassist.util.proxy.ProxyObject;

@SuppressWarnings("rawtypes")
public class ModelMethodHandler extends Delegate implements Model {
	
	private Class<Model> selfClass; 
	private AttributeMethodHandler attrHandler;
	
	public ModelMethodHandler(AttributeMethodHandler attrHandler){
		this.attrHandler = attrHandler;
		@SuppressWarnings("unchecked")
		Class<Model> activeClass = (Class<Model>) attrHandler.activeClass();
		selfClass = activeClass;
	}
	
	@Override
	public boolean save() {
		return ActiveBeansUtils.insert(
			ActiveBeans.repository(), 
			selfClass,
			(Model) self()
		) == 1;
	}

	@Override
	public boolean update() {
		return ActiveBeansUtils.update(
				ActiveBeans.repository(), 
				selfClass,
				(Model) self()
			) == 1;
	}

	@Override
	public boolean update(Object attrs) {
		attrs(attrs);
		return update();
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
		return (Model) self();
	}

	@Override
	public boolean destroy() {
		return ActiveBeansUtils.delete(
			ActiveBeans.repository(), 
			selfClass,
			(Model) self()
		) == 1;
	}

}
