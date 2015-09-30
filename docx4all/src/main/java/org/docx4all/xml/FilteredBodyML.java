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

import java.util.List;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of BodyML, used to represent a document fragment
 * 
 * @author sylvain
 *
 */
public class FilteredBodyML extends BodyML {
	private static Logger log = LoggerFactory.getLogger(FilteredBodyML.class);

	private int startIndex = -1;
	private int endIndex = -1;

	public FilteredBodyML(Object docxObject, int startIndex, int endIndex, ElementMLFactory elementMLFactory) {
		this(docxObject, startIndex, endIndex, elementMLFactory, false);
	}

	public FilteredBodyML(Object docxObject, int startIndex, int endIndex, ElementMLFactory elementMLFactory, boolean isDummy) {
		super(docxObject, elementMLFactory, isDummy);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	@Override
	public Object clone() {
		Object obj = null;
		if (this.docxObject != null) {
			obj = XmlUtils.deepCopy(this.docxObject);
		}
		return new FilteredBodyML(obj, startIndex, endIndex, getElementMLFactory(), this.isDummy);
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		return super.canAddChild(idx, child);
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		super.addChild(idx, child, adopt);
	}

	@Override
	public void deleteChild(ElementML child) {
		super.deleteChild(child);
	}

	@Override
	protected List<Object> getDocxChildren() {
		return super.getDocxChildren();
	}

	private List<ElementML> filteredList = null;

	// TODO: perf issue, please implement a cache !!!
	protected List<ElementML> getFilteredChildren() {
		if (filteredList == null) {
			if (startIndex > -1 && endIndex > -1) {
				filteredList = super.getChildren().subList(startIndex, endIndex + 1);
			}
		}
		if (filteredList == null) {
			return super.getChildren();
		}
		return filteredList;
	}

	@Override
	public List<ElementML> getChildren() {
		return getFilteredChildren();
	}

	@Override
	public int getChildrenCount() {
		return (getFilteredChildren() == null) ? 0 : getFilteredChildren().size();
	}

	@Override
	public ElementML getChild(int idx) {
		if (getFilteredChildren() != null && !getFilteredChildren().isEmpty()) {
			return getFilteredChildren().get(idx);
		}
		return null;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

}// BodyML class
