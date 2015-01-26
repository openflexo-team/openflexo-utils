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

public abstract class FieldSourceCode extends AbstractSourceCode {
	private static final Logger logger = Logger.getLogger(FieldSourceCode.class.getPackage().getName());

	protected FieldSourceCode(SourceCodeOwner owner, String propertyName, String hasParseErrorPropertyName,
			String parseErrorWarningPropertyName) {
		super(owner, propertyName, hasParseErrorPropertyName, parseErrorWarningPropertyName);
	}

	protected FieldSourceCode(SourceCodeOwner owner) {
		super(owner);
	}

	@Override
	public abstract String makeComputedCode();

	public abstract void interpretEditedJavaField(ParsedJavaField javaField) throws DuplicateMethodSignatureException;

	@Override
	public void interpretEditedCode(ParsedJavaElement javaElement) throws DuplicateMethodSignatureException {
		interpretEditedJavaField((ParsedJavaField) javaElement);
	}

	public ParsedJavaField getParsedField() throws ParserNotInstalledException {
		return parseCode(getCode());
	}

	@Override
	protected ParsedJavaField parseCode(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaFieldParser == null) {
			throw new ParserNotInstalledException();
		}

		try {
			// Try to parse
			ParsedJavaField parsedJavaField = _javaFieldParser.parseField(qualifiedCode, getOwner().getClassLibrary());
			setHasParseErrors(false);
			return parsedJavaField;
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning")
			// +" field: "+qualifiedCode
					+ "</font></html>");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parse error while parsing field: " + qualifiedCode);
			}
			return null;
		}
	}

	@Override
	public ParsedJavadoc parseJavadoc(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaFieldParser == null) {
			throw new ParserNotInstalledException();
		}
		try {
			return _javaFieldParser.parseJavadocForField(qualifiedCode, getOwner().getClassLibrary());
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning") + "</font></html>");
			return null;
		}
	}

	public void replaceFieldDeclarationInEditedCode(String newFieldDeclaration) {

		int beginIndex;
		int endIndex;

		// logger.info("Called replaceFieldDeclarationInEditedCode() with "+newFieldDeclaration);

		// First look javadoc
		int javadocBeginIndex = _editedCode.indexOf("/**");
		if (javadocBeginIndex > -1) {
			beginIndex = _editedCode.indexOf("*/") + 2;
		} else {
			beginIndex = 0;
		}

		if (_editedCode.indexOf("=") > 0) {
			endIndex = _editedCode.indexOf("=");
		} else if (_editedCode.indexOf(";") > 0) {
			endIndex = _editedCode.indexOf(";");
		} else {
			endIndex = _editedCode.length();
		}

		if (endIndex > beginIndex) {
			_editedCode = _editedCode.substring(0, beginIndex) + newFieldDeclaration + _editedCode.substring(endIndex);
		}

	}

	private static JavaFieldParser _javaFieldParser;

	public static void setJavaFieldParser(JavaFieldParser javaFieldParser) {
		_javaFieldParser = javaFieldParser;
	}

	public static JavaFieldParser getJavaFieldParser() {
		return _javaFieldParser;
	}

	/**
	 * Overrides isJavaParserInstalled
	 * 
	 * @see org.openflexo.javaparser.AbstractSourceCode#isJavaParserInstalled()
	 */
	@Override
	protected boolean isJavaParserInstalled() {
		return _javaFieldParser != null;
	}

}
