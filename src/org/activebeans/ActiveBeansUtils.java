package org.activebeans;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.sql.DataSource;

public final class ActiveBeansUtils {
	
	private ActiveBeansUtils() {

	}
	
	public static <T extends Model<?, ?, ?, ?>> int insert(
			DataSource ds, Class<T> activeClass, T model){
		return insert(ds, activeClass, model, null);
	}
	
	private static List<Object> insertBelongsToAssociation(@SuppressWarnings("rawtypes") Model model, Association assoc){
		AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
		ActiveIntrospector clazzIntro = new ActiveIntrospector(assoc.with());
		@SuppressWarnings("rawtypes")
		Model obj = (Model) handler.get(assoc);
		List<Property> keys = clazzIntro.keys();
		List<Object> keyVals = new ArrayList<Object>();
		if(obj == null){
			keyVals.addAll(Collections.nCopies(keys.size(), null));
		}else{
			ActiveDelegate activeHandler = (ActiveDelegate)((ProxyObject)obj).getHandler();
			AttributeMethodHandler attrHandler = activeHandler.attrHandler();
			boolean saved = false;
			for (Property key : keys) {
				saved |= attrHandler.get(key) != null; 
			}
			if(!saved){
				obj.save();
			}
			for (Property key : keys) {
				keyVals.add(attrHandler.get(key)); 
			}
		}
		return keyVals;
	}
	
	private static <T extends Model<?, ?, ?, ?>> void insertHasManyAssociation(Class<T> activeClass, T model, Association assoc){
		Models<?, ?, ?, ?> objs = (Models<?, ?, ?, ?>) ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler().get(assoc);
		if(objs != null){
			for(@SuppressWarnings("rawtypes") Model obj : objs){
				obj.save();
			}
		}
	}
	
	public static <T extends Model<?, ?, ?, ?>> int insert(
			DataSource ds, Class<T> activeClass, T model, GeneratedKeysMapHandler generatedKeys){
		List<Object> params = new ArrayList<Object>();
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		final List<Property> generatedkeys = new ArrayList<Property>();
		final AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
		for (Property prop : intro.properties()) {
			if(prop.autoIncrement()){
				generatedkeys.add(prop);
			}else{
				params.add(handler.get(prop));
			}
		}
		for (Association assoc : intro.belongsTos()) {
			params.addAll(insertBelongsToAssociation(model, assoc));
		}
		final Map<Property, Object> generatedKeyMap = new LinkedHashMap<Property, Object>();
		int result = executePreparedSql(
			ds,
			new ResultSetHandler() {
				@Override
				public void handle(ResultSet keys) throws SQLException {
					if(keys.next()){
						for (int i=0; i < generatedkeys.size(); i++) {
							Property prop = generatedkeys.get(i);
							Object val = keys.getObject(i + 1);
							generatedKeyMap.put(prop, val);
							handler.set(prop, val);
							i++;
						}
					}
				}
			},
			new ActiveMigration(activeClass, ds).table().insertStatement(), 
			params
		);
		if(generatedKeys != null){
			generatedKeys.handle(generatedKeyMap);
		}
		for (Association assoc : intro.hasManys()) {
			insertHasManyAssociation(activeClass, model, assoc);
		}
		return result;
	}
	
	public static <T extends Model<?, ?, ?, ?>> T get(
			final DataSource ds, final Class<T> activeClass, List<?> keys){
		final List<T> resultList = new ArrayList<T>();
		executePreparedSqlForResult(ds, 
			new ResultSetHandler() {
				@Override
				public void handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						resultList.add(toModel(ds, rs, activeClass));
					}
				}
			}, 
			new ActiveMigration(activeClass, ds).table().selectStatement(), 
			keys
		);
		return resultList.isEmpty()?null:resultList.get(0);
	}
	
	public static <T extends Model<?, ?, ?, ?>> int update(DataSource ds, 
			Class<T> activeClass, T model){
		List<Object> params = new ArrayList<Object>();
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		final List<Object> keys = new ArrayList<Object>();
		AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
		for (Property prop : intro.properties()) {
			Object val = handler.get(prop);
			if(prop.key()){
				keys.add(val);
			}else{
				params.add(val);
			}
		}
		for (Association assoc : intro.belongsTos()) {
			params.addAll(insertBelongsToAssociation(model, assoc));
		}
		params.addAll(keys);
		int result = executePreparedSql(
			ds,
			new ActiveMigration(activeClass, ds).table().updateStatement(), 
			params
		);
		return result;
	}
	
	public static <T> int update(DataSource ds, 
			Class<? extends Model<?, T, ?, ?>> activeClass, T attrs){
		OptionsMethodHandler handler = (OptionsMethodHandler) ((ProxyObject)attrs).getHandler();
		int result = executePreparedSql(
			ds,
			new ActiveMigration(activeClass, ds).table().updateAllStatement(attrs), 
			handler.properties().values().toArray()
		);
		return result;
	}
	
	public static <T extends Model<?, ?, ?, ?>> int delete(DataSource ds, 
			Class<T> activeClass, T model){
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		final List<Object> keys = new ArrayList<Object>();
		AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
		for (Property prop : intro.keys()) {
			keys.add(handler.get(prop));
		}
		int result = executePreparedSql(
			ds,
			new ActiveMigration(activeClass, ds).table().deleteStatement(), 
			keys
		);
		return result;
	}
	
	public static <T extends Model<?, ?, ?, ?>> int delete(DataSource ds, Class<T> activeClass){
		return executeSql(
			ds,
			new ActiveMigration(activeClass, ds).table().deleteAllStatement()
		)[0];
	}
	
	public static <T extends Model<?, ?, ?, ?>> T model(Class<T> activeClass){
		ActiveDelegate delegate = new ActiveDelegate(activeClass);
		ProxyFactory f = new ProxyFactory();
		f.setSuperclass(activeClass);
		f.setFilter(delegate);
		try {
			return activeClass.cast(f.create(new Class[0], new Object[0], delegate));
		} catch (Exception e) {
			throw new ActiveBeansException(e);
		}
	}
	
	public static <T extends Model<T, ?, U, ?>, U> U conditions(Class<T> activeClass){
		return conditions(activeClass, null, Collections.emptyList());
	}
	
	public static <T extends Model<T, ?, U, ?>, U> U conditions(Class<T> activeClass, 
			Class<? extends Model<?, ?, ?, ?>> associatedClass, Object... associatedKeys){
		return conditions(activeClass, associatedClass, Arrays.asList(associatedKeys));
	}
	
	public static <T extends Model<T, ?, U, ?>, U> U conditions(Class<T> activeClass, 
			Class<? extends Model<?, ?, ?, ?>> associatedClass, List<Object> associatedKeys){
		ProxyFactory f = new ProxyFactory();
		@SuppressWarnings("unchecked")
		Class<U> condsInterface = (Class<U>) new ActiveIntrospector(activeClass).conditionsInterface();
		f.setInterfaces(new Class[]{condsInterface});
		f.setFilter(new ConditionsMethodFilter(activeClass));
		try {
			return condsInterface.cast(f.create(new Class[0], new Object[0],
				new ConditionsMethodHandler(activeClass, associatedClass, associatedKeys)));
		} catch (Exception e) {
			throw new ActiveBeansException(e);
		}
	}
	
	public static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U all(
			DataSource ds, final Class<T> activeClass, Object conds) {
		return all(ds, activeClass, null, null, conds);
	}
	
	private static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U all(
			final DataSource ds, final Class<T> activeClass, Association assoc, T assocModel, final Object conds) {
		return models(activeClass, new ModelsMethodHandler(activeClass, assoc, assocModel, conds){
			@Override
			protected Set<Object> query(Object conditions) {
				final Set<Object> data = new LinkedHashSet<Object>();
				ResultSetHandler rsHandler = new ResultSetHandler() {
					@Override
					public void handle(ResultSet rs) throws SQLException {
						while(rs.next()){
							data.add(toModel(ds, rs, activeClass));
						}
					}
				};
				Table table = new ActiveMigration(activeClass, ds).table();
				if(conditions == null){
					executeSqlForResult(ds, rsHandler, table.selectAllWithOrderStatement());
				}else{
					ConditionsMethodHandler condHandler = (ConditionsMethodHandler) 
						((ProxyObject)conditions).getHandler();
					executePreparedSqlForResult(ds, rsHandler, 
						table.selectAllWithOrderStatement(conditions), condHandler.params());
				}
				return data;
			}
		});
	}
	
	public static <T extends Model<?, ?, U, ?>, U> T first(final DataSource ds, 
			final Class<T> activeClass, U conds){
		final List<T> resultList = new ArrayList<T>();
		final ResultSetHandler rsHandler = new ResultSetHandler() {
			@Override
			public void handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					resultList.add(toModel(ds, rs, activeClass));
				}
			}
		};
		Table table = new ActiveMigration(activeClass, ds).table();
		if(conds == null){
			executeSqlForResult(ds, rsHandler, table.selectFirstStatement());
		}else{
			ConditionsMethodHandler condHandler = (ConditionsMethodHandler) 
				((ProxyObject)conds).getHandler();
			executePreparedSqlForResult(ds, rsHandler, 
				table.selectFirstStatement(conds), condHandler.params());
		}
		return resultList.isEmpty()? null:resultList.get(0);
	}
	
	public static <T extends Model<?, ?, U, ?>, U> T last(final DataSource ds, 
			final Class<T> activeClass, U conds){
		final List<T> resultList = new ArrayList<T>();
		final ResultSetHandler rsHandler = new ResultSetHandler() {
			@Override
			public void handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					resultList.add(toModel(ds, rs, activeClass));
				}
			}
		};
		Table table = new ActiveMigration(activeClass, ds).table();
		if(conds == null){
			executeSqlForResult(ds, rsHandler, table.selectLastStatement());
		}else{
			ConditionsMethodHandler condHandler = (ConditionsMethodHandler) 
				((ProxyObject)conds).getHandler();
			executePreparedSqlForResult(ds, rsHandler, 
				table.selectLastStatement(conds), condHandler.params());
		}
		return resultList.isEmpty()? null:resultList.get(0);
	}
	
	private static <T extends Model<?, ?, ?, ?>> T toModel(final DataSource ds, ResultSet rs, final Class<T> activeClass){
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		T model = ActiveBeansUtils.model(activeClass);
		AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)model).getHandler()).attrHandler();
		for (Property p : intro.properties()) {
			try {
				handler.set(p, rs.getObject(camelCaseToUnderscore(p.name())));
			} catch (SQLException e) {
				throw new ActiveBeansException(e);
			}
		}
		for (Association assoc : intro.belongsTos()) {
			Class<? extends Model<?, ?, ?, ? extends Models<?, ?, ?, ?>>> assocClass = assoc.with();
			List<Object> keys = new ArrayList<Object>();
			for(String k : associationKeys(assocClass)){
				try {
					keys.add(rs.getObject(k));
				} catch (SQLException e) {
					throw new ActiveBeansException(e);
				}
			}
			handler.set(assoc, get(ds, assocClass, keys));
		}
		for (Association assoc : intro.hasManys()) {
			List<Object> keys = new ArrayList<Object>();
			for (Property k : intro.keys()) {
				try {
					keys.add(rs.getObject(camelCaseToUnderscore(k.name())));
				} catch (SQLException e) {
					throw new ActiveBeansException(e);
				}
			}
			@SuppressWarnings("rawtypes")
			Class assocClass = assoc.with();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Models models = all(ds, assocClass, new ActiveIntrospector(assocClass).belongsTo(activeClass), 
				model, conditions(assocClass, activeClass, keys));
			handler.set(assoc, models);
		}
		return model;
	}
	
	public static List<String> keys(Class<? extends Model<?, ?, ?, ?>> clazz){
		List<String> keys = new ArrayList<String>();
		for (Property k : new ActiveIntrospector(clazz).keys()) {
				keys.add(camelCaseToUnderscore(k.name()));
		}
		return keys;
	}
	
	public static List<String> associationKeys(Class<? extends Model<?, ?, ?, ?>> clazz){
		List<String> keys = new ArrayList<String>();
		for (Property k : new ActiveIntrospector(clazz).keys()) {
				keys.add(associationColumnPrefix(clazz) + camelCaseToUnderscore(k.name()));
		}
		return keys;
	}
	
	public static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U models(Class<T> activeClass) {
		return models(activeClass, null, null);
	}
	
	public static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U models(Class<T> activeClass, Association assoc, Model<?, ?, ?, ?> assocModel) {
		return models(activeClass, new ModelsMethodHandler(activeClass, assoc, assocModel));
	}
	
	private static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U models(
			final Class<T> activeClass, MethodHandler handler) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		ProxyFactory f = new ProxyFactory();
		@SuppressWarnings("unchecked")
		final Class<U> modelsInterface = (Class<U>) intro.modelsInterface();
		f.setInterfaces(new Class[] { modelsInterface });
		f.setFilter(new MethodFilter() {
			@Override
			public boolean isHandled(Method m) {
				return !(m.getDeclaringClass().equals(Models.class)
					&& Arrays.asList("and", "attrs", "reverse")
					.contains(m.getName()));
			}
		});
		try {
			return modelsInterface.cast(f.create(new Class[0],
				new Object[0], handler));
		} catch (Throwable t) {
			throw new ActiveBeansException(t);
		}
	}

	public static Map<String, Class<?>> classNameMap(Class<?>[] classes) {
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		for (Class<?> i : classes) {
			map.put(i.getName(), i);
		}
		return map;
	}

	public static String camelCaseToUnderscore(String input) {
		List<String> toks = new ArrayList<String>();
		for (String tok : splitByCharacterType(input, true)) {
			toks.add(tok.toLowerCase());
		}
		return join(toks.toArray(), "_", 0, toks.size());
	}
	
	@SuppressWarnings("unused")
	private static String underscoreToCamelCase(String input){
		List<String> toks = new ArrayList<String>();
		for(String tok : input.split("_")){
			toks.add(Character.toTitleCase(tok.charAt(0)) + tok.substring(1));
		}
		return Introspector.decapitalize(join(toks.toArray(), "", 0, toks.size()));
	}
	
	public static String associationColumnPrefix(Class<? extends Model<?, ?, ?, ?>> clazz){
		return camelCaseToUnderscore(clazz.getSimpleName()) + "_";
	}

	public static void close(ResultSet... rsArray) {
		for (ResultSet rs : rsArray) {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static void close(Statement... stmtArray) {
		for (Statement stmt : stmtArray) {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static void close(Connection... connArray) {
		for (Connection conn : connArray) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static int[] executeSql(DataSource ds, List<String> stmts) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			for (String s : stmts) {
				stmt.addBatch(s);
			}
			return stmt.executeBatch();
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			close(stmt);
			close(conn);
		}
	}

	public static int[] executeSql(DataSource ds, String... stmts) {
		return executeSql(ds, Arrays.asList(stmts));
	}
	
	public static void executeSqlForResult(DataSource ds, ResultSetHandler handler, String sql) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			handler.handle(rs);
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
	}
	
	public static int executePreparedSql(DataSource ds, String sql, List<?> params) {
		return executePreparedSql(ds, null, sql, params.toArray());
	}
	
	public static int executePreparedSql(DataSource ds, String sql, Object... params) {
		return executePreparedSql(ds, null, sql, params);
	}
	
	public static int executePreparedSql(DataSource ds, ResultSetHandler generatedKeysHandler, String sql, List<?> params) {
		return executePreparedSql(ds, generatedKeysHandler, sql, params.toArray());
	}
	
	public static int executePreparedSql(DataSource ds, ResultSetHandler generatedKeysHandler, String sql, Object... params) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean returnGeneratedKeys = generatedKeysHandler != null; 
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql, 
				returnGeneratedKeys?Statement.RETURN_GENERATED_KEYS:Statement.NO_GENERATED_KEYS);
			for (int i=0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			int result = stmt.executeUpdate();
			if(returnGeneratedKeys){
				rs = stmt.getGeneratedKeys();
				generatedKeysHandler.handle(rs);
			}
			return result;
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
	}
	
	public static void executePreparedSqlForResult(DataSource ds, ResultSetHandler handler, String sql, List<?> params) {
		executePreparedSqlForResult(ds, handler, sql, params.toArray());
	}
	
	public static void executePreparedSqlForResult(DataSource ds, ResultSetHandler handler, String sql, Object... params) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null; 
		try {
			conn = ds.getConnection();
			stmt = conn.prepareStatement(sql);
			for (int i=0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			rs = stmt.executeQuery();
			handler.handle(rs);
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
	}
	
	public static Object defaultValue(Class<?> type) {
		Object rtn = null;
		if (Boolean.TYPE.equals(type)) {
			rtn = false;
		} else if (Character.TYPE.equals(type)) {
			rtn = '\u0000';
		} else if (Byte.TYPE.equals(type) || Short.TYPE.equals(type)
				|| Integer.TYPE.equals(type)) {
			rtn = 0;
		} else if (Long.TYPE.equals(type)) {
			rtn = 0L;
		} else if (Float.TYPE.equals(type)) {
			rtn = 0.0f;
		} else if (Double.TYPE.equals(type)) {
			rtn = 0.0d;
		}
		return rtn;
	}

	private static String[] splitByCharacterType(String str, boolean camelCase) {
		if (str == null) {
			return null;
		}
		if (str.length() == 0) {
			return new String[0];
		}
		char[] c = str.toCharArray();
		List<String> list = new ArrayList<String>();
		int tokenStart = 0;
		int currentType = Character.getType(c[tokenStart]);
		for (int pos = tokenStart + 1; pos < c.length; pos++) {
			int type = Character.getType(c[pos]);
			if (type == currentType) {
				continue;
			}
			if (camelCase && type == Character.LOWERCASE_LETTER
					&& currentType == Character.UPPERCASE_LETTER) {
				int newTokenStart = pos - 1;
				if (newTokenStart != tokenStart) {
					list.add(new String(c, tokenStart, newTokenStart
							- tokenStart));
					tokenStart = newTokenStart;
				}
			} else {
				list.add(new String(c, tokenStart, pos - tokenStart));
				tokenStart = pos;
			}
			currentType = type;
		}
		list.add(new String(c, tokenStart, c.length - tokenStart));
		return list.toArray(new String[list.size()]);
	}

	private static String join(Object[] array, String separator,
			int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		if (separator == null) {
			separator = "";
		}

		// endIndex - startIndex > 0: Len = NofStrings *(len(firstString) +
		// len(separator))
		// (Assuming that all Strings are roughly equally long)
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return "";
		}

		bufSize *= ((array[startIndex] == null ? 16 : array[startIndex]
				.toString().length()) + separator.length());

		StringBuilder buf = new StringBuilder(bufSize);

		for (int i = startIndex; i < endIndex; i++) {
			if (i > startIndex) {
				buf.append(separator);
			}
			if (array[i] != null) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}

}
