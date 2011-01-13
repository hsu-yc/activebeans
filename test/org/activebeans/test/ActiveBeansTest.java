package org.activebeans.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activebeans.Active;
import org.activebeans.ActiveBeans;
import org.activebeans.ActiveIntrospector;
import org.activebeans.ActiveMethodFilter;
import org.activebeans.Association;
import org.activebeans.BelongsToAssociationMethods;
import org.activebeans.HasManyAssociationMethods;
import org.activebeans.Model;
import org.activebeans.Property;
import org.activebeans.PropertyAccessors;
import org.activebeans.test.model.Post;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveBeansTest {

	private Class<? extends Model> activeClass;

	private Active activeAt;

	private Class<?> activeInterf;

	private Class<?> activeCollectionInterf;

	private ActiveIntrospector<?> activeIntro;

	@Before
	public void init() throws ClassNotFoundException {
		activeClass = Post.class;
		activeAt = activeClass.getAnnotation(Active.class);
		activeInterf = Class.forName(activeClass.getPackage().getName()
				+ ".Active" + activeClass.getSimpleName());
		activeCollectionInterf = Class.forName(activeClass.getName()
				+ "$Models");
		activeIntro = ActiveIntrospector.of(activeClass);
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
	public void noopInstantiation() {
		Model activeInst = ActiveBeans.build(activeClass);
		assertTrue(activeClass.isInstance(activeInst));
		activeInst.attributes(null);
		assertFalse(activeInst.destroy());
		assertFalse(activeInst.save());
		assertFalse(activeInst.update());
		assertFalse(activeInst.update(null));
	}

}
