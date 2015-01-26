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

import org.openflexo.foundation.FlexoException;
import org.openflexo.localization.FlexoLocalization;

import com.thoughtworks.qdox.parser.ParseException;

public class FJPJavaParseException extends FJPJavaElement {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FJPJavaParseException.class.getPackage().getName());

	ParseException _parseException;
	String _sourceName;

	public FJPJavaParseException(String sourceName, ParseException parseException) {
		super(null);
		_parseException = parseException;
		_sourceName = sourceName;
	}

	public int getColumn() {
		return _parseException.getColumn();
	}

	public int getLine() {
		return _parseException.getLine();
	}

	public String getLocalizedMessage() {
		return _parseException.getLocalizedMessage();
	}

	public String getMessage() {
		return _parseException.getMessage();
	}

	private FJPParseException _flexoException;

	public FJPParseException getParseException() {
		if (_flexoException == null) {
			_flexoException = new FJPParseException();
		}
		return _flexoException;
	}

	public class FJPParseException extends FlexoException {
		public FJPParseException() {
			super("Parse error while parsing " + _sourceName);
		}

		public int getColumn() {
			return _parseException.getColumn();
		}

		public int getLine() {
			return _parseException.getLine();
		}

		@Override
		public String getLocalizedMessage() {
			return FlexoLocalization.localizedForKey("parse_error_while_parsing") + " " + _sourceName + " "
					+ FlexoLocalization.localizedForKey("line") + " " + getLine() + " " + FlexoLocalization.localizedForKey("at") + " "
					+ getColumn();
		}

		@Override
		public String getMessage() {
			return _parseException.getMessage();
		}

		public ParseException getParseException() {
			return _parseException;
		}

	}

}
