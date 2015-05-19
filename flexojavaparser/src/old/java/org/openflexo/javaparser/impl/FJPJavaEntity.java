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

import org.openflexo.javaparser.ParsedJavaElement;

import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.JavaClassParent;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

public abstract class FJPJavaEntity extends FJPJavaElement implements ParsedJavaElement {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FJPJavaEntity.class.getPackage().getName());

	private final AbstractJavaEntity _qdJavaEntity;
	private JavadocItem _javadocItem;

	public FJPJavaEntity(AbstractJavaEntity qdJavaEntity, FJPJavaSource aJavaSource) {
		super(aJavaSource);
		_qdJavaEntity = qdJavaEntity;
	}

	@Override
	public JavadocItem getJavadoc() {
		if (_javadocItem == null
				&& (_qdJavaEntity.getComment() != null || _qdJavaEntity.getTags() != null && _qdJavaEntity.getTags().length > 0)) {
			_javadocItem = new JavadocItem(_qdJavaEntity.getComment(), _qdJavaEntity.getTags());
		}
		return _javadocItem;
	}

	public String getComment() {
		return _qdJavaEntity.getComment();
	}

	@Override
	public int getLineNumber() {
		return _qdJavaEntity.getLineNumber();
	}

	public abstract int getLinesCount();

	@Override
	public String[] getModifiers() {
		return _qdJavaEntity.getModifiers();
	}

	public JavaClassParent getParent() {
		return _qdJavaEntity.getParent();
	}

	public JavaSource getSource() {
		return _qdJavaEntity.getSource();
	}

	public boolean isAbstract() {
		return _qdJavaEntity.isAbstract();
	}

	public boolean isFinal() {
		return _qdJavaEntity.isFinal();
	}

	public boolean isNative() {
		return _qdJavaEntity.isNative();
	}

	public boolean isPrivate() {
		return _qdJavaEntity.isPrivate();
	}

	public boolean isProtected() {
		return _qdJavaEntity.isProtected();
	}

	public boolean isPublic() {
		return _qdJavaEntity.isPublic();
	}

	public boolean isStatic() {
		return _qdJavaEntity.isStatic();
	}

	public boolean isStrictfp() {
		return _qdJavaEntity.isStrictfp();
	}

	public boolean isSynchronized() {
		return _qdJavaEntity.isSynchronized();
	}

	public boolean isTransient() {
		return _qdJavaEntity.isTransient();
	}

	public boolean isVolatile() {
		return _qdJavaEntity.isVolatile();
	}

	protected static String getNonQualifiedName(Type aType) {
		String fullQualified = aType.toString();
		if (fullQualified.lastIndexOf(".") >= 0) {
			return fullQualified.substring(fullQualified.lastIndexOf(".") + 1);
		}
		return fullQualified;
	}
}
