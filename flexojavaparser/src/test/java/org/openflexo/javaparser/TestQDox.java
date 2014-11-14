package org.openflexo.javaparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaSource;

@RunWith(OrderedRunner.class)
public class TestQDox {

	static JavaProjectBuilder jpBuilder;

	@BeforeClass
	public static void initSourceFolder() {
		jpBuilder = new JavaProjectBuilder();
		Resource directory = ResourceLocator.locateResource("TestJavaParser");
		if (directory instanceof FileResourceImpl) {
			File sourceDirectory = ((FileResourceImpl) directory).getFile();
			System.out.println("sourceDirectory=" + sourceDirectory);
			jpBuilder.addSourceTree(sourceDirectory);
			// System.out.println(jpBuilder.getClasses());
			// System.out.println(jpBuilder.getSources());
		}

		assertEquals(2, jpBuilder.getSources().size());
		assertEquals(2, jpBuilder.getClasses().size());

	}

	@Test
	@TestOrder(1)
	public void test1() {

		JavaClass testJava1 = jpBuilder.getClassByName("test.TestJava1");
		assertNotNull(testJava1);

		// System.out.println("testJava1=" + testJava1);

		System.out.println("source=" + testJava1.getSource());
		assertTrue(testJava1.getSource() instanceof DefaultJavaSource);
		System.out.println("codeBlock=" + testJava1.getCodeBlock());

		assertEquals("Javadoc for TestJava1", testJava1.getComment());
		assertEquals(1, testJava1.getTags().size());
		assertEquals("author", testJava1.getTags().get(0).getName());
		assertEquals("sylvain", testJava1.getTags().get(0).getValue());

		assertEquals(1, testJava1.getAnnotations().size());
		assertEquals(jpBuilder.getClassByName(Deprecated.class.getName()), testJava1.getAnnotations().get(0).getType());

		assertEquals(3, testJava1.getFields().size());
		assertEquals("anInt", testJava1.getFieldByName("anInt").getName());
		assertEquals(jpBuilder.getClassByName(Integer.TYPE.getName()), testJava1.getFieldByName("anInt").getType());

		JavaField someInts = testJava1.getFieldByName("someInts");
		assertEquals("someInts", someInts.getName());
		assertTrue(someInts.getType() instanceof JavaParameterizedType);
		assertEquals(1, someInts.getType().getDimensions());
		assertEquals("new int[10]", someInts.getInitializationExpression());

		JavaField someStrings = testJava1.getFieldByName("someStrings");
		assertEquals("someStrings", someStrings.getName());
		assertTrue(someStrings.getType() instanceof JavaParameterizedType);
		assertEquals(jpBuilder.getClassByName(String.class.getName()), ((JavaParameterizedType) someStrings.getType())
				.getActualTypeArguments().get(0));
		assertEquals(List.class.getName(), ((JavaParameterizedType) someStrings.getType()).getCanonicalName());

	}

	/*@Test
	@TestOrder(2)
	public void test2() {

		JavaClass testJava2 = jpBuilder.getClassByName("test.TestJava2");
		assertNotNull(testJava2);

		System.out.println("testJava2=" + testJava2);

		System.out.println("source=" + testJava2.getSource());
		System.out.println("codeBlock=" + testJava2.getCodeBlock());

		JavaClass testString = jpBuilder.getClassByName("java.lang.String");
		assertNotNull(testString);

		System.out.println("testString=" + testString);

		System.out.println("source=" + testString.getSource());
		System.out.println("codeBlock=" + testString.getCodeBlock());

		System.out.println("sourceClass=" + testString.getSource().getClass());

		JavaClass testTestQDox = jpBuilder.getClassByName("org.openflexo.flexo.javaparser.TestQDox");
		assertNotNull(testTestQDox);

		System.out.println("testTestQDox=" + testTestQDox);

		System.out.println("source=" + testTestQDox.getSource());
		System.out.println("codeBlock=" + testTestQDox.getCodeBlock());

		System.out.println("sourceClass=" + testTestQDox.getSource().getClass());

	}

	@Test
	@TestOrder(3)
	public void test3() {

		JavaClass testString = jpBuilder.getClassByName("java.lang.String");
		assertNotNull(testString);

		System.out.println("testString=" + testString);

		System.out.println("source=" + testString.getSource());
		System.out.println("codeBlock=" + testString.getCodeBlock());

		System.out.println("sourceClass=" + testString.getSource().getClass());

	}

	@Test
	@TestOrder(4)
	public void test4() {

		JavaClass testTestQDox = jpBuilder.getClassByName("org.openflexo.flexo.javaparser.TestQDox");
		assertNotNull(testTestQDox);

		System.out.println("testTestQDox=" + testTestQDox);

		System.out.println("source=" + testTestQDox.getSource());
		System.out.println("codeBlock=" + testTestQDox.getCodeBlock());

		System.out.println("sourceClass=" + testTestQDox.getSource().getClass());

	}

	@Test
	@TestOrder(5)
	public void test5() {

		System.out.println(System.getProperty("user.dir"));
		jpBuilder.addSourceTree(new File(System.getProperty("user.dir")));

		JavaClass testTestQDox = jpBuilder.getClassByName("org.openflexo.flexo.javaparser.TestQDox");
		assertNotNull(testTestQDox);

		System.out.println("testTestQDox=" + testTestQDox);

		System.out.println("source=" + testTestQDox.getSource());
		System.out.println("codeBlock=" + testTestQDox.getCodeBlock());

		System.out.println("sourceClass=" + testTestQDox.getSource().getClass());

	}*/

}
