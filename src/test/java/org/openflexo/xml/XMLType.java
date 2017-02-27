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
import java.util.HashMap;
import java.util.Map;

public class XMLType implements Type {

	private static final Object NAME_ATTR = "name";

	private String _uri = null;

	Map<String, XMLAttr> attributeNames;

	XMLType(String uri) {
		super();
		_uri = uri;
		attributeNames = new HashMap<>();
	}

	public void createAttribute(String name) {
		if (!hasAttribute(name))
			attributeNames.put(name, new XMLAttr(name));
	}

	public Boolean hasAttribute(String name) {
		return attributeNames.containsKey(name);
	}

	public XMLAttr getAttributeByName(String name) {
		XMLAttr attr = attributeNames.get(name);
		if (attr == null && name.equals(NAME_ATTR)) {
			attr = new XMLAttr(name);
			attributeNames.put(name, attr);
		}
		return attr;
	}

	public String getURI() {
		return _uri;
	}

}
