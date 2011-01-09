package org.activebeans.test.model;

import java.util.Date;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.Property;

@Active(
	with = { 
		@Property(name = "id", type = long.class),
		@Property(name = "subject", type = String.class),
		@Property(name = "created", type = Date.class) 
	}, 
	hasMany = @Association(with = Comment.class)
)
public class Post {

}