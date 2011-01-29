package org.activebeans;

public class Column {

	private final String name;

	private final int jdbcType;

	private final boolean notNull;

	private final boolean key;

	private final boolean autoIncrement;

	private String definition;

	private Column(Builder builder) {
		name = builder.name;
		jdbcType = builder.jdbcType;
		notNull = builder.notNull;
		key = builder.key;
		autoIncrement = builder.autoIncrement;
		definition = name + " " + ActiveTypeMapper.sqlTypeName(jdbcType)
				+ (notNull ? " not" : "") + " null"
				+ (autoIncrement ? " auto_increment" : "");
	}

	public String name() {
		return name;
	}

	public int jdbcType() {
		return jdbcType;
	}

	public boolean notNull() {
		return notNull;
	}

	public boolean key() {
		return key;
	}

	public boolean autoIncrement() {
		return autoIncrement;
	}

	public String definition() {
		return definition;
	}

	public static class Builder {

		private final String name;

		private final int jdbcType;

		private boolean notNull;

		private boolean key;

		private boolean autoIncrement;

		public Builder(String name, int jdbcType) {
			this.name = name;
			this.jdbcType = jdbcType;
		}

		public Builder notNull(boolean val) {
			notNull = val;
			return this;
		}

		public Builder key(boolean val) {
			key = val;
			return this;
		}

		public Builder autoIncrement(boolean val) {
			autoIncrement = val;
			return this;
		}

		public Column build() {
			return new Column(this);
		}

	}

}
