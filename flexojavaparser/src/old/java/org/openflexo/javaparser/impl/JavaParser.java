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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.javaparser.ClassSourceCode;
import org.openflexo.javaparser.FieldSourceCode;
import org.openflexo.javaparser.MethodSourceCode;

import com.thoughtworks.qdox.model.ClassLibrary;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

public class JavaParser {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JavaParser.class.getPackage().getName());

	private FJPJavaDocBuilder _builder;
	private Hashtable<String, Type> _types;
	private Vector<FJPJavaSource> _sources;
	private Vector<ClassLibrary> _classLibraries;

	public static void init() {
		DefaultJavaParser javaParser = new DefaultJavaParser();
		MethodSourceCode.setJavaMethodParser(javaParser);
		FieldSourceCode.setJavaFieldParser(javaParser);
		ClassSourceCode.setJavaClassParser(javaParser);
	}

	public JavaParser(ClassLibrary classLibrary) {
		_builder = new FJPJavaDocBuilder(classLibrary);
		_types = new Hashtable<String, Type>();
		_sources = new Vector<FJPJavaSource>();
		_classLibraries = null;
	}

	protected JavaSource parseFileForSource(File sourceFile, FJPJavaSource source) throws FileNotFoundException, IOException {
		JavaSource returned = _builder.addSource(sourceFile);
		addSource(source);
		return returned;
	}

	protected JavaSource parseStringForSource(String sourceString, FJPJavaSource source) {
		JavaSource returned = _builder.addSource(new StringReader(sourceString));
		addSource(source);
		return returned;
	}

	protected JavaSource parseStringForSource(String sourceString, FJPJavaSource source, String sourceInfo) {
		JavaSource returned = _builder.addSource(new StringReader(sourceString), sourceInfo);
		addSource(source);
		return returned;
	}

	private void addSource(FJPJavaSource source) {
		_sources.add(source);
		_classLibraries = null;
	}

	public Vector<ClassLibrary> getClassLibraries() {
		if (_classLibraries == null) {
			_classLibraries = new Vector<ClassLibrary>();
			for (FJPJavaSource source : _sources) {
				if (!_classLibraries.contains(source.getClassLibrary())) {
					_classLibraries.add(source.getClassLibrary());
				}
			}
		}
		return _classLibraries;
	}

	public Type getType(String fullQualifiedName) {
		Type returned = _types.get(fullQualifiedName);
		if (returned == null) {
			returned = retrieveType(fullQualifiedName);
			_types.put(fullQualifiedName, returned);
		}
		return returned;
	}

	private Type retrieveType(String fullQualifiedName) {
		for (ClassLibrary cl : getClassLibraries()) {
			if (cl.contains(fullQualifiedName)) {
				return cl.getClassByName(fullQualifiedName).asType();
			}
		}
		return new Type(fullQualifiedName);
	}

	public Type getType(Class aClass) {
		return getType(aClass.getName());
	}

	public FJPJavaClass getClassByName(String fullQualifiedName) {
		JavaClass foundClass = _builder.getClassByName(fullQualifiedName);
		if (foundClass == null) {
			return null;
		}
		for (FJPJavaSource source : _sources) {
			if (source.getClass(foundClass) != null) {
				return source.getClass(foundClass);
			}
		}
		return null;
	}

}
