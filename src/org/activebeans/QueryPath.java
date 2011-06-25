package org.activebeans;

public interface QueryPath<T, U> {
	
	T where(U val);
	
	U on();
	
}