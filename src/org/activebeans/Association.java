package org.activebeans;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Association {

	Class<? extends Model<?, ?, ?, ? extends Models<?, ?, ?, ?>>> with();

	String name() default "";

	boolean required() default false;

}
