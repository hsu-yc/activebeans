package org.activebeans.test;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.util.proxy.ProxyObject;

import javax.sql.DataSource;

import lombok.eclipse.handlers.HandleActive;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveBeansUtils;
import org.activebeans.ActiveDelegate;
import org.activebeans.ActiveIntrospector;
import org.activebeans.ActiveMigration;
import org.activebeans.Association;
import org.activebeans.AttributeMethodHandler;
import org.activebeans.CollectionAssociationMethods;
import org.activebeans.Column;
import org.activebeans.Condition;
import org.activebeans.ConditionsMethodFilter;
import org.activebeans.ConditionsMethodHandler;
import org.activebeans.ConditionsMethodHandler.Operator;
import org.activebeans.ConditionsMethodHandler.Order;
import org.activebeans.DataSourceIntrospector;
import org.activebeans.DataType;
import org.activebeans.GeneratedKeysMapHandler;
import org.activebeans.Model;
import org.activebeans.ModelsMethodHandler;
import org.activebeans.OptionsMethodFilter;
import org.activebeans.OptionsMethodHandler;
import org.activebeans.Property;
import org.activebeans.PropertyMethods;
import org.activebeans.QueryPath;
import org.activebeans.ResultSetHandler;
import org.activebeans.SingularAssociationMethods;
import org.activebeans.SingularOption;
import org.activebeans.Table;
import org.activebeans.test.model.Comment;
import org.activebeans.test.model.Comment.Options;
import org.activebeans.test.model.Post;
import org.activebeans.test.model.Post.Conditions;
import org.activebeans.test.model.Post.Models;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
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
		Date created = new Date();
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
	public void propertyMethodsHandler() {
		Class<? extends Model<?, ?, ?, ?>> postClass = Post.class;
		AttributeMethodHandler post = new AttributeMethodHandler(postClass);
		ActiveIntrospector postIntro = new ActiveIntrospector(postClass);
		Property id = postIntro.property("id");
		Long idVal = 1L;
		post.set(id, idVal);
		assertEquals(idVal, post.get(id));
		Property subject = postIntro.property("subject");
		String subjectVal = "foo";
		post.set(subject, subjectVal);
		assertEquals(subjectVal, post.get(subject));
		Property created = postIntro.property("created");
		Date createdVal = new Date();
		post.set(created, createdVal);
		assertEquals(createdVal, post.get(created));
		Class<? extends Model<?, ?, ?, ?>> commentClass = Comment.class;
		AttributeMethodHandler comment = new AttributeMethodHandler(commentClass);
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property body = commentIntro.property("body");
		String bodyVal = "bar";
		comment.set(body, bodyVal);
		assertEquals(bodyVal, comment.get(body));
	}
	
	@Test
	public void belongsToAssociationMethodsHandler() {
		Class<? extends Model<?, ?, ?, ?>> commentClass = Comment.class;
		AttributeMethodHandler comment = new AttributeMethodHandler(commentClass);
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Class<Post> postClass = Post.class;
		Post postVal = ActiveBeans.build(postClass);
		Association post = commentIntro.belongsTo(postClass);
		comment.set(post, postVal);
		assertEquals(postVal, comment.get(post));
	}

	@Test
	public void hasManyAssociationMethodsHandler() {
		Class<? extends Model<?, ?, ?, ?>> postClass = Post.class;
		AttributeMethodHandler post = new AttributeMethodHandler(postClass);
		ActiveIntrospector postIntro = new ActiveIntrospector(postClass);
		assertNotNull(post.get(postIntro.hasMany(Comment.class)));
	}
	
	@Test
	public void optionsMethodHandler() {
		Class<Comment> commentClass = Comment.class;
		OptionsMethodHandler comment = new OptionsMethodHandler(commentClass);
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property id = commentIntro.property("id");
		Long idVal = 1L;
		comment.set(id, idVal);
		assertEquals(idVal, comment.get(id));
		assertTrue(comment.properties().containsValue(idVal));
		Class<Post> postClass = Post.class;
		Association post = commentIntro.belongsTo(postClass);
		Post postVal = ActiveBeans.build(postClass);
		comment.set(post, postVal);
		assertEquals(postVal, comment.get(post));
		assertTrue(comment.associations().containsValue(postVal));
	}
	
	@Test
	public void attrMethod() {
		Class<Comment> commentClass = Comment.class;
		Long id1 = 1L;
		Long id2 = 2L;
		Long id3 = 3L;
		String body = "body";
		String subj = "subj";
		Class<Post> postClass = Post.class;
		Comment comment = ActiveBeans.build(commentClass);
		assertEquals(comment, comment.attrs(
			ActiveBeans.options(commentClass)
				.id().val(id1)
				.body().val(body)
				.post().val(
					ActiveBeans.options(postClass)
						.subject().val(subj)
						.comments().val(
							ActiveBeans.options(commentClass)
								.id().val(id2),
							ActiveBeans.options(commentClass)
								.id().val(id3)	
						)
				)
		));
		assertEquals(id1, comment.getId());
		assertEquals(body, comment.getBody());
		Post post = comment.getPost();
		assertNotNull(post);
		assertEquals(subj, post.getSubject());
		List<Comment> comments = new ArrayList<Comment>();
		for (Comment c : post.getComments()) {
			comments.add(c);
		}
		assertEquals(2, comments.size());
		assertEquals(id2, comments.get(0).getId());
		assertEquals(id3, comments.get(1).getId());
	}
	
	@Test 
	public void saveMethod(){
		ActiveBeans.migrate(activeClass);
		Post model = ActiveBeans.build(activeClass);
		assertTrue(model.save());
		Long id = model.getId();
		assertTrue(id != null && id != 0);
	}
	
	@Test
	public void buildModelWithAttrs(){
		ActiveBeans.migrate(activeClass);
		String subj = "test";
		Date created = new Date();
		Post model = ActiveBeans.build(activeClass, 
			ActiveBeans.options(activeClass)
				.subject().val(subj)
				.created().val(created)
		);
		assertNotNull(model);
		assertEquals(subj, model.getSubject());
		assertEquals(created, model.getCreated());
	}
	
	@Test
	public void createModel(){
		ActiveBeans.migrate(activeClass);
		Post model = ActiveBeans.create(activeClass);
		assertNotNull(model);
		Long id = model.getId();
		assertTrue(id != null && id != 0);
	}
	
	@Test
	public void createModelWithAttrs(){
		ActiveBeans.migrate(activeClass);
		String subj = "test";
		Date created = new Date();
		Post model = ActiveBeans.create(activeClass, 
			ActiveBeans.options(activeClass)
				.subject().val(subj)
				.created().val(created)
		);
		assertNotNull(model);
		assertEquals(subj, model.getSubject());
		assertEquals(created, model.getCreated());
		Long id = model.getId();
		assertTrue(id != null && id != 0);
	}
	
	@Test
	public void getModel(){		
		ActiveBeans.migrate(activeClass);
		String subj = "test";
		Date created = new Date();
		Long id = ActiveBeans.create(activeClass, 
			ActiveBeans.options(activeClass)
				.subject().val(subj)
				.created().val(created)
		).getId();
		assertTrue(id != null && id != 0);
		Post model = ActiveBeans.get(activeClass, id);
		assertNotNull(model);
		assertEquals(subj, model.getSubject());
		assertEquals(new java.sql.Date(created.getTime()).toString(), 
			model.getCreated().toString());
	}
	
	@Test
	public void updateMethod(){		
		ActiveBeans.migrate(activeClass);
		Post model = ActiveBeans.build(activeClass);
		assertTrue(model.save());
		Long id = model.getId();
		assertTrue(id != null && id != 0);
		String subj = "test";
		Date created = new Date();
		model.setSubject(subj);
		model.setCreated(created);
		assertTrue(model.update());
		Post obj = ActiveBeans.get(activeClass, id);
		assertNotNull(obj);
		assertEquals(subj, obj.getSubject());
		assertEquals(new java.sql.Date(created.getTime()).toString(), 
			obj.getCreated().toString());
	}
	
	@Test
	public void updateMethodWithAttrs(){		
		ActiveBeans.migrate(activeClass);
		Post model = ActiveBeans.build(activeClass);
		assertTrue(model.save());
		Long id = model.getId();
		assertTrue(id != null && id != 0);
		String subj = "test";
		Date created = new Date();
		assertTrue(model.update(
			ActiveBeans.options(activeClass)
				.subject().val(subj)
				.created().val(created)
		));
		Post obj = ActiveBeans.get(activeClass, id);
		assertNotNull(obj);
		assertEquals(subj, obj.getSubject());
		assertEquals(new java.sql.Date(created.getTime()).toString(), 
			obj.getCreated().toString());
	}
	
	@Test
	public void updateAllModels(){		
		ActiveBeans.migrate(activeClass);
		int insertCnt = 3;
		for(int i=0; i< insertCnt; i++){
			ActiveBeans.create(activeClass);
		}
		String subj = "test";
		Date created = new Date();
		assertTrue(ActiveBeans.update(activeClass,
			ActiveBeans.options(activeClass)
				.subject().val(subj)
				.created().val(created)
		));
		int cnt = 0;
		for (Post p : ActiveBeans.all(activeClass)) {
			assertEquals(subj, p.getSubject());
			assertEquals(new java.sql.Date(created.getTime()).toString(), 
				p.getCreated().toString());
			cnt++;
		}
		assertEquals(insertCnt, cnt);
	}

	@Test
	public void destroyMethod(){		
		ActiveBeans.migrate(activeClass);
		Post model = ActiveBeans.build(activeClass);
		assertTrue(model.save());
		Long id = model.getId();
		assertTrue(id != null && id != 0);
		assertNotNull(ActiveBeans.get(activeClass, id));
		assertTrue(model.destroy());
		assertNull(ActiveBeans.get(activeClass, id));
	}
	
	@Test
	public void destroyAllModels(){		
		ActiveBeans.migrate(activeClass);
		for(int i=0; i< 3; i++){
			ActiveBeans.create(activeClass);
		}
		assertTrue(ActiveBeans.destroy(activeClass));
		int cnt = 0;
		for (@SuppressWarnings("unused") Post p : ActiveBeans.all(activeClass)) {
			cnt++;
		}
		assertEquals(0, cnt);
	}
	
	@Test
	public void conditionsMethodHandler() {
		Class<Comment> commentClass = Comment.class;
		ConditionsMethodHandler comment = new ConditionsMethodHandler(commentClass);
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property id = commentIntro.property("id");
		Long idVal = 1L;
		comment.set(id, Operator.EQL, idVal);
		assertEquals(idVal, comment.get(id, Operator.EQL));
		assertEquals(idVal, comment.properties().get(id).get(Operator.EQL));
		Class<Post> postClass = Post.class;
		Association post = commentIntro.belongsTo(postClass);
		Conditions postVal = ActiveBeans.conditions(postClass);
		comment.set(post, postVal);
		assertEquals(postVal, comment.get(post));
		assertEquals(postVal, comment.associations().get(post));
		comment.order(id, Order.ASC);
		assertEquals(Order.ASC, comment.order(id));
		assertTrue(comment.orders().containsValue(Order.ASC));
		Property body = commentIntro.property("body");
		comment.field(id);
		comment.field(body);
		Set<Property> fields = comment.fields();
		assertTrue(fields.contains(id));
		assertTrue(fields.contains(body));
	}
	
	@Test
	public void allModels(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Models models = ActiveBeans.all(activeClass);
		assertNotNull(models);
		List<String> subjs = Arrays.asList(subj1, subj2);
		int cnt = 0;
		for (Post post : models) {
			assertNotNull(post);
			assertTrue(subjs.contains(post.getSubject()));
			assertTrue(activeClass.isAssignableFrom(post.getClass()));
			cnt++;
		}
		assertEquals(2, cnt);
	}
	
	@Test
	public void allModelsWithEqlConditions(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Models models = ActiveBeans.all(activeClass, ActiveBeans.conditions(activeClass)
			.subject().eql(subj1));
		assertNotNull(models);
		int cnt = 0;
		for (Post post : models) {
			assertNotNull(post);
			assertEquals(subj1, post.getSubject());
			assertTrue(activeClass.isAssignableFrom(post.getClass()));
			cnt++;
		}
		assertEquals(1, cnt);
	}
	
	@Test
	public void firstModel(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Post first = ActiveBeans.first(activeClass);
		assertNotNull(first);
		assertTrue(activeClass.isAssignableFrom(first.getClass()));
		assertEquals(subj1, first.getSubject());
		Post firstInModels = ActiveBeans.all(activeClass).first();
		assertNotNull(firstInModels);
		assertTrue(activeClass.isAssignableFrom(firstInModels.getClass()));
		assertEquals(subj1, firstInModels.getSubject());
	}
	
	@Test
	public void firstModelWithConditions(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Post first = ActiveBeans.first(activeClass, ActiveBeans.conditions(activeClass)
			.subject().eql(subj2));
		assertNotNull(first);
		assertTrue(activeClass.isAssignableFrom(first.getClass()));
		assertEquals(subj2, first.getSubject());
		assertNull(ActiveBeans.first(activeClass, ActiveBeans.conditions(activeClass)
			.subject().eql("subj")));
		Post firstInModels = ActiveBeans.all(activeClass).first(ActiveBeans.conditions(activeClass)
				.subject().eql(subj2));
		assertNotNull(firstInModels);
		assertTrue(activeClass.isAssignableFrom(firstInModels.getClass()));
		assertEquals(subj2, firstInModels.getSubject());
		assertNull(ActiveBeans.all(activeClass).first(ActiveBeans.conditions(activeClass)
			.subject().eql("subj")));
	}
	
	@Test
	public void firstOrCreateModel(){
		ActiveBeans.migrate(activeClass);
		int insertCnt = 0;
		Long id = 1L;
		String subj = "subj";
		Post.Options options = ActiveBeans.options(activeClass)
			.subject().val(subj);
		assertEquals(id, ActiveBeans.firstOrCreate(activeClass, options).getId());
		insertCnt++;
		String subj2 = "subj2";
		for(int i=0; i<2; i++){
			ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
				.subject().val(subj2));
			insertCnt++;
		}
		Post model = ActiveBeans.firstOrCreate(activeClass);
		assertEquals(id, model.getId());
		assertEquals(subj, model.getSubject());
		Long id2 = 2L;
		Post model2 = ActiveBeans.firstOrCreate(activeClass, options, 
			ActiveBeans.conditions(activeClass)
				.subject().eql(subj2));
		assertEquals(id2, model2.getId());
		assertEquals(subj2, model2.getSubject());
		Post model3 = ActiveBeans.firstOrCreate(activeClass, options, 
				ActiveBeans.conditions(activeClass)
					.subject().eql("subj3"));
		insertCnt++;
		assertEquals(Long.valueOf(insertCnt), model3.getId());
		assertEquals(subj, model3.getSubject());
	}
	
	@Test
	public void lastModel(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Post last = ActiveBeans.last(activeClass);
		assertNotNull(last);
		assertTrue(activeClass.isAssignableFrom(last.getClass()));
		assertEquals(subj2, last.getSubject());
		Post lastinModels = ActiveBeans.all(activeClass).last();
		assertNotNull(lastinModels);
		assertTrue(activeClass.isAssignableFrom(lastinModels.getClass()));
		assertEquals(subj2, lastinModels.getSubject());
	}
	
	@Test
	public void lastModelWithConditions(){
		ActiveBeans.migrate(activeClass);
		String subj1 = "subj1";
		String subj2 = "subj2";
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj1));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
			.subject().val(subj2));
		Post last = ActiveBeans.last(activeClass, ActiveBeans.conditions(activeClass)
			.subject().eql(subj2));
		assertNotNull(last);
		assertTrue(activeClass.isAssignableFrom(last.getClass()));
		assertEquals(subj2, last.getSubject());
		assertNull(ActiveBeans.first(activeClass, ActiveBeans.conditions(activeClass)
			.subject().eql("subj")));
		Post lastInModels = ActiveBeans.all(activeClass).last(ActiveBeans.conditions(activeClass)
				.subject().eql(subj2));
		assertNotNull(lastInModels);
		assertTrue(activeClass.isAssignableFrom(lastInModels.getClass()));
		assertEquals(subj2, lastInModels.getSubject());
		assertNull(ActiveBeans.all(activeClass).first(ActiveBeans.conditions(activeClass)
			.subject().eql("subj")));
	}
	
	@Test
	public void orderModel(){
		ActiveBeans.migrate(activeClass);
		List<String> subjs = new ArrayList<String>();
		for(int i=0; i<3; i++){
			String subj = String.valueOf(i);
			ActiveBeans.create(activeClass, ActiveBeans.options(activeClass)
				.subject().val(subj));
			subjs.add(subj);
		}
		Collections.reverse(subjs);
		List<String> oSubjs = new ArrayList<String>();
		Conditions conds = ActiveBeans.conditions(activeClass)
			.subject().desc();
		Models rs = ActiveBeans.all(activeClass, conds);
		for (Post post : rs) {
			oSubjs.add(post.getSubject());
		}
		assertArrayEquals(subjs.toArray(), oSubjs.toArray());
		String firstSubj = oSubjs.get(0);
		assertEquals(firstSubj, ActiveBeans.first(activeClass, conds).getSubject());
		assertEquals(firstSubj, ActiveBeans.firstOrCreate(activeClass, null, conds)
			.getSubject());
		assertEquals(oSubjs.get(oSubjs.size() - 1), ActiveBeans.last(activeClass, conds).getSubject());
	}
	
	@Test 
	public void chainModelConditions(){
		Class<Comment> commentClass = Comment.class;
		Long id= 1L;
		String body = "body";
		Comment.Models models = ActiveBeans.all(commentClass)
			.all(ActiveBeans.conditions(commentClass)
				.id().eql(id)
				.id().asc()
				.id().field()
			).all(ActiveBeans.conditions(commentClass)
				.id().desc()
				.body().eql(body)
				.body().field()
			).popular();
		ModelsMethodHandler modelsHandler = (ModelsMethodHandler) ((ProxyObject)models).getHandler();
		ConditionsMethodHandler conds = (ConditionsMethodHandler) 
			((ProxyObject)modelsHandler.conditions()).getHandler();
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property idProp = commentIntro.property("id");
		assertEquals(id, conds.get(idProp, Operator.EQL));
		assertEquals(Order.DESC, conds.order(idProp));
		assertTrue(conds.fields().contains(idProp));
		Property bodyProp = commentIntro.property("body");
		assertEquals(body, conds.get(bodyProp, Operator.EQL));
		assertEquals(Order.DESC, conds.order(idProp));
		assertTrue(conds.fields().contains(bodyProp));
		assertEquals(Order.ASC, conds.order(bodyProp));
	}
	
	@Test
	public void models() {
		Class<Comment> commentClass = Comment.class;
		ActiveBeans.migrate(commentClass);
		int size = 3;
		for(int i=0; i < size; i++){
			ActiveBeans.create(commentClass);
		}
		Comment.Models models = ActiveBeans.all(commentClass);
		models.add(null);
		Set<Comment> oModels = Collections.singleton(ActiveBeans.build(commentClass));
		models.addAll(oModels);
		models.all(null);
		models.attrs(null);
		models.build();
		models.build(null);
		models.clear();
		models.contains(null);
		models.containsAll(oModels);
		models.create();
		models.create(null);
		models.destroy();
		models.equals(null);
		models.first();
		models.first(null);
		models.hashCode();
		models.isEmpty();
		models.iterator();
		models.last();
		models.last(null);
		models.remove(null);
		models.removeAll(oModels);
		models.retainAll(oModels);
		models.save();
		models.size();
		models.toArray();
		models.toArray(new Comment[0]);
		models.update();
		models.update(null);
		models.popular();
	}
	
	@Test
	public void options(){
		Class<Comment> commentClass = Comment.class;
		Options comment = ActiveBeans.options(commentClass);
		assertNotNull(comment);
		SingularOption<Options, Long> id = comment.id();
		Long idVal = 1L;
		assertNotNull(id);
		assertSame(comment, id.val(idVal));
		Class<Post> postClass = Post.class;
		SingularOption<Options, Post.Options> post = comment.post();
		assertNotNull(post);
		String subjVal = "subj";
		String bodyVal = "body";
		assertSame(comment, post.val(
			ActiveBeans.options(postClass)
				.subject().val(subjVal)
				.comments().val(
					ActiveBeans.options(commentClass)
						.body().val(bodyVal)
				)
		));
		OptionsMethodHandler handler = (OptionsMethodHandler)((ProxyObject)comment).getHandler();
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property idProp = commentIntro.property("id");
		assertEquals(idVal, handler.get(idProp));
		Object postObj = handler.get(commentIntro.belongsTo(postClass));
		assertTrue(postClass.isAssignableFrom(postObj.getClass()));
		Post postVal = postClass.cast(postObj);
		assertEquals(subjVal, postVal.getSubject());
		List<Comment> comments = new ArrayList<Comment>();
		for (Comment c : postVal.getComments()) {
			assertNotNull(c);
			comments.add(c);
		}
		assertEquals(1, comments.size());
		assertEquals(bodyVal, comments.get(0).getBody());
	}
	
	@Test
	public void conditions(){
		Class<Comment> commentClass = Comment.class;
		Comment.Conditions comment = 
			ActiveBeans.conditions(commentClass);
		assertNotNull(comment);
		Condition<Comment.Conditions, Long> id = comment.id();
		assertNotNull(id);
		Long idVal = 1L;
		assertSame(comment, id.eql(idVal));
		assertSame(comment, id.gt(idVal));
		assertSame(comment, id.gte(idVal));
		assertSame(comment, id.like(idVal));
		assertSame(comment, id.lt(idVal));
		assertSame(comment, id.lte(idVal));
		assertSame(comment, id.not(idVal));
		Class<Post> postClass = Post.class;
		QueryPath<Comment.Conditions, Conditions> postPath = comment.post();
		assertNotNull(postPath);
		String subjVal = "subj";
		String bodyVal = "body";
		assertSame(comment, postPath.where(
			ActiveBeans.conditions(postClass)
				.subject().eql(subjVal)
				.comments().where(
					ActiveBeans.conditions(commentClass)
						.body().eql(bodyVal)
				)
		));
		ConditionsMethodHandler commentHandler = (ConditionsMethodHandler)((ProxyObject)comment).getHandler();
		ActiveIntrospector commentIntro = new ActiveIntrospector(commentClass);
		Property idProp = commentIntro.property("id");
		Map<Operator, Object> idMap = commentHandler.get(idProp);
		assertEquals(idVal, idMap.get(Operator.EQL));
		assertEquals(idVal, idMap.get(Operator.GT));
		assertEquals(idVal, idMap.get(Operator.GTE));
		assertEquals(idVal, idMap.get(Operator.LIKE));
		assertEquals(idVal, idMap.get(Operator.LT));
		assertEquals(idVal, idMap.get(Operator.LTE));
		assertEquals(idVal, idMap.get(Operator.NOT));
		Object post = commentHandler.get(commentIntro.belongsTo(postClass));
		assertTrue(ActiveBeans.conditions(postClass).getClass().isAssignableFrom(
			post.getClass()));
		ConditionsMethodHandler postHandler = (ConditionsMethodHandler)((ProxyObject)post).getHandler();
		ActiveIntrospector postIntro = new ActiveIntrospector(postClass);
		Property subjProp = postIntro.property("subject");
		assertEquals(subjVal, postHandler.get(subjProp, Operator.EQL));
		Object commentObj = postHandler.get(postIntro.hasMany(commentClass));
		assertTrue(comment.getClass().isAssignableFrom(commentObj.getClass()));
		assertEquals(bodyVal, ((ConditionsMethodHandler)((ProxyObject)commentObj).getHandler())
			.get(commentIntro.property("body"), Operator.EQL));
		Comment.Conditions comment2 = ActiveBeans.conditions(commentClass);
		Conditions post2 = comment2.post().on();
		Comment.Conditions comment3 = post2.subject().eql(subjVal)
			.comments().on();
		assertSame(comment2, comment3);
		assertSame(post2, comment3.post().on());
		comment3.id().eql(idVal);
		assertEquals(subjVal, ((ConditionsMethodHandler)((ProxyObject)post2).getHandler())
			.get(subjProp).get(Operator.EQL));
		assertEquals(idVal, ((ConditionsMethodHandler)((ProxyObject)comment2).getHandler())
			.get(idProp).get(Operator.EQL));
		comment.id().field();
		Set<Property> fields = commentHandler.fields();
		assertTrue(fields.contains(idProp));
		Property bodyProp = commentIntro.property("body");
		assertFalse(fields.contains(bodyProp));
		comment.id().desc().body().asc();
		assertEquals(Order.DESC, commentHandler.order(idProp));
		assertEquals(Order.ASC, commentHandler.order(bodyProp));
		Map<Property, Order> orders = commentHandler.orders();
		assertEquals(Order.DESC, orders.get(idProp));
		assertEquals(Order.ASC, orders.get(bodyProp));
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
		assertTrue(updatedCols.containsAll(Arrays.asList(id.name(), name.name())));
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
			new Column.Builder(id, new DataType("int")).key(true)
				.autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar")).build()
		);
		assertEquals("insert " + tableName + "(" + id + ", " + name + ") values(default, ?)", 
			table.insertStatement());
	}
	
	@Test 
	public void insert() {
		Table table = new Table("test", 
			new Column.Builder("id", new DataType("int"))
				.key(true).autoIncrement(true).build(),
			new Column.Builder("name", new DataType("varchar", 50)).build()
		);
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.executePreparedSql(
				ds,
				new ResultSetHandler() {
					@Override
					public void handle(ResultSet keys) throws SQLException {
						assertTrue(keys.next());
					}
				},
				table.insertStatement(), "name value")
			);
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test 
	public void insertModelTable() {
		Table table = new ActiveMigration(activeClass, ds).table();
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.insert(ds, activeClass, ActiveBeans.build(activeClass),
				new GeneratedKeysMapHandler() {
					@Override
					public void handle(Map<Property, Object> keys) {
						int generatedCnt = 0;
						for (Property prop : activeIntro.properties()) {
							if(prop.autoIncrement()){
								generatedCnt++;
							}
						}
						assertEquals(generatedCnt, keys.size());
						for (Object key : keys.values()) {
							assertNotNull(key);
						}
					}
				})
			);
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test
	public void selectStatement() {
		String tableName = "test";
		String id = "id";
		String name = "name";
		Table table = new Table(tableName, 
			new Column.Builder(id, new DataType("int")).key(true)
				.autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar")).build()
		);
		assertEquals("select " + id + ", " + name + " from " + tableName 
			+ " where " + id + " = ?", table.selectStatement());
	}
	
	@Test 
	public void select() {
		final String id = "id";
		final String name = "name";
		final Table table = new Table("test", 
			new Column.Builder(id, new DataType("int"))
				.key(true).autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar", 50)).build()
		);
		final String nameVal = "name value";
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.executePreparedSql(
				ds,
				new ResultSetHandler() {
					@Override
					public void handle(ResultSet keys) throws SQLException {
						assertTrue(keys.next());
						final int idVal = keys.getInt(1);
						ActiveBeansUtils.executePreparedSqlForResult(ds, new ResultSetHandler() {
							@Override
							public void handle(ResultSet rs) throws SQLException {
								assertTrue(rs.next());
								assertEquals(idVal, rs.getInt(id));
								assertEquals(nameVal, rs.getString(name));
							}
						}, table.selectStatement(), idVal);
					}
				},
				table.insertStatement(), nameVal)
			);
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test 
	public void selectModelTable() {
		final Map<Property, Object> generatedKeys = new LinkedHashMap<Property, Object>();
		Table table = new ActiveMigration(activeClass, ds).table();
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.insert(ds, activeClass, ActiveBeans.build(activeClass),
				new GeneratedKeysMapHandler() {
					@Override
					public void handle(Map<Property, Object> keys) {
						generatedKeys.putAll(keys);
					}
				})
			);
			Object obj = ActiveBeansUtils.get(ds, activeClass, 
				new ArrayList<Object>(generatedKeys.values()));
			assertNotNull(obj);
			AttributeMethodHandler handler = ((ActiveDelegate)((ProxyObject)obj).getHandler())
				.attrHandler();
			for (Entry<Property, Object> e : generatedKeys.entrySet()) {
				assertEquals(e.getValue(), handler.get(e.getKey()));
			}
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test
	public void updateStatement() {
		String tableName = "test";
		String id = "id";
		String name = "name";
		String age = "age";
		Table table = new Table(tableName, 
			new Column.Builder(id, new DataType("int")).key(true)
				.autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar")).build(),
			new Column.Builder(age, new DataType("int")).build()
		);
		assertEquals("update " + tableName + " set "+ name + " = ?, " + age 
			+ " = ? where " + id + " = ?", table.updateStatement());
	}
	
	@Test 
	public void update() {
		final String id = "id";
		final String name = "name";
		final String age = "age";
		final Table table = new Table("test", 
			new Column.Builder(id, new DataType("int"))
				.key(true).autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar", 50)).build(),
			new Column.Builder(age, new DataType("int")).build()
		);
		final String nameVal = "name value";
		final int ageVal = 10;
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.executePreparedSql(
				ds,
				new ResultSetHandler() {
					@Override
					public void handle(ResultSet keys) throws SQLException {
						assertTrue(keys.next());
						final int idVal = keys.getInt(1);
						assertEquals(1, ActiveBeansUtils.executePreparedSql(
							ds, table.updateStatement(), nameVal, ageVal, idVal));
						ActiveBeansUtils.executePreparedSqlForResult(ds, new ResultSetHandler() {
							@Override
							public void handle(ResultSet rs) throws SQLException {
								assertTrue(rs.next());
								assertEquals(idVal, rs.getInt(id));
								assertEquals(nameVal, rs.getString(name));
								assertEquals(ageVal, rs.getInt(age));
							}
						}, table.selectStatement(), idVal);
					}
				},
				table.insertStatement(), "", 0)
			);
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test 
	public void updateModelTable() {
		final Map<Property, Object> generatedKeys = new LinkedHashMap<Property, Object>();
		Table table = new ActiveMigration(activeClass, ds).table();
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			final Post post = ActiveBeans.build(activeClass);
			assertEquals(1, ActiveBeansUtils.insert(ds, activeClass, post,
				new GeneratedKeysMapHandler() {
					@Override
					public void handle(Map<Property, Object> keys) {
						generatedKeys.putAll(keys);
					}
				})
			);
			final String subj = "subject";
			post.setSubject(subj);
			assertEquals(1, ActiveBeansUtils.update(ds, activeClass, post));
			Post obj = ActiveBeansUtils.get(ds, activeClass, 
				new ArrayList<Object>(generatedKeys.values()));
			assertNotNull(obj);
			assertEquals(subj, obj.getSubject());
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test
	public void deleteStatement() {
		String tableName = "test";
		String id = "id";
		String name = "name";
		String age = "age";
		Table table = new Table(tableName, 
			new Column.Builder(id, new DataType("int")).key(true)
				.autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar")).build(),
			new Column.Builder(age, new DataType("int")).build()
		);
		assertEquals("delete from " + tableName + " where " + id + " = ?", 
			table.deleteStatement());
	}
	
	@Test 
	public void delete() {
		final String id = "id";
		final String name = "name";
		final String age = "age";
		final Table table = new Table("test", 
			new Column.Builder(id, new DataType("int"))
				.key(true).autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar", 50)).build(),
			new Column.Builder(age, new DataType("int")).build()
		);
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			assertEquals(1, ActiveBeansUtils.executePreparedSql(
				ds,
				new ResultSetHandler() {
					@Override
					public void handle(ResultSet keys) throws SQLException {
						assertTrue(keys.next());
						final int idVal = keys.getInt(1);
						String selectStmt = table.selectStatement();
						ActiveBeansUtils.executePreparedSqlForResult(ds, new ResultSetHandler() {
							@Override
							public void handle(ResultSet rs) throws SQLException {
								assertTrue(rs.next());
							}
						}, selectStmt, idVal);
						assertEquals(1, ActiveBeansUtils.executePreparedSql(ds, 
							table.deleteStatement(), idVal));
						ActiveBeansUtils.executePreparedSqlForResult(ds, new ResultSetHandler() {
							@Override
							public void handle(ResultSet rs) throws SQLException {
								assertFalse(rs.next());
							}
						}, selectStmt, idVal);
					}
				},
				table.insertStatement(), "", 0)
			);
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test 
	public void deleteModelTable() {
		final Map<Property, Object> generatedKeys = new LinkedHashMap<Property, Object>();
		Table table = new ActiveMigration(activeClass, ds).table();
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			final Post post = ActiveBeans.build(activeClass);
			assertEquals(1, ActiveBeansUtils.insert(ds, activeClass, post,
				new GeneratedKeysMapHandler() {
					@Override
					public void handle(Map<Property, Object> keys) {
						generatedKeys.putAll(keys);
					}
				})
			);
			ArrayList<Object> keys = new ArrayList<Object>(generatedKeys.values());
			assertNotNull(ActiveBeansUtils.get(ds, activeClass, keys));
			assertEquals(1, ActiveBeansUtils.delete(ds, activeClass, post));
			assertNull(ActiveBeansUtils.get(ds, activeClass, keys));
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test
	public void selectAllStatement() {
		String tableName = "test";
		String id = "id";
		String name = "name";
		Table table = new Table(tableName, 
			new Column.Builder(id, new DataType("int")).key(true)
				.autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar")).build()
		);
		String selectAll = "select " + id + ", " + name + " from " + tableName;
		assertEquals(selectAll, table.selectAllStatement());
		assertEquals(selectAll + " order by " + id, table.selectAllWithOrderStatement());
	}
	
	@Test 
	public void selectAll() {
		final String id = "id";
		final String name = "name";
		final Table table = new Table("test", 
			new Column.Builder(id, new DataType("int"))
				.key(true).autoIncrement(true).build(),
			new Column.Builder(name, new DataType("varchar", 50)).build()
		);
		final String nameVal = "name value";
		try{
			ActiveBeansUtils.executeSql(ds, table.createStatment());
			final int rows = 3; 
			for(int i=0; i<rows; i++){
				assertEquals(1, ActiveBeansUtils.executePreparedSql(
					ds, table.insertStatement(), nameVal));
			}
			ActiveBeansUtils.executeSqlForResult(ds, new ResultSetHandler() {
				@Override
				public void handle(ResultSet rs) throws SQLException {
					assertTrue(rs.last());
					assertEquals(rows, rs.getRow());
				}
			}, table.selectAllWithOrderStatement());
		}finally{
			ActiveBeansUtils.executeSql(ds, table.dropStatement());
		}
	}
	
	@Test
	public void selectWithConditionsStatement(){
		long idEql = 1;
		String subjEql = "1";
		String subjGt = "2";
		String subjGte = "3";
		String subjLike = "4";
		String subjLt = "5";
		String subjLte = "6";
		String subjNot = "7";
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		Conditions conds = ActiveBeans.conditions(postClass)
			.id().eql(idEql)
			.subject().eql(subjEql)
			.subject().gt(subjGt)
			.subject().gte(subjGte)
			.subject().like(subjLike)
			.subject().lt(subjLt)
			.subject().lte(subjLte)
			.subject().not(subjNot);
		assertEquals(table.selectAllStatement() 
				+ " where id = ?"
				+ " and subject = ?"
				+ " and subject > ?"
				+ " and subject >= ?"
				+ " and subject like ?"
				+ " and subject < ?"
				+ " and subject <= ?"
				+ " and subject != ?", 
			table.selectAllStatement(conds));
		ConditionsMethodHandler handler = (ConditionsMethodHandler)((ProxyObject)conds).getHandler();
		assertArrayEquals(new Object[]{idEql, subjEql, subjGt, subjGte, subjLike, 
			subjLt, subjLte, subjNot}, handler.propertyValues().toArray());
	}
	
	@Test
	public void selectFirstStatement(){
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		assertEquals(
			table.selectAllWithOrderStatement() + " limit 1",
			table.selectFirstStatement()
		);
	}
	
	@Test
	public void selectFirstWithConditionsStatement(){
		long idEql = 1;
		String subjEql = "1";
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		Conditions conds = ActiveBeans.conditions(postClass)
			.id().eql(idEql)
			.subject().eql(subjEql);
		assertEquals(table.selectAllStatement() 
				+ " where id = ?"
				+ " and subject = ?"
				+ " " + table.defaultOrder()
				+ " " + table.firstLimit(), 
			table.selectFirstStatement(conds));
		ConditionsMethodHandler handler = (ConditionsMethodHandler)((ProxyObject)conds).getHandler();
		assertArrayEquals(new Object[]{idEql, subjEql}, handler.propertyValues().toArray());
	}
	
	@Test
	public void selectLastStatement(){
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		assertEquals(
			table.selectAllWithReverseOrderStatement() + " limit 1",
			table.selectLastStatement()
		);
	}
	
	@Test
	public void selectLastWithConditionsStatement(){
		long idEql = 1;
		String subjEql = "1";
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		Conditions conds = ActiveBeans.conditions(postClass)
			.id().eql(idEql)
			.subject().eql(subjEql);
		assertEquals(table.selectAllStatement() 
				+ " where id = ?"
				+ " and subject = ?"
				+ " " + table.reverseOrder()
				+ " " + table.firstLimit(), 
			table.selectLastStatement(conds));
		ConditionsMethodHandler handler = (ConditionsMethodHandler)((ProxyObject)conds).getHandler();
		assertArrayEquals(new Object[]{idEql, subjEql}, handler.propertyValues().toArray());
	}
	
	@Test
	public void updateAllStatement(){
		String subj = "1";
		Date created = new Date();
		Class<Post> postClass = Post.class;
		Table table = new ActiveMigration(postClass, ds).table();
		Post.Options options = ActiveBeans.options(postClass)
			.subject().val(subj)
			.created().val(created);
		assertEquals("update " + table.name()
				+ " set subject = ?"
				+ ", created = ?",
			table.updateAllStatement(options));
		OptionsMethodHandler handler = (OptionsMethodHandler)((ProxyObject)options).getHandler();
		assertArrayEquals(new Object[]{subj, created}, handler.properties().values().toArray());
	}
	
	@Test
	public void deleteAllStatement(){
		Table table = new ActiveMigration(Post.class, ds).table();
		assertEquals("delete from " + table.name(), table.deleteAllStatement());
	}
	
	@Test
	public void orderClause(){
		String field1 = "a";
		String field2 = "b";
		String field3 = "c";
		Order order1 = Order.ASC;
		Order order2 = Order.DESC;
		Order order3 = Order.ASC;
		Map<String, Order> order = new LinkedHashMap<String, Order>();
		order.put(field1, order1);
		order.put(field2, order2);
		order.put(field3, order3);
		assertEquals("order by " + field1 + " " + order1
				+ ", " + field2 + " " + order2
				+ ", " + field3 + " " + order3, 
			Table.stringOrderClause(order));
	}
	
	@Test
	public void reverseOrder(){
		assertEquals(Order.DESC, Order.ASC.reverse());
		assertEquals(Order.ASC, Order.DESC.reverse());
		Conditions conds = ActiveBeans.conditions(activeClass)
			.id().asc()
			.subject().desc();
		ConditionsMethodHandler handler = (ConditionsMethodHandler) ((ProxyObject)conds).getHandler();
		Map<Property, Order> orders = handler.orders();
		Map<Property, Order> reverse = handler.reverseOrders();
		for (Entry<Property, Order> e : orders.entrySet()) {
			assertEquals(e.getValue().reverse(), reverse.get(e.getKey()));
		}
	}
	
	@Test 
	public void chainConditions(){
		Long id= 1L;
		ConditionsMethodHandler conds1 = (ConditionsMethodHandler) (
				(ProxyObject)ActiveBeans.conditions(activeClass)
					.id().eql(id)
					.id().asc()
					.id().field()
			).getHandler();
		String subj = "subj";
		ConditionsMethodHandler conds2 = (ConditionsMethodHandler) (
				(ProxyObject)ActiveBeans.conditions(activeClass)
					.id().desc()
					.subject().eql(subj)
					.subject().field()
			).getHandler();
		conds1.chain(conds2);
		Property idProp = activeIntro.property("id");
		assertEquals(id, conds1.get(idProp, Operator.EQL));
		assertEquals(Order.DESC, conds1.order(idProp));
		assertTrue(conds1.fields().contains(idProp));
		Property subjProp = activeIntro.property("subject");
		assertEquals(subj, conds1.get(subjProp, Operator.EQL));
		assertEquals(Order.DESC, conds1.order(idProp));
		assertTrue(conds1.fields().contains(subjProp));
	}
	
}
