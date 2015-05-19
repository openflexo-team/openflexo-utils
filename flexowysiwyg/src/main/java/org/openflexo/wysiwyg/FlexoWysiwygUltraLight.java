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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import sferyx.administration.editors.CustomEditorPane;

public abstract class FlexoWysiwygUltraLight extends FlexoWysiwygLight {

	/**
	 * Creates the ultra light" version of wysiwyg component with only one toolbar and a set of the 10 most useful options. This class must
	 * implement <code>textChanged(String htmlText)</code> to be concrete.
	 * 
	 * @param isUltraLight
	 *            if true, will create ultra light version of the wysiwyg.
	 */
	public FlexoWysiwygUltraLight(boolean isViewSourceAvailable) {
		this(null, isViewSourceAvailable);
	}

	/**
	 * Creates the ultra light" version of wysiwyg component with only one toolbar and a set of the 10 most useful options. This class must
	 * implement <code>textChanged(String htmlText)</code> to be concrete.
	 * 
	 * @param isUltraLight
	 *            if true, will create ultra light version of the wysiwyg.
	 * @param htmlContent
	 *            if not null, will initialize the wysiwyg with this HTML content.
	 */
	public FlexoWysiwygUltraLight(String htmlContent, boolean isViewSourceAvailable) {

		super(htmlContent, isViewSourceAvailable);
		setShortcutToolbarVisible(false); // hide the editing toolbar
		setRemovedToolbarItems("headingStyles, fontsList, fontSizes, styleClasses, superscriptButton, subscriptButton, alignLeftButton, alignCenterButton, alignRightButton, alignJustifyButton, increaseIndentButton, decreaseIndentButton, setForegroundButton");
		// we need to get some buttons that were on the editing toolbar to put them on the formatting toolbar
		JToolBar editingToolbar = getEditingToolBar();
		JToolBar formattingToolbar = getFormattingToolBar();
		editingToolbar.add(new JToolBar.Separator());
		JButton currentIcon = null;
		for (Component c : editingToolbar.getComponents()) {
			if (c instanceof JButton) {
				currentIcon = (JButton) c;
				// very bourin but the tooltip text is the only identifier I can use (no name nor actionName on components)
				if (currentIcon.getToolTipText().equals("undo") || currentIcon.getToolTipText().equals("redo")
						|| currentIcon.getToolTipText().equals("insert hyperlink")) {
					formattingToolbar.add(currentIcon);
				}
			}
		}
		setSourceEditorVisible(false);
		if (getIsViewSourceAvailable()) {
			JButton switchViewButton = createMenuButton(getFormattingToolBar(), "switch view", "switchView",
					getSharedIcon("page-properties"));
			switchViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switchView();
				}
			});
		}
	}

	public void switchView() {

		if (isSourceEditorVisible()) {// switch to classic view
			for (Component c : getMainTabbedPane().getComponents()) { // ...get the editor tab content by browsing the content of the
																		// mainTabbedPane
				if (c instanceof JScrollPane && ((JScrollPane) c).getViewport().getView() instanceof CustomEditorPane) {
					getMainTabbedPane().setSelectedIndex(getMainTabbedPane().indexOfComponent(c)); // diplay it
				}
			}
			setSourceEditorVisible(false);
		} else { // switch to html source view,
			setSourceEditorVisible(true);
			for (Component c : getMainTabbedPane().getComponents()) { // ...get the source html tab content by browsing the content of the
																		// mainTabbedPane
				if (c instanceof JScrollPane && ((JScrollPane) c).getViewport().getView() instanceof JTextPane) {
					getMainTabbedPane().setSelectedIndex(getMainTabbedPane().indexOfComponent(c)); // diplay it
				}
			}
		}
		revalidate();
		repaint();
	}
}
