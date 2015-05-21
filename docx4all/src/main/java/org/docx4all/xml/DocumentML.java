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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.docx4all.swing.text.StyleSheet;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 30/11/2007
 */
public class DocumentML extends ElementML {
	private static Logger log = LoggerFactory.getLogger(DocumentML.class);

	private final WordprocessingMLPackage docPackage;
	private StyleSheet styleSheet;

	public DocumentML(WordprocessingMLPackage docPackage, ElementMLFactory elementMLFactory) {
		super((docPackage != null) ? docPackage.getMainDocumentPart().getJaxbElement() : null, elementMLFactory, false);
		this.docPackage = docPackage;
	}

	@Override
	public WordprocessingMLPackage getWordprocessingMLPackage() {
		return this.docPackage;
	}

	@Override
	public StyleSheet getStyleSheet() {
		if (this.styleSheet == null) {
			this.styleSheet = new StyleSheet();
			this.styleSheet.setWordprocessingMLPackage(docPackage);
		}
		return this.styleSheet;
	}

	@Override
	public Object clone() {
		WordprocessingMLPackage clonedPackage = null;

		if (this.docPackage != null) {
			MainDocumentPart documentPart = this.docPackage.getMainDocumentPart();
			org.docx4j.wml.Document doc = XmlUtils.deepCopy(documentPart.getJaxbElement());
			clonedPackage = getObjectFactory().createDocumentPackage(doc);
		}

		return new DocumentML(clonedPackage, getElementMLFactory());
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		boolean canAdd = true;

		if (!(child instanceof BodyML)) {
			canAdd = false;
		} else if (getChildrenCount() != 0) {
			canAdd = false;
		} else {
			canAdd = super.canAddChild(idx, child);
		}

		return canAdd;
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		if (!(child instanceof BodyML)) {
			throw new IllegalArgumentException("NOT a BodyML");
		}

		if (child.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}

		if (idx != 0) {
			throw new IllegalArgumentException("idx=" + idx + ". Zero is expected.");
		}

		if (getChildrenCount() != 0) {
			throw new IllegalStateException("Cannot afford having one more child.");
		}

		if (this.children == null) {
			this.children = new ArrayList<ElementML>();
		}

		this.children.add(child);

		if (adopt) {
			child.setParent(DocumentML.this);

			if (getDocxChildParent() instanceof org.docx4j.wml.Document && child.getDocxObject() instanceof org.docx4j.wml.Body) {
				org.docx4j.wml.Document doc = (org.docx4j.wml.Document) getDocxChildParent();
				org.docx4j.wml.Body body = (org.docx4j.wml.Body) child.getDocxObject();
				doc.setBody(body);
				body.setParent(doc);
			}
		}
	}

	@Override
	public boolean canAddSibling(ElementML elem, boolean after) {
		return false;
	}

	@Override
	public void addSibling(ElementML elem, boolean after) {
		throw new UnsupportedOperationException("DocumentML cannot have sibling.");
	}

	public Set<BigInteger> getSdtBlockIdSet() {
		BodyML bodyML = (BodyML) getChild(0);
		return bodyML.getSdtBlockIdSet();
	}

	@Override
	public void setDocxParent(Object docxParent) {
		throw new UnsupportedOperationException("DocumentML is root.");
	}

	@Override
	public void setParent(ElementML parent) {
		throw new UnsupportedOperationException("DocumentML is root.");
	}

	@Override
	protected List<Object> getDocxChildren() {
		if (this.docxObject == null) {
			return null;
		}

		List<Object> theChildren = new ArrayList<Object>(1);
		org.docx4j.wml.Document doc = (org.docx4j.wml.Document) this.docxObject;
		theChildren.add(doc.getBody());
		return theChildren;
	}

	@Override
	protected void init(Object docxObject) {
		org.docx4j.wml.Document doc = null;

		if (docxObject == null) {
			;// implied DocumentML
		} else if (docxObject instanceof org.docx4j.wml.Document) {
			doc = (org.docx4j.wml.Document) docxObject;
		} else {
			throw new IllegalArgumentException("Unsupported Docx Object = " + docxObject);
		}

		initChildren(doc);
	}

	private void initChildren(org.docx4j.wml.Document doc) {
		if (doc == null) {
			return;
		}

		if (doc.getBody() != null) {
			this.children = new ArrayList<ElementML>(1);
			BodyML bodyML = new BodyML(doc.getBody(), getElementMLFactory());
			bodyML.setParent(DocumentML.this);
			this.children.add(bodyML);
		}
	}// initChildren()

}// DocumentML class

