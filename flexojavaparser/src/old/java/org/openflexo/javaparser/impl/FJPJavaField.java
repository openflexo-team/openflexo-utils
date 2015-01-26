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

import org.openflexo.foundation.Inspectors;
import org.openflexo.javaparser.ParsedJavaField;
import org.openflexo.javaparser.impl.model.DMType;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

public class FJPJavaField extends FJPJavaEntity implements ParsedJavaField {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FJPJavaField.class.getPackage().getName());

	private JavaField _qdJavaField;

	public FJPJavaField(JavaField qdJavaField, FJPJavaSource aJavaSource) {
		super(qdJavaField, aJavaSource);
		_qdJavaField = qdJavaField;
	}

	public String getCallSignature() {
		return _qdJavaField.getCallSignature();
	}

	public String getDeclarationSignature(boolean withModifiers) {
		return _qdJavaField.getDeclarationSignature(withModifiers);
	}

	public String getDeclarationSignature() {
		return _qdJavaField.getDeclarationSignature(true);
	}

	@Override
	public String getInitializationExpression() {
		return _qdJavaField.getInitializationExpression();
	}

	@Override
	public String getName() {
		return _qdJavaField.getName();
	}

	public FJPJavaClass getParentClass() {
		return getClass((JavaClass) _qdJavaField.getParent());
	}

	@Override
	public String getInspectorName() {
		return Inspectors.CG.JAVA_FIELD_INSPECTOR;
	}

	@Override
	public int getLinesCount() {
		return 1;
	}

	@Override
	public DMType getType() {
		return (DMType) _qdJavaField.getType();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getUniqueIdentifier() {
		return getName();
	}

}
