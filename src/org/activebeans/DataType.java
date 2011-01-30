package org.activebeans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataType {

	private String name;

	private List<?> params;

	private String definition;

	public DataType(String name, Object... params) {
		this.name = name;
		this.params = Arrays.asList(params);
		definition = name;
		int paramLen = params.length;
		for (int i = 0; i < paramLen; i++) {
			definition += (i == 0 ? "(" : ", ") + params[i]
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
