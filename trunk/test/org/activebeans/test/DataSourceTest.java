package org.activebeans.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveTypeMapper;
import org.activebeans.DataSourceIntrospector;
import org.junit.Before;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataSourceTest {

	private DataSource ds;

	private DataSourceIntrospector intro;

	@Before
	public void init() {
		ds = getDataSource();
		intro = new DataSourceIntrospector(ds);
	}

	@Test
	public void createDataSource() throws SQLException {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			assertNotNull(conn);
			assertTrue(conn.isValid(0));
		} finally {
			ActiveBeansUtils.close(conn);
		}
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

	@Test
	public void tables() throws SQLException {
		String table = "tables";
		Connection conn = null;
		Statement create = null;
		Statement drop = null;
		try {
			conn = ds.getConnection();
			create = conn.createStatement();
			create.execute("create table if not exists " + table + "(id int)");
			assertTrue(intro.tables().contains(table));
			create.execute("drop table if exists " + table);
			assertFalse(intro.tables().contains(table));
		} finally {
			ActiveBeansUtils.close(create, drop);
			ActiveBeansUtils.close(conn);
		}
	}

	@Test
	public void columns() throws SQLException {
		String table = "tables";
		String id = "id";
		String name = "name";
		Connection conn = null;
		Statement create = null;
		Statement drop = null;
		try {
			conn = ds.getConnection();
			create = conn.createStatement();
			create.execute("create table if not exists " + table + "(" + id
					+ " int)");
			List<String> cols = intro.columns(table);
			assertTrue(cols.contains(id));
			assertFalse(cols.contains(name));
			create.execute("drop table if exists " + table);
		} finally {
			ActiveBeansUtils.close(create, drop);
			ActiveBeansUtils.close(conn);
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

}
