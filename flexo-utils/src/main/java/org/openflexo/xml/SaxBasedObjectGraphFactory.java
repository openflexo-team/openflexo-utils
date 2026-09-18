/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexoutils, a component of the software infrastructure 
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

package org.openflexo.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openflexo.IObjectGraphFactory;
import org.openflexo.xml.XMLReaderSAXHandler.ParsedElement;
import org.xml.sax.SAXException;

/**
 * An {@link IObjectGraphFactory} using a SaxParser to deserialize XML documents into objects Graph
 * 
 * @author xtof, sylvain
 * 
 */
public abstract class SaxBasedObjectGraphFactory<M extends T, E extends T, T, P>
		implements IObjectGraphFactory<M, E, T, P, ParsedElement<E, T, P>> {

	protected static final Logger LOGGER = Logger.getLogger(SaxBasedObjectGraphFactory.class.getPackage().getName());

	private SAXParserFactory factory = null;
	private SAXParser saxParser = null;
	private XMLReaderSAXHandler<M, E, T, P> handler = null;

	private M model;

	public SaxBasedObjectGraphFactory() {
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		handler = new XMLReaderSAXHandler<>(this);

		try {
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			saxParser = factory.newSAXParser();

		} catch (Exception e) {
			LOGGER.warning("Cannot create PARSER: " + e.getMessage());
		}
	}

	public M getModelContext() {
		return model;
	}

	@Override
	public void setModelContext(M model) {
		this.model = model;
		handler.initModelContext(model);
	}

	@Override
	public void resetModelContext() {
		model = null;
	}

	@Override
	public final M deserialize(String input) throws IOException {
		if (model != null) {

			try {
				saxParser.parse(input, handler);
			} catch (SAXException e) {
				LOGGER.warning("Cannot parse document: " + e.getMessage());
				throw new IOException(e.getMessage());
			}
			return this.model;
		}
		LOGGER.warning("Context is not set for parsing, aborting");
		return null;
	}

	@Override
	public final M deserialize(InputStream input) throws IOException {
		if (model != null) {

			try {
				saxParser.parse(input, handler);
			} catch (SAXException e) {
				LOGGER.warning("Cannot parse document: " + e.getMessage());
				throw new IOException(e.getMessage());
			}
			return this.model;

		}
		LOGGER.warning("Context is not set for parsing, aborting");
		return null;
	}

	/**
	 * Return property applicable to supplied object with supplied element name in 'object' context
	 * 
	 * @param object
	 * @param elementName
	 * @return
	 */
	public abstract P getPropertyForElementName(T object, String elementName);

	/**
	 * Return property applicable to supplied object with supplied attribute name in 'object' context
	 * 
	 * @param object
	 * @param elementName
	 * @return
	 */
	public abstract P getPropertyForAttributeName(T object, String attributeName);

	/**
	 * Handle CData for an object if not related to a given property
	 * 
	 * @param object
	 * @param value
	 */
	public abstract void handleCData(E object, String value);

}
