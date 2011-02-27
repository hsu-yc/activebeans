package org.activebeans;

import java.lang.reflect.Method;

public interface SingularAssociationMethods {

	Association association();

	Method get();
	
	Method set();
	
	Method option();
	
	Method condition();

}
