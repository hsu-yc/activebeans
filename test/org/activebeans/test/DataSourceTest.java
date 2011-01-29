package org.activebeans.test;

import java.sql.Connection;
import java.sql.SQLException;
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
	public void jdbcTypes() {
		assertTrue(intro.jdbcTypes().containsAll(ActiveTypeMapper.jdbcTypes()));
	}

	@Test
	public void sqlTypeNames() {
		Set<String> sqlTypeNames = intro.sqlTypeNames();
		for (Integer jdbcType : ActiveTypeMapper.jdbcTypes()) {
			assertTrue(sqlTypeNames.contains(ActiveTypeMapper
					.toSqlTypeName(jdbcType)));
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

	public static void doDataSourceBlock(DataSourceBlock tpl)
			throws SQLException {
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
