package org.activebeans;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.eclipse.handlers.HandleActive;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;

public class ActiveIntrospector {
	
	private Active at;

	private Class<?> clazz;

	private Class<?> attrsInterf;

	private Class<?> modelsInterf;
	
	private Class<?> optionsInterf;

	private Class<?> conditionsInterf;
	
	private Map<String, Property> propMap = new HashMap<String, Property>();

	private Map<Class<? extends Model<?, ?, ?, ?>>, Association> belongsToMap = new HashMap<Class<? extends Model<?, ?, ?, ?>>, Association>();

	private Map<Class<? extends Model<?, ?, ?, ?>>, Association> hasManyMap = new HashMap<Class<? extends Model<?, ?, ?, ?>>, Association>();

	private Map<Property, PropertyMethods> propMethodMap = new HashMap<Property, PropertyMethods>();

	private Map<Association, SingularAssociationMethods> belongsToMethodMap = new HashMap<Association, SingularAssociationMethods>();

	private Map<Association, CollectionAssociationMethods> hasManyMethodMap = new HashMap<Association, CollectionAssociationMethods>();

	private List<Property> keys = new ArrayList<Property>();

	private static Class<?> attrsInterf(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return ActiveBeansUtils.classNameMap(activeClass.getInterfaces()).get(
				HandleActive.attributesInterface(activeClass));
	}
	
	private static boolean isModelInterf(Type interf){
		return interf instanceof ParameterizedType && 
			((ParameterizedType)interf).getRawType().equals(Model.class);
	}

	private static ParameterizedType modelInterf(Class<?> activeClass) {
		ParameterizedType modelInterf = null;
		for (Type interf : activeClass.getGenericInterfaces()) {
			if(isModelInterf(interf)){
				modelInterf = (ParameterizedType) interf;
			}
		}
		return modelInterf;
	}
	
	private static Class<?>[] modelTypeParams(Class<?> activeClass) {
		List<Class<?>> params = new ArrayList<Class<?>>();
		for (Type p : modelInterf(activeClass).getActualTypeArguments()) {
			params.add((Class<?>)p);
		}
		return params.toArray(new Class[0]);
	}
	
	private static Class<?> modelsInterf(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return ActiveBeansUtils.classNameMap(modelTypeParams(activeClass))
					.get(HandleActive.modelsInterface(activeClass));
	}
	
	private static Class<?> optionsInterf(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return ActiveBeansUtils.classNameMap(modelTypeParams(activeClass))
					.get(HandleActive.optionsInterface(activeClass));
	}
	
	private static Class<?> conditionsInterf(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return ActiveBeansUtils.classNameMap(modelTypeParams(activeClass))
			.get(HandleActive.conditionsInterface(activeClass));
	}

	public ActiveIntrospector(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		clazz = activeClass;
		at = clazz.getAnnotation(Active.class);
		attrsInterf = attrsInterf(activeClass);
		modelsInterf = modelsInterf(activeClass);
		optionsInterf = optionsInterf(activeClass);
		conditionsInterf = conditionsInterf(activeClass);
		for (Property prop : at.with()) {
			propMap.put(prop.name(), prop);
			propMethodMap.put(prop, new JavaBeanPropertyMethods(attrsInterf, 
				optionsInterf, conditionsInterf, prop));
			if (prop.key()) {
				keys.add(prop);
			}
		}
		for (Association belongsTo : at.belongsTo()) {
			belongsToMap.put(belongsTo.with(), belongsTo);
			belongsToMethodMap.put(belongsTo, new JavaBeanBelongsToAssociationMethods(
				attrsInterf, optionsInterf, conditionsInterf, belongsTo));
		}
		for (Association hasMany : at.hasMany()) {
			hasManyMap.put(hasMany.with(), hasMany);
			hasManyMethodMap.put(hasMany, new JavaBeanHasManyAssociationMethods(
				attrsInterf, optionsInterf, conditionsInterf, hasMany));
		}
	}

	public static Set<Class<? extends Model<?, ?, ?, ?>>> activeClasses() {
		ClasspathDiscoverer disc = new ClasspathDiscoverer();
		final Set<Class<? extends Model<?, ?, ?, ?>>> classes = new HashSet<Class<? extends Model<?, ?, ?, ?>>>();
		disc.addAnnotationListener(new ClassAnnotationDiscoveryListener() {
			@Override
			public String[] supportedAnnotations() {
				return new String[] { Active.class.getName() };
			}

			@Override
			public void discovered(String clazz, String at) {
				try {
					@SuppressWarnings("rawtypes")
					Class rawClass = Class.forName(clazz);
					@SuppressWarnings("unchecked")
					Class<Model<?, ?, ?, ?>> activeClass = rawClass;
					classes.add(activeClass);
				} catch (ClassNotFoundException e) {
					throw new ActiveBeansException(e);
				}
			}
		});
		disc.discover();
		return classes;
	}

	public Active activeAnnotation() {
		return at;
	}

	public Class<?> activeClass() {
		return clazz;
	}

	public Class<?> attributesInterface() {
		return attrsInterf;
	}

	public Class<?> modelsInterface() {
		return modelsInterf;
	}
	
	public Class<?> optionsInterface() {
		return optionsInterf;
	}
	
	public Class<?> conditionsInterface() {
		return conditionsInterf;
	}

	public Property property(String name) {
		return propMap.get(name);
	}

	public List<Property> properties() {
		return new ArrayList<Property>(propMap.values());
	}

	public Association belongsTo(Class<? extends Model<?, ?, ?, ?>> model) {
		return belongsToMap.get(model);
	}

	public List<Association> belongsTos() {
		return new ArrayList<Association>(belongsToMap.values());
	}

	public Association hasMany(Class<? extends Model<?, ?, ?, ?>> model) {
		return hasManyMap.get(model);
	}

	public List<Association> hasManys() {
		return new ArrayList<Association>(hasManyMap.values());
	}

	public PropertyMethods propertyMethods(Property prop) {
		return propMethodMap.get(prop);
	}

	public List<PropertyMethods> propertyMethods() {
		return new ArrayList<PropertyMethods>(propMethodMap.values());
	}

	public SingularAssociationMethods belongsToMethods(Association assoc) {
		return belongsToMethodMap.get(assoc);
	}

	public List<SingularAssociationMethods> belongsToMethods() {
		return new ArrayList<SingularAssociationMethods>(
				belongsToMethodMap.values());
	}

	public CollectionAssociationMethods hasManyMethods(Association assoc) {
		return hasManyMethodMap.get(assoc);
	}

	public List<CollectionAssociationMethods> hasManyMethods() {
		return new ArrayList<CollectionAssociationMethods>(
				hasManyMethodMap.values());
	}

	public List<Property> keys() {
		return Collections.unmodifiableList(keys);
	}

}
