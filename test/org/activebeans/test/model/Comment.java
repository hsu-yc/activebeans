package org.activebeans.test.model;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.AssociationConditions;
import org.activebeans.Conditions;
import org.activebeans.Property;
import org.activebeans.PropertyCondition;

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
	
	public static Conditions<Comment> conditions(){
		return null;
	}
	
	public static PropertyCondition<Comment, Long> id(){
		return null;
	}
	
	public static PropertyCondition<Comment, String> body(){
		return null;
	}
	
	public static AssociationConditions<Comment, Post> post(){
		return null;
	}

}
