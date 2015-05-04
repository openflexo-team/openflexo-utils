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
import org.docx4j.wml.TcPr;

/**
 *	@author Jojada Tirtowidjojo - 05/06/2008
 */
public class TableCellPropertiesML extends ElementML implements PropertiesContainerML {
	private MutableAttributeSet attrs;
	
	public TableCellPropertiesML(TcPr tcPr) {
		super(tcPr, false);
	}
	
    public void addAttribute(Object name, Object value) {
    	this.attrs.addAttribute(name, value);
    }
    
	public void addAttributes(AttributeSet attrs) {
		this.attrs.addAttributes(attrs);
	}
	
	public MutableAttributeSet getAttributeSet() {
		return new SimpleAttributeSet(this.attrs);
	}
	
    public void removeAttributes(AttributeSet attributes) {
    	attrs.removeAttributes(attributes);
	}

    public void removeAttribute(Object name) {
    	attrs.removeAttribute(name);
    }

	public void save() {
		if (this.docxObject == null) {
			return;
		}
		
    	//TODO:Save TableCellPropertiesML attributes to TcPr child elements
	}
	
	public Object clone() {
		TcPr obj = null;
		if (this.docxObject != null) {
			obj = (TcPr) XmlUtils.deepCopy(this.docxObject);
		}
		return new TableCellPropertiesML(obj);
	}
	
	public boolean canAddChild(int idx, ElementML child) {
		return false;
	}
	
	public void addChild(int idx, ElementML child, boolean adopt) {
		throw new UnsupportedOperationException("Cannot have a child.");
	}
		
	public void setParent(ElementML parent) {
		if (parent != null && !(parent instanceof TableCellML)) {
			throw new IllegalArgumentException("NOT a TableCellML.");
		}
		this.parent = parent;
	}
	
	public List<Object> getDocxChildren() {
		return null;//do not have children
	}
		
	protected void init(Object docxObject) {
		this.attrs = new SimpleAttributeSet();
		
		if (docxObject != null) {
			StyleSheet.addAttributes(this.attrs, (TcPr) docxObject);
		}
	}
	
}// TableCellPropertiesML class



















