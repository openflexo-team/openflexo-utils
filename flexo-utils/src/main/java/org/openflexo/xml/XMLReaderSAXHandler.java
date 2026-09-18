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
import org.openflexo.IObjectGraphFactory.RootNodeStrategy;
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
 * @param <T>
 *            generic type of objects being part of built model
 * @param <P>
 *            type of properties
 * 
 * 
 * @author xtof,sylvain
 * 
 */

public class XMLReaderSAXHandler<M extends T, E extends T, T, P> extends DefaultHandler2 {

	protected static final Logger logger = Logger.getLogger(XMLReaderSAXHandler.class.getPackage().getName());

	public static final String NAMESPACE_Property = "Namespace";

	private T currentContainer = null;
	private E currentObject = null;
	private Type currentObjectType = null;

	private final StringBuffer cdataBuffer = new StringBuffer();

	private final Stack<ParsedElement<E, T, P>> indivStack = new Stack<>();

	private SaxBasedObjectGraphFactory<M, E, T, P> factory = null;

	private boolean isRoot;

	public static class ParsedElement<E extends T, T, P> {
		public String uri;
		public String localName;
		public String qName;
		public Attributes attributes;
		public Type objectType;
		public E object;
		public T container;
		// If element was parsed in a property context, store this property
		public P property;

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

	public XMLReaderSAXHandler(SaxBasedObjectGraphFactory<M, E, T, P> aFactory) {
		super();
		factory = aFactory;
	}

	public void initModelContext(M objectGraph) {
		currentContainer = objectGraph;
		// System.err.println("*********** initModelContext with " + objectGraph);
		isRoot = true;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		String NSPrefix = "p"; // default

		ParsedElement<E, T, P> pe = new ParsedElement<>(uri, localName, qName, attributes);

		currentObject = null;

		if (isRoot) {
			if (uri != null && !uri.isEmpty()) {
				// Current element is not contained => root node, set NameSpace
				List<String> namespace = new ArrayList<>();
				namespace.add(uri);
				namespace.add(NSPrefix);
				factory.setModelProperty(NAMESPACE_Property, namespace);
			}
		}

		// System.err.println(">>>> startElement " + localName + " container=" + currentContainer);

		P property = factory.getPropertyForElementName(currentContainer, localName);
		if (property != null) {
			currentObjectType = factory.getTypeForProperty(property);
		}

		if (currentObjectType == null) {
			if (uri == null || (uri.length() == 0)) {
				// If there is no base uri, we use the localName of the XML Tag
				currentObjectType = factory.getType(localName, localName, currentContainer);
			}
			else {
				currentObjectType = factory.getType(uri + "#" + localName, localName, currentContainer);
			}
		}

		pe.objectType = currentObjectType;
		pe.container = currentContainer;
		pe.property = property;

		// Is that the root node ?
		if (isRoot && factory.getRootNodeStrategy() == RootNodeStrategy.ROOT_NODE_IS_THE_MODEL) {
			factory.updateRootNode(pe);
			isRoot = false;
		}

		// Otherwise we will create a new instance if type was looked up
		else if (currentObjectType != null) {
			currentObject = factory.createInstance(currentObjectType, localName, pe);
		}

		// Or may be this is a simple property value ?
		else {
			if (property == null) {
				logger.warning("Could not find type " + uri + "#" + localName + " currentContainer=" + currentContainer);
			}
			else {
				// Maybe element matches a property
				// We will handle CDATA in endElement()
			}
		}

		// In all cases, handle the buffer
		cdataBuffer.delete(0, cdataBuffer.length());

		if (currentObject != null) {

			// processing Attributes

			int len = attributes.getLength();

			for (int i = 0; i < len; i++) {

				String typeName = attributes.getType(i);
				String attrQName = attributes.getQName(i);
				String attrName = attributes.getLocalName(i);
				// Unused String attrURI = attributes.getURI(i);
				NSPrefix = "p"; // default

				// System.err.println("typeName=" + typeName + " attrQName=" + attrQName + " attrName=" + attrName);

				if (attrQName != null && attrName != null && currentContainer == null) {
					// we only set prefix if there is no other Root Element
					NSPrefix = attrQName.split(":")[0];
					if (NSPrefix.equals(attrQName))
						NSPrefix = "";
				}

				if (typeName.equals(XMLCst.CDATA_TYPE_NAME)) {
					// Unused Type aType = String.class;
					if ((attrName == null || attrName.equals("")) && attrQName != null)
						if (NSPrefix.equals("")) {
							attrName = attrQName;
						}
						else {
							String[] split = attrQName.split(":");
							if (split.length > 1) {
								attrName = split[1];
							}
							else {
								attrName = split[0];
							}
						}

				}
				// add anything as attribute except name spaces....
				if (!NSPrefix.equalsIgnoreCase(XMLCst.XML_NS) && !NSPrefix.equalsIgnoreCase(XMLCst.XSI)
						&& !attrName.equalsIgnoreCase(XMLCst.XML_NS)) {
					P prop = factory.getPropertyForAttributeName(currentObject, attrName);
					if (prop != null) {
						factory.addOrSetDataPropertyValue(currentObject, prop, attributes.getValue(i));
					}
					else {
						logger.warning("Cannot find property " + attrName + " for " + currentObject);
					}
				}

			}

			// Handle root node management
			if (isRoot && currentObject != null) {

				if (factory.getRootNodeStrategy() == RootNodeStrategy.SINGLE_ROOT_NODE) {
					factory.setRootNode(currentObject);
				}
				else if (factory.getRootNodeStrategy() == RootNodeStrategy.MULTIPLE_ROOT_NODES) {
					factory.addToRootNodes(currentObject);
				}
				isRoot = false;
			}

		}

		pe.objectType = currentObjectType;
		pe.object = currentObject;
		pe.container = currentContainer;
		pe.property = property;

		indivStack.push(pe);

		if (currentObject != null) {
			currentContainer = currentObject;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		ParsedElement<E, T, P> pe = indivStack.pop();
		currentObject = pe.object;
		currentContainer = pe.container;

		// System.err.println("<<< endElement " + localName + " container=" + currentContainer);

		// CDATA allocation
		String str = cdataBuffer.toString().trim();

		boolean propertyWasHandled = false;

		if (str.length() > 0) {
			// Some contents to handle in this element
			if (pe.property != null && currentObject == null) {
				if (currentContainer != null) {
					// Element is a simple attribute of current container => only allocate String
					factory.addOrSetDataPropertyValue(currentContainer, pe.property, str);
					propertyWasHandled = true;
				}
			}
			else if (currentObject != null) {
				factory.handleCData(currentObject, str);
			}
			cdataBuffer.delete(0, cdataBuffer.length());
		}

		// Current element is contained in another one

		if (!propertyWasHandled && currentContainer != null && currentContainer != currentObject) {

			if (pe.property != null) {
				factory.addOrSetObjectPropertyValue(currentContainer, pe.property, currentObject);
			}

			// Furthermore, always add to children
			if (currentObject != null) {
				factory.addChildToObject(currentObject, currentContainer);
			}
		}

	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (length > 0)
			cdataBuffer.append(ch, start, length);
	}
}
