package org.activebeans;

public class Column {

	private final String name;

	private final DataType type;

	private final boolean notNull;

	private final boolean key;

	private final boolean autoIncrement;

	private final int length;

	private final String definition;

	private Column(Builder builder) {
		name = builder.name;
		type = builder.type;
		notNull = builder.notNull;
		key = builder.key;
		autoIncrement = builder.autoIncrement;
		length = builder.length;
		definition = name + " " + type.definition() + (notNull ? " not" : "")
				+ " null" + (autoIncrement ? " auto_increment" : "");
	}

	public String name() {
		return name;
	}

	public DataType type() {
		return type;
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

	public int length() {
		return length;
	}

	public String definition() {
		return definition;
	}

	public static class Builder {

		private final String name;

		private final DataType type;

		private boolean notNull;

		private boolean key;

		private boolean autoIncrement;

		private int length;

		public Builder(String name, DataType type) {
			this.name = name;
			this.type = type;
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

		public Builder length(int val) {
			length = val;
			return this;
		}

		public Column build() {
			return new Column(this);
		}

	}

}
