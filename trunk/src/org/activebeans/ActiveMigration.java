package org.activebeans;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class ActiveMigration<T extends Model<T, ?, ?>> {

	private static final String ASSOCIATION_SUFFIX = "_id";

	private Class<T> activeClass;

	private Table table;

	private DataSourceIntrospector dsIntro;

	private ActiveMigration(Class<T> activeClass, DataSource ds) {
		dsIntro = new DataSourceIntrospector(ds);
		this.activeClass = activeClass;
		String tableName = ActiveBeansUtils.camelCaseToUnderscore(activeClass
				.getSimpleName());
		ActiveIntrospector<T> ai = ActiveIntrospector.of(activeClass);
		List<Column> cols = new ArrayList<Column>();
		for (Property prop : ai.properties()) {
			Class<?> type = prop.type();
			Object len = null;
			if (type.equals(String.class)) {
				int propLen = prop.length();
				len = propLen == 0 ? ActiveTypeMapper.varcharLength() : propLen;
			}
			boolean key = prop.key();
			cols.add(new Column.Builder(ActiveBeansUtils
					.camelCaseToUnderscore(prop.name()), new DataType(
					ActiveTypeMapper.sqlTypeName(type), len))
					.autoIncrement(prop.autoIncrement()).key(key)
					.notNull(key || prop.required()).build());
		}
		for (Association belongsTo : ai.belongsTos()) {
			@SuppressWarnings("rawtypes")
			Class<? extends Model> belongsToClazz = belongsTo.with();
			boolean notNull = belongsTo.required();
			@SuppressWarnings("unchecked")
			ActiveIntrospector<?> btci = ActiveIntrospector.of(belongsToClazz);
			for (Property prop : btci.keys()) {
				Class<?> type = prop.type();
				Object len = null;
				if (type.equals(String.class)) {
					int propLen = prop.length();
					len = propLen == 0 ? ActiveTypeMapper.varcharLength()
							: propLen;
				}
				cols.add(new Column.Builder(ActiveBeansUtils
						.camelCaseToUnderscore(belongsToClazz.getSimpleName())
						+ ASSOCIATION_SUFFIX, new DataType(ActiveTypeMapper
						.sqlTypeName(type), len)).notNull(notNull).build());
			}

		}
		table = new Table(tableName, cols);
	}

	public static <U extends Model<U, ?, ?>> ActiveMigration<U> of(
			Class<U> activeClass, DataSource ds) {
		return new ActiveMigration<U>(activeClass, ds);
	}

	public Table table() {
		return table;
	}

	public Class<T> activeClass() {
		return activeClass;
	}

	public List<Column> alterColumns() {
		List<Column> alterCols;
		String tableName = table.name();
		List<Column> activeCols = table.columns();
		if (!dsIntro.tables().contains(tableName)) {
			alterCols = activeCols;
		} else {
			alterCols = new ArrayList<Column>();
			List<String> dbCols = dsIntro.columns(tableName);
			for (Column c : activeCols) {
				if (!dbCols.contains(c.name())) {
					alterCols.add(c);
				}
			}
		}
		return alterCols;
	}

	public String alterStatement() {
		String stmt = null;
		List<Column> alterCols = alterColumns();
		if (!dsIntro.tables().contains(table.name())) {
			stmt = table.createStatment();
		} else if (!alterCols.isEmpty()) {
			stmt = table.alterStatement(alterColumns());
		}
		return stmt;
	}

}
