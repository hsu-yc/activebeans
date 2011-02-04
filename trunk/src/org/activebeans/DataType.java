package org.activebeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataType {

	private String name;

	private List<Object> params = new ArrayList<Object>();

	private String definition;

	public DataType(String name, Object... ps) {
		this.name = name;
		for (Object p : ps) {
			if (p != null) {
				params.add(p);
			}
		}
		definition = name;
		int paramLen = params.size();
		for (int i = 0; i < paramLen; i++) {
			definition += (i == 0 ? "(" : ", ") + params.get(i)
					+ (i == paramLen - 1 ? ")" : "");
		}
	}

	public String name() {
		return name;
	}

	public List<?> params() {
		return Collections.unmodifiableList(params);
	}

	public String definition() {
		return definition;
	}

}
