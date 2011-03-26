package org.activebeans;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface GeneratedKeysResultSetHandler {

	void handle(ResultSet keys) throws SQLException;
	
}
