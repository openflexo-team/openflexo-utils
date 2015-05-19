/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexojavaparser, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

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
