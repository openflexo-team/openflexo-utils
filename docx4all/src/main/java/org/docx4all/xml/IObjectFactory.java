package org.docx4all.xml;

import java.math.BigInteger;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;

/**
 * This factory provides API used to handle DocX model
 * 
 * @author sylvain
 *
 */
public interface IObjectFactory {

	org.docx4j.wml.P createP(String textContent);

	org.docx4j.wml.CTSmartTagRun createCTSmartTagRun(String textContent);

	org.docx4j.wml.PPr createPPr();

	org.docx4j.wml.PPr.PStyle createPStyle(String styleId);

	org.docx4j.wml.R createR(String textContent);

	org.docx4j.wml.RPr createRPr();

	org.docx4j.wml.RStyle createRStyle(String styleId);

	org.docx4j.wml.Text createT(String textContent);

	WordprocessingMLPackage createDocumentPackage(org.docx4j.wml.Document doc);

	WordprocessingMLPackage createEmptyDocumentPackage();

	org.docx4j.wml.Document createEmptyDocument();

	org.docx4j.wml.Document createEmptySharedDocument();

	org.docx4j.wml.Jc createJc(Integer align);

	org.docx4j.wml.BooleanDefaultTrue createBooleanDefaultTrue(Boolean b);

	org.docx4j.wml.U createUnderline(String value, String color);

	org.docx4j.wml.RFonts createRPrRFonts(String ascii);

	org.docx4j.wml.HpsMeasure createHpsMeasure(Integer value);

	org.docx4j.wml.Id createId(BigInteger val);

	org.docx4j.wml.Tag createTag(String val);

	org.docx4j.wml.SdtBlock createSdtBlock();

	org.docx4j.wml.SdtPr createSdtPr();

	org.docx4j.wml.SdtContentBlock createSdtContentBlock();

	org.docx4j.wml.P.Hyperlink createHyperlink();

	public void textChanged(Text text);
}