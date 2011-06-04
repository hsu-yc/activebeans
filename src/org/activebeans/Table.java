package org.activebeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Table {

	private String name;

	private List<Column> cols = new ArrayList<Column>();

	private String create;

	private String drop;

	private String alter;

	private String insert;
	
	private String select;

	public Table(String name, List<Column> columns) {
		this.name = name;
		cols.addAll(columns);
		create = "create table if not exists " + name + "(";
		List<Column> keys = new ArrayList<Column>();
		for (int i = 0; i < cols.size(); i++) {
			Column c = cols.get(i);
			create += (i == 0 ? "" : ", ") + c.definition();
			if (c.key()) {
				keys.add(c);
			}
		}
		int numOfKeys = keys.size();
		for (int i = 0; i < numOfKeys; i++) {
			create += (i == 0 ? ", primary key(" : ", ")
					+ keys.get(i).name() + (i == numOfKeys - 1 ? ")" : "");
		}
		create += ")";
		drop = "drop table if exists " + name;
		alter = "alter table " + name;
		insert = "insert " + name + "(";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			insert += (i == 0 ? "" : ", ") + c.name();
		}
		insert += ") values(";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			insert += (i == 0 ? "" : ", ") + (c.autoIncrement()?"default":"?");
		}
		insert += ")";
		select = "select";
		for (int i=0;  i < cols.size(); i++) {
			Column c = cols.get(i);
			select += (i == 0 ? "" : ",") + " " + c.name();
		}
		select += " from " + name + " where";
		for (int i = 0; i < numOfKeys; i++) {
			select += " " + (i == 0 ? "" : "and")
					+ keys.get(i).name() + " = ?";
		}
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
		return create;
	}

	public String dropStatement() {
		return drop;
	}

	public String alterStatement(List<Column> cols) {
		String alterStmt = alter;
		for (int i = 0; i < cols.size(); i++) {
			alterStmt += (i == 0 ? "" : ",") + " add column "
					+ cols.get(i).definition();
		}
		return alterStmt;
	}

	public String alterStatement(Column... cols) {
		return alterStatement(Arrays.asList(cols));
	}
	
	public String insertStatement(){
		return insert;
	}
	
	public String selectStatement(){
		return select;
	}

}
