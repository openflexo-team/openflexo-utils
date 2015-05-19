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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.localization.FlexoLocalization;

public abstract class ClassSourceCode extends AbstractSourceCode {
	private static final Logger logger = Logger.getLogger(ClassSourceCode.class.getPackage().getName());

	private ParsedJavaClass _parsedClass;

	protected ClassSourceCode(SourceCodeOwner owner, String propertyName, String hasParseErrorPropertyName,
			String parseErrorWarningPropertyName) {
		super(owner, propertyName, hasParseErrorPropertyName, parseErrorWarningPropertyName);
	}

	protected ClassSourceCode(SourceCodeOwner owner) {
		super(owner);
	}

	@Override
	public abstract String makeComputedCode();

	public abstract void interpretEditedJavaClass(ParsedJavaClass javaClass);

	@Override
	public void interpretEditedCode(ParsedJavaElement javaElement) {
		interpretEditedJavaClass((ParsedJavaClass) javaElement);
	}

	public ParsedJavaClass getParsedClass() throws ParserNotInstalledException {
		if (_parsedClass == null) {
			return parseCode(getCode());
		}
		return _parsedClass;
	}

	@Override
	protected ParsedJavaClass parseCode(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaClassParser == null) {
			throw new ParserNotInstalledException();
		}

		try {
			// Try to parse
			if (qualifiedCode == null) {
				throw new JavaParseException();
			}
			_parsedClass = _javaClassParser.parseClass(qualifiedCode, getOwner().getClassLibrary());
			setHasParseErrors(false);
			return _parsedClass;
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning") + "</font></html>");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parse error while parsing class: " + qualifiedCode);
			}
			return null;
		}
	}

	@Override
	public ParsedJavadoc parseJavadoc(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaClassParser == null) {
			throw new ParserNotInstalledException();
		}
		try {
			return _javaClassParser.parseJavadocForClass(qualifiedCode, getOwner().getClassLibrary());
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning") + "</font></html>");
			return null;
		}
	}

	private static JavaClassParser _javaClassParser;

	public static void setJavaClassParser(JavaClassParser javaClassParser) {
		_javaClassParser = javaClassParser;
	}

	public static JavaClassParser getJavaClassParser() {
		return _javaClassParser;
	}

	/**
	 * Overrides isJavaParserInstalled
	 * 
	 * @see org.openflexo.javaparser.AbstractSourceCode#isJavaParserInstalled()
	 */
	@Override
	protected boolean isJavaParserInstalled() {
		return _javaClassParser != null;
	}

	public void setParsedClass(ParsedJavaClass parsedClass) {
		_parsedClass = parsedClass;
	}

}
