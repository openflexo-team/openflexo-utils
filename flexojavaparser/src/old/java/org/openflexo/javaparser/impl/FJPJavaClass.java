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

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InvalidNameException;

import org.openflexo.javaparser.DuplicateMethodSignatureException;
import org.openflexo.javaparser.ParsedJavaClass;
import org.openflexo.javaparser.ParsedJavaElement;
import org.openflexo.javaparser.impl.FJPTypeResolver.UnresolvedTypeException;
import org.openflexo.javaparser.model.DMEntity;
import org.openflexo.javaparser.model.DMMethod;
import org.openflexo.javaparser.model.DMProperty;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.Type;

public class FJPJavaClass extends FJPJavaEntity implements ParsedJavaClass {

	private static final Logger logger = Logger.getLogger(FJPJavaClass.class.getPackage().getName());

	private final JavaClass _qdJavaClass;

	public FJPJavaClass(JavaClass qdJavaClass, FJPJavaSource aJavaSource) {
		super(qdJavaClass, aJavaSource);
		_qdJavaClass = qdJavaClass;
	}

	public String getName() {
		return _qdJavaClass.getName();
	}

	public Type asType() {
		return _qdJavaClass.asType();
	}

	private FJPJavaClass[] _derivedClasses;

	public FJPJavaClass[] getDerivedClasses() {
		if (_derivedClasses == null) {
			_derivedClasses = retrieveClasses(_qdJavaClass.getDerivedClasses());
		}
		return _derivedClasses;
	}

	@Override
	public FJPJavaField getFieldByName(String name) {
		return getField(_qdJavaClass.getFieldByName(name));
	}

	private FJPJavaField[] _fields;

	public FJPJavaField[] getFields() {
		if (_fields == null) {
			_fields = retrieveFields(_qdJavaClass.getFields());
		}
		return _fields;
	}

	public String getFullyQualifiedName() {
		return _qdJavaClass.getFullyQualifiedName();
	}

	private FJPJavaClass[] _implementedInterfaces;

	public FJPJavaClass[] getImplementedInterfaces() {
		if (_implementedInterfaces == null) {
			_implementedInterfaces = retrieveClasses(_qdJavaClass.getImplementedInterfaces());
		}
		return _implementedInterfaces;
	}

	public Type[] getImplements() {
		return _qdJavaClass.getImplements();
	}

	private String implementsAsString = null;

	public String getImplementsAsString() {
		if (implementsAsString == null) {
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (Type t : getImplements()) {
				sb.append((isFirst ? "" : ",") + t.toString());
				isFirst = false;
			}
			implementsAsString = sb.toString();
		}
		return implementsAsString;
	}

	private FJPJavaMethod[] _methods;

	public FJPJavaMethod[] getMethods() {
		if (_methods == null) {
			_methods = retrieveMethods(_qdJavaClass.getMethods());
		}
		return _methods;
	}

	public FJPJavaMethod getMethodBySignature(String name, Type[] parameterTypes, boolean superclasses) {
		return getMethod(_qdJavaClass.getMethodBySignature(name, parameterTypes, superclasses));
	}

	@Override
	public FJPJavaMethod getMethodBySignature(String signature) {
		// logger.info("Looking for "+signature);

		// First look for full qualified
		for (FJPJavaMethod m : getMethods()) {
			// logger.info("Compare: "+signature+" and "+m.getCallSignature());
			if (m.getCallSignature().equals(signature)) {
				return m;
			}
		}

		// Not found
		// Try without qualifiers
		String unqualifiedSignature = FJPJavaMethod.unqualifySignature(signature);
		for (FJPJavaMethod m : getMethods()) {
			String us = FJPJavaMethod.unqualifySignature(m.getCallSignature());
			// logger.info("Compare: "+unqualifiedSignature+" and "+us);
			if (unqualifiedSignature.equals(us)) {
				return m;
			}
		}

		return null;
	}

	public FJPJavaMethod getMethodBySignature(String name, Type... parameterTypes) {
		return getMethod(_qdJavaClass.getMethodBySignature(name, parameterTypes));
	}

	public FJPJavaMethod[] getMethods(boolean name) {
		return retrieveMethods(_qdJavaClass.getMethods(name));
	}

	public FJPJavaMethod[] getMethodsBySignature(String name, Type[] parameterTypes, boolean superclasses) {
		return retrieveMethods(_qdJavaClass.getMethodsBySignature(name, parameterTypes, superclasses));
	}

	public FJPJavaClass getNestedClassByName(String arg0) {
		return getClass(_qdJavaClass.getNestedClassByName(arg0));
	}

	private FJPJavaClass[] _nestedClasses;

	public FJPJavaClass[] getNestedClasses() {
		if (_nestedClasses == null) {
			_nestedClasses = retrieveClasses(_qdJavaClass.getNestedClasses());
		}
		return _nestedClasses;
	}

	public String getPackage() {
		return _qdJavaClass.getPackage();
	}

	public Type getSuperClass() {
		if (_qdJavaClass != null) {
			return (Type) _qdJavaClass.getSuperClass();
		}
		return null;
	}

	public FJPJavaClass getSuperJavaClass() {
		return getClass(_qdJavaClass.getSuperJavaClass());
	}

	public String getSuperClassAsString() {
		if (getSuperClass() != null) {
			return getSuperClass().toString();
		}
		return "java.lang.Object";
	}

	public boolean isEnum() {
		return _qdJavaClass.isEnum();
	}

	public boolean isInner() {
		return _qdJavaClass.isInner();
	}

	public boolean isInterface() {
		return _qdJavaClass.isInterface();
	}

	private Vector<FJPJavaEntity> orderedChildren = null;

	public Vector<FJPJavaEntity> getOrderedChildren() {
		if (orderedChildren == null) {
			orderedChildren = new Vector<FJPJavaEntity>();
			for (FJPJavaField f : getFields()) {
				orderedChildren.add(f);
			}
			for (FJPJavaMethod m : getMethods()) {
				orderedChildren.add(m);
			}
			for (FJPJavaClass c : getNestedClasses()) {
				orderedChildren.add(c);
			}
			Collections.sort(orderedChildren, new Comparator<FJPJavaEntity>() {
				@Override
				public int compare(FJPJavaEntity o1, FJPJavaEntity o2) {
					return o1.getLineNumber() - o2.getLineNumber();
				}
			});
		}
		return orderedChildren;
	}

	@Override
	public Vector<? extends ParsedJavaElement> getMembers() {
		return getOrderedChildren();
	}

	@Override
	public int getLinesCount() {
		return 1;
	}

	private void registerMethods(DMEntity entity, FJPDMSet context, FJPJavaSource source, Vector<String> excludedSignatures) {
		Vector<DMMethod> allMethods = FJPDMMapper.searchForMethods(this, entity.getDMModel(), context, source, true, excludedSignatures);
		for (DMMethod m : allMethods) {
			entity.registerMethod(m);
		}
		;
	}

	private Vector registerProperties(DMEntity entity, FJPDMSet context, FJPJavaSource source, boolean includesGetOnlyProperties) {
		Vector<String> excludedSignatures = new Vector<String>();
		for (Enumeration en = FJPDMMapper.searchForProperties(this, entity.getDMModel(), context, source, includesGetOnlyProperties, true,
				excludedSignatures).elements(); en.hasMoreElements();) {
			DMProperty next = (DMProperty) en.nextElement();
			entity.registerProperty(next, false);
		}
		;
		return excludedSignatures;
	}

	private void updateProperty(DMEntity entity, DMProperty property, FJPDMSet context, FJPJavaSource source)
			throws FJPTypeResolver.CrossReferencedEntitiesException, InvalidNameException, DuplicatePropertyNameException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Update property " + property);
		}
		DMProperty updatedProperty = FJPDMMapper.makeProperty(this, property.getName(), entity.getDMModel(), context, source, true, true,
				null);
		if (updatedProperty == null) {
			logger.warning("Could not instanciate property " + property.getName() + " this should not happen.");
			return;
		}
		// For now, don't change implementation type from model reinjection
		updatedProperty.setImplementationType(property.getImplementationType());
		// Nor read-only property
		updatedProperty.setIsReadOnly(property.getIsReadOnly());
		logger.info("Update property " + property);
		property.update(updatedProperty, true);
	}

	private DMProperty createProperty(DMEntity entity, PropertyReference propertyReference, FJPDMSet context, FJPJavaSource source)
			throws FJPTypeResolver.CrossReferencedEntitiesException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Create property " + propertyReference.getName());
		}
		DMProperty returnedProperty = FJPDMMapper.makeProperty(this, propertyReference.getName(), entity.getDMModel(), context, source,
				true, true, null);
		logger.info("For property " + propertyReference.getName() + " Create property " + returnedProperty);
		if (returnedProperty != null) {
			returnedProperty.allowModifiedPropagation();
			entity.registerProperty(returnedProperty, false);
		}
		return returnedProperty;
	}

	private void updateMethod(DMEntity entity, DMMethod method, FJPDMSet context, FJPJavaSource source)
			throws FJPTypeResolver.CrossReferencedEntitiesException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Update method " + method);
		}
		DMMethod updatedMethod = FJPDMMapper.makeMethod(this, method.getSignature(), entity.getDMModel(), context, source, true);
		if (updatedMethod == null) {
			logger.info("Delete method " + method);
			method.delete();
		} else {
			logger.info("Update method " + method);
			try {
				method.update(updatedMethod, true);
			} catch (DuplicateMethodSignatureException e) {
				e.printStackTrace();
			}
		}
	}

	private DMMethod createMethod(DMEntity entity, MethodReference methodReference, FJPDMSet context, FJPJavaSource source)
			throws FJPTypeResolver.CrossReferencedEntitiesException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Create method " + methodReference.getSignature());
		}
		DMMethod returnedMethod = FJPDMMapper.makeMethod(this, methodReference.getSignature(), entity.getDMModel(), context, source, true);
		logger.info("For method " + methodReference.getSignature() + " Create method " + returnedMethod);
		if (returnedMethod != null) {
			returnedMethod.allowModifiedPropagation();
			entity.registerMethod(returnedMethod);
		}
		return returnedMethod;
	}

	private void updateEntity(DMEntity entity, FJPDMSet context, FJPJavaSource source) throws DuplicateClassNameException,
			InvalidNameException, FJPTypeResolver.CrossReferencedEntitiesException, FJPTypeResolver.UnresolvedTypeException {
		if (entity == null) {
			logger.warning("Entity is null !");
			return;
		}

		entity.setEntityPackageName(getPackage());
		entity.setEntityClassName(getName());

		DMType parentType = getSuperClass();
		entity.setParentType(parentType, true);

		if (FJPTypeResolver.isResolvable(parentType, entity.getDMModel(), context, source)) {
			FJPTypeResolver.resolveEntity(parentType, entity.getDMModel(), context, source, true);
		} else {
			throw new FJPTypeResolver.UnresolvedTypeException(parentType);
		}

		/*if (parentType != null) {
			if (FJPTypeResolver.isResolvable(parentType, entity.getDMModel(), context, source)) {
				DMEntity resolvedParentEntity = FJPTypeResolver.resolveEntity(parentType, entity.getDMModel(), context, source, true);
				if (resolvedParentEntity == null) throw new FJPTypeResolver.UnresolvedTypeException(parentType);
				entity.setParentEntity(resolvedParentEntity);
			}
			else {
				throw new FJPTypeResolver.UnresolvedTypeException(parentType);
			}
		}*/
	}

	/**
	 * Update this entity given a supplied set of properties and methods to include
	 * 
	 * @param aClassReference
	 * @throws UnresolvedTypeException
	 */
	public Vector<DMObject> update(DMEntity entity, ClassReference aClassReference, FJPDMSet context, FJPJavaSource source)
			throws FJPTypeResolver.CrossReferencedEntitiesException, FJPTypeResolver.UnresolvedTypeException {
		if (entity == null || aClassReference == null) {
			logger.warning("Could not update: could not find entity or class reference !");
		} else {
			Vector<DMObject> newObjects = new Vector<DMObject>();

			// First update package and class name, and parent class if different
			try {
				updateEntity(entity, context, source);
			} catch (FJPTypeResolver.CrossReferencedEntitiesException e) {
				// TODO implements this
				logger.warning("Needs to be stored and invoked in second step for cross referenced entities...");
				throw e;
			} catch (DuplicateClassNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Then what's about properties ?
			Vector<DMProperty> propertiesToDelete = new Vector<DMProperty>(entity.getProperties().values());
			for (PropertyReference nextPropertyRef : aClassReference.getProperties()) {
				if (nextPropertyRef.isSelected()) {
					DMProperty property = entity.getDMProperty(nextPropertyRef.getName());
					if (property != null) {
						// A selected property already declared, update it
						try {
							updateProperty(entity, property, context, source);
						} catch (FJPTypeResolver.CrossReferencedEntitiesException e) {
							// TODO implements this
							logger.warning("Needs to be stored and invoked in second step for cross referenced entities...");
						} catch (InvalidNameException e) {
							// TODO implements this
							logger.warning(e.getMessage());
						} catch (DuplicatePropertyNameException e) {
							e.printStackTrace();
							logger.warning(e.getMessage());
						}

						propertiesToDelete.remove(property);
					} else {
						// This is a newly imported property, creates it
						try {
							newObjects.add(createProperty(entity, nextPropertyRef, context, source));
						} catch (FJPTypeResolver.CrossReferencedEntitiesException e) {
							// TODO implements this
							logger.warning("Needs to be stored and invoked in second step for cross referenced entities...");
						}
					}
				}
			}
			for (Enumeration en = new Vector<DMProperty>(propertiesToDelete).elements(); en.hasMoreElements();) {
				DMProperty toDelete = (DMProperty) en.nextElement();
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Delete property " + toDelete);
				}
				toDelete.delete();
			}

			// Then what's about methods ?
			Vector<DMMethod> methodsToDelete = new Vector<DMMethod>(entity.getMethods().values());
			for (MethodReference nextMethodRef : aClassReference.getMethods()) {
				if (nextMethodRef.isSelected()) {
					DMMethod method = entity.getDeclaredMethod(nextMethodRef.getSignature());
					if (method != null) {
						// A selected method already declared, update it
						try {
							updateMethod(entity, method, context, source);
						} catch (FJPTypeResolver.CrossReferencedEntitiesException e) {
							// TODO implements this
							logger.warning("Needs to be stored and invoked in second step for cross referenced entities...");
						}
						methodsToDelete.remove(method);
					} else {
						// This is a newly imported property, creates it
						try {
							newObjects.add(createMethod(entity, nextMethodRef, context, source));
						} catch (FJPTypeResolver.CrossReferencedEntitiesException e) {
							// TODO implements this
							logger.warning("Needs to be stored and invoked in second step for cross referenced entities...");
						}
					}
				}
			}
			for (Enumeration en = new Vector<DMMethod>(methodsToDelete).elements(); en.hasMoreElements();) {
				DMMethod toDelete = (DMMethod) en.nextElement();
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Delete method " + toDelete);
				}
				toDelete.delete();
			}

			logger.info("Updated " + getName());

			return newObjects;
		}
		return null;
	}

	@Override
	public String toString() {
		return getFullyQualifiedName();
	}

	@Override
	public String getUniqueIdentifier() {
		return getFullyQualifiedName();
	}

}
