package org.activebeans;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

public class OptionsMethodHandler implements MethodHandler {
	
	private Map<Method, Property> propOptionMap = new HashMap<Method, Property>();
	
	private Map<Property, Object> propMap = new HashMap<Property, Object>();
	
	private Map<Method, Association> belongsToOptionMap = new HashMap<Method, Association>();
	
	private Map<Method, Association> hasManyOptionMap = new HashMap<Method, Association>();
	
	private Map<Association, Object> assocMap = new HashMap<Association, Object>();
	
	public OptionsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass){
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		for (PropertyMethods methods : intro.propertyMethods()) {
			propOptionMap.put(methods.option(), methods.property());
		}
		for (SingularAssociationMethods methods : intro.belongsToMethods()) {
			belongsToOptionMap.put(methods.option(), methods.association());
		}
		for (CollectionAssociationMethods methods : intro.hasManyMethods()) {
			hasManyOptionMap.put(methods.option(), methods.association());
		}
	}
	
	public void set(Property prop, Object val){
		propMap.put(prop, val);
	}
	
	public Object get(Property prop){
		return propMap.get(prop);
	}
	
	public void set(Association assoc, Object val){
		assocMap.put(assoc, val);
	}
	
	public Object get(Association assoc){
		return assocMap.get(assoc);
	}
	
	public Map<Property, Object> properties(){
		return Collections.unmodifiableMap(propMap);
	}
	
	public Map<Association, Object> associations(){
		return Collections.unmodifiableMap(assocMap);
	}
	
	@Override
	public Object invoke(final Object self, final Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		if(propOptionMap.containsKey(method)){
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					set(propOptionMap.get(method), val);
					return self;
				}
			};
		}else if(belongsToOptionMap.containsKey(method)){
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					set(belongsToOptionMap.get(method), val);
					return self;
				}
			};
		}else if(hasManyOptionMap.containsKey(method)){
			rtn = new CollectionOption<Object, Object>() {
				@Override
				public Object val(Object... val) {
					set(hasManyOptionMap.get(method), val);
					return self;
				}
			};
		}
		return rtn;
	}

}
