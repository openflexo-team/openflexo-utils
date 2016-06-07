/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.help.HelpBroker;
import javax.help.HelpSet;

import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Please comment this class
 * 
 * @author sguerin
 * 
 */
public class FlexoHelp extends Observable {

	private static final Logger logger = Logger.getLogger(FlexoHelp.class.getPackage().getName());

	private static HelpSet _hs = null;
	private static HelpBroker _hb = null;
	private static File _helpSetFile;

	private static String languageIdentifier = null;
	private static String distributionName = null;
	private static boolean initialized = false;

	public static void configure(String aLanguageIdentifier, String aDistributionName) {
		languageIdentifier = aLanguageIdentifier;
		distributionName = aDistributionName;
		initialized = true;
	}

	public static FlexoHelp instance = new FlexoHelp();

	private FlexoHelp() {
		super();
	}

	public static HelpBroker getHelpBroker() {
		if (!initialized) {
			logger.warning("HelpSet not initialized !");
		}
		if (_hb == null && getHelpSet() != null) {
			_hb = getHelpSet().createHelpBroker();
		}
		return _hb;
	}

	public static HelpSet getHelpSet() {
		if (!initialized) {
			logger.warning("HelpSet not initialized !");
		}
		if (_hs == null) {
			_hs = buildHelpSet();
		}
		return _hs;
	}

	public static void reloadHelpSet() {
		reloadHelpSet(null);
	}

	public static void reloadHelpSet(File helpSetFile) {
		if (!initialized) {
			logger.warning("HelpSet not initialized !");
		}
		if (_hs != null) {
			_hs.remove(_hs);
		}
		_hs = null;
		_hb = null;
		_helpSetFile = helpSetFile;
		getHelpSet();
		getHelpBroker();
		instance.setChanged();
		instance.notifyObservers();
	}

	private static HelpSet buildHelpSet() {
		// String helpsetfile = getHelpSetFile();
		HelpSet hs = null;
		// ClassLoader cl = FlexoHelp.class.getClassLoader();
		try {
			// URL hsURL=HelpSet.findHelpSet(cl,helpsetfile);
			URL hsURL = getHelpSetUrl();
			if (hsURL == null) {
				return null;
			}
			hs = new HelpSet(null, hsURL);
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Help files have not been found: " + e.getMessage());
			}
			e.printStackTrace();
		}
		return hs;
	}

	private static URL getHelpSetUrl() throws MalformedURLException {
		if (_helpSetFile == null) {
			_helpSetFile = getMostRecentHelpsetFile();
			if (_helpSetFile == null) {
				return null;
			}
		}
		if (logger.isLoggable(Level.INFO)) {
			logger.info("HelpSetFile:" + _helpSetFile.getAbsolutePath());
		}
		return _helpSetFile.toURL();
	}

	public static Resource getHelpSetDirectory() {
		return ResourceLocator.locateResource("Help");
	}

	private static File getMostRecentHelpsetFile() {
		String endPattern = "_" + distributionName + "_" + languageIdentifier + ".helpset";

		List<Resource> allFiles = null;

		// TODO en fait on veut juste les répertoires qui contiennent des .hs....
		allFiles = (List<Resource>) getHelpSetDirectory().getContents(Pattern.compile(".*[.]hs"));

		/*
		ResourceLocation[] allFiles = getHelpSetDirectory().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (!pathname.isDirectory()) {
					return false;
				}
				File[] all_Files = pathname.listFiles();
				if (all_Files == null) {
					return false;
				}
				for (int i = 0; i < all_Files.length; i++) {
					if (all_Files[i].getName().endsWith(".hs")) {
						return true;
					}
				}
				return false;
			}
		});
		*/

		if (allFiles == null) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Missing help directory: " + getHelpSetDirectory().getURI());
			}
			return null;
		}
		/*
		File directory = null;
		for (int i = 0; i < allFiles.length; i++) {
			if (allFiles[i].getName().endsWith(endPattern) && (directory == null || allFiles[i].lastModified() > directory.lastModified())) {
				directory = allFiles[i];
				break;
			}
		}
		if (directory == null) {
			for (int i = 0; i < allFiles.length; i++) {
				if (allFiles[i].getName().endsWith(".helpset")
						&& (directory == null || allFiles[i].lastModified() > directory.lastModified())) {
					directory = allFiles[i];
				}
			}
		}
		if (directory == null) {
			return null;
		}
		allFiles = directory.listFiles();
		for (int i = 0; i < allFiles.length; i++) {
			if (allFiles[i].getName().endsWith(".hs")) {
				return allFiles[i];
			}
		}
		*/
		return null;
	}

	public static boolean isAvailable() {
		return getHelpSet() != null && getHelpBroker() != null;
	}

	/*
	 * private static String getHelpSetFile() { //return ".."+File.separator+"FlexoHelp"+File.separator+"Help"+File.separator+"Flexo.hs";
	 * String helpSetFile = new FileResource(File.separator+"Help"+File.separator+"Flexo.hs").getAbsolutePath(); if
	 * (logger.isLoggable(Level.INFO)) logger.info("HelpSetFile:"+helpSetFile); return helpSetFile; }
	 */

}
