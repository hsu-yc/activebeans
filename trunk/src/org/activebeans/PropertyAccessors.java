package org.activebeans;

import java.lang.reflect.Method;

public interface PropertyAccessors {

	Property property();

	Method get();

	Method set();

}
