package org.activebeans;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Property {

	String name();

	Class<?> type();

	boolean key() default false;

	boolean autoIncrement() default false;

}
