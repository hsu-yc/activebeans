package org.activebeans.test;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveIntrospector;
import org.activebeans.test.model.Post;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ActiveBeansTest {

	private Class<Post> activeClass;

	private Active activeAt;

	private Class<?> activeInterf;

	private Class<?> activeCollectionInterf;

	@Before
	public void init() throws ClassNotFoundException {
		activeClass = Post.class;
		activeAt = Post.class.getAnnotation(Active.class);
		activeInterf = Class.forName(activeClass.getPackage().getName()
				+ ".Active" + activeClass.getSimpleName());
		activeCollectionInterf = Class.forName(activeClass.getCanonicalName()
				+ "$Models");
	}

	@Test
	public void activeIntrospector() {
		ActiveIntrospector<Post> activeIntro = ActiveIntrospector
				.of(activeClass);
		assertEquals(activeInterf, activeIntro.getActiveInterface());
		assertEquals(activeAt, activeIntro.getActiveAnnotation());
		assertEquals(activeClass, activeIntro.getActiveClass());
		assertEquals(activeCollectionInterf,
				activeIntro.getActiveCollectionInterface());
	}

	@Test
	public void noOpInstance() {
		Post activeInst = ActiveBeans.build(activeClass);
		assertTrue(activeClass.isInstance(activeInst));
		activeInst.attributes(null);
		assertFalse(activeInst.destroy());
		assertFalse(activeInst.save());
		assertFalse(activeInst.update());
		assertFalse(activeInst.update(null));
		assertNull(activeInst.getComments());
		assertNull(activeInst.getCreated());
		assertEquals(0L, activeInst.getId());
		assertNull(activeInst.getSubject());
		activeInst.setCreated(null);
		activeInst.setId(0);
		activeInst.setSubject(null);
	}
	/*
	 * @Mock private ActiveBeans activeBeans;
	 * 
	 * @Mock private QueryMethods<UserMapper> queryMethods;
	 * 
	 * @Mock private UserMapper userMap;
	 * 
	 * @Mock private GroupMapper groupMap;
	 * 
	 * @Mock private BelongsTo<GroupMapper> belongsTo;
	 * 
	 * private Map<String, Object> params;
	 * 
	 * private User user;
	 * 
	 * @Before public void init() { params = Collections.emptyMap(); user = new
	 * User(); }
	 * 
	 * @Test public void createByHash() {
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class, user); }
	 * 
	 * @Test public void createByBlockInitialization() {
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class, new Do<User>() {
	 * 
	 * @Override public User block(User t) { t.setAge(0); t.setName(""); return
	 * t; } }); }
	 * 
	 * @Test public void createByBareObject() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * when(userMap.get()).thenReturn(new User()); Base<User> arUser =
	 * activeBeans.of(UserMapper.class); User u = arUser.get(); u.setAge(0);
	 * u.setName(""); }
	 * 
	 * @Test public void specifyConditionsBySQLWithOrdinalParams() {
	 * activeBeans.where(UserMapper.class, "name = ? and age = ?", "", 0); }
	 * 
	 * @Test public void specifyConditionsBySQLWithNamedParam() {
	 * activeBeans.where(UserMapper.class, "name = :name and age = :age",
	 * params); }
	 * 
	 * @Test public void specifyConditionsByHashToUseEqualityWithSqlAnd() {
	 * activeBeans.where(UserMapper.class, params); }
	 * 
	 * @Test public void specifyConditionsByHashToUseRange() {
	 * activeBeans.where(UserMapper.class, Collections.singletonMap("grade", new
	 * Range<Integer>() {
	 * 
	 * @Override public Integer start() { return 10; }
	 * 
	 * @Override public Integer end() { return 30; } })); }
	 * 
	 * @Test public void specifyConditionsByHashToUseIn() {
	 * activeBeans.where(UserMapper.class, Collections.singletonMap("grade",
	 * Arrays.asList(1, 3, 5))); }
	 * 
	 * @Test public void specifyJoinTableConditionByNestedHash() {
	 * when(activeBeans.joins(eq(UserMapper.class), anyString())).thenReturn(
	 * queryMethods); activeBeans.joins(UserMapper.class, "schools").where(
	 * Collections.singletonMap("schools", Collections.singletonMap("type",
	 * "public"))); }
	 * 
	 * @Test public void specifyJoinTableConditionByNestedKeysInHash() {
	 * when(activeBeans.joins(eq(UserMapper.class), anyString())).thenReturn(
	 * queryMethods); activeBeans.joins(UserMapper.class, "schools").where(
	 * Collections.singletonMap("schools.type", "public")); }
	 * 
	 * @Test public void queryAttributes() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") boolean iaPresent =
	 * activeBeans.of(UserMapper.class) .isPresent("age"); }
	 * 
	 * @Test public void accessAttributeBeforeTypeCast() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") Object ageBeforeTypeCast =
	 * activeBeans.of(UserMapper.class) .beforeTypeCast("age"); }
	 * 
	 * @Test public void findByDynamicAttributes() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") List<UserMapper> uList =
	 * activeBeans.of(UserMapper.class) .findByAgeAndName(0, ""); }
	 * 
	 * @Test public void findByDynamicAttributesOnRelations() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * when(userMap.group()).thenReturn(belongsTo);
	 * when(belongsTo.get()).thenReturn(groupMap);
	 * 
	 * @SuppressWarnings("unused") List<GroupMapper> gList =
	 * activeBeans.of(UserMapper.class).group() .get().findByName(""); }
	 * 
	 * @Test public void findOrCreateByDynamicAttributes() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class) .findOrCreateByName(""); }
	 * 
	 * @Test public void findOrCreateByDynamicAttributesWithHashInitialization()
	 * { when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class) .findOrCreateByName(params); }
	 * 
	 * @Test public void findOrInitializeByDynamicAttributes() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class) .findOrInitializeByName(""); }
	 * 
	 * @Test public void
	 * findOrCreateByDynamicAttributesWithBlockInitialization() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * 
	 * @SuppressWarnings("unused") UserMapper u =
	 * activeBeans.of(UserMapper.class) .findOrInitializeByName(new Do<User>() {
	 * 
	 * @Override public User block(User t) { t.setAge(0); return t; } }); }
	 * 
	 * @Test public void associateByBelongingTo() {
	 * when(activeBeans.of(UserMapper.class)).thenReturn(userMap);
	 * when(userMap.group()).thenReturn(belongsTo);
	 * 
	 * @SuppressWarnings("unused") GroupMapper group =
	 * activeBeans.of(UserMapper.class).group().get(); }
	 */
}
