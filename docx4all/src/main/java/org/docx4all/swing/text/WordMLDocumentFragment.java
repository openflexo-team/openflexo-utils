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

import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import org.docx4all.xml.ElementMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordMLDocumentFragment extends WordMLDocument {
	private static Logger log = LoggerFactory.getLogger(WordMLDocumentFragment.class);

	private final int startIndex;
	private final int endIndex;

	public WordMLDocumentFragment(ElementMLFactory elementMLFactory, int startIndex, int endIndex) {
		super(elementMLFactory);
		this.startIndex = startIndex;
		this.endIndex = endIndex;

	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	// IMPORTANT
	// We override here parent method by commenting last paragraph management
	// which has no meaning here, and causes many troubles
	@Override
	protected void createElementStructure(List<ElementSpec> list) {
		ElementSpec[] specs = new ElementSpec[list.size()];
		list.toArray(specs);
		try {
			writeLock();
			super.create(specs);

			DocumentElement root = (DocumentElement) getDefaultRootElement();
			StyleConstants.setFontFamily((MutableAttributeSet) root.getAttributes(),
					FontManager.getInstance().getDocx4AllDefaultFontFamilyName());

			StyleConstants.setFontSize((MutableAttributeSet) root.getAttributes(), FontManager.getInstance().getDocx4AllDefaultFontSize());

			/*
			// Needs to validate the last ParagraphML's parent.
			DocumentElement lastPara = (DocumentElement) root.getElement(root.getElementCount() - 1);
			ElementML lastParaML = lastPara.getElementML();
			// detach from its previous parent
			lastParaML.delete();
			// make the new document root as new parent
			root.getElementML().getChild(0).addChild(lastParaML);*/

		} finally {
			writeUnlock();
		}
	}

}// WordMLDocumentFragment class
