package org.activebeans.test;

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

import lombok.eclipse.handlers.HandleActive;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveDelegate;
import org.activebeans.ActiveIntrospector;
import org.activebeans.ActiveMigration;
import org.activebeans.Association;
import org.activebeans.CollectionAssociationMethods;
import org.activebeans.CollectionOption;
import org.activebeans.Column;
import org.activebeans.Condition;
import org.activebeans.ConditionsMethodFilter;
import org.activebeans.DataSourceIntrospector;
import org.activebeans.DataType;
import org.activebeans.Model;
import org.activebeans.OptionsMethodFilter;
import org.activebeans.Property;
import org.activebeans.PropertyMethods;
import org.activebeans.SingularAssociationMethods;
import org.activebeans.SingularOption;
import org.activebeans.Table;
import org.activebeans.test.model.Comment;
import org.activebeans.test.model.Post;
import org.activebeans.test.model.Post.Conditions;
import org.activebeans.test.model.Post.Options;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ActiveBeansTest {

	private static final String TEST_CONTEXT = "test";

	private static Set<Class<? extends Model<?, ?, ?, ?>>> activeClasses;

	private static DataSource ds;

	private static Class<Post> activeClass;

	private static Active activeAt;

	private static Class<?> attrsInterf;

	private static Class<?> modelsInterf;
	
	private static Class<?> optionsInterf;
	
	private static Class<?> conditionsInterf;

	private static ActiveIntrospector activeIntro;

	private static DataSourceIntrospector dsIntro;

	@BeforeClass
	public static void staticInit() throws ClassNotFoundException {
		ds = DataSourceTest.getDataSource();
		ActiveBeans.setup(TEST_CONTEXT, ds);
		activeClasses = ActiveIntrospector.activeClasses();
		activeClass = Post.class;
		activeAt = activeClass.getAnnotation(Active.class);
		attrsInterf = Class.forName(HandleActive.attributesInterface(activeClass));
		modelsInterf = Class.forName(HandleActive.modelsInterface(activeClass));
		optionsInterf = Class.forName(HandleActive.optionsInterface(activeClass));
		conditionsInterf = Class.forName(HandleActive.conditionsInterface(activeClass));
		activeIntro = new ActiveIntrospector(activeClass);
		dsIntro = new DataSourceIntrospector(ds);
	}

	@Test
	public void typeIntrospection() {
		assertEquals(attrsInterf, activeIntro.attributesInterface());
		assertEquals(activeAt, activeIntro.activeAnnotation());
		assertEquals(activeClass, activeIntro.activeClass());
		assertEquals(modelsInterf, activeIntro.modelsInterface());
		assertEquals(optionsInterf, activeIntro.optionsInterface());
		assertEquals(conditionsInterf, activeIntro.conditionsInterface());
	}

	@Test
	public void propertyIntrospection() throws IntrospectionException, NoSuchMethodException {
		Property[] withs = activeAt.with();
		List<Property> props = activeIntro.properties();
		int propCount = withs.length;
		assertEquals(propCount, props.size());
		for (Property with : withs) {
			String propName = with.name();
			assertEquals(with, activeIntro.property(propName));
			PropertyDescriptor pd = new PropertyDescriptor(propName,
					activeClass);
			PropertyMethods methods = activeIntro.propertyMethods(with);
			assertEquals(pd.getReadMethod(), methods.get());
			assertEquals(pd.getWriteMethod(), methods.set());
			assertEquals(optionsInterf.getMethod(propName), methods.option());
			assertEquals(conditionsInterf.getMethod(propName), methods.condition());
		}
		assertEquals(propCount, activeIntro.propertyMethods().size());
	}

	@Test
	public void belongsToAssociationIntrospection() throws IntrospectionException, NoSuchMethodException {
		Association[] belongsTos = activeAt.belongsTo();
		List<Association> belongsToList = activeIntro.belongsTos();
		int assocCount = belongsTos.length;
		assertEquals(assocCount, belongsToList.size());
		for (Association belongsTo : belongsTos) {
			assertEquals(belongsTo, activeIntro.belongsTo(belongsTo.with()));
			String assocName = Introspector.decapitalize(belongsTo.with().getSimpleName());
			PropertyDescriptor pd = new PropertyDescriptor(assocName, activeClass);
			SingularAssociationMethods methods = activeIntro.belongsToMethods(belongsTo);
			assertEquals(pd.getReadMethod(), methods.get());
			assertEquals(pd.getWriteMethod(), methods.set());
			assertEquals(optionsInterf.getMethod(assocName), methods.option());
			assertEquals(conditionsInterf.getMethod(assocName), methods.condition());
		}
		assertEquals(assocCount, activeIntro.belongsToMethods().size());
	}

	@Test
	public void hasManyAssociationIntrospection() throws IntrospectionException, NoSuchMethodException {
		Association[] hasManys = activeAt.hasMany();
		List<Association> hasManysList = activeIntro.hasManys();
		int assocCount = hasManys.length;
		assertEquals(assocCount, hasManysList.size());
		for (Association hasMany : hasManys) {
			assertEquals(hasMany, activeIntro.hasMany(hasMany.with()));
			String typeName = hasMany.with().getSimpleName();
			String assocName = Introspector.decapitalize(typeName) + "s";
			PropertyDescriptor pd = new PropertyDescriptor(assocName, activeClass,
				"get" + typeName + "s", null);
			CollectionAssociationMethods methods = activeIntro
					.hasManyMethods(hasMany);
			assertEquals(pd.getReadMethod(), methods.get());
			assertEquals(optionsInterf.getMethod(assocName), methods.option());
			assertEquals(conditionsInterf.getMethod(assocName), methods.condition());
		}
		assertEquals(assocCount, activeIntro.hasManyMethods().size());
	}

	@Test
	public void activeMethodFilter() {
		ActiveDelegate delegate = new ActiveDelegate(activeClass);
		List<Method> handledMethods = new ArrayList<Method>();
		handledMethods.addAll(Arrays.asList(attrsInterf.getMethods()));
		handledMethods.addAll(Arrays.asList(Model.class.getMethods()));
		for (Method method : activeClass.getMethods()) {
			assertEquals(handledMethods.contains(method),
				delegate.isHandled(method));
		}
	}
	
	@Test
	public void optionsMethodFilter() {
		OptionsMethodFilter filter = new OptionsMethodFilter(activeClass);
		List<Method> handledMethods = Arrays.asList(optionsInterf.getMethods());
		for (Method method : optionsInterf.getMethods()) {
			assertEquals(handledMethods.contains(method),
					filter.isHandled(method));
		}
	}
	
	@Test
	public void conditionsMethodFilter() {
		ConditionsMethodFilter filter = new ConditionsMethodFilter(activeClass);
		List<Method> handledMethods = Arrays.asList(conditionsInterf.getMethods());
		for (Method method : conditionsInterf.getMethods()) {
			assertEquals(handledMethods.contains(method),
					filter.isHandled(method));
		}
	}

	@Test
	public void propertyMethods() {
		Post post = ActiveBeans.build(Post.class);
		Long id = 1L;
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
		Comment.Models comments = post.getComments();
		assertNotNull(comments);
	}

	@Test
	public void noopModel() {
		Model<?, ?, ?, ?> model = ActiveBeans.build(activeClass);
		assertNotNull(model);
		assertNull(model.attrs(null));
		assertFalse(model.destroy());
		assertFalse(model.save());
		assertFalse(model.update());
		assertFalse(model.update(null));
	}

	@Test
	public void noopModels() {
		Post post = ActiveBeans.build(Post.class);
		Comment.Models comments = post.getComments();
		assertNotNull(comments);
		assertNull(comments.add(null));
		assertNull(comments.all());
		assertNull(comments.all(null));
		assertNull(comments.attrs(null));
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
	public void noopOptions(){
		Options options = ActiveBeans.options(activeClass);
		assertNotNull(options);
		SingularOption<Options, Long> id = options.id();
		assertNotNull(id);
		assertSame(options, id.val(0L));
		CollectionOption<Options, org.activebeans.test.model.Comment.Options> comments = options.comments();
		assertNotNull(comments);
		assertSame(options, comments.val());
	}
	
	@Test
	public void noopCondtions(){
		Conditions conds = ActiveBeans.conditions(activeClass);
		assertNotNull(conds);
		Condition<Conditions, Long> id = conds.id();
		assertNotNull(id);
		assertSame(conds, id.eql(0L));
		assertSame(conds, id.gt(0L));
		assertSame(conds, id.gte(0L));
		assertSame(conds, id.like(0L));
		assertSame(conds, id.lt(0L));
		assertSame(conds, id.lte(0L));
		assertSame(conds, id.not(0L));
		SingularOption<Conditions, org.activebeans.test.model.Comment.Conditions> comments = conds.comments();
		assertNotNull(comments);
		assertSame(conds, comments.val(null));
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
		ActiveIntrospector pIntro = new ActiveIntrospector(Post.class);
		List<Property> keys = pIntro.keys();
		assertEquals(1, keys.size());
		assertEquals(pIntro.property("id"), keys.get(0));
	}

	@Test
	public void dataTypeDefinition() {
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
	public void columnDefinition() {
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
	public void createAndDropTableStatements() {
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
	public void createAndDropTable() throws SQLException {
		ActiveMigration migr = new ActiveMigration(Comment.class, ds);
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
		Table table = new ActiveMigration(activeClass, ds).table();
		ActiveBeans.migrate(activeClass);
		String tableName = table.name();
		assertTrue(dsIntro.tables().contains(tableName));
		ActiveBeansUtils.executeSql(ds, table.dropStatement());
		assertFalse(dsIntro.tables().contains(tableName));
	}

	@Test
	public void migrateAll() {
		List<String> tableNames = new ArrayList<String>();
		List<String> dropStmts = new ArrayList<String>();
		for (Class<? extends Model<?, ?, ?, ?>> clazz : activeClasses) {
			Table table = new ActiveMigration(clazz, ds).table();
			tableNames.add(table.name());
			dropStmts.add(table.dropStatement());
		}
		ActiveBeans.autoMigrate();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		ActiveBeansUtils.executeSql(ds, dropStmts);
	}

	@Test
	public void alterTableStatement() {
		Column id = new Column.Builder("id", new DataType("int")).key(true)
				.build();
		Column name = new Column.Builder("name", new DataType("varchar", 50))
				.build();
		Table table = new Table("test", id, name);
		String alter = "alter table " + table.name();
		assertEquals(alter + " add column " + id.definition(),
				table.alterStatement(id));
		assertEquals(alter + " add column " + id.definition() + ", add column "
				+ name.definition(), table.alterStatement(id, name));
	}

	@Test
	public void alterTable() {
		Column id = new Column.Builder("id", new DataType("int")).key(true)
				.build();
		Column name = new Column.Builder("name", new DataType("varchar", 50))
				.build();
		Table table = new Table("test", id);
		ActiveBeansUtils.executeSql(ds, table.createStatment());
		String tableName = table.name();
		List<String> cols = dsIntro.columns(tableName);
		assertEquals(1, cols.size());
		assertEquals(id.name(), cols.get(0));
		ActiveBeansUtils.executeSql(ds, table.alterStatement(name));
		List<String> updatedCols = dsIntro.columns(tableName);
		assertEquals(2, updatedCols.size());
		assertTrue(updatedCols.containsAll(Arrays.asList(new String[] {
				id.name(), name.name() })));
		ActiveBeansUtils.executeSql(ds, table.dropStatement());
	}

	@Test
	public void upgradeOne() {
		ActiveMigration migr = new ActiveMigration(activeClass, ds);
		Table activeTable = migr.table();
		List<String> colNames = new ArrayList<String>();
		List<Column> cols = activeTable.columns();
		for (Column c : cols) {
			colNames.add(c.name());
		}
		String tableName = activeTable.name();
		Table currentTable = new Table(tableName, cols.get(0));
		int colSize = cols.size();
		assertEquals(colSize, migr.alterColumns().size());
		assertEquals(activeTable.createStatment(), migr.alterStatement());
		ActiveBeansUtils.executeSql(ds, currentTable.createStatment());
		List<Column> alterCols = migr.alterColumns();
		assertEquals(colSize - currentTable.columns().size(), alterCols.size());
		assertEquals(activeTable.alterStatement(alterCols),
				migr.alterStatement());
		assertFalse(dsIntro.columns(tableName).containsAll(colNames));
		ActiveBeans.upgrade(activeClass);
		assertTrue(dsIntro.columns(tableName).containsAll(colNames));
		ActiveBeansUtils.executeSql(ds, activeTable.dropStatement());
	}

	@Test
	public void upgradeAll() {
		List<Table> tables = new ArrayList<Table>();
		List<String> tableNames = new ArrayList<String>();
		List<String> dropStmts = new ArrayList<String>();
		for (Class<? extends Model<? ,?, ?, ?>> clazz : activeClasses) {
			Table table = new ActiveMigration(clazz, ds).table();
			tables.add(table);
			tableNames.add(table.name());
			dropStmts.add(table.dropStatement());
		}
		ActiveBeansUtils.executeSql(ds, dropStmts);
		assertTrue(Collections.disjoint(dsIntro.tables(), tableNames));
		ActiveBeans.autoUpgrade();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		Table table = tables.get(0);
		String dropStmt = table.dropStatement();
		ActiveBeansUtils.executeSql(ds, dropStmt);
		ActiveBeans.autoUpgrade();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		ActiveBeansUtils.executeSql(ds, dropStmt);
		ActiveBeansUtils.executeSql(ds, new Table(table.name(), table.columns()
				.get(0)).createStatment());
		ActiveBeans.autoUpgrade();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		ActiveBeans.autoUpgrade();
		assertTrue(dsIntro.tables().containsAll(tableNames));
		ActiveBeansUtils.executeSql(ds, dropStmts);
	}

	@Test
	public void insertStatement() {
		String tableName = "test";
		String id = "id";
		String name = "name";
		Table table = new Table(tableName, 
			new Column.Builder(id, new DataType("int")).key(true).build(),
			new Column.Builder(name, new DataType("varchar")).build()
		);
		assertEquals("insert " + tableName + "(" + id + ", " + name + ") values(?, ?)", 
			table.insertStatement());
	}
	
	@Test 
	public void insert() {
		Table table = new Table("test", 
			new Column.Builder("id", new DataType("int")).key(true).build(),
			new Column.Builder("name", new DataType("varchar", 50)).build()
		);
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.executePreparedSql(ds, 
				table.insertStatement(), 1, "name value"));
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
}
