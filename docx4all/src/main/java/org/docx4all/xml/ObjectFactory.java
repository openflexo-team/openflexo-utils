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

import javax.swing.text.StyleConstants;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.JcEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 02/01/2008
 */
public class ObjectFactory implements IObjectFactory {

	protected static Logger log = LoggerFactory.getLogger(ObjectFactory.class);

	private final org.docx4j.wml.ObjectFactory _jaxbFactory = new org.docx4j.wml.ObjectFactory();

	// public final static JAXBElement<P> createPara(String textContent) {
	// org.docx4j.wml.P p = createP(textContent);
	// return _jaxbFactory.createP(p);
	// }

	public ObjectFactory() {

	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createP(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.P createP(String textContent) {
		org.docx4j.wml.P p = _jaxbFactory.createP();
		if (textContent != null) {
			org.docx4j.wml.R r = createR(textContent);
			p.getParagraphContent().add(r);
			r.setParent(p);
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createCTSmartTagRun(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.CTSmartTagRun createCTSmartTagRun(String textContent) {
		org.docx4j.wml.CTSmartTagRun ct = _jaxbFactory.createCTSmartTagRun();
		if (textContent != null) {
			org.docx4j.wml.R r = createR(textContent);
			ct.getParagraphContent().add(r);
			r.setParent(ct);
		}
		return ct;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createPPr()
	 */
	@Override
	public org.docx4j.wml.PPr createPPr() {
		return _jaxbFactory.createPPr();
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createPStyle(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.PPr.PStyle createPStyle(String styleId) {
		org.docx4j.wml.PPr.PStyle pStyle = _jaxbFactory.createPPrBasePStyle();
		pStyle.setVal(styleId);
		return pStyle;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createR(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.R createR(String textContent) {
		org.docx4j.wml.R r = _jaxbFactory.createR();

		if (org.docx4all.ui.main.Constants.NEWLINE.equals(textContent)) {
			org.docx4j.wml.R.Cr cr = _jaxbFactory.createRCr();
			r.getRunContent().add(cr);
			cr.setParent(r);
		} else if (textContent != null) {
			org.docx4j.wml.Text text = _jaxbFactory.createText();
			text.setValue(textContent);
			text.setSpace("preserve");
			r.getRunContent().add(text);
			text.setParent(r);
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createRPr()
	 */
	@Override
	public org.docx4j.wml.RPr createRPr() {
		return _jaxbFactory.createRPr();
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createRStyle(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.RStyle createRStyle(String styleId) {
		org.docx4j.wml.RStyle rStyle = _jaxbFactory.createRStyle();
		rStyle.setVal(styleId);
		return rStyle;
	}

	// public final static JAXBElement<Text> createT(String textContent) {
	// org.docx4j.wml.Text text = _jaxbFactory.createText();
	// text.setValue(textContent);
	// return _jaxbFactory.createT(text);
	// }

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createT(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.Text createT(String textContent) {
		org.docx4j.wml.Text text = _jaxbFactory.createText();
		text.setValue(textContent);
		text.setSpace("preserve");
		return text;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createDocumentPackage(org.docx4j.wml.Document)
	 */
	@Override
	public WordprocessingMLPackage createDocumentPackage(org.docx4j.wml.Document doc) {
		// Create a package
		WordprocessingMLPackage wmlPack = new WordprocessingMLPackage();

		try {
			// Create main document part
			MainDocumentPart wordDocumentPart = new MainDocumentPart();

			// Put the content in the part
			wordDocumentPart.setJaxbElement(doc);

			// Add the main document part to the package relationships
			// (creating it if necessary)
			wmlPack.addTargetPart(wordDocumentPart);

			// Create a styles part
			StyleDefinitionsPart stylesPart = new StyleDefinitionsPart();
			log.debug("Unmarshalling default styles..");
			stylesPart.unmarshalDefaultStyles();

			// Add the styles part to the main document part relationships
			// (creating it if necessary)
			wordDocumentPart.addTargetPart(stylesPart); // NB - add it to main doc part, not package!

		} catch (Exception e) {
			// TODO: Synch with WordprocessingMLPackage.createTestPackage()
			e.printStackTrace();
			wmlPack = null;
		}

		// Return the new package
		return wmlPack;

	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createEmptyDocumentPackage()
	 */
	@Override
	public WordprocessingMLPackage createEmptyDocumentPackage() {
		org.docx4j.wml.Document doc = createEmptyDocument();
		return createDocumentPackage(doc);
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createEmptyDocument()
	 */
	@Override
	public org.docx4j.wml.Document createEmptyDocument() {
		org.docx4j.wml.P para = createP("");

		org.docx4j.wml.Body body = _jaxbFactory.createBody();
		body.getEGBlockLevelElts().add(para);

		org.docx4j.wml.SectPr sectPr = _jaxbFactory.createSectPr();
		body.setSectPr(sectPr);

		org.docx4j.wml.Document doc = _jaxbFactory.createDocument();
		doc.setBody(body);

		return doc;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createEmptySharedDocument()
	 */
	@Override
	public org.docx4j.wml.Document createEmptySharedDocument() {
		org.docx4j.wml.P para = createP("");

		org.docx4j.wml.SdtContentBlock sdtContent = createSdtContentBlock();
		sdtContent.getEGContentBlockContent().add(para);

		org.docx4j.wml.SdtBlock sdtBlock = createSdtBlock();
		org.docx4j.wml.SdtPr sdtPr = createSdtPr();
		sdtPr.setId();
		sdtPr.setTag(createTag("0"));
		sdtBlock.setSdtPr(sdtPr);
		sdtBlock.setSdtContent(sdtContent);

		org.docx4j.wml.Body body = _jaxbFactory.createBody();
		body.getEGBlockLevelElts().add(sdtBlock);

		org.docx4j.wml.Document doc = _jaxbFactory.createDocument();
		doc.setBody(body);

		return doc;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createJc(java.lang.Integer)
	 */
	@Override
	public org.docx4j.wml.Jc createJc(Integer align) {
		org.docx4j.wml.Jc theJc = null;

		if (align != null) {
			theJc = _jaxbFactory.createJc();
			if (align.intValue() == StyleConstants.ALIGN_LEFT) {
				theJc.setVal(JcEnumeration.LEFT);
			} else if (align.intValue() == StyleConstants.ALIGN_RIGHT) {
				theJc.setVal(JcEnumeration.RIGHT);
			} else if (align.intValue() == StyleConstants.ALIGN_CENTER) {
				theJc.setVal(JcEnumeration.CENTER);
			} else if (align.intValue() == StyleConstants.ALIGN_JUSTIFIED) {
				theJc.setVal(JcEnumeration.BOTH);
			} else {
				theJc = null;
			}
		}

		return theJc;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createBooleanDefaultTrue(java.lang.Boolean)
	 */
	@Override
	public org.docx4j.wml.BooleanDefaultTrue createBooleanDefaultTrue(Boolean b) {
		BooleanDefaultTrue bdt = _jaxbFactory.createBooleanDefaultTrue();
		bdt.setVal(b);
		return bdt;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createUnderline(java.lang.String, java.lang.String)
	 */
	@Override
	public org.docx4j.wml.U createUnderline(String value, String color) {
		org.docx4j.wml.U u = _jaxbFactory.createU();
		org.docx4j.wml.UnderlineEnumeration ue = org.docx4j.wml.UnderlineEnumeration.fromValue(value);
		u.setVal(ue);
		u.setColor(color);
		return u;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createRPrRFonts(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.RFonts createRPrRFonts(String ascii) {
		org.docx4j.wml.RFonts rfonts = _jaxbFactory.createRFonts();
		rfonts.setAscii(ascii);
		return rfonts;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createHpsMeasure(java.lang.Integer)
	 */
	@Override
	public org.docx4j.wml.HpsMeasure createHpsMeasure(Integer value) {
		org.docx4j.wml.HpsMeasure sz = _jaxbFactory.createHpsMeasure();
		sz.setVal(new BigInteger(value.toString()));
		return sz;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createId(java.math.BigInteger)
	 */
	@Override
	public org.docx4j.wml.Id createId(BigInteger val) {
		org.docx4j.wml.Id id = _jaxbFactory.createId();
		id.setVal(val);
		return id;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createTag(java.lang.String)
	 */
	@Override
	public org.docx4j.wml.Tag createTag(String val) {
		org.docx4j.wml.Tag tag = _jaxbFactory.createTag();
		tag.setVal(val);
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createSdtBlock()
	 */
	@Override
	public org.docx4j.wml.SdtBlock createSdtBlock() {
		org.docx4j.wml.SdtBlock sdtBlock = _jaxbFactory.createSdtBlock();
		return sdtBlock;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createSdtPr()
	 */
	@Override
	public org.docx4j.wml.SdtPr createSdtPr() {
		org.docx4j.wml.SdtPr sdtPr = _jaxbFactory.createSdtPr();
		return sdtPr;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createSdtContentBlock()
	 */
	@Override
	public org.docx4j.wml.SdtContentBlock createSdtContentBlock() {
		org.docx4j.wml.SdtContentBlock sdtContentBlock = _jaxbFactory.createSdtContentBlock();
		return sdtContentBlock;
	}

	/* (non-Javadoc)
	 * @see org.docx4all.xml.IObjectFactory#createHyperlink()
	 */
	@Override
	public org.docx4j.wml.P.Hyperlink createHyperlink() {
		org.docx4j.wml.P.Hyperlink hyperlink = _jaxbFactory.createPHyperlink();
		return hyperlink;
	}

}// ObjectFactory class
