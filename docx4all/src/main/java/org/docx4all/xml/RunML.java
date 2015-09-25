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

import javax.swing.text.AttributeSet;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.namespace.QName;

import org.docx4all.util.XmlUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Jojada Tirtowidjojo - 30/11/2007
 */
public class RunML extends ElementML {
	private static Logger log = LoggerFactory.getLogger(RunML.class);

	private RunPropertiesML rPr;
	private org.docx4j.wml.FldChar fldChar;

	public RunML(Object docxObject, ElementMLFactory elementMLFactory) {
		this(docxObject, elementMLFactory, false);
	}

	public RunML(Object docxObject, ElementMLFactory elementMLFactory, boolean isDummy) {
		super(docxObject, elementMLFactory, isDummy);
	}

	public void addAttributes(AttributeSet attrs, boolean replace) {
		if (this.rPr == null) {
			if (attrs.getAttributeCount() > 0) {
				RunPropertiesML ml = getElementMLFactory().createRunPropertiesML(attrs);
				setRunProperties(ml);
			}
		} else {
			if (replace) {
				this.rPr.removeAttributes(attrs);
			}
			this.rPr.addAttributes(attrs);
			this.rPr.save();
		}
	}

	/**
	 * Gets the run property element of this run element.
	 * 
	 * @return a RunPropertiesML, if any null, otherwise
	 */
	public PropertiesContainerML getRunProperties() {
		return this.rPr;
	}

	public void setRunProperties(RunPropertiesML rPr) {
		if (rPr != null && rPr.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}

		this.rPr = rPr;
		if (this.docxObject instanceof org.docx4j.wml.R) {
			org.docx4j.wml.RPr newDocxRPr = null;
			if (rPr != null) {
				rPr.setParent(RunML.this);
				newDocxRPr = (org.docx4j.wml.RPr) rPr.getDocxObject();
			}
			org.docx4j.wml.R run = (org.docx4j.wml.R) this.docxObject;
			run.setRPr(newDocxRPr);
			if (newDocxRPr != null) {
				newDocxRPr.setParent(run);
			}
		}
	}

	@Override
	public Object clone() {
		Object obj = null;
		if (this.docxObject != null) {
			obj = XmlUtils.deepCopy(this.docxObject);
		}
		return new RunML(obj, getElementMLFactory(), this.isDummy);
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		boolean canAdd = true;

		if (!(child instanceof RunContentML)) {
			canAdd = false;
		} else {
			canAdd = super.canAddChild(idx, child);
		}

		return canAdd;
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		if (!(child instanceof RunContentML)) {
			throw new IllegalArgumentException("NOT a RunContentML");
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}
		super.addChild(idx, child, adopt);
	}

	@Override
	public void setParent(ElementML parent) {
		if (parent != null && !(parent instanceof ParagraphML) && !(parent instanceof RunInsML) && !(parent instanceof RunDelML)
				&& !(parent instanceof HyperlinkML) && !(parent instanceof InlineTransparentML)) {
			throw new IllegalArgumentException("Invalid parent type = " + parent.getClass());
		}
		this.parent = parent;
	}

	public org.docx4j.wml.FldChar getFldChar() {
		return this.fldChar;
	}

	@Override
	protected List<Object> getDocxChildren() {
		List<Object> theChildren = null;

		JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();
		if (this.docxObject == null) {
			;// implied RunML
		} else if (inspector.isElement(this.docxObject)) {
			Object value = JAXBIntrospector.getValue(this.docxObject);
			if (value instanceof org.docx4j.wml.R) {
				org.docx4j.wml.R run = (org.docx4j.wml.R) value;
				theChildren = run.getRunContent();
			}
		}

		return theChildren;
	}

	@Override
	protected void init(Object docxObject) {
		org.docx4j.wml.R run = null;

		JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();

		if (docxObject == null) {
			;// implied RunML

		} else if (inspector.isElement(docxObject)) {
			Object value = JAXBIntrospector.getValue(docxObject);

			if (value instanceof org.docx4j.wml.R) {
				run = (org.docx4j.wml.R) value;
				this.isDummy = false;
			} else {
				// Create a dummy RunML for this unsupported element
				// TODO: A more informative text content in dummy RunML
				QName name = inspector.getElementName(docxObject);
				String renderedText;
				if (name != null) {
					renderedText = XmlUtil.getEnclosingTagPair(name);
				} else {
					// Should not happen but it could.
					renderedText = "<w:unknownTag></w:unknownTag>";
					log.warn("init(): Unknown tag was detected for a JAXBElement = " + XmlUtils.marshaltoString(docxObject, true));
				}
				if (getObjectFactory() != null) {
					run = getObjectFactory().createR(renderedText);
				}
				this.isDummy = true;
			}

		} else if (docxObject instanceof Node) {
			// docxObject is NOT a JAXB Element
			// If Xerces is on the path, this will be a org.apache.xerces.dom.NodeImpl;
			// otherwise, it will be com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
			String renderedText = XmlUtil.getEnclosingTagPair((Node) docxObject);
			run = getObjectFactory().createR(renderedText);
			this.isDummy = true;

		} else {
			throw new IllegalArgumentException("Unsupported Docx Object = " + docxObject.getClass().getName());
		}

		initRunProperties(run);
		initChildren(run);
	}

	private void initRunProperties(org.docx4j.wml.R run) {
		this.rPr = null;
		if (run != null) {
			RPr rPr = run.getRPr();
			if (rPr != null) {
				this.rPr = new RunPropertiesML(rPr, getElementMLFactory());
				this.rPr.setParent(RunML.this);
			}
		}
	}

	private void initChildren(org.docx4j.wml.R run) {
		this.children = null;
		this.fldChar = null;

		if (run == null) {
			return;
		}

		List<Object> rKids = run.getRunContent();
		if (!rKids.isEmpty()) {
			this.children = new ArrayList<ElementML>(rKids.size());
			for (Object o : rKids) {
				Object value = JAXBIntrospector.getValue(o);

				RunContentML child = null;
				if (value instanceof org.docx4j.wml.Drawing) {
					org.docx4j.wml.Drawing drawing = (org.docx4j.wml.Drawing) value;
					List<Object> list = drawing.getAnchorOrInline();
					for (Object item : list) {
						if (item instanceof org.docx4j.dml.wordprocessingDrawing.Inline) {
							child = new InlineDrawingML(drawing, getElementMLFactory(), this.isDummy);
						} else {
							// Anchor is not supported yet.
							// Let Drawing object be rendered as RunContentML.
							// TODO: Support Drawing's Anchor element.
							child = new RunContentML(drawing, getElementMLFactory(), this.isDummy);
						}
						child.setParent(RunML.this);
						this.children.add(child);
					}
				} else if (value instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
					// suppress
				} else {
					if (value instanceof org.docx4j.wml.FldChar) {
						this.fldChar = (org.docx4j.wml.FldChar) value;
					}

					child = new RunContentML(o, getElementMLFactory(), this.isDummy);
					child.setParent(RunML.this);
					this.children.add(child);
				}
			}
		}
	}// initChildren()

}// RunML class
