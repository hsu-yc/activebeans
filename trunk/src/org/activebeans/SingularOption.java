package org.activebeans;

public interface SingularOption<T, U> {
	
	T val(U val);
	
	T asc();
	
	T desc();
	
}