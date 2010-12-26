package org.activebeans.test.model;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.Property;


@Active(
	with = @Property(name = "id", type = long.class), 
	belongsTo = @Association(type = Post.class)
)
public class Comment {
	
	public static Models popular(){
		return null;
	}
	
}
