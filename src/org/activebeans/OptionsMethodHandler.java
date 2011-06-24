package org.activebeans;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodHandler;

public class OptionsMethodHandler implements MethodHandler {
	
	public enum Order { ASC, DESC } 
	
	private Map<Method, Property> propOptionMap = new HashMap<Method, Property>();
	
	private Map<Property, Object> propMap = new HashMap<Property, Object>();
	
	private Map<Property, Order> orders = new LinkedHashMap<Property, Order>();
	
	private Set<Property> fields = new LinkedHashSet<Property>();
	
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
	
	public void order(Property prop, Order order) {
		orders.put(prop, order);
	}
	
	public Order order(Property prop) {
		return orders.get(prop);
	}
	
	public Map<Property, Order> orders(){
		return Collections.unmodifiableMap(orders);
	}
	
	public void field(Property prop) {
		fields.add(prop);
	}
	
	public Set<Property> fields(){
		return Collections.unmodifiableSet(fields);
	}
	
	@Override
	public Object invoke(final Object self, final Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		if(propOptionMap.containsKey(method)){
			final Property prop = propOptionMap.get(method);
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					set(prop, val);
					return self;
				}
				@Override
				public Object asc() {
					return self;
				}
				@Override
				public Object desc() {
					return self;
				}
				@Override
				public Object field() {
					return self;
				}
			};
		}else if(belongsToOptionMap.containsKey(method)){
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					Association assoc = belongsToOptionMap.get(method);
					@SuppressWarnings("rawtypes")
					Model rawModel = ActiveBeansUtils.model(assoc.with());
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Model model = rawModel.attrs(val);
					set(assoc, model);     
					return self;
				}
				@Override
				public Object asc() {
					throw new UnsupportedOperationException();
				}
				@Override
				public Object desc() {
					throw new UnsupportedOperationException();
				}
				@Override
				public Object field() {
					throw new UnsupportedOperationException();
				}
			};
		}else if(hasManyOptionMap.containsKey(method)){
			rtn = new CollectionOption<Object, Object>() {
				@SuppressWarnings("unchecked")
				@Override
				public Object val(Object... val) {
					Association assoc = hasManyOptionMap.get(method);
					Class<? extends Model<?, ?, ?, ?>> activeClass = assoc.with();
					@SuppressWarnings("rawtypes")
					Models models = ActiveBeansUtils.models(activeClass);
					for (Object v : val) {
						@SuppressWarnings("rawtypes")
						Model rawModel = ActiveBeansUtils.model(assoc.with());
						@SuppressWarnings("rawtypes")
						Model model = rawModel.attrs(v);
						models.add(model);
					}
					set(assoc, models);
					return self;
				}
			};
		}
		return rtn;
	}

}
