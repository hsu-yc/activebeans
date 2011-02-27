package org.activebeans;

import java.lang.reflect.Method;

public interface PropertyMethods {

	Property property();

	Method get();

	Method set();
	
	Method option();
	
	Method condition();

}
