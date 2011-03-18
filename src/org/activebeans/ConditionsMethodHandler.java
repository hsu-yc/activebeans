package org.activebeans;

import java.lang.reflect.Method;
import java.util.HashMap;

import javassist.util.proxy.MethodHandler;

public class ConditionsMethodHandler implements MethodHandler {
	
	private HashMap<Method, Property> propConditionMap = new HashMap<Method, Property>();
	
	private HashMap<Method, Association> belongsToConditionMap = new HashMap<Method, Association>();
	
	private HashMap<Method, Association> hasManyConditionMap = new HashMap<Method, Association>();
	
	public ConditionsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass){
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
	
	@Override
	public Object invoke(final Object self, Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		if(propConditionMap.containsKey(method) || belongsToConditionMap.containsKey(method)){
			rtn = new Condition<Object, Object>() {
				@Override
				public Object gt(Object val) {
					return self;
				}
				@Override
				public Object lt(Object val) {
					return self;
				}
				@Override
				public Object gte(Object val) {
					return self;
				}
				@Override
				public Object lte(Object val) {
					return self;
				}
				@Override
				public Object not(Object val) {
					return self;
				}
				@Override
				public Object eql(Object val) {
					return self;
				}
				@Override
				public Object like(Object val) {
					return self;
				}
			};
		}else if(hasManyConditionMap.containsKey(method)){
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					return self;
				}
			};
		}
		return rtn;
	}

}
