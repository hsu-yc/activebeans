package org.activebeans.test;

import java.util.List;
import java.util.Map;

import org.activebeans.Base;
import org.activebeans.BelongsTo;
import org.activebeans.Do;

public interface UserMapper extends Base<User> {

	BelongsTo<GroupMapper> group();

	List<UserMapper> findByAgeAndName(int age, String name);

	UserMapper findOrCreateByName(String name);

	UserMapper findOrCreateByName(Map<String, ?> hash);

	UserMapper findOrInitializeByName(String name);

	UserMapper findOrInitializeByName(Do<User> init);

}