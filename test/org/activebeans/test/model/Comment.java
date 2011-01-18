package org.activebeans.test.model;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.Property;

@Active(
	with = {
		@Property(name = "id", type = long.class, key=true, autoIncrement=true),
		@Property(name = "body", type = String.class)
	},
	belongsTo = @Association(with = Post.class)
)
public class Comment {

	public static Models popular() {
		return null;
	}

}
