package org.activebeans;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

public class DataSourceIntrospector {

	private Map<String, Integer> jdbcTypeMap = new HashMap<String, Integer>();

	public DataSourceIntrospector(DataSource ds) {
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = ds.getConnection();
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTypeInfo();
			while (rs.next()) {
				jdbcTypeMap.put(rs.getString("TYPE_NAME"),
						rs.getInt("DATA_TYPE"));
			}
		} catch (SQLException e) {
			throw new ActiveBeansException(e);
		} finally {
			ActiveBeansUtils.close(rs, conn);
		}
	}

	public Set<String> sqlTypeNames() {
		return Collections.unmodifiableSet(jdbcTypeMap.keySet());
	}

	public Collection<Integer> jdbcTypes() {
		return Collections.unmodifiableCollection(jdbcTypeMap.values());
	}

	public int jdbcType(String sqlTypeName) {
		return jdbcTypeMap.get(sqlTypeName);
	}

}
