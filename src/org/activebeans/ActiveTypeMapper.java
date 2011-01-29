package org.activebeans;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActiveTypeMapper {

	private static Map<Class<?>, Integer> jdbcTypeMap = new HashMap<Class<?>, Integer>();

	static {
		jdbcTypeMap.put(String.class, Types.VARCHAR);
		jdbcTypeMap.put(BigDecimal.class, Types.NUMERIC);
		jdbcTypeMap.put(boolean.class, Types.BIT);
		jdbcTypeMap.put(Boolean.class, Types.BIT);
		jdbcTypeMap.put(byte.class, Types.TINYINT);
		jdbcTypeMap.put(Byte.class, Types.TINYINT);
		jdbcTypeMap.put(short.class, Types.SMALLINT);
		jdbcTypeMap.put(Short.class, Types.SMALLINT);
		jdbcTypeMap.put(int.class, Types.INTEGER);
		jdbcTypeMap.put(Integer.class, Types.INTEGER);
		jdbcTypeMap.put(long.class, Types.BIGINT);
		jdbcTypeMap.put(Long.class, Types.BIGINT);
		jdbcTypeMap.put(float.class, Types.REAL);
		jdbcTypeMap.put(Float.class, Types.REAL);
		jdbcTypeMap.put(double.class, Types.DOUBLE);
		jdbcTypeMap.put(Double.class, Types.DOUBLE);
		jdbcTypeMap.put(byte[].class, Types.BINARY);
		jdbcTypeMap.put(java.sql.Date.class, Types.DATE);
		jdbcTypeMap.put(java.sql.Time.class, Types.TIME);
		jdbcTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
		// jdbcTypeMap.put(java.sql.Clob.class, Types.CLOB);
		// jdbcTypeMap.put(java.sql.Blob.class, Types.BLOB);
		// jdbcTypeMap.put(java.sql.Array.class, Types.ARRAY);
		// jdbcTypeMap.put(java.sql.Struct.class, Types.STRUCT);
		// jdbcTypeMap.put(java.sql.Ref.class, Types.REF);
		// jdbcTypeMap.put(java.net.URL.class, Types.DATALINK);
		// jdbcTypeMap.put(Class.class, Types.JAVA_OBJECT);
		// jdbcTypeMap.put(java.sql.RowId.class, Types.ROWID);
		// jdbcTypeMap.put(java.sql.NClob.class, Types.NCLOB);
		// jdbcTypeMap.put(java.sql.SQLXML.class, Types.SQLXML);
	}

	private static Map<Integer, String> sqlTypeNameMap = new HashMap<Integer, String>();

	static {
		for (Field field : Types.class.getFields()) {
			try {
				sqlTypeNameMap.put(field.getInt(null), field.getName());
			} catch (IllegalAccessException e) {
				throw new ActiveBeansException(e);
			}
		}
	}

	private ActiveTypeMapper() {

	}

	public static Set<Class<?>> javaTypes() {
		return Collections.unmodifiableSet(jdbcTypeMap.keySet());
	}

	public static Collection<Integer> jdbcTypes() {
		return Collections.unmodifiableCollection(jdbcTypeMap.values());
	}

	public static int toJdbcType(Class<?> javaType) {
		return jdbcTypeMap.get(javaType);
	}

	public static String sqlTypeName(int jdbcType) {
		return sqlTypeNameMap.get(jdbcType);
	}

	public static String sqlTypeName(Class<?> javaType) {
		return sqlTypeName(toJdbcType(javaType));
	}

}
