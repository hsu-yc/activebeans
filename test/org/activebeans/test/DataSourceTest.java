package org.activebeans.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveTypeMapper;
import org.activebeans.DataSourceIntrospector;
import org.junit.Before;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataSourceTest {

	private DataSourceIntrospector intro;

	@Before
	public void init() {
		intro = new DataSourceIntrospector(getDataSource());
	}

	@Test
	public void createDataSource() throws SQLException {
		doDataSourceBlock(new DataSourceBlock() {
			@Override
			public void execute(Connection conn) throws SQLException {
				assertNotNull(conn);
				assertTrue(conn.isValid(0));
			}
		});
	}

	@Test
	public void sqlTypeNames() {
		Set<String> sqlTypeNames = new HashSet<String>();
		for (String sqlTypeName : intro.sqlTypeNames()) {
			sqlTypeNames.add(sqlTypeName.toLowerCase());
		}
		for (String sqlTypeName : ActiveTypeMapper.sqlTypeNames()) {
			assertTrue(sqlTypeNames.contains(sqlTypeName.toLowerCase()));
		}
	}

	static DataSource getDataSource() {
		MysqlDataSource ds = new MysqlDataSource();
		ds.setUser("root");
		ds.setPassword("root");
		ds.setServerName("localhost");
		ds.setDatabaseName("activebeans");
		return ds;
	}

	static void doDataSourceBlock(DataSourceBlock tpl) throws SQLException {
		DataSource ds = getDataSource();
		Connection conn = null;
		try {
			tpl.execute(conn = ds.getConnection());
		} finally {
			ActiveBeansUtils.close(conn);
		}
	}

	interface DataSourceBlock {

		void execute(Connection conn) throws SQLException;

	}

}
