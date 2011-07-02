package org.activebeans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.ProxyObject;

@SuppressWarnings("rawtypes")
public class ModelsMethodHandler extends Delegate implements Models {
	
	private Class<? extends Model<?, ?, ?, ?>> activeClass;
	
	private Class<?> modelsInterface;
	
	private Class<?> conditionsInterface;
	
	private Object conds;
	
	private Set<Object> data = new LinkedHashSet<Object>();
	
	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		this(activeClass, null);
	}
	
	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass, Object conds) {
		this.activeClass = activeClass;
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		modelsInterface = intro.modelsInterface();
		conditionsInterface = intro.conditionsInterface();
		this.conds = conds;
	}

	public Object conditions(){
		return conds; 
	}
	
	@Override
	public boolean add(Object e) {
		data.add(e);
		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		@SuppressWarnings("unchecked")
		boolean rtn = data.addAll(c);
		return rtn;
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return data.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public Iterator iterator() {
		onIteration(data, conds);
		return data.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return data.remove(o);
	}

	@Override
	public boolean removeAll(Collection c) {
		return data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		return data.retainAll(c);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return data.toArray(a);
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
	public boolean update(Object opts) {
		return false;
	}

	@Override
	public Models attrs(Object opts) {
		return null;
	}

	@Override
	public boolean destroy() {
		return false;
	}

	@Override
	public Model build() {
		return null;
	}

	@Override
	public Model build(Object opts) {
		return null;
	}

	@Override
	public Model create() {
		return null;
	}

	@Override
	public Model create(Object opts) {
		return null;
	}

	@Override
	public Model get(Object key, Object... keys) {
		return null;
	}

	@Override
	public Model first() {
		return null;
	}

	@Override
	public Model first(Object cond) {
		return null;
	}

	@Override
	public Model last() {
		return null;
	}

	@Override
	public Model last(Object conds) {
		return null;
	}

	@Override
	public Models all(Object conds) {
		if(this.conds == null){
			this.conds = conds;
		}else if(conds != null){
			ConditionsMethodHandler thisConds = (ConditionsMethodHandler) ((ProxyObject)this.conds).getHandler();
			thisConds.chain((ConditionsMethodHandler) ((ProxyObject)conds).getHandler());
		}
		return (Models) self();
	}
	
	@Override
	protected Object methodMissing(Object self, Method method, Method proceed,
			Object[] args) throws Throwable {
		String name = method.getName();
		Class<?>[] params = method.getParameterTypes();
		Object rtn = null;
		boolean conditionsType = params.length > 0?params[0].equals(conditionsInterface):false;
		Object arg = conditionsType?args[0]:null; 
		if(name.equals("all") && conditionsType){
			rtn = all(arg);
		}else if(name.equals("attrs") && conditionsType){
			attrs(arg);
		}else if(method.getReturnType().equals(modelsInterface)){
			try{
				Method finder = activeClass.getMethod(name, params);
				if(Modifier.isStatic(finder.getModifiers())){
					ModelsMethodHandler models = (ModelsMethodHandler) ((ProxyObject)finder.invoke(null, args)).getHandler();
					all(models.conditions());
					rtn = self;
				}
			}catch(NoSuchMethodException e){}
		}
		return rtn;
	}
	
	protected void onIteration(Set<Object> data, Object conds){ 
		
	}
	
}