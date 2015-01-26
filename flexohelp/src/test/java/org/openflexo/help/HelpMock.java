/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexohelp, a component of the software infrastructure 
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

package org.openflexo.help;

import javax.help.CSH;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Please comment this class
 * 
 * @author sguerin
 * 
 */
public class HelpMock {
	JFrame f;

	JMenuItem overviewHelp;

	JMenuItem wkfModuleHelp;

	JMenuItem ieModuleHelp;

	JMenuItem dmModuleHelp;

	JMenuItem cgfModuleHelp;

	public HelpMock() {
		f = new JFrame("Test help");
		JButton button = new JButton("DM Module");
		f.getContentPane().add(button);
		JMenuBar mbar = new JMenuBar();
		// menus Fichier et Aide
		JMenu help = new JMenu("Aide");
		// ajout d un item dans le menu Aide
		help.add(overviewHelp = new JMenuItem("Overview"));
		help.add(wkfModuleHelp = new JMenuItem("WKFModule"));
		// ajout des menu a la barre de menu
		mbar.add(help);
		// creation des objetsHelpSet et HelpBroker
		// affectation de l aide au composant
		CSH.setHelpIDString(overviewHelp, "top");
		CSH.setHelpIDString(wkfModuleHelp, "wkf-module");
		CSH.setHelpIDString(button, "dm-module");
		// gestion des evenements
		overviewHelp.addActionListener(new CSH.DisplayHelpFromSource(FlexoHelp.getHelpBroker()));
		wkfModuleHelp.addActionListener(new CSH.DisplayHelpFromSource(FlexoHelp.getHelpBroker()));
		button.addActionListener(new CSH.DisplayHelpFromSource(FlexoHelp.getHelpBroker()));
		// attachement de la barre de menu a la fenetre
		f.setJMenuBar(mbar);
		f.setSize(500, 300);
		f.setVisible(true);
	}

	public static void main(String argv[]) {
		new HelpMock();
	}

}
