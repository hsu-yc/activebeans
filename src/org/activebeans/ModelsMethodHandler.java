package org.activebeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ModelsMethodHandler extends Delegate implements Models {
	
	@SuppressWarnings("unused")
	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	
	@SuppressWarnings("unused")
	private Class<?> modelsInterface;
	
	private List<Object> data = new ArrayList<Object>();
	
	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		this.activeClass = activeClass;
		modelsInterface = new ActiveIntrospector(activeClass).modelsInterface();
	}

	protected void onIteration(List<Object> data){
		
	}

	@Override
	public boolean add(Object e) {
		data.add(e);
		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator iterator() {
		onIteration(data);
		return data.iterator();
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray(Object[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(Object opts) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Models attrs(Object opts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean destroy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Model build() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model build(Object opts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model create(Object opts) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model get(Object key, Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model first() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model first(Object cond) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model last() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model last(Object cond) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Models all(Object cond) {
		// TODO Auto-generated method stub
		return null;
	}
	
}