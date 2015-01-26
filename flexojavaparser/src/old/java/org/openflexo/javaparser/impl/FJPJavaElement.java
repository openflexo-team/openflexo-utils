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

package org.openflexo.javaparser.impl;

import java.util.logging.Logger;

import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.ClassLibrary;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public abstract class FJPJavaElement {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FJPJavaElement.class.getPackage().getName());

	protected FJPJavaSource javaSource;

	public FJPJavaElement(FJPJavaSource aJavaSource) {
		super();
		javaSource = aJavaSource;
	}

	public JavaParser getJavaParser() {
		return getJavaSource().getJavaParser();
	}

	public FJPJavaSource getJavaSource() {
		return javaSource;
	}

	public FJPJavaClass getClass(JavaClass aClass) {
		return getJavaSource().getClass(aClass);
	}

	public FJPJavaMethod getMethod(JavaMethod aMethod) {
		return getJavaSource().getMethod(aMethod);
	}

	public FJPJavaField getField(JavaField aField) {
		return getJavaSource().getField(aField);
	}

	public FJPJavaElement getFJPJavaElement(AbstractJavaEntity abstractJavaEntity) {
		return getJavaSource().getFJPJavaElement(abstractJavaEntity);
	}

	protected FJPJavaClass[] retrieveClasses(JavaClass[] someClasses) {
		return getJavaSource().retrieveClasses(someClasses);
	}

	protected FJPJavaMethod[] retrieveMethods(JavaMethod[] someMethods) {
		return getJavaSource().retrieveMethods(someMethods);
	}

	protected FJPJavaField[] retrieveFields(JavaField[] someFields) {
		return getJavaSource().retrieveFields(someFields);
	}

	public ClassLibrary getClassLibrary() {
		return getJavaSource().getClassLibrary();
	}

	public String getClassNamePrefix() {
		return getJavaSource().getClassNamePrefix();
	}

	public Type getType(String fullQualifiedName) {
		return getJavaParser().getType(fullQualifiedName);
	}

	public Type getType(Class aClass) {
		return getJavaParser().getType(aClass);
	}

	public FJPJavaClass getClassByName(String fullQualifiedName) {
		return getJavaParser().getClassByName(fullQualifiedName);
	}

}
