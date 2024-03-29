package org.activebeans;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

public class ConditionsMethodHandler implements MethodHandler {

	public enum Order { 
		
		ASC {
			@Override
			public Order reverse() {
				return DESC;
			}
		}, DESC {
			@Override
			public Order reverse() {
				return ASC;
			}
		};
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
		
		public abstract Order reverse();
	
	}
	
	public enum Operator {
		
		GT(">"), 
		LT("<"), 
		GTE(">="), 
		LTE("<="), 
		NOT("!="), 
		EQL("=") , 
		LIKE("like"),
		IN("in") {
			@Override
			public String prepareOperand(Object val) {
				return prepareMultipleOperands((Object[])val);
			}
			@Override
			public List<Object> prepareParams(Object val) {
				return prepareMultipleParams((Object[]) val);
			}
		},
		NIN("not in") {
			@Override
			public String prepareOperand(Object val) {
				return prepareMultipleOperands((Object[])val);
			}
			@Override
			public List<Object> prepareParams(Object val) {
				return prepareMultipleParams((Object[]) val);
			}
		};
		
		private String sqlOp;
		
		Operator(String sqlOp){
			this.sqlOp = sqlOp;
		}
		
		@Override
		public String toString() {
			return sqlOp;
		}
		
		public String prepareOperand(Object val){
			return "?";
		}
		
		public List<Object> prepareParams(Object val){
			return Collections.singletonList(val);
		}
		
		private static String prepareMultipleOperands(Object[] vals){
			String rs;
			if(vals.length == 0){
				rs = "''";
			}else{
				rs = "";
				for(int i=0; i<vals.length; i++){
					rs += (i>0?", ":"") + "?";
				}
			}
			return "(" + rs + ")";
		}
		
		private static List<Object> prepareMultipleParams(Object[] vals){
			return Arrays.asList(vals);
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
	
	private Class<? extends Model<?, ?, ?, ?>> associatedClass;
	
	private List<Object> associatedKeys = new ArrayList<Object>();
	
	public ConditionsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass){
		this(activeClass, null, Collections.emptyList());
	}
	
	public ConditionsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass, 
			Class<? extends Model<?, ?, ?, ?>> associatedClass, List<Object> associatedKeys){
		this.activeClass = activeClass;
		this.associatedClass = associatedClass;
		this.associatedKeys.addAll(associatedKeys);
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
	
	public Class<? extends Model<?, ?, ?, ?>> activeClass(){
		return activeClass;
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
	
	public List<Object> params(){
		List<Object> params = new ArrayList<Object>();
		for (Object conds : assocMap.values()) {
			for (Map<Operator, Object> valMap : ((ConditionsMethodHandler)((ProxyObject)conds).getHandler()).propMap.values()) {
				for(Entry<Operator, Object> e : valMap.entrySet()){
					params.addAll(e.getKey().prepareParams(e.getValue()));
				}
			}
		}
		params.addAll(associatedKeys);
		for (Map<Operator, Object> valMap : propMap.values()) {
			for(Entry<Operator, Object> e : valMap.entrySet()){
				params.addAll(e.getKey().prepareParams(e.getValue()));
			}
		}
		return params;
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
	
	public Map<Property, Order> reverseOrders(){
		Map<Property, Order> reverse = new LinkedHashMap<Property, Order>();
		for (Entry<Property, Order> e : orders.entrySet()) {
			reverse.put(e.getKey(), e.getValue().reverse());
		}
		return reverse;
	}
	
	public void field(Property prop) {
		fields.add(prop);
	}
	
	public Set<Property> fields(){
		return Collections.unmodifiableSet(fields);
	}
	
	public Class<? extends Model<?, ?, ?, ?>> associatedClass(){
		return associatedClass;
	}
	
	public List<Object> associatedKeys(){
		return Collections.unmodifiableList(associatedKeys);
	}
	
	public void chain(ConditionsMethodHandler conds){
		for (Entry<Property, Map<Operator, Object>> e1 : conds.properties().entrySet()) {
			for (Entry<Operator, Object> e2 : e1.getValue().entrySet()) {
				set(e1.getKey(), e2.getKey(), e2.getValue());
			}
		}
		assocMap.putAll(conds.associations());
		orders.putAll(conds.orders());
		fields.addAll(conds.fields());
	}
	
	public String prepareWhereClause(){
		String clause = "";
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		String tableName = ActiveBeansUtils.camelCaseToUnderscore(
			activeClass.getSimpleName());
		boolean empty = true;
		for(Entry<Association, Object> nested : assocMap.entrySet()){
			Association assoc = nested.getKey();
			Class<? extends Model<?, ?, ?, ?>> assocClass = assoc.with();
			String assocTableName = ActiveBeansUtils.camelCaseToUnderscore(
				assocClass.getSimpleName());
			boolean isBelongsTo = intro.belongsTo(assocClass) != null;
			if(isBelongsTo || intro.hasMany(assocClass) != null){
				clause += " join " + assocTableName + " on ";
				List<String> foreignKeys;
				List<String> keys;
				if(isBelongsTo){
					foreignKeys = ActiveBeansUtils.associationKeys(assocClass);
					keys = ActiveBeansUtils.keys(assocClass);
				}else{
					foreignKeys = ActiveBeansUtils.keys(activeClass);
					keys = ActiveBeansUtils.associationKeys(activeClass);
				}
				for(int i=0; i<keys.size(); i++){
					clause += Table.qualify(assocTableName, keys.get(i)) + " = " + Table.qualify(tableName, foreignKeys.get(i));
				}
				ConditionsMethodHandler assocHandler = (ConditionsMethodHandler) ((ProxyObject)nested.getValue()).getHandler();
				for (Entry<Property, Map<Operator, Object>> prop : assocHandler.properties().entrySet()) {
					for(Entry<Operator, Object> exp : prop.getValue().entrySet()){
						Operator op = exp.getKey();
						clause += " " + (empty?"where":"and") + " " + Table.qualify(assocTableName, ActiveBeansUtils.camelCaseToUnderscore(prop.getKey().name()))
							+ " " + op + " " + op.prepareOperand(exp.getValue());
						empty = false;
					}
				}
			}
		}
		if(associatedClass != null){
			for (String k : ActiveBeansUtils.associationKeys(associatedClass)) {
				clause += " " + (empty?"where":"and") + " " + Table.qualify(tableName, k) + " = ?"; 
				empty = false;
			}
		}
		for (Entry<Property, Map<Operator, Object>> prop : propMap.entrySet()) {
			for(Entry<Operator, Object> exp : prop.getValue().entrySet()){
				Operator op = exp.getKey();
				clause += " " + (empty?"where":"and") + " " + Table.qualify(tableName, ActiveBeansUtils.camelCaseToUnderscore(prop.getKey().name()))
					+ " " + op + " " + op.prepareOperand(exp.getValue());
				empty = false;
			}
		}
		return clause;
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
				public Object in(Object... val) {
					set(prop, Operator.IN, val);
					return self;
				}
				@Override
				public Object nin(Object... val) {
					set(prop, Operator.NIN, val);
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
