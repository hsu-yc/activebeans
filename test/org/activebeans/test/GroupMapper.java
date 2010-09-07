package org.activebeans.test;

import java.util.List;

import org.activebeans.Base;

public interface GroupMapper extends Base<Group> {

	List<GroupMapper> findByName(String name);

}
