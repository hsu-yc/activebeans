package org.activebeans;

import java.util.List;

public interface CollectionAssociation<T extends Base<?>> {

	List<T> get();
	
	List<T> get(boolean forceReload);
	
	CollectionAssociation<T> add(T... obj);
	
	CollectionAssociation<T> delete(T... obj);
	
	CollectionAssociation<T> set(T... obj);
	
	List<?> ids();
	
	CollectionAssociation<T> setIds(Object... ids);
	
	CollectionAssociation<T> clear();
	
	boolean empty();
	
	int size();
	
}
