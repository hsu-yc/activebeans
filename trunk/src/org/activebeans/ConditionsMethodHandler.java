package org.activebeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

public class ConditionsMethodHandler implements MethodHandler {

	public enum Order { 
		
		ASC, DESC;
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	
	}
	
	public enum Operator {
		
		GT(">"), 
		LT("<"), 
		GTE(">="), 
		LTE("<="), 
		NOT("!="), 
		EQL("="), 
		LIKE("like");
		
		private String sqlOp;
		
		Operator(String sqlOp){
			this.sqlOp = sqlOp;
		}
		
		@Override
		public String toString() {
			return sqlOp;
		}
		
	}
	
	private HashMap<Method, Property> propConditionMap = new HashMap<Method, Property>();
	
	private Map<Property, Map<Operator, Object>> propMap = new LinkedHashMap<Property, Map<Operator, Object>>();
	
	private Map<Property, Order> orders = new LinkedHashMap<Property, Order>();
	
	private Set<Property> fields = new LinkedHashSet<Property>();
	
	private HashMap<Method, Association> belongsToConditionMap = new HashMap<Method, Association>();
	
	private HashMap<Method, Association> hasManyConditionMap = new HashMap<Method, Association>();

	private Map<Association, Object> assocMap = new LinkedHashMap<Association, Object>();
	
	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	
	public ConditionsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass){
		this.activeClass = activeClass;
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		for (PropertyMethods methods : intro.propertyMethods()) {
			propConditionMap.put(methods.condition(), methods.property());
		}
		for (SingularAssociationMethods methods : intro.belongsToMethods()) {
			belongsToConditionMap.put(methods.condition(), methods.association());
		}
		for (CollectionAssociationMethods methods : intro.hasManyMethods()) {
			hasManyConditionMap.put(methods.condition(), methods.association());
		}
	}
	
	public void set(Property prop, Operator op, Object val){
		Map<Operator, Object> map = propMap.get(prop);
		if(map == null){
			map = new LinkedHashMap<Operator, Object>();
			propMap.put(prop, map);
		}
		map.put(op, val);
	}
	
	public void set(Association assoc, Object val){
		assocMap.put(assoc, val);
	}
	
	public Object get(Property prop, Operator op){
		return propMap.get(prop).get(op);
	}
	
	public Map<Operator, Object> get(Property prop){
		return Collections.unmodifiableMap(propMap.get(prop));
	}
	
	public Object get(Association assoc){
		Object rtn = assocMap.get(assoc);
		if(rtn == null){
			@SuppressWarnings("rawtypes")
			Class clazz = assoc.with();
			@SuppressWarnings("unchecked")
			Object conditions = ActiveBeansUtils.conditions(clazz);
			rtn = conditions;
			set(assoc, rtn);
		}
		return rtn;
	}
	
	public Map<Property, Map<Operator, Object>> properties(){
		return Collections.unmodifiableMap(propMap);
	}
	
	public List<Object> propertyValues(){
		List<Object> vals = new ArrayList<Object>();
		for (Map<Operator, Object> valMap : propMap.values()) {
			vals.addAll(valMap.values());
		}
		return vals;
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
	public Object invoke(final Object self, Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		if(propConditionMap.containsKey(method)){
			final Property prop = propConditionMap.get(method);
			rtn = new Condition<Object, Object>() {
				@Override
				public Object gt(Object val) {
					set(prop, Operator.GT, val);
					return self;
				}
				@Override
				public Object lt(Object val) {
					set(prop, Operator.LT, val);
					return self;
				}
				@Override
				public Object gte(Object val) {
					set(prop, Operator.GTE, val);
					return self;
				}
				@Override
				public Object lte(Object val) {
					set(prop, Operator.LTE, val);
					return self;
				}
				@Override
				public Object not(Object val) {
					set(prop, Operator.NOT, val);
					return self;
				}
				@Override
				public Object eql(Object val) {
					set(prop, Operator.EQL, val);
					return self;
				}
				@Override
				public Object like(Object val) {
					set(prop, Operator.LIKE, val);
					return self;
				}
				@Override
				public Object asc() {
					order(prop, Order.ASC);
					return self;
				}
				@Override
				public Object desc() {
					order(prop, Order.DESC);
					return self;
				}
				@Override
				public Object field() {
					ConditionsMethodHandler.this.field(prop);
					return self;
				}
			};
		}else if(hasManyConditionMap.containsKey(method) || belongsToConditionMap.containsKey(method)){
			Association hasMany = hasManyConditionMap.get(method);
			final Association assoc = hasMany == null? belongsToConditionMap.get(method):hasMany;
			rtn = new QueryPath<Object, Object>() {
				@Override
				public Object where(Object val) {
					set(assoc, val);
					return self;
				}
				@Override
				public Object on() {
					Object conditions = get(assoc);
					ActiveIntrospector intro = new ActiveIntrospector(assoc.with());
					Association belongsTo = intro.belongsTo(activeClass);
					Association inverseAssoc = belongsTo == null?intro.hasMany(activeClass):belongsTo;  
					if(inverseAssoc != null){
						ConditionsMethodHandler handler = (ConditionsMethodHandler) ((ProxyObject)conditions).getHandler();
						handler.set(inverseAssoc, self);
					}
					return conditions;
				}
			};
		}
		return rtn;
	}

}
