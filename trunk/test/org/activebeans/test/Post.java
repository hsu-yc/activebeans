package org.activebeans.test;

import java.util.Date;

import javax.annotation.Generated;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.Property;

@Active(
	with = { 
		@Property(name = "id", type = long.class),
		@Property(name = "subject", type = String.class),
		@Property(name = "created", type = Date.class) 
	},
	hasMany = @Association(type = Comment.class)
)
public class Post {
	
	@Generated("")
	public static boolean destroyAll(){
		return org.activebeans.ActiveBeans.destroy(Post.class);
	}
	
	@Generated("")
	public static Models all(java.util.Map<String, ?> conditions){
		return Models.class.cast(org.activebeans.ActiveBeans.all(Post.class, conditions));
	}
	
	@Generated("")
	public static Models all(){
		return Models.class.cast(org.activebeans.ActiveBeans.all(Post.class));
	}
	
	public static Models popular(){
		return null;
	}
	
}