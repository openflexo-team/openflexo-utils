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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.openflexo.IObjectGraphFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * This SaxHandler is used to de-serialize any XML file<br>
 * It works with a {@link IObjectGraphFactory} which must implement the building logic
 * 
 * @param <M>
 *            type of model being built, a sub-type of O
 * @param <E>
 *            type of model element being built, a sub-type of O
 * @param <O>
 *            generic type of objects being part of built model
 * 
 * 
 * @author xtof,sylvain
 * 
 */

public class XMLReaderSAXHandler<M extends O, E extends O, O> extends DefaultHandler2 {

	protected static final Logger LOGGER = Logger.getLogger(XMLReaderSAXHandler.class.getPackage().getName());

	public static final String NAMESPACE_Property = "Namespace";

	private E currentContainer = null;
	private E currentObject = null;
	private Type currentObjectType = null;

	private final StringBuffer cdataBuffer = new StringBuffer();

	private final Stack<ParsedElement<E>> indivStack = new Stack<>();

	private IObjectGraphFactory<M, E, O, ParsedElement<E>> factory = null;

	public static class ParsedElement<E> {
		String uri;
		String localName;
		String qName;
		Attributes attributes;
		Type objectType;
		E object;
		E container;

		public ParsedElement(String uri, String localName, String qName, Attributes attributes) {
			this.uri = uri;
			this.localName = localName;
			this.qName = qName;
		}

		@Override
		public String toString() {
			return "<" + localName + "> -> " + object + " in " + container;
		}
	}

	public XMLReaderSAXHandler(IObjectGraphFactory<M, E, O, ParsedElement<E>> aFactory) {
		super();
		factory = aFactory;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		String NSPrefix = "p"; // default

		ParsedElement<E> pe = new ParsedElement<>(uri, localName, qName, attributes);

		currentObject = null;

		// Current element is not contained => root node, set NameSpace

		if (currentContainer == null) {
			if (uri != null && !uri.isEmpty()) {
				List<String> namespace = new ArrayList<>();
				namespace.add(uri);
				namespace.add(NSPrefix);
				factory.setModelProperty(NAMESPACE_Property, namespace);
			}

		}
		if (indivStack.isEmpty()) {
			currentContainer = null;
		}

		try {

			if (uri == null || (uri.length() == 0)) {
				// If there is no base uri, we use the localName of the XML Tag
				currentObjectType = factory.getTypeForObject(localName, currentContainer, localName);
			}
			else {
				if (currentContainer != null) {
					// find if there is an object property corresponding
					if (factory.objectHasPropertyNamed(currentContainer, localName)) {
						currentObjectType = factory.getTypeForProperty(currentContainer, localName);
					}
					else {
						currentObjectType = factory.getTypeForObject(uri + "#" + localName, currentContainer, localName);

					}
				}
				else {
					currentObjectType = factory.getTypeForObject(uri + "#" + localName, null, localName);

				}
			}

			// creates individual if it is a complex Type
			if (currentObjectType != null) {

				currentObject = factory.createInstance(currentObjectType, localName, pe);

				cdataBuffer.delete(0, cdataBuffer.length());
			}
			else {
				LOGGER.warning("Could not find type " + uri + "#" + localName);
			}

			if (currentObject != null) {

				// processing Attributes

				int len = attributes.getLength();

				for (int i = 0; i < len; i++) {

					String typeName = attributes.getType(i);
					String attrQName = attributes.getQName(i);
					String attrName = attributes.getLocalName(i);
					// Unused String attrURI = attributes.getURI(i);
					NSPrefix = "p"; // default

					if (attrQName != null && attrName != null && currentContainer == null) {
						// we only set prefix if there is no other Root Element
						NSPrefix = attrQName.split(":")[0];
						if (NSPrefix.equals(attrQName))
							NSPrefix = "";
					}

					if (typeName.equals(XMLCst.CDATA_TYPE_NAME)) {
						// Unused Type aType = String.class;
						if ((attrName == null || attrName.equals("")) && attrQName != null)
							if (NSPrefix.equals(""))
								attrName = attrQName;
							else
								attrName = attrQName.split(":")[1];

					}
					// add anything as attribute except name spaces....
					if (!NSPrefix.equalsIgnoreCase(XMLCst.XML_NS) && !NSPrefix.equalsIgnoreCase(XMLCst.XSI)
							&& !attrName.equalsIgnoreCase(XMLCst.XML_NS)) {
						factory.addPropertyValueForObject(currentObject, attrName, attributes.getValue(i));
					}

				}

				// Current element is not contained in another one, it is root!

				if (currentContainer == null && currentObject != null) {

					factory.addToRootNodes(currentObject);
				}

			}

			pe.objectType = currentObjectType;
			pe.object = currentObject;
			pe.container = currentContainer;

			indivStack.push(pe);

			currentContainer = currentObject;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		ParsedElement<E> pe = indivStack.pop();
		currentObject = pe.object;
		currentContainer = pe.container;

		boolean isAttribute = false;

		if (currentContainer != null) {
			isAttribute = factory.objectHasPropertyNamed(currentContainer, localName);
		}
		else {
			isAttribute = factory.modelHasPropertyNamed(localName);
		}

		// CDATA allocation

		String str = cdataBuffer.toString().trim();

		/*if (localName.equals("name")) {
			System.out.println("Bon on est la");
			System.out.println("localName=" + localName);
			System.out.println("str=" + str);
			System.out.println("isAttribute=" + isAttribute);
			System.out.println("currentObject=" + currentObject);
			System.out.println("currentContainer=" + currentContainer);
		}*/

		// Element is a simple attribute of current container => only allocate String
		if (isAttribute && currentObject == null) {
			currentObject = currentContainer;
			if (str.length() > 0) {
				if (currentObject != null) {
					factory.addPropertyValueForObject(currentObject, localName, str);
				}
				else {
					// System.out.println("Bon on passe bien la");
					factory.addPropertyValueForModel(localName, str);
				}
				cdataBuffer.delete(0, cdataBuffer.length());
			}
		}
		else {

			isAttribute = factory.objectHasPropertyNamed(currentContainer, localName);

			// Allocation of CDATA information depends on the type of entity we
			// have to allocate content to (Individual or Attribute)
			// As such, it depends on the interpretation that has been done of
			// XSD MetaModel
			//
			// Same stands for individuals to be allocated to ObjectProperties

			if (str.length() > 0) {
				factory.addPropertyValueForObject(currentObject, XMLCst.CDATA_ATTR_NAME, str);
				cdataBuffer.delete(0, cdataBuffer.length());
			}

			// Current element is contained in another one

			if (currentContainer != null && currentContainer != currentObject) {

				if (isAttribute && currentObject != null) {
					factory.addPropertyValueForObject(currentContainer, localName, currentObject);

				}
				// Always add to children
				if (currentObject != null) {
					factory.addChildToObject(currentObject, currentContainer);
				}
			}
		}

	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (length > 0)
			cdataBuffer.append(ch, start, length);
	}
}
