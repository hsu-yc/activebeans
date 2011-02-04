package org.activebeans;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActiveTypeMapper {

	private static int varcharLength = 50;

	private static Map<Class<?>, String> sqlTypeNameMap = new HashMap<Class<?>, String>();

	static {
		sqlTypeNameMap.put(String.class, "varchar");
		sqlTypeNameMap.put(BigDecimal.class, "decimal");
		sqlTypeNameMap.put(boolean.class, "bool");
		sqlTypeNameMap.put(Boolean.class, "bool");
		sqlTypeNameMap.put(byte.class, "tinyint");
		sqlTypeNameMap.put(Byte.class, "tinyint");
		sqlTypeNameMap.put(short.class, "smallint");
		sqlTypeNameMap.put(Short.class, "smallint");
		sqlTypeNameMap.put(int.class, "int");
		sqlTypeNameMap.put(Integer.class, "int");
		sqlTypeNameMap.put(long.class, "bigint");
		sqlTypeNameMap.put(Long.class, "bigint");
		sqlTypeNameMap.put(float.class, "float");
		sqlTypeNameMap.put(Float.class, "float");
		sqlTypeNameMap.put(double.class, "double");
		sqlTypeNameMap.put(Double.class, "double");
		sqlTypeNameMap.put(byte[].class, "blob");
		sqlTypeNameMap.put(Byte[].class, "blob");
		sqlTypeNameMap.put(java.util.Date.class, "date");
		sqlTypeNameMap.put(java.sql.Date.class, "date");
		sqlTypeNameMap.put(java.sql.Time.class, "datetime");
		sqlTypeNameMap.put(java.sql.Timestamp.class, "timestamp");
		sqlTypeNameMap.put(InputStream.class, "blob");
		sqlTypeNameMap.put(Reader.class, "text");
	}

	private ActiveTypeMapper() {

	}

	public static Set<Class<?>> javaTypes() {
		return Collections.unmodifiableSet(sqlTypeNameMap.keySet());
	}

	public static Set<String> sqlTypeNames() {
		return new HashSet<String>(sqlTypeNameMap.values());
	}

	public static String sqlTypeName(Class<?> javaType) {
		return sqlTypeNameMap.get(javaType);
	}

	public static int varcharLength() {
		return varcharLength;
	}

	public static void varcharLength(int val) {
		varcharLength = val;
	}

}
