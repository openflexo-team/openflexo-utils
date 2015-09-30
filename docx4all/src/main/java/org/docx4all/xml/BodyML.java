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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBIntrospector;
import javax.xml.namespace.QName;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 08/01/2008
 */
public class BodyML extends ElementML {
	private static Logger log = LoggerFactory.getLogger(BodyML.class);

	Set<BigInteger> sdtBlockIdSet = new HashSet<BigInteger>();

	public BodyML(Object docxObject, ElementMLFactory elementMLFactory) {
		this(docxObject, elementMLFactory, false);
	}

	public BodyML(Object docxObject, ElementMLFactory elementMLFactory, boolean isDummy) {
		super(docxObject, elementMLFactory, isDummy);
	}

	@Override
	public Object clone() {
		Object obj = null;
		if (this.docxObject != null) {
			obj = XmlUtils.deepCopy(this.docxObject);
		}
		return new BodyML(obj, getElementMLFactory(), this.isDummy);
	}

	@Override
	public boolean canAddChild(int idx, ElementML child) {
		boolean canAdd = false;

		if ((child instanceof ParagraphML) || (child instanceof SdtBlockML)) {
			canAdd = super.canAddChild(idx, child);
		}

		return canAdd;
	}

	@Override
	public void addChild(int idx, ElementML child, boolean adopt) {
		if (!(child instanceof ParagraphML) && !(child instanceof TableML) && !(child instanceof SdtBlockML)) {
			throw new IllegalArgumentException("Child type = " + child.getClass().getSimpleName());
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}
		super.addChild(idx, child, adopt);

		if (child instanceof SdtBlockML) {
			SdtBlockML sdt = (SdtBlockML) child;
			String id = sdt.getSdtProperties().getPlutextId();
			if (id != null) {
				sdtBlockIdSet.add(BigInteger.valueOf(Long.valueOf(id)));
			}
		}
	}

	@Override
	public void deleteChild(ElementML child) {
		super.deleteChild(child);

		if (child instanceof SdtBlockML) {
			SdtBlockML sdt = (SdtBlockML) child;
			String id = sdt.getSdtProperties().getPlutextId();
			if (id != null) {
				sdtBlockIdSet.remove(BigInteger.valueOf(Long.valueOf(id)));
			}
		}
	}

	@Override
	public boolean canAddSibling(ElementML elem, boolean after) {
		return false;
	}

	@Override
	public void addSibling(ElementML elem, boolean after) {
		throw new UnsupportedOperationException("BodyML cannot have sibling.");
	}

	public Set<BigInteger> getSdtBlockIdSet() {
		return new HashSet<BigInteger>(sdtBlockIdSet);
	}

	@Override
	public void setParent(ElementML parent) {
		if (parent != null && !(parent instanceof DocumentML)) {
			throw new IllegalArgumentException("NOT a DocumentML.");
		}
		this.parent = parent;
	}

	@Override
	protected List<Object> getDocxChildren() {
		if (this.docxObject == null) {
			return null;
		}

		org.docx4j.wml.Body body = (org.docx4j.wml.Body) this.docxObject;
		return body.getEGBlockLevelElts();
	}

	@Override
	protected void init(Object docxObject) {
		org.docx4j.wml.Body body = null;

		if (docxObject == null) {
			;// implied BodyML

		}
		else if (docxObject instanceof org.docx4j.wml.Body) {
			body = (org.docx4j.wml.Body) docxObject;
			this.isDummy = false;

		}
		else {
			throw new IllegalArgumentException("Unsupported Docx Object = " + docxObject);
		}

		initChildren(body);
	}

	private void initChildren(org.docx4j.wml.Body body) {
		if (body == null) {
			return;
		}

		List<Object> bodyChildren = body.getEGBlockLevelElts();
		if (!bodyChildren.isEmpty()) {
			this.children = new ArrayList<ElementML>(bodyChildren.size());

			ElementML ml = null;
			for (Object obj : bodyChildren) {
				Object value = JAXBIntrospector.getValue(obj);

				if (value instanceof org.docx4j.wml.SdtBlock) {
					SdtBlockML sdt = new SdtBlockML(obj, getElementMLFactory());
					String id = sdt.getSdtProperties().getPlutextId();
					System.out.println(id); // 1,033,453,472
					if (id != null) {

						if (sdtBlockIdSet == null) {
							// Jo, how can this be happening??
							log.error("sdtBlockIdSet unexpectedly null!!! <---------------------------------------");
							sdtBlockIdSet = new HashSet<BigInteger>();
						}
						sdtBlockIdSet.add(BigInteger.valueOf(Long.valueOf(id)));
					}
					ml = sdt;
					ml.setParent(BodyML.this);
					this.children.add(ml);

				}
				else if (value instanceof org.docx4j.wml.Tbl) {
					ml = new TableML(obj, getElementMLFactory());
					ml.setParent(BodyML.this);
					this.children.add(ml);

				}
				else if (value instanceof org.docx4j.wml.CTMarkupRange) {
					// suppress <w:bookmarkStart> and <w:bookmarkEnd>
					JAXBIntrospector inspector = Context.jc.createJAXBIntrospector();
					QName name = inspector.getElementName(obj);
					if (name != null && (name.getLocalPart() == "bookmarkStart" || name.getLocalPart() == "bookmarkEnd")) {
						// suppress
					}
					else {
						ml = new ParagraphML(obj, getElementMLFactory());
						ml.setParent(BodyML.this);
						this.children.add(ml);
					}
				}
				else {
					ml = new ParagraphML(obj, getElementMLFactory());
					ml.setParent(BodyML.this);
					this.children.add(ml);
				}
			}
		}
	}// initChildren()

}// BodyML class
