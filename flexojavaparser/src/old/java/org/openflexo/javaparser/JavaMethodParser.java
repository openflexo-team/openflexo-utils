/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.javaparser;

import org.openflexo.javaparser.model.DMClassLibrary;
import org.openflexo.javaparser.model.DMMethod;
import org.openflexo.javaparser.model.DMProperty;

public interface JavaMethodParser {
	public ParsedJavadoc parseJavadocForMethod(String methodCode, DMClassLibrary classLibrary) throws JavaParseException;

	public ParsedJavaMethod parseMethod(String methodCode, DMClassLibrary classLibrary) throws JavaParseException;

	public void updateWith(DMMethod method, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;

	public void updateGetterWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;

	public void updateSetterWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;

	public void updateAdditionAccessorWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;

	public void updateRemovalAccessorWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;
}