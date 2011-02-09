package org.activebeans.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveIntrospector;
import org.activebeans.ActiveMethodFilter;
import org.activebeans.ActiveMigration;
import org.activebeans.Association;
import org.activebeans.BelongsToAssociationMethods;
import org.activebeans.Column;
import org.activebeans.DataSourceIntrospector;
import org.activebeans.DataType;
import org.activebeans.HasManyAssociationMethods;
import org.activebeans.Model;
import org.activebeans.Property;
import org.activebeans.PropertyAccessors;
import org.activebeans.Table;
import org.activebeans.test.model.Comment;
import org.activebeans.test.model.Comment.Models;
import org.activebeans.test.model.Post;
import org.junit.BeforeClass;
import org.junit.Test;

public class ActiveBeansTest {

	private static final String TEST_CONTEXT = "test";

	private static Set<Class<Model>> activeClasses;

	private static DataSource ds;

	private static Class<? extends Model> activeClass;

	private static Active activeAt;

	private static Class<?> activeInterf;

	private static Class<?> activeCollectionInterf;

	private static ActiveIntrospector<?> activeIntro;

	private static DataSourceIntrospector dsIntro;

	@BeforeClass
	public static void staticInit() throws ClassNotFoundException {
		ds = DataSourceTest.getDataSource();
		ActiveBeans.setup(TEST_CONTEXT, ds);
		activeClasses = ActiveIntrospector.activeClasses();
		activeClass = Post.class;
		activeAt = activeClass.getAnnotation(Active.class);
		activeInterf = Class.forName(activeClass.getPackage().getName()
				+ ".Active" + activeClass.getSimpleName());
		activeCollectionInterf = Class.forName(activeClass.getName()
				+ "$Models");
		activeIntro = ActiveIntrospector.of(activeClass);
		dsIntro = new DataSourceIntrospector(ds);
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
		ActiveBeans.setup(TEST_CONTEXT, ds);
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
		assertTrue(activeClasses.containsAll(Arrays.asList(new Class[] {
				Post.class, Comment.class })));
	}

	@Test
	public void keys() {
		ActiveIntrospector<Post> pIntro = ActiveIntrospector.of(Post.class);
		List<Property> keys = pIntro.keys();
		assertEquals(1, keys.size());
		assertEquals(pIntro.property("id"), keys.get(0));
	}

	@Test
	public void dataType() {
		String name = "decimal";
		assertEquals(name, new DataType(name).definition());
		int len = 10;
		assertEquals(name + "(" + len + ")",
				new DataType(name, len).definition());
		int decimals = 4;
		assertEquals(name + "(" + len + ", " + decimals + ")", new DataType(
				name, len, decimals).definition());
	}

	@Test
	public void column() {
		DataType dataType = new DataType("int");
		String dataTypeDef = dataType.definition();
		String name = "id";
		Column.Builder col = new Column.Builder(name, dataType);
		assertEquals(name + " " + dataTypeDef + " null", col.build()
				.definition());
		assertEquals(name + " " + dataTypeDef + " not null", col.notNull(true)
				.build().definition());
		assertEquals(name + " " + dataTypeDef + " null auto_increment", col
				.notNull(false).autoIncrement(true).build().definition());
		assertEquals(name + " " + dataTypeDef + " not null auto_increment", col
				.notNull(true).build().definition());
	}

	@Test
	public void createTable() {
		String tableName = "test";
		String create = "create table if not exists " + tableName + "(";
		Column name = new Column.Builder("name", new DataType("varchar", 50))
				.build();
		String nameDef = name.definition();
		assertEquals(create + nameDef + ")",
				new Table(tableName, name).createStatment());
		Column date = new Column.Builder("create_date", new DataType("date"))
				.build();
		String dateDef = date.definition();
		assertEquals(create + nameDef + ", " + dateDef + ")", new Table(
				tableName, name, date).createStatment());
		Column id = new Column.Builder("id", new DataType("int")).key(true)
				.build();
		String idDef = id.definition();
		assertEquals(create + idDef + ", " + nameDef + ", " + dateDef
				+ ", primary key(" + id.name() + "))", new Table(tableName, id,
				name, date).createStatment());
		Column id2 = new Column.Builder("id2", new DataType("int")).key(true)
				.build();
		Table table = new Table(tableName, id, id2, name, date);
		assertEquals(
				create + idDef + ", " + id2.definition() + ", " + nameDef
						+ ", " + dateDef + ", primary key(" + id.name() + ", "
						+ id2.name() + "))", table.createStatment());
		assertEquals("drop table if exists " + tableName, table.dropStatement());
	}

	@Test
	public void migration() throws SQLException {
		ActiveMigration<?> migr = ActiveMigration.of(Comment.class);
		final Table table = migr.table();
		ActiveBeansUtils.executeSql(ds, table.createStatment());
		String tableName = table.name();
		List<String> defCols = new ArrayList<String>();
		for (Column col : table.columns()) {
			defCols.add(col.name());
		}
		assertTrue(dsIntro.columns(tableName).containsAll(defCols));
		ActiveBeansUtils.executeSql(ds, table.dropStatement());
		assertFalse(dsIntro.tables().contains(tableName));
	}

	@Test
	public void migrateOne() {
		Table table = ActiveMigration.of(activeClass).table();
		ActiveBeans.migrate(activeClass);
		String tableName = table.name();
		assertTrue(dsIntro.tables().contains(tableName));
		ActiveBeansUtils.executeSql(ds, table.dropStatement());
		assertFalse(dsIntro.tables().contains(tableName));
	}

	@Test
	public void migrateAll() {
		List<Table> tables = new ArrayList<Table>();
		List<String> tableNames = new ArrayList<String>();
		List<String> dropStmts = new ArrayList<String>();
		for (Class<Model> clazz : activeClasses) {
			Table table = ActiveMigration.of(clazz).table();
			tables.add(table);
			tableNames.add(table.name());
			dropStmts.add(table.dropStatement());
		}
		ActiveBeans.autoMigrate();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		ActiveBeansUtils.executeSql(ds, dropStmts);
		assertTrue(Collections.disjoint(dsIntro.tables(), tableNames));
	}

	@Test
	public void alterTable() {
		Column id = new Column.Builder("id", new DataType("int")).key(true)
				.build();
		Column name = new Column.Builder("name", new DataType("varchar"))
				.build();
		Table table = new Table("test", id, name);
		String alter = "alter table " + table.name();
		assertEquals(alter + " add column " + id.definition(),
				table.alterStatement(id));
		assertEquals(alter + " add column " + id.definition() + ", add column "
				+ name.definition(), table.alterStatement(id, name));
	}
}
