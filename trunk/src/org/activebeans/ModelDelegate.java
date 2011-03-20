package org.activebeans;

@SuppressWarnings("rawtypes")
public class ModelDelegate implements Model {
	
	@SuppressWarnings("unused")
	private AttributeMethodHandler attrs;
	
	public ModelDelegate(AttributeMethodHandler attrs){
		this.attrs = attrs;
	}
	
	@Override
	public boolean save() {
		return false;
	}

	@Override
	public boolean update() {
		return false;
	}

	@Override
	public boolean update(Object attrs) {
		return false;
	}

	@Override
	public Model attrs(Object attrs) {
		return null;
	}

	@Override
	public boolean destroy() {
		return false;
	}

}
