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

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;

/**
 *	@author Jojada Tirtowidjojo - 30/11/2007
 */
public interface PropertiesContainerML {
	MutableAttributeSet getAttributeSet();
    void addAttribute(Object name, Object value);
	void addAttributes(AttributeSet attrs);
    void removeAttributes(AttributeSet attributes);
    void removeAttribute(Object name);
	void save();
}// PropertiesContainerML interface
