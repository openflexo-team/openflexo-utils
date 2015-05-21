/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4all.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * This ImpliedContainerML is used for collecting a group of ElementML(s). Those collected ElementML(s) becomes its children but not its
 * adopted children meaning they still have their own parents.
 * 
 * @author Jojada Tirtowidjojo - 08/02/2008
 */
public class ImpliedContainerML extends ElementML {
	public ImpliedContainerML(ElementMLFactory elementMLFactory) {
		super(elementMLFactory);
	}

	@Override
	public boolean canAddSibling(ElementML elem, boolean after) {
		return false;
	}

	@Override
	public void setParent(ElementML parent) {
		this.parent = parent;
	}

	@Override
	public void setDocxParent(Object docxParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		ImpliedContainerML ml = new ImpliedContainerML(getElementMLFactory());
		if (this.children != null) {
			ml.children = new ArrayList<ElementML>(this.children);
			ml.isDummy = this.isDummy;
		}
		return ml;
	}

	@Override
	public boolean isBlockElement() {
		return true;
	}

	@Override
	public boolean breaksFlow() {
		return true;
	}

	@Override
	protected void init(Object docxObject) {
		;// not implemented
	}

	@Override
	protected List<Object> getDocxChildren() {
		return null;
	}

} // ImpliedContainerML class

