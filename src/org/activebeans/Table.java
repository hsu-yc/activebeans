package org.activebeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Table {

	private String name;

	private List<Column> cols = new ArrayList<Column>();

	private String definition;

	private String dropStatement;

	public Table(String name, Column col, Column... moreCols) {
		this.name = name;
		cols.add(col);
		cols.addAll(Arrays.asList(moreCols));
		definition = "create table if not exists " + name + "(";
		List<Column> keys = new ArrayList<Column>();
		for (int i = 0; i < cols.size(); i++) {
			Column c = cols.get(i);
			definition += (i == 0 ? "" : ", ") + c.definition();
			if (c.key()) {
				keys.add(c);
			}
		}
		int numOfKeys = keys.size();
		for (int i = 0; i < numOfKeys; i++) {
			definition += (i == 0 ? ", primary key(" : ", ")
					+ keys.get(i).name() + (i == numOfKeys - 1 ? ")" : "");
		}
		definition += ")";
		dropStatement = "drop table if exists " + name;
	}

	public String name() {
		return name;
	}

	public List<Column> columns() {
		return Collections.unmodifiableList(cols);
	}

	public String definition() {
		return definition;
	}

	public String dropStatement() {
		return dropStatement;
	}

}
