package org.activebeans;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActiveTypeMapper {

	private static Map<Class<?>, Integer> typeMap = new HashMap<Class<?>, Integer>();

	static {
		typeMap.put(String.class, Types.VARCHAR);
		typeMap.put(BigDecimal.class, Types.NUMERIC);
		typeMap.put(boolean.class, Types.BIT);
		typeMap.put(Boolean.class, Types.BIT);
		typeMap.put(byte.class, Types.TINYINT);
		typeMap.put(Byte.class, Types.TINYINT);
		typeMap.put(short.class, Types.SMALLINT);
		typeMap.put(Short.class, Types.SMALLINT);
		typeMap.put(int.class, Types.INTEGER);
		typeMap.put(Integer.class, Types.INTEGER);
		typeMap.put(long.class, Types.BIGINT);
		typeMap.put(Long.class, Types.BIGINT);
		typeMap.put(float.class, Types.REAL);
		typeMap.put(Float.class, Types.REAL);
		typeMap.put(double.class, Types.DOUBLE);
		typeMap.put(Double.class, Types.DOUBLE);
		typeMap.put(byte[].class, Types.BINARY);
		typeMap.put(java.sql.Date.class, Types.DATE);
		typeMap.put(java.sql.Time.class, Types.TIME);
		typeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
		// typeMap.put(java.sql.Clob.class, Types.CLOB);
		// typeMap.put(java.sql.Blob.class, Types.BLOB);
		// typeMap.put(java.sql.Array.class, Types.ARRAY);
		// typeMap.put(java.sql.Struct.class, Types.STRUCT);
		// typeMap.put(java.sql.Ref.class, Types.REF);
		// typeMap.put(java.net.URL.class, Types.DATALINK);
		// typeMap.put(Class.class, Types.JAVA_OBJECT);
		// typeMap.put(java.sql.RowId.class, Types.ROWID);
		// typeMap.put(java.sql.NClob.class, Types.NCLOB);
		// typeMap.put(java.sql.SQLXML.class, Types.SQLXML);
	}

	private ActiveTypeMapper() {

	}

	public static Set<Class<?>> javaTypes() {
		return Collections.unmodifiableSet(typeMap.keySet());
	}

	public static Collection<Integer> jdbcTypes() {
		return Collections.unmodifiableCollection(typeMap.values());
	}

	public static int toJdbcType(Class<?> javaType) {
		return typeMap.get(javaType);
	}

}
