package org.activebeans.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activebeans.ActiveBeans;
import org.activebeans.Base;
import org.activebeans.BelongsTo;
import org.activebeans.Initialization;
import org.activebeans.QueryMethods;
import org.activebeans.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveBeanTest {

	@Mock
	private ActiveBeans activeBean;

	@Mock
	private QueryMethods<UserMapper> queryMethods;

	@Mock
	private UserMapper userMap;

	@Mock
	private GroupMapper groupMap;

	@Mock
	private BelongsTo<GroupMapper> belongsTo;

	private Map<String, Object> params;

	@Before
	public void init() {
		params = Collections.emptyMap();
	}

	@Test
	public void createByHash() {
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class, params);
	}

	@Test
	public void createByBlockInitialization() {
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class,
				new Initialization<User>() {
					@Override
					public User init(User t) {
						t.setAge(0);
						t.setName("");
						return t;
					}
				});
	}

	@Test
	public void createByBareObject() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.bean()).thenReturn(new User());
		Base<User> arUser = activeBean.create(UserMapper.class);
		User u = arUser.bean();
		u.setAge(0);
		u.setName("");
	}

	@Test
	public void specifyConditionsBySQLWithOrdinalParams() {
		activeBean.where(UserMapper.class, "name = ? and age = ?", "", 0);
	}

	@Test
	public void specifyConditionsBySQLWithNamedParam() {
		activeBean.where(UserMapper.class, "name = :name and age = :age",
				params);
	}

	@Test
	public void specifyConditionsByHashToUseEqualityWithSqlAnd() {
		activeBean.where(UserMapper.class, params);
	}

	@Test
	public void specifyConditionsByHashToUseRange() {
		activeBean.where(UserMapper.class,
				Collections.singletonMap("grade", new Range<Integer>() {
					@Override
					public Integer start() {
						return 10;
					}

					@Override
					public Integer end() {
						return 30;
					}
				}));
	}

	@Test
	public void specifyConditionsByHashToUseIn() {
		activeBean.where(UserMapper.class,
				Collections.singletonMap("grade", Arrays.asList(1, 3, 5)));
	}

	@Test
	public void specifyJoinTableConditionByNestedHash() {
		when(activeBean.joins(eq(UserMapper.class), anyString()))
				.thenReturn(queryMethods);
		activeBean.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools",
						Collections.singletonMap("type", "public")));
	}

	@Test
	public void specifyJoinTableConditionByNestedKeysInHash() {
		when(activeBean.joins(eq(UserMapper.class), anyString()))
				.thenReturn(queryMethods);
		activeBean.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools.type", "public"));
	}

	@Test
	public void queryAttributes() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		boolean iaPresent = activeBean.create(UserMapper.class).present(
				"age");
	}

	@Test
	public void accessAttributeBeforeTypeCast() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		Object ageBeforeTypeCast = activeBean.create(UserMapper.class)
				.beforeTypeCast("age");
	}

	@Test
	public void findByDynamicAttributes() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		List<UserMapper> uList = activeBean.create(UserMapper.class)
				.findByAgeAndName(0, "");
	}

	@Test
	public void findByDynamicAttributesOnRelations() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		when(belongsTo.get()).thenReturn(groupMap);
		@SuppressWarnings("unused")
		List<GroupMapper> gList = activeBean.create(UserMapper.class)
				.group().get().findByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributes() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class)
				.findOrCreateByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithHashInitialization() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class)
				.findOrCreateByName(params);
	}

	@Test
	public void findOrInitializeByDynamicAttributes() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class)
				.findOrInitializeByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithBlockInitialization() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBean.create(UserMapper.class)
				.findOrInitializeByName(new Initialization<User>() {
					@Override
					public User init(User t) {
						t.setAge(0);
						return t;
					}
				});
	}

	@Test
	public void associateByBelongingTo() {
		when(activeBean.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		@SuppressWarnings("unused")
		GroupMapper g = activeBean.create(UserMapper.class).group().get();
	}

}
