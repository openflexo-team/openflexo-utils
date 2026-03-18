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

package org.openflexo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * An abstract factory interface used to create factories for graph of objects.
 * 
 * The graph being built is formed of vertices of type E, which are structured with parent/child relationships, and with associations based
 * on property names
 * 
 * @param <M>
 *            type of model being built, a sub-type of O
 * @param <E>
 *            type of model element being built, a sub-type of O
 * @param <T>
 *            generic type of objects being part of built model
 * @param <P>
 *            type of properties
 * @param <O>
 *            type of parsed object
 *
 * @author xtof,sylvain
 * 
 */
public interface IObjectGraphFactory<M extends T, E extends T, T, P, O> {

	public enum RootNodeStrategy {
		SINGLE_ROOT_NODE, // The model M contains a single root node E
		MULTIPLE_ROOT_NODES, // The model M contains multiple root nodes E
		ROOT_NODE_IS_THE_MODEL // The model M is the root node
	}

	/**
	 * Initialize the model context : the graph being built
	 * 
	 * @param objectGraph
	 */
	public void setModelContext(M objectGraph);

	/**
	 * Resets the model context, enables re-use of the same factory in various contexts
	 */
	public void resetModelContext();

	/**
	 * Return strategy for the root node(s)
	 * 
	 * @return
	 */
	public RootNodeStrategy getRootNodeStrategy();

	/**
	 * Sets root node of model being built, considering RootNodeStratgey is SINGLE_ROOT_NODE
	 * 
	 * @param anObject
	 */
	public void setRootNode(E rootNode);

	/**
	 * Adds anObject to root nodes of model being built, considering RootNodeStratgey is MULTIPLE_ROOT_NODES
	 * 
	 * @param anObject
	 */
	public void addToRootNodes(E rootNode);

	/**
	 * Update already created root node (the model context) with parsed object
	 * 
	 * @param supportObject
	 */
	public void updateRootNode(O parsed);

	/**
	 * Add supplied child to supplied container
	 * 
	 * @param child
	 * @param container
	 */
	public void addChildToObject(E child, T container);

	/**
	 * Returns the type of object corresponding to the given URI, it must be a type of object relevant in the context of the current graph
	 * to be built
	 * 
	 * @param typeURI
	 *            URI of the type
	 * @param localName
	 *            the name of the object to be typed
	 * @param container
	 *            the object containing the object to be typed
	 * @return the relevant Type
	 */
	public Type getType(String typeURI, String localName, T container);

	/**
	 * Create an instance of supplied type and name
	 * 
	 * @param aType
	 * @param name
	 * @param object
	 *            object being parsed
	 * @return
	 */
	public E createInstance(Type aType, String name, O parsed);

	/**
	 * Return property applicable to supplied object with supplied name
	 * 
	 * @param object
	 * @param propertyName
	 * @return
	 */
	public P getPropertyNamed(T object, String propertyName);

	/**
	 * Returns the type of object corresponding a given property
	 * 
	 * @param property
	 * @return
	 */
	public Type getTypeForProperty(P property);

	/**
	 * Add (or set if the property has single cardinality) value for property
	 */
	public void addOrSetDataPropertyValue(T targetObject, P property, Object value);

	/**
	 * Add (or set if the property has single cardinality) value for property
	 */
	public void addOrSetObjectPropertyValue(T targetObject, P property, T value);

	/**
	 * Sets a property value for the model being built
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void setModelProperty(String propertyName, Object value);

	// ***************************************************
	// Methods concerning deserialization
	// ***************************************************

	public M deserialize(String input) throws Exception, IOException;

	public M deserialize(InputStream input) throws Exception, IOException;

}
