package org.activebeans;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

public class AttributeMethodHandler implements MethodHandler {

	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	
	private Map<Method, Property> propGetterMap = new HashMap<Method, Property>();

	private Map<Method, Property> propSetterMap = new HashMap<Method, Property>();

	private Map<Property, Object> propMap = new HashMap<Property, Object>();

	private Map<Method, Association> belongsToGetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> belongsToSetterMap = new HashMap<Method, Association>();

	private Map<Method, Association> hasManyGetterMap = new HashMap<Method, Association>();

	private Map<Association, Object> assocMap = new HashMap<Association, Object>();
	
	public AttributeMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		this.activeClass = activeClass;
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		for (PropertyMethods methods : intro.propertyMethods()) {
			Property prop = methods.property();
			propGetterMap.put(methods.get(), prop);
			propSetterMap.put(methods.set(), prop);
		}
		for (SingularAssociationMethods methods : intro.belongsToMethods()) {
			Association assoc = methods.association();
			belongsToGetterMap.put(methods.get(), assoc);
			belongsToSetterMap.put(methods.set(), assoc);
		}
		for (CollectionAssociationMethods methods : intro.hasManyMethods()) {
			hasManyGetterMap.put(methods.get(), methods.association());
		}
	}
	
	public Class<? extends Model<?, ?, ?, ?>> activeClass(){
		return activeClass;
	}
	
	public Object get(Property prop){
		return propMap.get(prop);
	}
	
	public void set(Property prop, Object val){
		propMap.put(prop, val);
	}
	
	public Object get(Association assoc){
		Object rtn = assocMap.get(assoc);
		if(rtn == null && hasManyGetterMap.containsValue(assoc)){
			rtn = ActiveBeansUtils.models(assoc.with());
			set(assoc, rtn);
		}
		return rtn;
	}
	
	public void set(Association assoc, Object val){
		assocMap.put(assoc, val);
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		Object rtn = null;
		if (propGetterMap.containsKey(method)) {
			rtn = get(propGetterMap.get(method));
		} else if (propSetterMap.containsKey(method)) {
			set(propSetterMap.get(method), args[0]);
			rtn = Void.TYPE;
		} else if (belongsToGetterMap.containsKey(method)) {
			rtn = get(belongsToGetterMap.get(method));
		} else if (belongsToSetterMap.containsKey(method)) {
			set(belongsToSetterMap.get(method), args[0]);
			rtn = Void.TYPE;
		} else if (hasManyGetterMap.containsKey(method)) {
			rtn = get(hasManyGetterMap.get(method));
		}
		return rtn;
	}

}
