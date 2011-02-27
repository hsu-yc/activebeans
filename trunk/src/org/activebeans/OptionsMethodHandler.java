package org.activebeans;

import java.lang.reflect.Method;
import java.util.HashMap;

import javassist.util.proxy.MethodHandler;

public class OptionsMethodHandler implements MethodHandler {
	
	private HashMap<Method, Property> propOptionMap = new HashMap<Method, Property>();
	
	private HashMap<Method, Association> belongsToOptionMap = new HashMap<Method, Association>();
	
	private HashMap<Method, Association> hasManyOptionMap = new HashMap<Method, Association>();
	
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
	
	@Override
	public Object invoke(final Object self, Method method, Method proceed, Object[] args)
			throws Throwable {
		Object rtn = null;
		if(propOptionMap.containsKey(method) || belongsToOptionMap.containsKey(method)){
			rtn = new SingularOption<Object, Object>() {
				@Override
				public Object val(Object val) {
					return self;
				}
			};
		}else if(hasManyOptionMap.containsKey(method)){
			rtn = new CollectionOption<Object, Object>() {
				@Override
				public Object val(Object... val) {
					return self;
				}
			};
		}
		return rtn;
	}

}
