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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.javaparser.DuplicateMethodSignatureException;
import org.openflexo.javaparser.JavaClassParser;
import org.openflexo.javaparser.JavaFieldParser;
import org.openflexo.javaparser.JavaMethodParser;
import org.openflexo.javaparser.JavaParseException;
import org.openflexo.javaparser.ParsedJavaClass;
import org.openflexo.javaparser.ParsedJavaField;
import org.openflexo.javaparser.ParsedJavaMethod;
import org.openflexo.javaparser.ParsedJavaMethod.ParsedJavaMethodParameter;
import org.openflexo.javaparser.ParsedJavadoc;
import org.openflexo.javaparser.ParsedJavadocItem;
import org.openflexo.javaparser.impl.FJPTypeResolver.CrossReferencedEntitiesException;
import org.openflexo.javaparser.model.DMClassLibrary;
import org.openflexo.javaparser.model.DMMethod;
import org.openflexo.javaparser.model.DMProperty;
import org.openflexo.toolbox.StringUtils;

import com.thoughtworks.qdox.parser.ParseException;

public class DefaultJavaParser implements JavaClassParser, JavaMethodParser, JavaFieldParser {

	private static final Logger logger = Logger.getLogger(DefaultJavaParser.class.getPackage().getName());

	@Override
	public ParsedJavadoc parseJavadocForClass(String classCode, DMClassLibrary classLibrary) throws JavaParseException {
		try {
			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, classCode, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			return parsedClass.getJavadoc();
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error");
			}
			throw new JavaParseException();
		}

	}

	@Override
	public ParsedJavaClass parseClass(String classCode, DMClassLibrary classLibrary) throws JavaParseException {
		try {
			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, classCode, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			return parsedClass;
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error");
			}
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Code: " + classCode);
			}
			throw new JavaParseException();
		}
	}

	@Override
	public FJPJavaMethod parseMethod(String methodCode, DMClassLibrary classLibrary) throws JavaParseException {
		try {
			String parsedString = "public class TemporaryClass {" + StringUtils.LINE_SEPARATOR + methodCode + StringUtils.LINE_SEPARATOR
					+ "}" + StringUtils.LINE_SEPARATOR;

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parsing " + parsedString);
			}

			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, parsedString, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			if (parsedClass.getMethods().length == 0) {
				return null;
			}
			FJPJavaMethod parsedMethod = parsedClass.getMethods()[0];

			return parsedMethod;
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error: " + methodCode);
			}
			throw new JavaParseException();
		}

	}

	@Override
	public ParsedJavaField parseField(String fieldCode, DMClassLibrary classLibrary) throws JavaParseException {
		String parsedString = null;
		try {
			parsedString = "public class TemporaryClass {" + StringUtils.LINE_SEPARATOR + fieldCode + StringUtils.LINE_SEPARATOR + "}"
					+ StringUtils.LINE_SEPARATOR;

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parsing " + parsedString);
			}

			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, parsedString, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			if (parsedClass.getFields().length == 0) {
				return null;
			}
			FJPJavaField parsedField = parsedClass.getFields()[0];

			return parsedField;
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error: " + fieldCode);
			}
			throw new JavaParseException();
		}

	}

	@Override
	public ParsedJavadoc parseJavadocForMethod(String methodCode, DMClassLibrary classLibrary) throws JavaParseException {
		try {
			String parsedString = "public class TemporaryClass {" + StringUtils.LINE_SEPARATOR + methodCode + StringUtils.LINE_SEPARATOR
					+ "}" + StringUtils.LINE_SEPARATOR;

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parsing " + parsedString);
			}

			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, parsedString, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			if (parsedClass.getMethods().length == 0) {
				return null;
			}
			FJPJavaMethod parsedMethod = parsedClass.getMethods()[0];

			return parsedMethod.getJavadoc();
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error");
			}
			throw new JavaParseException();
		}
	}

	@Override
	public ParsedJavadoc parseJavadocForField(String fieldCode, DMClassLibrary classLibrary) throws JavaParseException {
		try {
			String parsedString = "public class TemporaryClass {" + StringUtils.LINE_SEPARATOR + fieldCode + StringUtils.LINE_SEPARATOR
					+ "}" + StringUtils.LINE_SEPARATOR;

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parsing " + parsedString);
			}

			String sourceName = "TemporaryClass";
			FJPJavaSource source = new FJPJavaSource(sourceName, parsedString, classLibrary);
			FJPJavaClass parsedClass = source.getRootClass();
			if (parsedClass.getFields().length == 0) {
				return null;
			}
			FJPJavaField parsedField = parsedClass.getFields()[0];

			return parsedField.getJavadoc();
		}

		catch (ParseException e) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Parse error");
			}
			throw new JavaParseException();
		}

	}

	public static void main(String[] args) {
		DefaultJavaParser parser = new DefaultJavaParser();
		String codeToParse = "/**" + StringUtils.LINE_SEPARATOR + " * description first line" + StringUtils.LINE_SEPARATOR
				+ " * description second line" + StringUtils.LINE_SEPARATOR + " * description third line" + StringUtils.LINE_SEPARATOR
				+ " * " + StringUtils.LINE_SEPARATOR + " * @doc UserManual a description for user manual" + StringUtils.LINE_SEPARATOR
				+ " * @doc Technical a technical description" + StringUtils.LINE_SEPARATOR + " * @param param1 param1 description"
				+ StringUtils.LINE_SEPARATOR + " * @param param2 param2 description" + StringUtils.LINE_SEPARATOR
				+ " * @param param3 param3 description" + StringUtils.LINE_SEPARATOR + " * @return foo" + StringUtils.LINE_SEPARATOR
				+ " */" + StringUtils.LINE_SEPARATOR + "public a() {}";
		try {
			ParsedJavadoc jd = parser.parseJavadocForMethod(codeToParse, null);
			if (logger.isLoggable(Level.INFO)) {
				for (ParsedJavadocItem d : jd.getDocletTags()) {
					logger.info("tag " + d.getTag() + " name=[" + d.getParameterName() + "] value=(" + d.getParameterValue() + ")");
				}
				logger.info("jd comment=" + jd.getComment());
			}

		} catch (JavaParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateWith(DMMethod method, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException {
		FJPJavaMethod parsedMethod = (FJPJavaMethod) javaMethod;
		FJPJavaClass parsedClass = parsedMethod.getParentClass();
		FJPJavaSource source = parsedMethod.getJavaSource();

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update method " + method + " from parsed method");
		}
		try {
			DMMethod updatedMethod = FJPDMMapper.makeMethod(parsedClass, parsedMethod.getCallSignature(), method.getDMModel(), null,
					source, false);
			if (updatedMethod == null) {
				logger.warning("Could not lookup method " + updatedMethod);
				return;
			} else {
				method.update(updatedMethod, true);
			}
		} catch (CrossReferencedEntitiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update method " + method + " from parsed method: DONE ");
		}
	}

	@Override
	public void updateGetterWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update property " + property + " from parsed getter method");
		}

		if (javaMethod.getJavadoc() != null) {
			property.setDescription(javaMethod.getJavadoc().getComment());
		}

		// TODO: we can handle type reinjection here
		// TODO: handle 'static' here
	}

	@Override
	public void updateSetterWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update property " + property + " from parsed setter method");
		}

		if (javaMethod.getMethodParameters().size() == 1) {
			ParsedJavaMethodParameter setterParam = javaMethod.getMethodParameters().firstElement();
			property.setSetterParamName(setterParam.getName());
		}

		// TODO: we can handle type reinjection here
		// TODO: handle 'static' here
	}

	@Override
	public void updateAdditionAccessorWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update property " + property + " from parsed addition method");
		}

		if (javaMethod.getMethodParameters().size() == 1) {
			ParsedJavaMethodParameter additionMethodParam = javaMethod.getMethodParameters().firstElement();
			property.setAdditionAccessorParamName(additionMethodParam.getName());
		}

		// TODO: we can handle type reinjection here
		// TODO: we can also handle key type reinjection here
		// TODO: handle 'static' here
	}

	@Override
	public void updateRemovalAccessorWith(DMProperty property, ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update property " + property + " from parsed removal method");
		}

		if (javaMethod.getMethodParameters().size() == 1) {
			ParsedJavaMethodParameter removalMethodParam = javaMethod.getMethodParameters().firstElement();
			property.setRemovalAccessorParamName(removalMethodParam.getName());
		}

		// TODO: we can handle key type reinjection here
		// TODO: handle 'static' here
	}

	@Override
	public void updatePropertyFromJavaField(DMProperty property, ParsedJavaField javaField) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Try to update property " + property + " from parsed field");
			// TODO implement this
			// TODO: handle 'static' here
		}
	}

}
