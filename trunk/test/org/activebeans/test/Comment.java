package org.activebeans.test;

import org.activebeans.Active;
import org.activebeans.Association;
import org.activebeans.Property;


@Active(with = @Property(name = "id", type = long.class), belongsTo = @Association(type = Post.class))
public class Comment {

}
