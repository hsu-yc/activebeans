package org.activebeans;

public class ActiveIntrospector<T extends Model> {

	private Active at;

	private Class<T> clazz;

	private Class<?> interf;

	private Class<? extends Models<T>> collectionInterf;

	private static String interfName(Class<? extends Model> activeClass) {
		return activeClass.getPackage().getName() + ".Active"
				+ activeClass.getSimpleName();
	}

	private static String collectionInterfName(
			Class<? extends Model> activeClass) {
		return activeClass.getCanonicalName() + "$Models";
	}

	private static Class<?> interf(Class<? extends Model> activeClass) {
		Class<?> interf = null;
		Class<?>[] interfs = activeClass.getInterfaces();
		String interfName = interfName(activeClass);
		for (Class<?> i : interfs) {
			if (i.getCanonicalName().equals(interfName)) {
				interf = i;
				break;
			}
		}
		return interf;
	}

	private ActiveIntrospector(Class<T> activeClass) {
		clazz = activeClass;
		at = clazz.getAnnotation(Active.class);
		interf = interf(clazz);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Models<T>> collectionInterf = (Class<? extends Models<T>>) Class
					.forName(collectionInterfName(activeClass));
			this.collectionInterf = collectionInterf;
		} catch (ClassNotFoundException e) {
			throw new ActiveBeansException(e);
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

}
