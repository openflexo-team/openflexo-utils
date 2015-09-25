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

import org.docx4all.util.XmlUtil;
import org.docx4all.xml.BodyML;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.ElementMLFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
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

	@Override
	public void applyFilter() {
		try {
			writeLock();

			DocumentElement elem = (DocumentElement) getDefaultRootElement();
			ElementML docML = elem.getElementML();

			// Do not include document's last paragraph.
			elem = (DocumentElement) elem.getElement(elem.getElementCount() - 1);
			ElementML paraML = elem.getElementML();
			ElementML bodyML = paraML.getParent();
			paraML.delete();

			WordprocessingMLPackage wmlPackage = XmlUtil.applyFilter(docML.getWordprocessingMLPackage());

			System.out.println("Now we try to filter from " + startIndex + " to " + endIndex);

			// Restore document's last paragraph 'paraML'.
			bodyML.addChild(paraML);

			org.docx4j.wml.Document wmlDoc = wmlPackage.getMainDocumentPart().getJaxbElement();
			replaceBodyML(new BodyML(wmlDoc.getBody(), getElementMLFactory()));

		} finally {
			writeUnlock();
		}
	}

}// WordMLDocumentFragment class
