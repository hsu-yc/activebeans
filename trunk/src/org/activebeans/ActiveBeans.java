package org.activebeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.ProxyFactory;

import javax.sql.DataSource;

public class ActiveBeans {

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

	public static <T extends Model<T, ?>> void migrate(Class<T> activeClass) {
		Table table = ActiveMigration.of(activeClass, defaultDs).table();
		ActiveBeansUtils.executeSql(defaultDs, table.dropStatement(),
				table.createStatment());
	}

	public static void autoMigrate() {
		List<String> stmts = new ArrayList<String>();
		for (@SuppressWarnings("rawtypes")
		Class<? extends Model> clazz : ActiveIntrospector.activeClasses()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			ActiveMigration<? extends Model> migr = ActiveMigration.of(clazz,
					defaultDs);
			Table table = migr.table();
			stmts.add(table.dropStatement());
			stmts.add(table.createStatment());
		}
		ActiveBeansUtils.executeSql(defaultDs, stmts);
	}

	public static <T extends Model<T, ?>> void upgrade(Class<T> activeClass) {
		String alterStmt = ActiveMigration.of(activeClass, defaultDs)
				.alterStatement();
		if (alterStmt != null) {
			ActiveBeansUtils.executeSql(defaultDs, alterStmt);
		}
	}

	public static void autoUpgrade() {
		List<String> stmts = new ArrayList<String>();
		for (@SuppressWarnings("rawtypes")
		Class<? extends Model> clazz : ActiveIntrospector.activeClasses()) {
			@SuppressWarnings("unchecked")
			String alterStmt = ActiveMigration.of(clazz, defaultDs)
					.alterStatement();
			if (alterStmt != null) {
				stmts.add(alterStmt);
			}
		}
		ActiveBeansUtils.executeSql(defaultDs, stmts);
	}
	
	public static <T extends Model<T, U>, U> U options(Class<T> activeClass){
		return null;
	}

	public static <T extends Model<T, ?>> T build(Class<T> activeClass) {
		ProxyFactory f = new ProxyFactory();
		f.setSuperclass(activeClass);
		f.setFilter(ActiveMethodFilter.of(activeClass));
		try {
			return activeClass.cast(f.create(new Class[0], new Object[0],
					ActiveMethodHandler.of(activeClass)));
		} catch (Exception e) {
			throw new ActiveBeansException(e);
		}
	}

	public static <T extends Model<T, U>, U> T build(Class<T> modelClass,
			U attrs) {
		return null;
	}

	public static <T extends Model<T, U>, U> T create(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model<T, U>, U> T create(Class<T> modelClass,
			U attrs) {
		return null;
	}

	public static boolean destroy(Class<? extends Model<?, ?>> modelClass) {
		return false;
	}

	public static <T> boolean update(Class<? extends Model<?, T>> modelClass,
			T attrs) {
		return false;
	}

	public static <T extends Model<T, ?>> T get(Class<T> modelClass, Object key,
			Object... keys) {
		return null;
	}

	public static <T extends Model<T, ?>> T first(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model<T, ?>> T first(Class<T> modelClass,
			Map<String, ?> conditions) {
		return null;
	}

	public static <T extends Model<T, ?>> T last(Class<T> modelClass) {
		return null;
	}

	public static <T extends Model<T, ?>> T last(Class<T> modelClass,
			Map<String, ?> conditions) {
		return null;
	}

	public static <T extends Models<U, ?>, U extends Model<U, ?>> T all(
			Class<T> modelsClass) {
		return null;
	}

	public static <T extends Models<U, ?>, U extends Model<U, ?>> T all(
			Class<T> modelsClass, Map<String, ?> conditions) {
		return null;
	}

}