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
	private ActiveBeans activeBeans;

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
		UserMapper u = activeBeans.create(UserMapper.class, params);
	}

	@Test
	public void createByBlockInitialization() {
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.create(UserMapper.class,
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
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.bean()).thenReturn(new User());
		Base<User> arUser = activeBeans.create(UserMapper.class);
		User u = arUser.bean();
		u.setAge(0);
		u.setName("");
	}

	@Test
	public void specifyConditionsBySQLWithOrdinalParams() {
		activeBeans.where(UserMapper.class, "name = ? and age = ?", "", 0);
	}

	@Test
	public void specifyConditionsBySQLWithNamedParam() {
		activeBeans.where(UserMapper.class, "name = :name and age = :age",
				params);
	}

	@Test
	public void specifyConditionsByHashToUseEqualityWithSqlAnd() {
		activeBeans.where(UserMapper.class, params);
	}

	@Test
	public void specifyConditionsByHashToUseRange() {
		activeBeans.where(UserMapper.class,
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
		activeBeans.where(UserMapper.class,
				Collections.singletonMap("grade", Arrays.asList(1, 3, 5)));
	}

	@Test
	public void specifyJoinTableConditionByNestedHash() {
		when(activeBeans.joins(eq(UserMapper.class), anyString()))
				.thenReturn(queryMethods);
		activeBeans.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools",
						Collections.singletonMap("type", "public")));
	}

	@Test
	public void specifyJoinTableConditionByNestedKeysInHash() {
		when(activeBeans.joins(eq(UserMapper.class), anyString()))
				.thenReturn(queryMethods);
		activeBeans.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools.type", "public"));
	}

	@Test
	public void queryAttributes() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		boolean iaPresent = activeBeans.create(UserMapper.class).present(
				"age");
	}

	@Test
	public void accessAttributeBeforeTypeCast() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		Object ageBeforeTypeCast = activeBeans.create(UserMapper.class)
				.beforeTypeCast("age");
	}

	@Test
	public void findByDynamicAttributes() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		List<UserMapper> uList = activeBeans.create(UserMapper.class)
				.findByAgeAndName(0, "");
	}

	@Test
	public void findByDynamicAttributesOnRelations() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		when(belongsTo.get()).thenReturn(groupMap);
		@SuppressWarnings("unused")
		List<GroupMapper> gList = activeBeans.create(UserMapper.class)
				.group().get().findByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributes() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.create(UserMapper.class)
				.findOrCreateByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithHashInitialization() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.create(UserMapper.class)
				.findOrCreateByName(params);
	}

	@Test
	public void findOrInitializeByDynamicAttributes() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.create(UserMapper.class)
				.findOrInitializeByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithBlockInitialization() {
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.create(UserMapper.class)
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
		when(activeBeans.create(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		@SuppressWarnings("unused")
		GroupMapper g = activeBeans.create(UserMapper.class).group().get();
	}

}
