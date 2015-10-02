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

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

import org.docx4all.ui.main.Constants;
import org.docx4all.ui.main.WordMLEditor;
import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.microsoft.MicrosoftFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.jdesktop.application.ResourceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 05/03/2008
 */
public class FontManager {
	private static Logger log = LoggerFactory.getLogger(FontManager.class);

	public final static String UNKNOWN_FONT_NAME = "<Not Known>";
	public final static String UNKNOWN_FONT_SIZE = "##";

	private final static String DOCX4ALL_DEFAULT_FONT_FAMILY_NAME;
	private final static String DOCX4ALL_DEFAULT_FONT_SIZE;

	private final static FontManager _instance = new FontManager();

	private final static BestMatchingMapper mapper;

	private final static String[] AVAILABLE_FONT_SIZES = new String[] { UNKNOWN_FONT_SIZE, "8", "9", "10", "11", "12", "13", "14", "16",
			"18", "20", "22", "24", "26", "28", "32", "36", "40", "44", "48", "52", "56", "64", "72" };

	private final static String[] AVAILABLE_FONT_FAMILY_NAMES;

	static {
		log.info("Static initializer..");
		// Prepare available fonts that are listed in font combobox.
		log.info("Initialising fonts nameList.");
		Map<String, MicrosoftFonts.Font> msFontsFilenames = BestMatchingMapper.getMsFontsFilenames();
		List<String> nameList = new ArrayList<String>();
		for (Map.Entry<String, MicrosoftFonts.Font> entry : msFontsFilenames.entrySet()) {
			MicrosoftFonts.Font font = entry.getValue();
			if (booleanValue(font.isCoreWebFont()) || booleanValue(font.isClearTypeCollection()) || booleanValue(font.isSecondary())) {
				nameList.add(font.getName());
			}
		}

		// Initialise DOCX4ALL_DEFAULT_FONT.
		// The font name and size are configured in WordMLEditor.properties file
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);
		String temp = rm.getString(Constants.APP_DEFAULT_FONT_FAMILY_NAME);
		if (temp == null || temp.trim().length() == 0) {
			temp = "Times New Roman";
		}
		else {
			temp = temp.trim();
		}
		DOCX4ALL_DEFAULT_FONT_FAMILY_NAME = temp;
		if (!nameList.contains(DOCX4ALL_DEFAULT_FONT_FAMILY_NAME)) {
			// defaultFontName has to be listed in nameList
			throw new RuntimeException("Invalid " + Constants.APP_DEFAULT_FONT_FAMILY_NAME + " property value.");
		}

		temp = rm.getString(Constants.APP_DEFAULT_FONT_SIZE);
		if (temp == null || temp.trim().length() == 0) {
			temp = "22";
		}
		else {
			temp = temp.trim();
		}
		DOCX4ALL_DEFAULT_FONT_SIZE = temp;
		boolean invalidSize = true;
		for (String s : AVAILABLE_FONT_SIZES) {
			if (s.equals(DOCX4ALL_DEFAULT_FONT_SIZE)) {
				invalidSize = false;
			}
		}

		if (invalidSize) {
			throw new RuntimeException("Invalid " + Constants.APP_DEFAULT_FONT_SIZE + " property value.");
		}

		// Initialise substituter with all available font family names
		log.info("Initialising substituter.");
		mapper = new BestMatchingMapper();

		Set<String> fontsInUse = new java.util.HashSet<String>(nameList.size());
		for (String s : nameList) {
			fontsInUse.add(s);
		}

		try {
			FontTablePart fontTablePart = new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
			// For present purposes, use a fairly complete list of
			// panose values which can be found in the default part.
			org.docx4j.wml.Fonts tablePartDefaultFonts = (org.docx4j.wml.Fonts) fontTablePart.unmarshalDefaultFonts();

			// NB - we don't actually attach this part to any WordMLPackage

			// Process embedded fonts in fontTablePart.
			// This has to be done before calling populateFontMappings()
			// so that the embedded fonts can be taken into account.
			fontTablePart.processEmbeddings();

			// NB, the above won't have any effect, since there are no
			// embedded fonts in the default part which we just unmarshalled

			mapper.populateFontMappings(fontsInUse, tablePartDefaultFonts);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}

		// Populate AVAILABLE_FONT_FAMILY_NAMES.
		Collections.sort(nameList);
		nameList.add(0, UNKNOWN_FONT_NAME);
		AVAILABLE_FONT_FAMILY_NAMES = new String[nameList.size()];
		nameList.toArray(AVAILABLE_FONT_FAMILY_NAMES);

		log.info("FontManager static initialization complete.");
	}

	private final Hashtable<FontTableKey, Font> _fontTable = new Hashtable<FontTableKey, Font>();
	private final FontTableKey _fontTableKey = new FontTableKey(null, 0, 0);

	public final static FontManager getInstance() {
		return _instance;
	}

	private final static boolean booleanValue(Boolean b) {
		return (b == null) ? false : b.booleanValue();
	}

	private FontManager() {
		super();
	}

	public String[] getAvailableFontFamilyNames() {
		return AVAILABLE_FONT_FAMILY_NAMES;
	}

	public String[] getAvailableFontSizes() {
		return AVAILABLE_FONT_SIZES;
	}

	public String getDocx4AllDefaultFontFamilyName() {
		return DOCX4ALL_DEFAULT_FONT_FAMILY_NAME;
	}

	public int getDocx4AllDefaultFontSize() {
		return Integer.parseInt(DOCX4ALL_DEFAULT_FONT_SIZE);
	}

	public String getSourceViewFontFamilyName() {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);
		String name = rm.getString(Constants.SOURCE_VIEW_FONT_FAMILY_NAME);
		if (name == null || name.trim().length() == 0) {
			name = getDocx4AllDefaultFontFamilyName();
		}
		return name.trim();
	}

	public int getSourceViewFontSize() {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);

		int theSize = getDocx4AllDefaultFontSize();
		String s = rm.getString(Constants.SOURCE_VIEW_FONT_SIZE);
		if (s != null && s.length() > 0) {
			theSize = Integer.parseInt(s.trim());
		}
		return theSize;
	}

	public void addFontsInUse(WordprocessingMLPackage docPackage) {

		// Then is run once when the editor is starting up
		// from Stylesheet.setWordprocessingMLPackage,
		// which itself is invoked from:
		// org.docx4all.swing.text.StyleSheet.getDefaultStyleSheet(StyleSheet.java:68) (on startup)
		// and then again when a document is opened.

		log.info("");

		Set fontsInUse = docPackage.getMainDocumentPart().fontsInUse();
		FontTablePart fontTablePart = docPackage.getMainDocumentPart().getFontTablePart();

		try {
			// Handle fonts - this is platform specific
			// Algorithm - to be implemented:
			// 1. Get a list of all the fonts in the document
			org.docx4j.wml.Fonts fonts;
			if (fontTablePart != null) {
				fonts = fontTablePart.getJaxbElement();
			}
			else {
				log.warn("FontTable missing; creating default part.");

				fontTablePart = new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
				fonts = (org.docx4j.wml.Fonts) fontTablePart.unmarshalDefaultFonts();
			}

			// 2. Process embedded fonts in fontTablePart.
			// This has to be done before calling populateFontMappings()
			// so that the embedded fonts can be taken into account.
			fontTablePart.processEmbeddings();

			// 3. For each font, find the closest match on the system
			mapper.populateFontMappings(fontsInUse, fonts);

			if (log.isDebugEnabled()) {
				int i = 0;
				Map fontMappings = mapper.getFontMappings();
				Iterator fontMappingsIterator = fontMappings.entrySet().iterator();
				while (fontMappingsIterator.hasNext()) {
					Map.Entry pairs = (Map.Entry) fontMappingsIterator.next();

					String key = pairs.getKey().toString();
					PhysicalFont pf = (PhysicalFont) pairs.getValue();

					log.debug("FontMapping[" + (i++) + "]: key=" + key
					// + " tripletName=" + fm.getPostScriptName() + " -->> "
					// + fm.getEmbeddedFile());
							+ " -->> " + pf.getEmbeddedFile());
				}
			}

		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	public Font getFontInAction(AttributeSet attr) {
		int style = Font.PLAIN;
		if (StyleConstants.isBold(attr)) {
			style |= Font.BOLD;
		}
		if (StyleConstants.isItalic(attr)) {
			style |= Font.ITALIC;
		}
		String family = StyleConstants.getFontFamily(attr);

		// font size in OpenXML is in half points, not points,
		// so we need to divide by 2
		int size = StyleConstants.getFontSize(attr) / 2;

		// But Java2D 'point size' appears to be smaller than Windows 'point size'
		// Adjust with experimental multiplication factor for now.
		size = size * 14 / 9;

		// Reduce the font size by 2 for superscript or subscript
		if (StyleConstants.isSuperscript(attr) || StyleConstants.isSubscript(attr)) {
			size -= 2;
		}

		return getFontInAction(family, style, size);
	}

	private boolean defaultTried = false;

	public Font getFontInAction(String fontname, int style, int size) {
		_fontTableKey.setValue(fontname, style, size);
		Font theFont = _fontTable.get(_fontTableKey);

		if (theFont == null) {
			// Not in cache.
			// Derive from Substituter.FontMapping
			// String fmKey = SubstituterImplPanose.normalise(family);
			PhysicalFont pf = mapper.get(fontname);
			String path = null;
			if (pf != null && pf.getEmbeddedFile() != null) {
				path = pf.getEmbeddedFile();

				// Strip file:, and replace %20 with spaces
				path = org.docx4j.fonts.FontUtils.pathFromURL(path);

				if (log.isDebugEnabled()) {
					log.debug("family=" + fontname
					// + " fmKey=" + fmKey
					// + " --> FontMapping=" + fm
							+ " - " + path);
				}

				try {
					int fontFormat = Font.TRUETYPE_FONT;
					if (path.toLowerCase().endsWith(".otf") || path.toLowerCase().endsWith(".pfb")) {
						fontFormat = Font.TYPE1_FONT;
					}

					/* per http://elliotth.blogspot.com.au/2007/04/far-east-asian-fonts-with-java-7-on.html
					 * 
					 * If you call StyleContext.getDefaultStyleContext.getFont instead of new Font, you'll 
					 * get a composite font. The only thing to watch out for is that StyleContext's so-called 
					 * "cache" doesn't have an eviction policy. So if you're creating lots of randomized fonts, 
					 * this might cause problems.
					 * 
					 * Obviously, returning a composite font isn't documented behavior of this method, but it 
					 * would hurt Swing to regress, so it's unlikely to be broken. And you can always 
					 * reflect FontManager.getCompositeFontUIResource if the worst comes to the worst. 
					*/
					theFont = javax.swing.text.StyleContext.getDefaultStyleContext().getFont(fontname, style, size);

					/* OR, but requires JDK 7
					
					theFont = Font.createFont(fontFormat, new File(path));
					theFont = theFont.deriveFont(style, size);
										
					// JDK 7 way; see https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/share/classes/sun/font/FontUtilities.java
					if (!sun.font.FontUtilities.fontSupportsDefaultEncoding(theFont)) {
						theFont = sun.font.FontUtilities.getCompositeFontUIResource(theFont);
					}
					
					// the old way, but recent javac give error (Eclipse is ok) 
					//    if (! sun.font.FontManager.fontSupportsDefaultEncoding(theFont)) {
					//    	theFont = sun.font.FontManager.getCompositeFontUIResource(theFont);
					//    }
					//
					*/

					FontTableKey key = new FontTableKey(fontname, style, size);
					_fontTable.put(key, theFont);

				} catch (Exception exc) {
					// should not happen.
					throw new RuntimeException(exc);
				}
			}
			else {
				log.debug("Cannot create font '" + fontname + "'");
				if (pf == null) {
					log.debug(".. no mapping for that key.");
				}
				else {
					log.debug(".. found a mapping, but getEmbeddedFile returned null!");
				}

				if (defaultTried) {
					if (_fontTable.values().size() > 0) {
						log.debug("Failed to get default font; using first available");
						return _fontTable.values().iterator().next();
					}
					return Font.getFont(fontname);
				}
				else {
					log.debug("Using Docx4all default font.");
					defaultTried = true;
					theFont = getFontInAction(getDocx4AllDefaultFontFamilyName(), Font.PLAIN, getDocx4AllDefaultFontSize());
				}
			}
		}

		return theFont;
	}

	/**
	 * key for TableFont
	 */
	private static class FontTableKey {
		private String fontFamilyName;
		private int fontStyle;
		private int fontSize;

		public FontTableKey(String fontFamilyName, int fontStyle, int fontSize) {
			setValue(fontFamilyName, fontStyle, fontSize);
		}

		public void setValue(String fontFamilyName, int fontStyle, int fontSize) {
			this.fontFamilyName = (fontFamilyName != null) ? fontFamilyName.intern() : null;
			this.fontStyle = fontStyle;
			this.fontSize = fontSize;
		}

		@Override
		public int hashCode() {
			int code = (fontFamilyName != null) ? fontFamilyName.hashCode() : 0;
			return code ^ fontStyle ^ fontSize;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FontTableKey) {
				FontTableKey ftk = (FontTableKey) obj;
				return fontSize == ftk.fontSize && fontStyle == ftk.fontStyle && fontFamilyName.equals((ftk.fontFamilyName));
			}
			return false;
		}
	} // FontTableKey inner class

}// FontManager class
