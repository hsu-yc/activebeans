package org.activebeans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.ProxyObject;

@SuppressWarnings("rawtypes")
public class ModelsMethodHandler extends Delegate implements Models {
	
	private Class<? extends Model> activeClass;
	
	private Class<?> modelsInterface;
	
	private Class<?> conditionsInterface;
	
	private Association assoc;
	
	private Model assocModel;
	
	private Object conds;
	
	private boolean loaded;
	
	private Set<Object> data = new LinkedHashSet<Object>();
	
	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass, Association assoc, Model assocModel) {
		this(activeClass, assoc, assocModel, null);
	}
	
	public ModelsMethodHandler(Class<? extends Model<?, ?, ?, ?>> activeClass, Association assoc, Model assocModel, Object conds) {
		this.activeClass = activeClass;
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		modelsInterface = intro.modelsInterface();
		conditionsInterface = intro.conditionsInterface();
		this.assoc = assoc;
		this.assocModel = assocModel;
		this.conds = conds;
	}

	public Object conditions(){
		return conds; 
	}
	
	@Override
	public boolean add(Object e) {
		load();
		data.add(e);
		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		load();
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
		load();
		return data.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		load();
		return data.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		load();
		return data.isEmpty();
	}

	@Override
	public Iterator iterator() {
		load();
		return data.iterator();
	}

	@Override
	public boolean remove(Object o) {
		load();
		return data.remove(o);
	}

	@Override
	public boolean removeAll(Collection c) {
		load();
		return data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
		load();
		return data.retainAll(c);
	}

	@Override
	public int size() {
		load();
		return data.size();
	}

	@Override
	public Object[] toArray() {
		load();
		return data.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		load();
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
		Model model = ActiveBeansUtils.model(activeClass);
		add(model);
		if(assoc != null){
			AttributeMethodHandler attrHandler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
			attrHandler.set(assoc, assocModel);
		}
		return model;
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
	public Model first() {
		return first(null);
	}

	@Override
	public Model first(Object conds) {
		all(conds);
		@SuppressWarnings("unchecked")
		Model first = ActiveBeansUtils.first(ActiveBeans.repository(), activeClass, this.conds);
		return first;
	}

	@Override
	public Model last() {
		return last(null);
	}

	@Override
	public Model last(Object conds) {
		all(conds);
		@SuppressWarnings("unchecked")
		Model last = ActiveBeansUtils.last(ActiveBeans.repository(), activeClass, this.conds);
		return last;
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
	
	private void load(){
		if(!loaded){
			data.addAll(query(conds));
			loaded = true;
		}
	}
	
	protected Set<Object> query(Object conds){ 
		return Collections.emptySet();
	}
	
}