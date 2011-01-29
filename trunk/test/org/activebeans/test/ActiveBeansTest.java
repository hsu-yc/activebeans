package org.activebeans.test;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveIntrospector;
import org.activebeans.ActiveMethodFilter;
import org.activebeans.ActiveTypeMapper;
import org.activebeans.Association;
import org.activebeans.BelongsToAssociationMethods;
import org.activebeans.Column;
import org.activebeans.HasManyAssociationMethods;
import org.activebeans.Model;
import org.activebeans.Property;
import org.activebeans.PropertyAccessors;
import org.activebeans.test.model.Comment;
import org.activebeans.test.model.Comment.Models;
import org.activebeans.test.model.Post;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ActiveBeansTest {

	private Class<? extends Model> activeClass;

	private Active activeAt;

	private Class<?> activeInterf;

	private Class<?> activeCollectionInterf;

	private ActiveIntrospector<?> activeIntro;

	private DataSource ds;

	@Before
	public void init() throws ClassNotFoundException {
		activeClass = Post.class;
		activeAt = activeClass.getAnnotation(Active.class);
		activeInterf = Class.forName(activeClass.getPackage().getName()
				+ ".Active" + activeClass.getSimpleName());
		activeCollectionInterf = Class.forName(activeClass.getName()
				+ "$Models");
		activeIntro = ActiveIntrospector.of(activeClass);
		ds = DataSourceTest.getDataSource();
	}

	@Test
	public void typeIntrospection() {
		assertEquals(activeInterf, activeIntro.activeInterface());
		assertEquals(activeAt, activeIntro.activeAnnotation());
		assertEquals(activeClass, activeIntro.activeClass());
		assertEquals(activeCollectionInterf,
				activeIntro.activeCollectionInterface());
	}

	@Test
	public void propertyIntrospection() throws IntrospectionException {
		Property[] withs = activeAt.with();
		List<Property> props = activeIntro.properties();
		int propCount = withs.length;
		assertEquals(propCount, props.size());
		for (Property with : withs) {
			String propName = with.name();
			assertEquals(with, activeIntro.property(propName));
			PropertyDescriptor pd = new PropertyDescriptor(propName,
					activeClass);
			PropertyAccessors accessors = activeIntro.accessors(with);
			assertEquals(pd.getReadMethod(), accessors.get());
			assertEquals(pd.getWriteMethod(), accessors.set());
		}
		assertEquals(propCount, activeIntro.accessors().size());
	}

	@Test
	public void belongsToAssociationIntrospection()
			throws IntrospectionException {
		Association[] belongsTos = activeAt.belongsTo();
		List<Association> belongsToList = activeIntro.belongsTos();
		int assocCount = belongsTos.length;
		assertEquals(assocCount, belongsToList.size());
		for (Association belongsTo : belongsTos) {
			assertEquals(belongsTo, activeIntro.belongsTo(belongsTo.with()));
			PropertyDescriptor pd = new PropertyDescriptor(
					Introspector.decapitalize(belongsTo.with().getSimpleName()),
					activeClass);
			BelongsToAssociationMethods methods = activeIntro
					.belongsToMethods(belongsTo);
			assertEquals(pd.getReadMethod(), methods.retrieve());
			assertEquals(pd.getWriteMethod(), methods.assign());
		}
		assertEquals(assocCount, activeIntro.belongsToMethods().size());
	}

	@Test
	public void hasManyAssociationIntrospection() throws IntrospectionException {
		Association[] hasManys = activeAt.hasMany();
		List<Association> hasManysList = activeIntro.hasManys();
		int assocCount = hasManys.length;
		assertEquals(assocCount, hasManysList.size());
		for (Association hasMany : hasManys) {
			assertEquals(hasMany, activeIntro.hasMany(hasMany.with()));
			String typeName = hasMany.with().getSimpleName();
			PropertyDescriptor pd = new PropertyDescriptor(
					Introspector.decapitalize(typeName) + "s", activeClass,
					"get" + typeName + "s", null);
			HasManyAssociationMethods methods = activeIntro
					.hasManyMethods(hasMany);
			assertEquals(pd.getReadMethod(), methods.retrieve());
		}
		assertEquals(assocCount, activeIntro.hasManyMethods().size());
	}

	@Test
	public void methodFilter() {
		ActiveMethodFilter<? extends Model> filter = ActiveMethodFilter
				.of(activeClass);
		List<Method> handledMathods = new ArrayList<Method>();
		handledMathods.addAll(Arrays.asList(activeInterf.getMethods()));
		handledMathods.addAll(Arrays.asList(Model.class.getMethods()));
		for (Method method : activeClass.getMethods()) {
			assertEquals(handledMathods.contains(method),
					filter.isHandled(method));
		}
	}

	@Test
	public void propertyAccessors() {
		Post post = ActiveBeans.build(Post.class);
		long id = 1;
		post.setId(id);
		assertEquals(id, post.getId());
		String subject = "foo";
		post.setSubject(subject);
		assertEquals(subject, post.getSubject());
		Date created = new Date(System.currentTimeMillis());
		post.setCreated(created);
		assertEquals(created, post.getCreated());
		Comment comment = ActiveBeans.build(Comment.class);
		comment.setId(id);
		assertEquals(id, comment.getId());
	}

	@Test
	public void belongsToAssociationMethods() {
		Comment comment = ActiveBeans.build(Comment.class);
		Post post = ActiveBeans.build(Post.class);
		comment.setPost(post);
		assertEquals(post, comment.getPost());
	}

	@Test
	public void hasManyAssociationMethods() {
		Post post = ActiveBeans.build(Post.class);
		Models comments = post.getComments();
		assertNotNull(comments);
	}

	@Test
	public void noopModel() {
		Model model = ActiveBeans.build(activeClass);
		assertTrue(activeClass.isInstance(model));
		model.attributes(null);
		assertFalse(model.destroy());
		assertFalse(model.save());
		assertFalse(model.update());
		assertFalse(model.update(null));
	}

	@Test
	public void noopModels() {
		Post post = ActiveBeans.build(Post.class);
		Models comments = post.getComments();
		assertNull(comments.add(null));
		assertNull(comments.all());
		assertNull(comments.all(null));
		comments.attributes(null);
		assertNull(comments.build());
		assertNull(comments.build(null));
		assertNull(comments.create());
		assertNull(comments.create(null));
		assertFalse(comments.destroy());
		assertNull(comments.first());
		assertNull(comments.first(null));
		assertNull(comments.get(null));
		assertNull(comments.iterator());
		assertNull(comments.last());
		assertNull(comments.last(null));
		assertNull(comments.popular());
		assertFalse(comments.save());
		assertFalse(comments.update());
		assertFalse(comments.update(null));
	}

	@Test
	public void setup() {
		ActiveBeans.setup("test", ds);
		assertSame(ds, ActiveBeans.repository());
	}

	@Test
	public void camelCaseToUnderscore() {
		assertEquals("hello_world",
				ActiveBeansUtils.camelCaseToUnderscore("helloWorld"));
		assertEquals("foo_bar",
				ActiveBeansUtils.camelCaseToUnderscore("FooBar"));
	}

	@Test
	public void activeBeansDiscovery() {
		assertTrue(ActiveIntrospector.activeClasses().containsAll(
				Arrays.asList(new Class[] { Post.class, Comment.class })));
	}

	@Test
	public void keys() {
		ActiveIntrospector<Post> pIntro = ActiveIntrospector.of(Post.class);
		List<Property> keys = pIntro.keys();
		assertEquals(1, keys.size());
		assertEquals(pIntro.property("id"), keys.get(0));
	}

	@Test
	public void column() {
		String name = "id";
		int jdbcType = Types.INTEGER;
		String sqlTypeName = ActiveTypeMapper.sqlTypeName(jdbcType);
		Column.Builder col = new Column.Builder(name, jdbcType);
		assertEquals(name + " " + sqlTypeName + " null", col.build()
				.definition());
		assertEquals(name + " " + sqlTypeName + " not null", col.notNull(true)
				.build().definition());
		assertEquals(name + " " + sqlTypeName + " null auto_increment", col
				.notNull(false).autoIncrement(true).build().definition());
		assertEquals(name + " " + sqlTypeName + " not null auto_increment", col
				.notNull(true).build().definition());
	}
}
