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
import org.openflexo.toolbox.StringUtils;

public abstract class MethodSourceCode extends AbstractSourceCode {
	private static final Logger logger = Logger.getLogger(MethodSourceCode.class.getPackage().getName());

	protected MethodSourceCode(SourceCodeOwner owner, String propertyName, String hasParseErrorPropertyName,
			String parseErrorWarningPropertyName) {
		super(owner, propertyName, hasParseErrorPropertyName, parseErrorWarningPropertyName);
	}

	protected MethodSourceCode(SourceCodeOwner owner) {
		super(owner);
	}

	@Override
	public abstract String makeComputedCode();

	public abstract void interpretEditedJavaMethod(ParsedJavaMethod javaMethod) throws DuplicateMethodSignatureException;

	@Override
	public void interpretEditedCode(ParsedJavaElement javaElement) throws DuplicateMethodSignatureException {
		interpretEditedJavaMethod((ParsedJavaMethod) javaElement);
	}

	public ParsedJavaMethod getParsedMethod() throws ParserNotInstalledException {
		return parseCode(getCode());
	}

	@Override
	protected ParsedJavaMethod parseCode(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaMethodParser == null) {
			throw new ParserNotInstalledException();
		}

		try {
			// Try to parse
			ParsedJavaMethod parsedJavaMethod = _javaMethodParser.parseMethod(qualifiedCode, getOwner().getClassLibrary());
			setHasParseErrors(false);
			return parsedJavaMethod;
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning")
			// +" method: "+qualifiedCode
					+ "</font></html>");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Parse error while parsing method: " + qualifiedCode);
			}
			return null;
		}
	}

	@Override
	public ParsedJavadoc parseJavadoc(final String qualifiedCode) throws ParserNotInstalledException {
		if (_javaMethodParser == null) {
			throw new ParserNotInstalledException();
		}
		try {
			return _javaMethodParser.parseJavadocForMethod(qualifiedCode, getOwner().getClassLibrary());
		} catch (JavaParseException e) {
			setHasParseErrors(true);
			setParseErrorWarning("<html><font color=\"red\">" + FlexoLocalization.localizedForKey("parse_error_warning") + "</font></html>");
			return null;
		}
	}

	public String getCoreCode() {
		String code = getCode();
		return code.substring(code.indexOf("{"), code.lastIndexOf("}"));
	}

	public void replaceMethodDeclarationInEditedCode(String newMethodDeclaration) {
		int beginIndex;
		int endIndex;

		// logger.info("Called replaceMethodDeclarationInEditedCode() with "+newMethodDeclaration);

		// First look javadoc
		int javadocBeginIndex = _editedCode.indexOf("/**");
		if (javadocBeginIndex > -1) {
			beginIndex = _editedCode.indexOf("*/") + 2 + StringUtils.LINE_SEPARATOR.length();
		} else {
			beginIndex = 0;
		}
		endIndex = _editedCode.indexOf(")", beginIndex) + 1;

		// logger.info("Called replaceMethodDeclarationInEditedCode() beginIndex="+beginIndex+" endIndex="+endIndex);

		if (endIndex > beginIndex) {
			_editedCode = _editedCode.substring(0, beginIndex) + newMethodDeclaration + _editedCode.substring(endIndex);
		}

	}

	private static JavaMethodParser _javaMethodParser;

	public static void setJavaMethodParser(JavaMethodParser javaMethodParser) {
		_javaMethodParser = javaMethodParser;
	}

	public static JavaMethodParser getJavaMethodParser() {
		return _javaMethodParser;
	}

	/**
	 * Overrides isJavaParserInstalled
	 * 
	 * @see org.openflexo.javaparser.AbstractSourceCode#isJavaParserInstalled()
	 */
	@Override
	protected boolean isJavaParserInstalled() {
		return _javaMethodParser != null;
	}

}
