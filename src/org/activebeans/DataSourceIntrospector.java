package org.activebeans;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

public class DataSourceIntrospector {

	private Map<Integer, String> typeNameMap = new HashMap<Integer, String>();

	public DataSourceIntrospector(DataSource ds) {
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTypeInfo();
			while (rs.next()) {
				typeNameMap.put(rs.getInt("DATA_TYPE"),
						rs.getString("TYPE_NAME"));
			}
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			ActiveBeansUtils.close(rs, conn);
		}
	}

	public Set<Integer> jdbcTypes() {
		return Collections.unmodifiableSet(typeNameMap.keySet());
	}

	public String typeName(int type) {
		return typeNameMap.get(type);
	}

}
