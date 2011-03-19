package org.activebeans;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface GeneratedKeysHandler {

	void handle(ResultSet keys) throws SQLException;
	
}
