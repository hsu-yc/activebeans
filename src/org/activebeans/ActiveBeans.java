package org.activebeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.ProxyFactory;

import javax.sql.DataSource;

public final class ActiveBeans {

	private static String defaultContext;

	private static DataSource defaultDs;

	private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();

	private ActiveBeans() {

	}

	public static void setup(String context, DataSource ds) {
		if (dataSourceMap.isEmpty() || context.equals(defaultContext)) {
			defaultContext = context;
			defaultDs = ds;
		}
		dataSourceMap.put(context, ds);
	}

	public static DataSource repository(String context) {
		return dataSourceMap.get(context);
	}

	public static DataSource repository() {
		return defaultDs;
	}

	public static void migrate(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		Table table = new ActiveMigration(activeClass, defaultDs).table();
		ActiveBeansUtils.executeSql(defaultDs, table.dropStatement(),
				table.createStatment());
	}

	public static void autoMigrate() {
		List<String> stmts = new ArrayList<String>();
		for (Class<? extends Model<?, ?, ?, ?>> clazz : ActiveIntrospector.activeClasses()) {
			ActiveMigration migr = new ActiveMigration(clazz, defaultDs);
			Table table = migr.table();
			stmts.add(table.dropStatement());
			stmts.add(table.createStatment());
		}
		ActiveBeansUtils.executeSql(defaultDs, stmts);
	}

	public static void upgrade(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		String alterStmt = new ActiveMigration(activeClass, defaultDs)
				.alterStatement();
		if (alterStmt != null) {
			ActiveBeansUtils.executeSql(defaultDs, alterStmt);
		}
	}

	public static void autoUpgrade() {
		List<String> stmts = new ArrayList<String>();
		for (Class<? extends Model<?, ?, ?, ?>> clazz : ActiveIntrospector.activeClasses()) {
			String alterStmt = new ActiveMigration(clazz, defaultDs)
					.alterStatement();
			if (alterStmt != null) {
				stmts.add(alterStmt);
			}
		}
		ActiveBeansUtils.executeSql(defaultDs, stmts);
	}
	
	public static <T extends Model<T, U, ?, ?>, U> U options(Class<T> activeClass){
		ProxyFactory f = new ProxyFactory();
		@SuppressWarnings("unchecked")
		Class<U> optionsInterface = (Class<U>) new ActiveIntrospector(activeClass).optionsInterface();
		f.setInterfaces(new Class[]{optionsInterface});
		f.setFilter(new OptionsMethodFilter(activeClass));
		try {
			return optionsInterface.cast(f.create(new Class[0], new Object[0],
				new OptionsMethodHandler(activeClass)));
		} catch (Exception e) {
			throw new ActiveBeansException(e);
		}
	}
	
	public static <T extends Model<T, ?, U, ?>, U> U conditions(Class<T> activeClass){
		return ActiveBeansUtils.conditions(activeClass);
	}

	public static <T extends Model<T, U, ?, ?>, U> T build(Class<T> activeClass) {
		return build(activeClass, null);
	}

	public static <T extends Model<T, U, ?, ?>, U> T build(Class<T> modelClass,
			U attrs) {
		T model = ActiveBeansUtils.model(modelClass);
		if(attrs != null){
			model.attrs(attrs);
		}
		return model;
	}

	public static <T extends Model<T, U, ?, ?>, U> T create(Class<T> modelClass) {
		return create(modelClass, null);
	}

	public static <T extends Model<T, U, ?, ?>, U> T create(Class<T> modelClass,
			U attrs) {
		T model = build(modelClass, attrs);
		model.save();
		return model;
	}
	
	public static <T extends Model<T, U, V, ?>, U, V> T firstOrCreate(
			Class<T> modelClass) {
		return firstOrCreate(modelClass, null, null);
	}
	
	public static <T extends Model<T, U, V, ?>, U, V> T firstOrCreate(
			Class<T> modelClass, U attrs) {
		return firstOrCreate(modelClass, attrs, null);
	}
	
	public static <T extends Model<T, U, V, ?>, U, V> T firstOrCreate(
			Class<T> modelClass, U attrs, V conds) {
		T model = first(modelClass, conds);
		if(model == null){
			model = create(modelClass, attrs);
		}
		return model;
	}

	public static boolean destroy(Class<? extends Model<?, ?, ?, ?>> modelClass) {
		ActiveBeansUtils.delete(defaultDs, modelClass);
		return true;
	}

	public static <T> boolean update(Class<? extends Model<?, T, ?, ?>> modelClass,
			T attrs) {
		ActiveBeansUtils.update(
			defaultDs, 
			modelClass,
			attrs
		);
		return true;
	}

	public static <T extends Model<T, ?, ?, ?>> T get(Class<T> modelClass, Object key,
			Object... keys) {
		List<Object> keyParams = new ArrayList<Object>();
		keyParams.add(key);
		keyParams.addAll(Arrays.asList(keys));
		return ActiveBeansUtils.get(defaultDs, modelClass, keyParams);
	}

	public static <T extends Model<T, ?, U, ?>, U> T first(Class<T> modelClass) {
		return first(modelClass, null);
	}

	public static <T extends Model<T, ?, U, ?>, U> T first(Class<T> modelClass,
			U conditions) {
		return ActiveBeansUtils.first(defaultDs, modelClass, conditions);
	}

	public static <T extends Model<T, ?, U, ?>, U> T last(Class<T> modelClass) {
		return last(modelClass, null);
	}

	public static <T extends Model<T, ?, U, ?>, U> T last(Class<T> modelClass,
			U conditions) {
		return ActiveBeansUtils.last(defaultDs, modelClass, conditions);
	}

	public static <T extends Model<T, ?, U, V>, U, V extends Models<T, ?, U, V>> V all(
			final Class<T> modelClass) {
		return all(modelClass, null);
	}

	public static <T extends Model<T, ?, U, V>, U, V extends Models<T, ?, U, V>> V all(
			Class<T> modelClass, U conditions) {
		return ActiveBeansUtils.all(defaultDs, modelClass, conditions);
	}

}