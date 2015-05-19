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

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.javaparser.ParsedJavaMethod;
import org.openflexo.toolbox.StringUtils;

import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

public class FJPJavaMethod extends FJPJavaEntity implements ParsedJavaMethod {

	private static final Logger logger = Logger.getLogger(FJPJavaMethod.class.getPackage().getName());

	private final JavaMethod _qdJavaMethod;

	public FJPJavaMethod(JavaMethod qdJavaMethod, FJPJavaSource aJavaSource) {
		super(qdJavaMethod, aJavaSource);
		_qdJavaMethod = qdJavaMethod;
	}

	@Override
	public String getName() {
		return _qdJavaMethod.getName();
	}

	@Override
	public String getCallSignature() {
		String returned = getName() + "(" + getParametersTypesAsString() + ")";
		return returned;
	}

	public String getSimplifiedCallSignature() {
		String returned = getName() + "(" + getSimplifiedParametersTypesAsString() + ")";
		return returned;
	}

	public String getDeclarationSignature(boolean withModifiers) {
		String returned = (withModifiers ? getModifiersAsString() : "") + getReturnAsString() + " " + getName() + "("
				+ getParametersAsString() + ")" + (getExceptions().length > 0 ? " throws " + getExceptionsAsString() : "");
		return returned;
	}

	public String getDeclarationSignature() {
		return getDeclarationSignature(true);
	}

	public Type[] getExceptions() {
		return _qdJavaMethod.getExceptions();
	}

	public JavaParameter getParameterByName(String name) {
		return _qdJavaMethod.getParameterByName(name);
	}

	private Vector<FJPJavaParameter> _parameters;

	public Vector<FJPJavaParameter> getParameters() {
		if (_parameters == null) {
			_parameters = new Vector<FJPJavaParameter>();
			for (JavaParameter p : _qdJavaMethod.getParameters()) {
				_parameters.add(new FJPJavaParameter(p, getJavaSource()));
			}
		}
		return _parameters;
	}

	@Override
	public Vector<? extends ParsedJavaMethodParameter> getMethodParameters() {
		return getParameters();
	}

	public FJPJavaClass getParentClass() {
		return getClass(_qdJavaMethod.getParentClass());
	}

	public String getPropertyName() {
		return _qdJavaMethod.getPropertyName();
	}

	public Type getPropertyType() {
		return _qdJavaMethod.getPropertyType();
	}

	public Type getReturns() {
		return _qdJavaMethod.getReturns();
	}

	private String _sourceCode = null;
	private int _sourceCodeLineCount = 0;

	private String _declarationSourceCode;

	public String getSourceCode() {
		// logger.info("getSourceCode() called for "+getCallSignature());
		if (_sourceCode == null) {
			String remainderCode = StringUtils.extractStringFromLine(getJavaSource().getSourceCode(), getLineNumber() - 1);
			_declarationSourceCode = remainderCode.substring(0, remainderCode.indexOf("{"));
			if (_declarationSourceCode.trim().equals("")) {
				logger.warning("Could not extract declaration source code, building it...");
				_declarationSourceCode = getDeclarationSignature();
			}
			/*else {
				logger.info("Remainder code="+remainderCode);
				logger.info("_declarationSourceCode="+_declarationSourceCode);
			}*/
			_sourceCode = _declarationSourceCode + getCoreCode();
			_sourceCodeLineCount = StringUtils.linesNb(_sourceCode);
		}
		return _sourceCode;
	}

	/*public String getSourceCodeDebug()
	{
		logger.info("getSourceCode() called for "+getCallSignature());
		String remainderCode = StringUtils.extractStringFromLine(getJavaSource().getSourceCode(), getLineNumber()-1);
		_declarationSourceCode = remainderCode.substring(0,remainderCode.indexOf("{"));
		if (_declarationSourceCode.trim().equals("")) {
			logger.warning("Could not extract declaration source code, building it...");
			_declarationSourceCode = getDeclarationSignature();
		}
		else {
			logger.info("Remainder code="+remainderCode);
			logger.info("_declarationSourceCode="+_declarationSourceCode);
		}
		logger.info("getCoreCode()="+getCoreCode());
		_sourceCode = _declarationSourceCode+getCoreCode();
		_sourceCodeLineCount = StringUtils.linesNb(_sourceCode);
		logger.info("_sourceCode="+_sourceCode);
		logger.info("_sourceCodeLineCount="+_sourceCodeLineCount);
		return _sourceCode;
	}*/

	@Override
	public String getCoreCode() {
		return "{" + _qdJavaMethod.getSourceCode() + "}";
	}

	@Override
	public int getLinesCount() {
		return _sourceCodeLineCount;
	}

	public boolean isConstructor() {
		return _qdJavaMethod.isConstructor();
	}

	public boolean isPropertyAccessor() {
		return _qdJavaMethod.isPropertyAccessor();
	}

	public boolean isPropertyMutator() {
		return _qdJavaMethod.isPropertyMutator();
	}

	private String exceptionsAsString = null;

	public String getExceptionsAsString() {
		if (exceptionsAsString == null) {
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (Type t : getExceptions()) {
				sb.append((isFirst ? "" : ",") + t.toString());
				isFirst = false;
			}
			exceptionsAsString = sb.toString();
		}
		return exceptionsAsString;
	}

	private String parametersAsString = null;

	public String getParametersAsString() {
		if (parametersAsString == null) {
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (FJPJavaParameter p : getParameters()) {
				sb.append((isFirst ? "" : ", ") + p.getTypeAsString() + (p.isVarArgs() ? "..." : "") + " " + p.getName());
				isFirst = false;
			}
			parametersAsString = sb.toString();
		}
		return parametersAsString;
	}

	private String parametersTypesAsString = null;

	public String getParametersTypesAsString() {
		if (parametersTypesAsString == null) {
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (FJPJavaParameter p : getParameters()) {
				sb.append((isFirst ? "" : ",") + p.getTypeAsString() + (p.isVarArgs() ? "..." : ""));
				isFirst = false;
			}
			parametersTypesAsString = sb.toString();
		}
		return parametersTypesAsString;
	}

	private String simplifiedParametersTypesAsString = null;

	public String getSimplifiedParametersTypesAsString() {
		if (simplifiedParametersTypesAsString == null) {
			boolean isFirst = true;
			StringBuffer sb = new StringBuffer();
			for (FJPJavaParameter p : getParameters()) {
				sb.append((isFirst ? "" : ",") + getNonQualifiedName(p.getType()) + (p.isVarArgs() ? "..." : "")
						+ (p.isArray() ? "[]" : ""));
				isFirst = false;
			}
			simplifiedParametersTypesAsString = sb.toString();
		}
		return simplifiedParametersTypesAsString;
	}

	private String modifiersAsString = null;

	public String getModifiersAsString() {
		if (modifiersAsString == null) {
			StringBuffer sb = new StringBuffer();
			for (String m : getModifiers()) {
				sb.append(m + " ");
			}
			modifiersAsString = sb.toString();
		}
		return modifiersAsString;
	}

	public String getReturnAsString() {
		if (getReturns() != null) {
			return getReturns().toString();
		}
		return "void";
	}

	@Override
	public String toString() {
		return getSimplifiedCallSignature();
	}

	@Override
	public String getUniqueIdentifier() {
		return getCallSignature();
	}

	/**
	 * Utility method used to unqualify a single type represented as String
	 * 
	 * @param qualifiedType
	 * @return
	 */
	private static String unqualifiedType(String qualifiedType) {
		String returned = qualifiedType;

		// remove generics if any
		if (qualifiedType.indexOf("<") > -1 && qualifiedType.indexOf(">") > -1 && qualifiedType.indexOf("<") < qualifiedType.indexOf(">")) {
			returned = qualifiedType.substring(0, qualifiedType.indexOf("<"));
		}

		// take last qualified item
		if (returned.lastIndexOf(".") > -1) {
			returned = returned.substring(returned.lastIndexOf(".") + 1);
		}

		return returned;
	}

	/**
	 * Utility method used to unqualify a list of types represented as a coma-separated list String
	 * 
	 * @param qualifiedTypes
	 * @return
	 */
	private static String unqualifiedTypes(String qualifiedTypes) {
		StringBuffer sb = new StringBuffer();
		DMTypeTokenizer st = new DMTypeTokenizer(qualifiedTypes);
		boolean isFirst = true;
		while (st.hasMoreTokens()) {
			if (!isFirst) {
				sb.append(",");
			}
			sb.append(unqualifiedType(st.nextToken()));
			isFirst = false;
		}
		return sb.toString();
	}

	/**
	 * Utility method used to unqualify a method signature (used to match methods)
	 * 
	 * @param aSignature
	 * @return
	 */
	public static String unqualifySignature(String aSignature) {
		if (aSignature.indexOf("(") > -1 && aSignature.indexOf(")") > -1 && aSignature.indexOf("(") < aSignature.indexOf(")")) {
			return aSignature.substring(0, aSignature.indexOf("(") + 1)
					+ unqualifiedTypes(aSignature.substring(aSignature.indexOf("(") + 1, aSignature.indexOf(")"))) + ")";
		}
		logger.warning("Signature " + aSignature + " does NOT seem to be valid: " + aSignature);
		return aSignature.trim();

	}

}
