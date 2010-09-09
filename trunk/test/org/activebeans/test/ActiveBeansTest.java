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
import org.activebeans.Do;
import org.activebeans.QueryMethods;
import org.activebeans.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveBeansTest {

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

	private User user;

	@Before
	public void init() {
		params = Collections.emptyMap();
		user = new User();
	}

	@Test
	public void createByHash() {
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class, user);
	}

	@Test
	public void createByBlockInitialization() {
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class, new Do<User>() {
			@Override
			public User block(User t) {
				t.setAge(0);
				t.setName("");
				return t;
			}
		});
	}

	@Test
	public void createByBareObject() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		when(userMap.bean()).thenReturn(new User());
		Base<User> arUser = activeBeans.newBean(UserMapper.class);
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
		when(activeBeans.joins(eq(UserMapper.class), anyString())).thenReturn(
				queryMethods);
		activeBeans.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools",
						Collections.singletonMap("type", "public")));
	}

	@Test
	public void specifyJoinTableConditionByNestedKeysInHash() {
		when(activeBeans.joins(eq(UserMapper.class), anyString())).thenReturn(
				queryMethods);
		activeBeans.joins(UserMapper.class, "schools").where(
				Collections.singletonMap("schools.type", "public"));
	}

	@Test
	public void queryAttributes() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		boolean iaPresent = activeBeans.newBean(UserMapper.class)
				.present("age");
	}

	@Test
	public void accessAttributeBeforeTypeCast() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		Object ageBeforeTypeCast = activeBeans.newBean(UserMapper.class)
				.beforeTypeCast("age");
	}

	@Test
	public void findByDynamicAttributes() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		List<UserMapper> uList = activeBeans.newBean(UserMapper.class)
				.findByAgeAndName(0, "");
	}

	@Test
	public void findByDynamicAttributesOnRelations() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		when(belongsTo.get()).thenReturn(groupMap);
		@SuppressWarnings("unused")
		List<GroupMapper> gList = activeBeans.newBean(UserMapper.class).group()
				.get().findByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributes() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class)
				.findOrCreateByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithHashInitialization() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class)
				.findOrCreateByName(params);
	}

	@Test
	public void findOrInitializeByDynamicAttributes() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class)
				.findOrInitializeByName("");
	}

	@Test
	public void findOrCreateByDynamicAttributesWithBlockInitialization() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		@SuppressWarnings("unused")
		UserMapper u = activeBeans.newBean(UserMapper.class)
				.findOrInitializeByName(new Do<User>() {
					@Override
					public User block(User t) {
						t.setAge(0);
						return t;
					}
				});
	}

	@Test
	public void associateByBelongingTo() {
		when(activeBeans.newBean(UserMapper.class)).thenReturn(userMap);
		when(userMap.group()).thenReturn(belongsTo);
		@SuppressWarnings("unused")
		GroupMapper g = activeBeans.newBean(UserMapper.class).group().get();
	}

}
