package org.activebeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveIntrospector<T extends Model> {

	private Active at;

	private Class<T> clazz;

	private Class<?> interf;

	private Class<? extends Models<T>> collectionInterf;

	private Map<String, Property> propMap = new HashMap<String, Property>();

	private Map<Class<? extends Model>, Association> belongsToMap = new HashMap<Class<? extends Model>, Association>();

	private Map<Class<? extends Model>, Association> hasManyMap = new HashMap<Class<? extends Model>, Association>();

	private static String interfName(Class<? extends Model> activeClass) {
		return activeClass.getPackage().getName() + ".Active"
				+ activeClass.getSimpleName();
	}

	private static String collectionInterfName(
			Class<? extends Model> activeClass) {
		return activeClass.getName() + "$Models";
	}

	private static Class<?> interf(Class<? extends Model> activeClass) {
		return ActiveBeansUtils.classNameMap(activeClass.getInterfaces()).get(
				interfName(activeClass));
	}

	private static <T extends Model> Class<? extends Models<T>> collectionInterf(
			Class<T> activeClass) {
		@SuppressWarnings("unchecked")
		Class<? extends Models<T>> clazz = (Class<? extends Models<T>>) ActiveBeansUtils
				.classNameMap(activeClass.getDeclaredClasses()).get(
						collectionInterfName(activeClass));
		return clazz;
	}

	private ActiveIntrospector(Class<T> activeClass) {
		clazz = activeClass;
		at = clazz.getAnnotation(Active.class);
		interf = interf(clazz);
		collectionInterf = collectionInterf(clazz);
		for (Property prop : at.with()) {
			propMap.put(prop.name(), prop);
		}
		for (Association belongsTo : at.belongsTo()) {
			belongsToMap.put(belongsTo.with(), belongsTo);
		}
		for (Association hasMany : at.hasMany()) {
			hasManyMap.put(hasMany.with(), hasMany);
		}
	}

	public static <U extends Model> ActiveIntrospector<U> of(
			Class<U> activeClass) {
		return new ActiveIntrospector<U>(activeClass);
	}

	public Active getActiveAnnotation() {
		return at;
	}

	public Class<T> getActiveClass() {
		return clazz;
	}

	public Class<?> getActiveInterface() {
		return interf;
	}

	public Class<? extends Models<T>> getActiveCollectionInterface() {
		return collectionInterf;
	}

	public Property getProperty(String name) {
		return propMap.get(name);
	}

	public List<Property> getProperties() {
		return new ArrayList<Property>(propMap.values());
	}

	public Association getBelongsTo(Class<? extends Model> model) {
		return belongsToMap.get(model);
	}

	public List<Association> getBelongsTos() {
		return new ArrayList<Association>(belongsToMap.values());
	}

	public Association getHasMany(Class<? extends Model> model) {
		return hasManyMap.get(model);
	}

	public List<Association> getHasManys() {
		return new ArrayList<Association>(hasManyMap.values());
	}

}
