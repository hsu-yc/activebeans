package org.activebeans;

import java.util.ArrayList;
import java.util.List;

public class ActiveMigration<T extends Model> {

	private static final String ASSOCIATION_SUFFIX = "_id";

	private Class<T> activeClass;

	private Table table;

	private ActiveMigration(Class<T> activeClass) {
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
			Class<? extends Model> belongsToClazz = belongsTo.with();
			boolean notNull = belongsTo.required();
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

	public static <U extends Model> ActiveMigration<U> of(Class<U> activeClass) {
		return new ActiveMigration<U>(activeClass);
	}

	public Table table() {
		return table;
	}

	public Class<T> activeClass() {
		return activeClass;
	}

}
