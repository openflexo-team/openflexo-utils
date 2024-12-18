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
 * @param <O>
 *            generic type of objects being part of built model
 *
 * @author xtof,sylvain
 * 
 */
public interface IObjectGraphFactory<M extends O, E extends O, O> {

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
	 * Adds anObject to root nodes of model being built
	 * 
	 * @param anObject
	 */
	public void addToRootNodes(E anObject);

	/**
	 * Add supplied child to supplied container
	 * 
	 * @param child
	 * @param container
	 */
	public void addChildToObject(E child, E container);

	/**
	 * Sets a property value for the model being built
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void setModelProperty(String propertyName, Object value);

	/**
	 * Returns the type of object corresponding to the given URI, it must be a type of object relevant in the context of the current graph
	 * to be built
	 * 
	 * @param typeURI
	 *            URI of the type
	 * @param objectName
	 *            the name of the object to be typed
	 * @param container
	 *            the object containing the object to be typed
	 * @return the relevant Type
	 */
	public Type getTypeForObject(String typeURI, O container, String objectName);

	/**
	 * Returns the type of object corresponding a given container and a local name
	 * 
	 * @param currentContainer
	 * @param localName
	 * @return
	 */
	public Type getTypeForProperty(E currentContainer, String localName);

	/**
	 * Create an instance of supplied type and name
	 * 
	 * @param aType
	 * @param name
	 * @return
	 */
	public E createInstance(Type aType, String name);

	/**
	 * Returns boolean indicating if supplied object has a property with supplied name
	 * 
	 * @param object
	 * @param propertyName
	 * @return
	 */
	public boolean objectHasPropertyNamed(O object, String propertyName);

	/**
	 * Add (or set if the property has single cardinality) value for property
	 */
	public void addPropertyValueForObject(E object, String propertyName, Object value);

	// ***************************************************
	// Methods concerning deserialization
	// ***************************************************

	public Object deserialize(String input) throws Exception, IOException;

	public Object deserialize(InputStream input) throws Exception, IOException;

}
