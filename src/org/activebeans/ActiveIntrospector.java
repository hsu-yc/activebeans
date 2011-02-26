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

public class ActiveIntrospector<T extends Model<T, ?, ?, W>, U, V, W extends Models<T, ?, ?, W>> {
	
	private Active at;

	private Class<T> clazz;

	private Class<?> attrsInterf;

	private Class<W> modelsInterf;

	private Map<String, Property> propMap = new HashMap<String, Property>();

	private Map<Class<? extends Model<?, ?, ?, ?>>, Association> belongsToMap = new HashMap<Class<? extends Model<?, ?, ?, ?>>, Association>();

	private Map<Class<? extends Model<?, ?, ?, ?>>, Association> hasManyMap = new HashMap<Class<? extends Model<?, ?, ?, ?>>, Association>();

	private Map<Property, PropertyAccessors> accessorMap = new HashMap<Property, PropertyAccessors>();

	private Map<Association, BelongsToAssociationMethods> belongsToMethodMap = new HashMap<Association, BelongsToAssociationMethods>();

	private Map<Association, HasManyAssociationMethods> hasManyMethodMap = new HashMap<Association, HasManyAssociationMethods>();

	private List<Property> keys = new ArrayList<Property>();

	private static Class<?> attrsInterf(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return ActiveBeansUtils.classNameMap(activeClass.getInterfaces()).get(
				HandleActive.attributesInterface(activeClass));
	}
	
	private static boolean isModelInterf(Type interf){
		return interf instanceof ParameterizedType && 
			((ParameterizedType)interf).getRawType().equals(Model.class);
	}

	private static <X extends Model<X, ?, ?, Y>, Y extends Models<X, ?, ?, Y>> ParameterizedType modelInterf(
			Class<X> activeClass) {
		ParameterizedType modelInterf = null;
		for (Type interf : activeClass.getGenericInterfaces()) {
			if(isModelInterf(interf)){
				modelInterf = (ParameterizedType) interf;
			}
		}
		return modelInterf;
	}
	
	private static <X extends Model<X, ?, ?, Y>, Y extends Models<X, ?, ?, Y>> Class<?>[] modelTypeParams(
			Class<X> activeClass) {
		List<Class<?>> params = new ArrayList<Class<?>>();
		for (Type p : modelInterf(activeClass).getActualTypeArguments()) {
			params.add((Class<?>)p);
		}
		return params.toArray(new Class[0]);
	}
	
	private static <X extends Model<X, ?, ?, Y>, Y extends Models<X, ?, ?, Y>> Class<Y> modelsInterf(
			Class<X> activeClass) {
		@SuppressWarnings("unchecked")
		Class<Y> modelsInterf = (Class<Y>) ActiveBeansUtils.classNameMap(modelTypeParams(activeClass))
					.get(HandleActive.modelsInterface(activeClass));
		return modelsInterf;
	}

	private ActiveIntrospector(Class<T> activeClass) {
		clazz = activeClass;
		at = clazz.getAnnotation(Active.class);
		attrsInterf = attrsInterf(clazz);
		modelsInterf = modelsInterf(clazz);
		for (Property prop : at.with()) {
			propMap.put(prop.name(), prop);
			accessorMap.put(prop, new JavaBeanPropertyAccessors(attrsInterf, prop));
			if (prop.key()) {
				keys.add(prop);
			}
		}
		for (Association belongsTo : at.belongsTo()) {
			belongsToMap.put(belongsTo.with(), belongsTo);
			belongsToMethodMap.put(belongsTo,
					new JavaBeanBelongsToAssociationMethods(attrsInterf, belongsTo));
		}
		for (Association hasMany : at.hasMany()) {
			hasManyMap.put(hasMany.with(), hasMany);
			hasManyMethodMap.put(hasMany,
					new JavaBeanHasManyAssociationMethods(attrsInterf, hasMany));
		}
	}

	public static <X extends Model<X, Y, Z, A>, Y, Z, A extends Models<X, Y, Z, A>> ActiveIntrospector<X, Y, Z, A> of(
			Class<X> activeClass) {
		return new ActiveIntrospector<X, Y, Z, A>(activeClass);
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

	public Class<T> activeClass() {
		return clazz;
	}

	public Class<?> attributesInterface() {
		return attrsInterf;
	}

	public Class<W> modelsInterface() {
		return modelsInterf;
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

	public PropertyAccessors accessors(Property prop) {
		return accessorMap.get(prop);
	}

	public List<PropertyAccessors> accessors() {
		return new ArrayList<PropertyAccessors>(accessorMap.values());
	}

	public BelongsToAssociationMethods belongsToMethods(Association assoc) {
		return belongsToMethodMap.get(assoc);
	}

	public List<BelongsToAssociationMethods> belongsToMethods() {
		return new ArrayList<BelongsToAssociationMethods>(
				belongsToMethodMap.values());
	}

	public HasManyAssociationMethods hasManyMethods(Association assoc) {
		return hasManyMethodMap.get(assoc);
	}

	public List<HasManyAssociationMethods> hasManyMethods() {
		return new ArrayList<HasManyAssociationMethods>(
				hasManyMethodMap.values());
	}

	public List<Property> keys() {
		return Collections.unmodifiableList(keys);
	}

}
