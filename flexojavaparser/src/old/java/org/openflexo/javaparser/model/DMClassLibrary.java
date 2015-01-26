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

package org.openflexo.javaparser.model;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import com.thoughtworks.qdox.model.ClassLibrary;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaClassParent;
import com.thoughtworks.qdox.model.Type;

public class DMClassLibrary extends ClassLibrary {

	protected static final Logger logger = Logger.getLogger(DMClassLibrary.class.getPackage().getName());

	private Hashtable<String, JavaClass> _javaClassCache;
	private HashSet<Type> _unresolvedTypes;

	public DMClassLibrary() {
		super(null);
		_javaClassCache = new Hashtable<String, JavaClass>();
		addDefaultLoader();
		_cachedTypes = new Hashtable<JavaClassParent, Hashtable<String, Hashtable<Integer, Type>>>();
		_unresolvedTypes = new HashSet<Type>();
		_unresolvedClassName = new HashSet<String>();
	}

	public void clearLibrary() {
		_javaClassCache.clear();
		_cachedTypes.clear();
		_unresolvedTypes.clear();
		_unresolvedClassName.clear();
		_javaClassCache = new Hashtable<String, JavaClass>();
		_cachedTypes = new Hashtable<JavaClassParent, Hashtable<String, Hashtable<Integer, Type>>>();
		_unresolvedTypes = new HashSet<Type>();
		_unresolvedClassName = new HashSet<String>();
	}

	public void close() {
		clearLibrary();
	}

	@Override
	public synchronized JavaClass getClassByName(String name) {
		return _javaClassCache.get(name);
	}

	public synchronized void registerClassForName(JavaClass aClass) {
		// logger.info("Registering "+aClass.getFullyQualifiedName());
		_javaClassCache.put(aClass.getFullyQualifiedName(), aClass);
	}

	private Hashtable<JavaClassParent, Hashtable<String, Hashtable<Integer, Type>>> _cachedTypes;

	private JavaClass retrieveCachedContext(JavaClass context) {
		JavaClass returned = _javaClassCache.get(context.getFullyQualifiedName());
		if (returned == null) {
			returned = context;
		}
		return returned;
	}

	public synchronized void clearUnresolvedTypesForNewRegisteredEntity(DMEntity entity) {
		for (Type t : _unresolvedTypes) {
			if (t instanceof DMType) {
				// logger.info("Unresolve "+t+" because "+entity+" newly registered");
				((DMType) t).clearUnresolved();
			}
		}
		if (_unresolvedClassName.contains(entity.getFullQualifiedName())) {
			_unresolvedClassName.remove(entity.getFullQualifiedName());
		}
	}

	public synchronized Type retrieveType(String name, int dimensions, JavaClassParent context) {
		// logger.info("retrieveType "+name);
		if (context instanceof JavaClass) {
			// Try to retrieve cache data
			context = retrieveCachedContext((JavaClass) context);
		}
		Hashtable<String, Hashtable<Integer, Type>> _typesForContext = _cachedTypes.get(context);
		if (_typesForContext != null) {
			Hashtable<Integer, Type> _typesWithName = _typesForContext.get(name);
			if (_typesWithName != null) {
				Type returned = _typesWithName.get(dimensions);
				if (returned != null && returned.isResolved() && _unresolvedTypes.contains(returned)) {
					// logger.info("Found "+returned+" newly resolved !");
					_unresolvedTypes.remove(returned);
				}
				return returned;
			}
		}
		return null;
	}

	public synchronized void registerType(Type typeToStore, String name, int dimensions, JavaClassParent context) {
		// logger.info("registerType "+name);
		if (context instanceof JavaClass) {
			// Try to retrieve cache data
			context = retrieveCachedContext((JavaClass) context);
		}
		Hashtable<String, Hashtable<Integer, Type>> _typesForContext = _cachedTypes.get(context);
		if (_typesForContext == null) {
			_typesForContext = new Hashtable<String, Hashtable<Integer, Type>>();
			_cachedTypes.put(context, _typesForContext);
		}
		Hashtable<Integer, Type> _typesWithName = _typesForContext.get(name);
		if (_typesWithName == null) {
			_typesWithName = new Hashtable<Integer, Type>();
			_typesForContext.put(name, _typesWithName);
		}
		_typesWithName.put(dimensions, typeToStore);
		if (!typeToStore.isResolved()) {
			// logger.info("Add "+typeToStore+" to unresolved types");
			_unresolvedTypes.add(typeToStore);
		}
	}

	private Set<String> _unresolvedClassName;

	@Override
	public synchronized Class<?> getClass(String className) {
		if (_unresolvedClassName.contains(className)) {
			return null;
		}
		Class<?> returned = super.getClass(className);
		if (returned == null) {
			returned = dataModel.getProject().getJarClassLoader().findClass(className);
		}
		if (returned != null) {
			// logger.info("Found "+returned+" in "+returned.getClassLoader());
		} else {
			_unresolvedClassName.add(className);
		}
		return returned;
	}

}
