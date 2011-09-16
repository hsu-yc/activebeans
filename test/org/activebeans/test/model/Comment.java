package org.activebeans.test.model;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.Association;
import org.activebeans.Property;

@Active(
	with = {
		@Property(name = "id", type = Long.class, key=true, autoIncrement=true),
		@Property(name = "body", type = String.class)
	},
	belongsTo = @Association(with = Post.class)
)
public class Comment {
	
	public static Models popular() {
		return ActiveBeans.all(Comment.class, ActiveBeans.conditions(Comment.class)
			.body().desc());
	}

}