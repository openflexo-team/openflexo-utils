/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexowysiwyg, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.wysiwyg;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.openflexo.toolbox.FileResource;

public class FlexoWysiwygPopup extends JFrame {

	protected FlexoWysiwyg wysiwyg;

	protected EditableHtmlWidget model;

	/**
	 * Creates a JFrame for the Wysiwyg component, with the menu bar. This version of the wysiwyg does not allow the user to import images
	 * or to create HTML forms.
	 * 
	 * @param targetWidget
	 *            the IE widget responsible for getting and setting the HTML content of the editor (cannot be null).
	 * @param cssFile
	 *            the CSS file to apply on the document.
	 * @see FlexoWysiwyg
	 */
	public FlexoWysiwygPopup(EditableHtmlWidget model, FileResource cssFile) throws HeadlessException {

		super();
		this.model = model;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if (model == null) {
			throw new NullPointerException("targetWidget cannot be null");
		}

		this.wysiwyg = new FlexoWysiwyg(model.getValue(), cssFile, true) {
			@Override
			public void notifyTextChanged() {
				updateModelFromWidget();
			}
		};
		// remove support for images
		wysiwyg.setRemovedToolbarItems("insertImageButton");
		wysiwyg.setRemovedMenuItems("insertInsertImageMenuItem");
		wysiwyg.setPreferredSize(new Dimension(830, 500));

		setContentPane(wysiwyg);
		// setAlwaysOnTop(true);
		pack();
		// center frame
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (d.getWidth() - getWidth()) / 2, (int) (d.getHeight() - getHeight()) / 2);
		setVisible(true);
		toFront();
	}

	@Override
	public void dispose() {
		updateModelFromWidget();
		super.dispose();
	}

	public FlexoWysiwyg getWysiwyg() {
		return wysiwyg;
	}

	/**
	 * @param targetWidget
	 */
	protected void updateModelFromWidget() {
		model.setValue(wysiwyg.getBodyContent());
	}

	public static void main(String[] args) throws Exception {

		File documentBaseFolder = new File("/Users/ajasselette/Desktop/WysiwygTest/");
		if (!documentBaseFolder.exists()) {
			documentBaseFolder.mkdir();
		}

		EditableHtmlWidget targetWidget = new EditableHtmlWidget() {
			@Override
			public String getValue() {
				return "<html><body>Test</body></html>";
			}

			@Override
			public void setValue(String value) {
				System.out.println("CHANGE:\n" + value);
			}
		};
		FlexoWysiwygPopup popup = new FlexoWysiwygPopup(targetWidget, new FileResource(""));
		popup.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
