package org.activebeans.test;

import java.util.Date;

import org.activebeans.Active;
import org.activebeans.Property;

@Active({ 
	@Property(name = "id", type = long.class),
	@Property(name = "subject", type = String.class),
	@Property(name = "created", type = Date.class)
})
public class Post {
	
} 
 