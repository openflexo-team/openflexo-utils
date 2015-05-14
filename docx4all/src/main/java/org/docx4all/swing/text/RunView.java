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

package org.docx4all.swing.text;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.event.DocumentEvent;
import javax.swing.text.CompositeView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.docx4all.swing.text.WordMLDocument.BlockElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunView extends CompositeView {
	private static Logger log = LoggerFactory.getLogger(RunView.class);

	public RunView(Element elem) {
		super(elem);
	}

	public View getTextView(int pos) {
		return super.getViewAtPosition(pos, null);
	}

	@Override
	public void setParent(View parent) {
		super.setParent(parent);

		// Because javax.swing.text.View.setParent()
		// resets the parent field of all children
		// when 'parent' is null we passes on
		// the new parent to all children.
		// This behaviour is specifically needed when
		// 'parent' is javax.swing.text.FlowView$LogicalView;
		// ie: parent.getParent() instanceof ImpliedParagraphView.
		if (parent != null && parent.getParent() instanceof ImpliedParagraphView) {
			for (int i = 0; i < getViewCount(); i++) {
				View v = getView(i);
				v.setParent(this);
			}
		}
	}

	@Override
	public float getPreferredSpan(int axis) {
		float maxpref = 0;
		float pref = 0;
		int n = getViewCount();
		for (int i = 0; i < n; i++) {
			View v = getView(i);
			pref += v.getPreferredSpan(axis);
			if (v.getBreakWeight(axis, 0, Integer.MAX_VALUE) >= ForcedBreakWeight) {
				maxpref = Math.max(maxpref, pref);
				pref = 0;
			}
		}
		maxpref = Math.max(maxpref, pref);
		return maxpref;
	}

	@Override
	public void paint(Graphics g, Shape allocation) {
		;// do nothing
	}

	@Override
	protected void childAllocation(int index, Rectangle a) {
		;// do nothing
	}

	@Override
	protected boolean isBefore(int x, int y, Rectangle alloc) {
		return false;
	}

	@Override
	protected boolean isAfter(int x, int y, Rectangle alloc) {
		return false;
	}

	@Override
	protected View getViewAtPoint(int x, int y, Rectangle alloc) {
		return null;
	}

	@Override
	public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		System.out.println("hop, insertUpdate with " + e + " shape=" + a + " element=" + getElement());
		System.out.println("element=" + getElement());
		if (getElement() instanceof BlockElement) {
			BlockElement blockElement = (BlockElement) getElement();
			System.out.println("blockElement elementML=" + blockElement.getElementML());
		}
		super.insertUpdate(e, a, f);
		prout = true;
	}

	private boolean prout = false;

	@Override
	public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		System.out.println("hop, removeUpdate with " + e + " shape=" + a + " element=" + getElement());
		super.removeUpdate(e, a, f);
	}

	@Override
	public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
		System.out.println("hop, changedUpdate with " + e + " shape=" + a + " element=" + getElement());
		super.changedUpdate(e, a, f);
	}

}// RunView class
