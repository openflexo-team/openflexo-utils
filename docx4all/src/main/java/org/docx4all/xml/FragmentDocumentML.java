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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FragmentDocumentML extends DocumentML {
	private static Logger log = LoggerFactory.getLogger(FragmentDocumentML.class);

	public FragmentDocumentML(WordprocessingMLPackage docPackage, int startIndex, int endIndex, ElementMLFactory elementMLFactory) {
		super(docPackage, startIndex, endIndex, elementMLFactory);
	}

	private FilteredBodyML bodyML;

	@Override
	protected void init(Object docxObject, int startIndex, int endIndex) {
		org.docx4j.wml.Document doc = null;

		if (docxObject == null) {
			;// implied DocumentML
		}
		else if (docxObject instanceof org.docx4j.wml.Document) {
			doc = (org.docx4j.wml.Document) docxObject;
		}
		else {
			throw new IllegalArgumentException("Unsupported Docx Object = " + docxObject);
		}

		initChildren(doc, startIndex, endIndex);
	}

	protected void initChildren(org.docx4j.wml.Document doc, int startIndex, int endIndex) {
		if (doc == null) {
			return;
		}

		if (doc.getBody() != null) {
			this.children = new ArrayList<ElementML>(1);
			bodyML = new FilteredBodyML(doc.getBody(), startIndex, endIndex, getElementMLFactory());
			bodyML.setParent(FragmentDocumentML.this);
			this.children.add(bodyML);
		}
	}// initChildren()

}// FragmentDocumentML class
