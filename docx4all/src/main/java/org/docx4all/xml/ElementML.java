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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.docx4all.swing.text.StyleSheet;
import org.docx4all.ui.main.Constants;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * @author Jojada Tirtowidjojo - 30/11/2007
 */
public abstract class ElementML implements Cloneable {
	public final static ParagraphML IMPLIED_PARAGRAPH;
	public final static RunML IMPLIED_RUN;
	public final static RunContentML IMPLIED_NEWLINE;

	static {
		IMPLIED_PARAGRAPH = new ParagraphML(null, null);
		IMPLIED_RUN = new RunML(null, null);
		IMPLIED_NEWLINE = new RunContentML(null, null, true);
		IMPLIED_NEWLINE.setTextContent(Constants.NEWLINE);
	}

	protected Object docxObject;
	protected boolean isDummy;
	protected WordML.Tag tag;
	protected ElementML parent, godParent;
	protected List<ElementML> children;

	private final ElementMLFactory elementMLFactory;

	public ElementML(ElementMLFactory elementMLFactory) {
		this.elementMLFactory = elementMLFactory;
	}

	public ElementML(Object docxObject, ElementMLFactory elementMLFactory, boolean isDummy) {
		this(docxObject, -1, -1, elementMLFactory, isDummy);
	}

	public ElementML(Object docxObject, int startIndex, int endIndex, ElementMLFactory elementMLFactory, boolean isDummy) {
		this.docxObject = docxObject;
		this.elementMLFactory = elementMLFactory;
		this.isDummy = isDummy;

		if (this.docxObject != null) {
			QName name = Context.jc.createJAXBIntrospector().getElementName(docxObject);
			if (name != null) {
				tag = WordML.getTag(name.getLocalPart());
			}
		}
		init(docxObject, startIndex, endIndex);
	}

	public ElementMLFactory getElementMLFactory() {
		return elementMLFactory;
	}

	public IObjectFactory getObjectFactory() {
		if (getElementMLFactory() != null) {
			return getElementMLFactory().getObjectFactory();
		}
		return null;
	}

	public void setGodParent(ElementML parent) {
		this.godParent = parent;
	}

	public abstract void setParent(ElementML parent);

	@Override
	public abstract Object clone();

	protected void init(Object docxObject, int startIndex, int endIndex) {
		init(docxObject);
	}

	protected abstract void init(Object docxObject);

	protected abstract List<Object> getDocxChildren();

	/**
	 * The real (direct) parent of those docx children listed in getDocxChildren().
	 * 
	 * @return docxObject
	 */
	protected Object getDocxChildParent() {
		return getDocxObject();
	}

	public void setDocxParent(Object docxParent) {
		if (this.docxObject == null) {
			;// do nothing
		}
		else {
			try {
				this.docxObject.getClass().getMethod("setParent", Object.class).invoke(this.docxObject, docxParent);
			} catch (NoSuchMethodException exc) {
				;// ignore
			} catch (IllegalAccessException exc) {
				;// ignore
			} catch (InvocationTargetException exc) {
				;// ignore
			}
		}
	}// setDocxParent()

	public boolean canAddChild(int idx, ElementML child) {
		boolean canAdd = true;

		if (child.getParent() != null) {
			canAdd = false;
		}
		else if (this.children == null) {
			canAdd = (idx == 0);
		}

		return canAdd;
	}

	public boolean canAddSibling(ElementML elem, boolean after) {
		boolean canAdd = true;

		if (elem.getParent() != null) {
			canAdd = false;
		}
		else if (getParent() == null) {
			canAdd = false;
		}
		else {
			int idx = getParent().getChildIndex(this);
			if (idx < 0) {
				canAdd = false;
			}
			else {
				if (after) {
					idx++;
				}
				canAdd = getParent().canAddChild(idx, elem);
			}
		}

		return canAdd;
	}

	public void addSibling(ElementML elem, boolean after) {
		if (elem.getParent() != null) {
			throw new IllegalArgumentException("Not an orphan.");
		}

		if (getParent() == null) {
			throw new IllegalStateException("Parent is NULL.");
		}

		int idx = getParent().getChildIndex(this);
		if (idx < 0) {
			throw new IllegalStateException("Index position not found.");
		}

		if (after) {
			idx++;
		}

		getParent().addChild(idx, elem);
	}

	public boolean canAddChild(ElementML child) {
		int idx = (getChildren() == null) ? 0 : getChildren().size();
		return canAddChild(idx, child);
	}

	public void addChild(ElementML child) {
		addChild(child, true);
	}

	public void addChild(ElementML child, boolean adopt) {
		int idx = (getChildren() == null) ? 0 : getChildren().size();
		addChild(idx, child, adopt);
	}

	public void addChild(int idx, ElementML child) {
		addChild(idx, child, true);
	}

	public void addChild(int idx, ElementML child, boolean adopt) {
		if (this.children == null) {
			if (idx == 0) {
				// Add to this ElementML's children
				this.children = new ArrayList<ElementML>();
				this.children.add(child);

				if (adopt) {
					child.setParent(ElementML.this);

					// Add to Docx structure
					if (getDocxChildParent() != null && child.getDocxObject() != null) {
						List<Object> list = getDocxChildren();
						list.add(child.getDocxObject());
						child.setDocxParent(getDocxChildParent());
					}
				}
			}
			else {
				throw new IndexOutOfBoundsException("Index: " + idx + ", Size: 0");
			}

		}
		else {
			// Add to this ElementML's children
			this.children.add(idx, child);

			if (adopt) {
				child.setParent(ElementML.this);

				// Add to Docx structure
				if (getDocxChildParent() != null && child.getDocxObject() != null) {
					List<Object> list = getDocxChildren();

					// The index position in the Docx structure may
					// be different from that in this ElementML structure.
					// Therefore, we find the index position from siblings.
					// TODO: Should we care about this difference ?

					// Browse older siblings for index position
					int siblingIndex = -1;
					if (idx > 0) {
						for (int i = idx - 1; 0 <= i && siblingIndex == -1; i--) {
							Object obj = this.children.get(i).getDocxObject();
							siblingIndex = list.indexOf(obj);
						}
					}

					if (siblingIndex > -1) {
						list.add(siblingIndex + 1, child.getDocxObject());
						child.setDocxParent(getDocxChildParent());

					}
					else if (idx < this.children.size() - 1) {
						// Browse younger siblings for index position
						for (int i = idx + 1; i < this.children.size() && siblingIndex == -1; i++) {
							Object obj = this.children.get(i).getDocxObject();
							siblingIndex = list.indexOf(obj);
						}

						if (siblingIndex > -1) {
							list.add(siblingIndex, child.getDocxObject());
							child.setDocxParent(getDocxChildParent());
						}
					}

					if (siblingIndex == -1) {
						// Add child anyway
						list.add(child.getDocxObject());
						child.setDocxParent(getDocxChildParent());
					}
				}
			} // if (adopt)
		}
	} // addChild()

	public void delete() {
		if (getParent() == null) {
			return;
		}
		getParent().deleteChild(ElementML.this);
	}

	public void deleteChild(ElementML child) {
		if (this.children == null) {
			// delete from Docx structure
			if (getDocxObject() != null && child.getDocxObject() != null) {
				List<Object> list = getDocxChildren();
				if (list != null) {
					list.remove(child.getDocxObject());
				}
				child.setDocxParent(null);
			}
		}
		else {
			// Delete from this ElementML's children
			this.children.remove(child);
			child.setParent(null);

			// delete from Docx structure
			if (getDocxObject() != null && child.getDocxObject() != null) {
				List<Object> list = getDocxChildren();
				if (list != null) {
					list.remove(child.getDocxObject());
				}
				child.setDocxParent(null);
			}
		}
	}

	public int getChildIndex(ElementML elem) {
		return (this.children != null && elem != null) ? this.children.indexOf(elem) : -1;
	}

	/**
	 * The DOM element associated with this ElementML.
	 * 
	 * @return docxObject DOM element
	 */
	public Object getDocxObject() {
		return this.docxObject;
	}

	/**
	 * An implied ElementML is an ElementML that does not have a DOM element associated with it.
	 * 
	 * @return true, if this is an implied ElementML false, otherwise
	 * @see getDocxObject()
	 */
	public boolean isImplied() {
		return this.docxObject == null;
	}

	/**
	 * A dummy ElementML is an ElementML that is declared as dummy.
	 * 
	 * @return true, if this ElementML has been declared as dummy. false, otherwise
	 */
	public boolean isDummy() {
		return isDummy;
	}

	public WordML.Tag getTag() {
		return this.tag;
	}

	public ElementML getParent() {
		return this.parent;
	}

	public ElementML getGodParent() {
		return this.godParent;
	}

	public List<ElementML> getChildren() {
		return this.children;
	}

	public int getChildrenCount() {
		return (this.children == null) ? 0 : this.children.size();
	}

	public ElementML getChild(int idx) {
		if (this.children != null && !this.children.isEmpty()) {
			return this.children.get(idx);
		}
		return null;
	}

	public boolean isBlockElement() {
		return getTag().isBlockTag();
	}

	public boolean breaksFlow() {
		return getTag().breaksFlow();
	}

	public StyleSheet getStyleSheet() {
		return (getParent() != null) ? getParent().getStyleSheet() : null;
	}

	public WordprocessingMLPackage getWordprocessingMLPackage() {
		return (getParent() != null) ? getParent().getWordprocessingMLPackage() : null;
	}

	@Override
	public String toString() {
		String dummy = "";
		if (isImplied()) {
			dummy = "IMPLIED_";
		}
		else if (isDummy()) {
			dummy = "DUMMY_";
		}

		StringBuffer sb = new StringBuffer(dummy);
		sb.append(getClass().getSimpleName());
		sb.append("@");
		sb.append(hashCode());

		return sb.toString();
	}

} // ElementML class
