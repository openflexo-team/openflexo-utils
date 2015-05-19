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

import java.io.File;

public abstract class FlexoWysiwygLight extends FlexoWysiwyg {

	/**
	 * Creates the light version of wysiwyg component without the JMenuBar and without any CSS support. This version removes a set of the
	 * less useful options. This class must implement <code>textChanged(String htmlText)</code> to be concrete.
	 */

	public FlexoWysiwygLight(boolean isViewSourceAvailable) {
		this(null, isViewSourceAvailable);
	}

	/**
	 * Creates the light version of wysiwyg component without the JMenuBar and without any CSS support. This version removes a set of the
	 * less useful options. This class must implement <code>textChanged(String htmlText)</code> to be concrete.
	 * 
	 * @param htmlContent
	 *            if not null, will initialize the wysiwyg with this HTML content.
	 */
	public FlexoWysiwygLight(String htmlContent, boolean isViewSourceAvailable) {
		this(htmlContent, null, isViewSourceAvailable);
	}

	/**
	 * Creates the light version of wysiwyg component without the JMenuBar and with CSS support. This version removes a set of the less
	 * useful options. This class must implement <code>textChanged(String htmlText)</code> to be concrete.
	 * 
	 * @param htmlContent
	 *            if not null, will initialize the wysiwyg with this HTML content.
	 * @param cssFile
	 *            the CSS file to apply on the document.
	 */
	public FlexoWysiwygLight(String htmlContent, File cssFile, boolean isViewSourceAvailable) {

		super(htmlContent, cssFile, isViewSourceAvailable);
		// remove elements
		setMainMenuVisible(false);
		setRemovedMenus("menuTable");
		setRemovedMenuItems("insertTableMenuItem");
		setRemovedToolbarItems("printFileButton, fontsList, fontSizeButton, decreaseIndentButton, increaseIndentButton, subscriptButton, superscriptButton");
		setRemovedPopupMenuItems("insertTableMenuItem, fontPropertiesMenuItem, paragraphPropertiesPopupMenuItem, listPropertiesMenuItem, imagePropertiesMenuItem");
		setPreviewVisible(false);
		setStatusBarVisible(false);
	}

	@Override
	/**
	 * Overridden to remove all table options ,since they are handled by the LaTeX transcriptor for now.
	 */
	protected void initTableToolbar() {
		// override to prevent table toolbar from drawing
	}
}
