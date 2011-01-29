package org.activebeans;

import java.util.Collections;
import java.util.List;

public class Table {

	private String name;

	private List<Column> cols;

	public Table(String name, List<Column> cols) {
		this.name = name;
		this.cols = cols;
	}

	public String name() {
		return name;
	}

	public List<Column> columns() {
		return Collections.unmodifiableList(cols);
	}

}
