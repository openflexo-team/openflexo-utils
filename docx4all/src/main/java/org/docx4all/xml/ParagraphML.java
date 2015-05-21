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
import org.docx4j.wml.PPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 30/11/2007
 */
public class ParagraphML extends ElementML {
	private static Logger log = LoggerFactory.getLogger(ParagraphML.class);

	private ParagraphPropertiesML pPr;

	public ParagraphML(Object docxObject, ElementMLFactory elementMLFactory) {
		this(docxObject, elementMLFactory, false);
	}

	public ParagraphML(Object docxObject, ElementMLFactory elementMLFactory, boolean isDummy) {
		super(docxObject, elementMLFactory, isDummy);
	}

	public void addAttributes(AttributeSet attrs, boolean replace) {
		if (this.pPr == null) {
			if (attrs.getAttributeCount() > 0) {
				ParagraphPropertiesML ml = getElementMLFactory().createParagraphPropertiesML(attrs);
				setParagraphProperties(ml);
			}
		} else {
			if (replace) {
				this.pPr.removeAttributes(attrs);
			}
			this.pPr.addAttributes(attrs);
			this.pPr.save();
		}
	}

	/**
	 * Gets paragraph properties of this paragraph.
	 * 
	 * @return a PropertiesContainerML, if any null, otherwise
	 */
	public PropertiesContainerML getParagraphProperties() {
		return this.pPr;
	}

	public void setParagraphProperties(ParagraphPropertiesML pPr) {
		if (pPr != null && pPr.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}

		this.pPr = pPr;

		if (this.docxObject instanceof org.docx4j.wml.P) {
			org.docx4j.wml.PPr newDocxPPr = null;
			if (pPr != null) {
				pPr.setParent(ParagraphML.this);
				newDocxPPr = (org.docx4j.wml.PPr) pPr.getDocxObject();
			}
			((org.docx4j.wml.P) this.docxObject).setPPr(newDocxPPr);

			if (newDocxPPr != null) {
				newDocxPPr.setParent(this.docxObject);
			}
		}
	}

	@Override
	public Object clone() {
		Object obj = null;
		if (this.docxObject != null) {
			obj = XmlUtils.deepCopy(this.docxObject);
		}

		return new ParagraphML(obj, getElementMLFactory(), this.isDummy);
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		boolean canAdd = true;

		if (!(child instanceof RunML) && !(child instanceof RunInsML) && !(child instanceof RunDelML) && !(child instanceof HyperlinkML)) {
			canAdd = false;
		} else {
			canAdd = super.canAddChild(idx, child);
		}

		return canAdd;
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		if (!(child instanceof RunML) && !(child instanceof RunInsML) && !(child instanceof RunDelML) && !(child instanceof HyperlinkML)) {
			throw new IllegalArgumentException("Cannot become a child.");
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}
		super.addChild(idx, child, adopt);
	}

	@Override
	public void setParent(ElementML parent) {
		if (parent != null && !(parent instanceof BodyML) && !(parent instanceof SdtBlockML) && !(parent instanceof TableCellML)) {
			throw new IllegalArgumentException("Parent type = " + parent.getClass().getSimpleName());
		}
		this.parent = parent;
	}

	@Override
	protected List<Object> getDocxChildren() {
		List<Object> theChildren = null;

		JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();

		if (this.docxObject == null) {
			;// do nothing

		} else if (inspector.isElement(docxObject)) {
			Object value = JAXBIntrospector.getValue(docxObject);
			if (value instanceof org.docx4j.wml.P) {
				theChildren = ((org.docx4j.wml.P) value).getParagraphContent();
			}

		} else {
			;// should not come here. See init().
		}

		return theChildren;
	}

	@Override
	protected void init(Object docxObject) {
		org.docx4j.wml.P para = null;

		JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();

		if (docxObject == null) {
			;// implied ParagraphML

		} else if (inspector.isElement(docxObject)) {
			Object value = JAXBIntrospector.getValue(docxObject);
			if (value instanceof org.docx4j.wml.P) {
				para = (org.docx4j.wml.P) value;
				this.isDummy = false;

			} else {
				// Create a dummy ParagraphML for this unsupported element
				// TODO: A more informative text content in dummy ParagraphML
				QName name = inspector.getElementName(docxObject);
				String renderedText;
				if (name != null) {
					renderedText = XmlUtil.getEnclosingTagPair(name);
				} else {
					// Should not happen but it could.
					renderedText = "<w:unknownTag></w:unknownTag>";
					log.warn("init(): Unknown tag was detected for a JAXBElement = " + XmlUtils.marshaltoString(docxObject, true));
				}
				para = getObjectFactory().createP(renderedText);
				this.isDummy = true;
			}

		} else {
			throw new IllegalArgumentException("Unsupported Docx Object = " + docxObject);
		}

		initParagraphProperties(para);
		initChildren(para);
	}

	private void initParagraphProperties(org.docx4j.wml.P para) {
		this.pPr = null;
		if (para != null) {
			// if not an implied ParagraphML
			PPr pProp = para.getPPr();
			if (pProp != null) {
				this.pPr = new ParagraphPropertiesML(pProp, getElementMLFactory());
				this.pPr.setParent(ParagraphML.this);
			}
		}
	}

	private void initChildren(org.docx4j.wml.P para) {
		this.children = null;

		if (para == null) {
			return;
		}

		List<Object> pKids = para.getParagraphContent();
		if (!pKids.isEmpty()) {
			this.children = new ArrayList<ElementML>(pKids.size());

			ElementML ml = null;
			for (Object o : pKids) {
				Object value = JAXBIntrospector.getValue(o);

				if (value instanceof org.docx4j.wml.RunIns) {
					ml = new RunInsML(value, getElementMLFactory(), this.isDummy);
					ml.setParent(ParagraphML.this);
					this.children.add(ml);
				} else if (value instanceof org.docx4j.wml.RunDel) {
					ml = new RunDelML(value, getElementMLFactory(), this.isDummy);
					ml.setParent(ParagraphML.this);
					this.children.add(ml);
				} else if (value instanceof org.docx4j.wml.P.Hyperlink) {
					ml = new HyperlinkML(value, getElementMLFactory(), this.isDummy);
					ml.setParent(ParagraphML.this);
					this.children.add(ml);

				} else if (value instanceof org.docx4j.wml.CTSmartTagRun) {
					InlineTransparentML transparent = new InlineTransparentML(value, getElementMLFactory(), this.isDummy);
					// Current implementation is using InlineTransparentML
					// as surrogate container.
					if (transparent.getChildrenCount() > 0) {
						List<ElementML> list = new ArrayList<ElementML>(transparent.getChildren());
						for (ElementML elem : list) {
							elem.delete();
							elem.setParent(ParagraphML.this);
							this.children.add(elem);
						}
					}
				} else if (value instanceof org.docx4j.wml.CTMarkupRange) {
					// suppress <w:bookmarkStart> and <w:bookmarkEnd>
					JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();
					QName name = inspector.getElementName(o);
					if (name != null && (name.getLocalPart() == "bookmarkStart" || name.getLocalPart() == "bookmarkEnd")) {
						// suppress
					} else {
						ml = new RunML(o, getElementMLFactory(), this.isDummy);
						ml.setParent(ParagraphML.this);
						this.children.add(ml);
					}
				} else if (value instanceof org.docx4j.wml.ProofErr) {
					// Ignore those
				} else {
					// System.out.println("and about ??? : " + value + " of " + value.getClass());
					ml = new RunML(o, getElementMLFactory(), this.isDummy);
					ml.setParent(ParagraphML.this);
					this.children.add(ml);
				}
			}
		}
	}// initChildren()

}// ParagraphML class
