package org.activebeans;

public interface Condition<T, U> {

	T gt(U val);
	
	T lt(U val);
	
	T gte(U val);
	
	T lte(U val);
	
	T not(U val);
	
	T eql(U val);
	
	T like(U val);
	
	T field();
	
	T asc();
	
	T desc();
	
}
