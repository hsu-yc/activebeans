package org.activebeans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public final class ActiveBeansUtils {

	private ActiveBeansUtils() {

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

	public static void executeSql(DataSource ds, List<String> stmts) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = ds.getConnection();
			stmt = conn.createStatement();
			for (String s : stmts) {
				stmt.addBatch(s);
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			ActiveBeansUtils.close(stmt);
			ActiveBeansUtils.close(conn);
		}
	}

	public static void executeSql(DataSource ds, String... stmts) {
		executeSql(ds, Arrays.asList(stmts));
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
