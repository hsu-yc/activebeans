package org.activebeans.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DataSourceTest {

	@Test
	public void createDataSource() throws SQLException {
		DataSource ds = getDataSource();
		Connection conn = null;
		try {
			assertNotNull(conn = ds.getConnection());
			assertTrue(conn.isValid(0));
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	public static DataSource getDataSource() {
		MysqlDataSource ds = new MysqlDataSource();
		ds.setUser("root");
		ds.setPassword("root");
		ds.setServerName("localhost");
		ds.setDatabaseName("activebeans");
		return ds;
	}

}
