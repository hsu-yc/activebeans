package org.activebeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Table {

	private String name;

	private List<Column> cols = new ArrayList<Column>();

	private String createStatement;

	private String dropStatement;

	private String alterTable;

	public Table(String name, List<Column> columns) {
		this.name = name;
		cols.addAll(columns);
		createStatement = "create table if not exists " + name + "(";
		List<Column> keys = new ArrayList<Column>();
		for (int i = 0; i < cols.size(); i++) {
			Column c = cols.get(i);
			createStatement += (i == 0 ? "" : ", ") + c.definition();
			if (c.key()) {
				keys.add(c);
			}
		}
		int numOfKeys = keys.size();
		for (int i = 0; i < numOfKeys; i++) {
			createStatement += (i == 0 ? ", primary key(" : ", ")
					+ keys.get(i).name() + (i == numOfKeys - 1 ? ")" : "");
		}
		createStatement += ")";
		dropStatement = "drop table if exists " + name;
		alterTable = "alter table " + name;
	}

	public Table(String name, Column... columns) {
		this(name, Arrays.asList(columns));
	}

	public String name() {
		return name;
	}

	public List<Column> columns() {
		return Collections.unmodifiableList(cols);
	}

	public String createStatment() {
		return createStatement;
	}

	public String dropStatement() {
		return dropStatement;
	}

	public String alterStatement(List<Column> cols) {
		String alterStmt = alterTable;
		for (int i = 0; i < cols.size(); i++) {
			alterStmt += (i == 0 ? "" : ",") + " add column "
					+ cols.get(i).definition();
		}
		return alterStmt;
	}

	public String alterStatement(Column... cols) {
		return alterStatement(Arrays.asList(cols));
	}

}
