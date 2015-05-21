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

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import org.docx4all.swing.text.StyleSheet;
import org.docx4j.XmlUtils;
import org.docx4j.wml.TblPr;

/**
 * @author Jojada Tirtowidjojo - 03/06/2008
 */
public class TablePropertiesML extends ElementML implements PropertiesContainerML {
	private MutableAttributeSet attrs;

	public TablePropertiesML(TblPr tblPr, ElementMLFactory elementMLFactory) {
		super(tblPr, elementMLFactory, false);
	}

	@Override
	public void addAttribute(Object name, Object value) {
		this.attrs.addAttribute(name, value);
	}

	@Override
	public void addAttributes(AttributeSet attrs) {
		this.attrs.addAttributes(attrs);
	}

	@Override
	public MutableAttributeSet getAttributeSet() {
		return new SimpleAttributeSet(this.attrs);
	}

	@Override
	public void removeAttributes(AttributeSet attributes) {
		attrs.removeAttributes(attributes);
	}

	@Override
	public void removeAttribute(Object name) {
		attrs.removeAttribute(name);
	}

	@Override
	public void save() {
		if (this.docxObject == null) {
			return;
		}

		// TODO:Save TablePropertiesML attributes to TblPr child elements
	}

	@Override
	public Object clone() {
		TblPr obj = null;
		if (this.docxObject != null) {
			obj = (TblPr) XmlUtils.deepCopy(this.docxObject);
		}
		return new TablePropertiesML(obj, getElementMLFactory());
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		return false;
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		throw new UnsupportedOperationException("Cannot have a child.");
	}

	@Override
	public void setParent(ElementML parent) {
		if (parent != null && !(parent instanceof TableML)) {
			throw new IllegalArgumentException("NOT a TableML.");
		}
		this.parent = parent;
	}

	@Override
	public List<Object> getDocxChildren() {
		return null;// do not have children
	}

	@Override
	protected void init(Object docxObject) {
		this.attrs = new SimpleAttributeSet();

		if (docxObject != null) {
			StyleSheet.addAttributes(this.attrs, (TblPr) docxObject);
		}
	}

}// TablePropertiesML class

