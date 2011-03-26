package org.activebeans;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import javax.sql.DataSource;

public final class ActiveBeansUtils {

	private ActiveBeansUtils() {

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
	
	public static <T extends Model<?, ?, ?, ?>, U extends Models<?, ?, ?, ?>> U models(final Class<T> activeClass) {
		ActiveIntrospector intro = new ActiveIntrospector(activeClass);
		ProxyFactory f = new ProxyFactory();
		@SuppressWarnings("unchecked")
		final Class<U> modelsInterface = (Class<U>) intro.modelsInterface();
		f.setInterfaces(new Class[] { modelsInterface });
		f.setFilter(new MethodFilter() {
			@Override
			public boolean isHandled(Method m) {
				return !(m.getDeclaringClass().equals(Models.class)
					&& Arrays.asList(new String[] { "all", "attrs" })
					.contains(m.getName()));
			}
		});
		try {
			return modelsInterface.cast(f.create(new Class[0],
				new Object[0], new MethodHandler() {
					private List<Object> objs = new ArrayList<Object>();
					@Override
					public Object invoke(Object self, Method method,
							Method proceed, Object[] args)
							throws Throwable {
						Object rtn = null; 
						if(method.equals(Iterable.class.getMethod("iterator"))){
							rtn = objs.iterator();
						} else if(method.equals(modelsInterface.getMethod("add", activeClass)) ||
								method.equals(Models.class.getMethod("add", Model.class))){
							objs.add(args[0]);
							rtn = self;
						}else {
							rtn = ActiveBeansUtils.defaultValue(method.getReturnType());
						}
						return rtn;
					}
				}));
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
			ActiveBeansUtils.close(stmt);
			ActiveBeansUtils.close(conn);
		}
	}

	public static int[] executeSql(DataSource ds, String... stmts) {
		return executeSql(ds, Arrays.asList(stmts));
	}
	
	public static int executePreparedSql(DataSource ds, String sql, Object... params) {
		return executePreparedSql(ds, null, sql, params);
	}
	
	public static int executePreparedSql(DataSource ds, GeneratedKeysHandler handler, String sql, Object... params) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean returnGeneratedKeys = handler != null; 
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
				handler.handle(rs);
			}
			return result;
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			ActiveBeansUtils.close(rs);
			ActiveBeansUtils.close(stmt);
			ActiveBeansUtils.close(conn);
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
