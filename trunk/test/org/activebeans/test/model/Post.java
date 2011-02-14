package org.activebeans.test.model;

import java.sql.Date;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.AssociationConditions;
import org.activebeans.Conditions;
import org.activebeans.Property;
import org.activebeans.PropertyCondition;

@Active(
	with = { 
		@Property(name = "id", type = long.class, key=true, autoIncrement=true),
		@Property(name = "subject", type = String.class),
		@Property(name = "created", type = Date.class) 
	}, 
	hasMany = @Association(with = Comment.class)
)
public class Post {

	public void hello(){
		
	}
	
	public void world(){
		
	}
	
	public static Conditions<Post> conditions(){
		return null;
	}
	
	public static PropertyCondition<Post, Long> id(){
		return null;
	}
	
	public static PropertyCondition<Post, String> subject() {
		return null;
	}
	
	public static PropertyCondition<Post, Date> created() {
		return null;
	}
	
	public static AssociationConditions<Post, Comment> comments() {
		return null;
	}
	
}