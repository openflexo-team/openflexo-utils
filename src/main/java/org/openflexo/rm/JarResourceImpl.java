/*
 * (c) Copyright 2014- Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.rm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 
 * This Resource Impl represents a Jar File Containing other files
 * 
 * @author xtof
 *
 */

public class JarResourceImpl extends BasicResourceImpl implements Resource {


	private static final Logger LOGGER = Logger.getLogger(JarResourceImpl.class.getPackage().getName());

	private List<Resource> contents;
	private JarFile jarfile = null;
	public JarFile getJarfile() {
		return jarfile;
	}

	private String jarfilename = null;


	public JarResourceImpl(ResourceLocatorDelegate locator, String filename)
			throws MalformedURLException {
		super(locator);
		this._relativePath = filename;
		jarfile = null;
		try {
			jarfile = new JarFile(filename);
		} catch (UnsupportedEncodingException e) {
			LOGGER.severe("Unable to create JarResource with filename: " +filename);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("Unable to create JarResource with filename: " +filename);
			e.printStackTrace();
		}
		if (jarfile != null){
			_url = new URL("file:"+filename);
			jarfilename = filename;
		}
		else {
			LOGGER.severe("Unable to create JarResource with filename: " +filename);
			return;
		}
		
		// Add the jarResource in the locator resource list if not contained
		/*if(locator instanceof ClasspathResourceLocatorImpl){
			ClasspathResourceLocatorImpl classPathLocator = (ClasspathResourceLocatorImpl)locator;
			if(classPathLocator.getJarResourcesList().get(this)==null){
				classPathLocator.getJarResourcesList().put(this.getRelativePath(), this);
			}
		}*/
	}

	public JarResourceImpl(ResourceLocatorDelegate locator, JarFile jarFile)
			throws MalformedURLException {
		super(locator);
		this._relativePath = jarFile.getName();
		this.jarfile = jarFile;
		if (jarfile != null){
			_url = new URL("file:"+jarFile.getName());
			jarfilename = jarFile.getName();
		}
		else {
			LOGGER.severe("Unable to create JarResource with filename: " +jarFile.getName());
			return;
		}
	}

	@Override
	public Resource getContainer() {
		return null;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public List<? extends Resource> getContents() {
		URL url = getURL();

		if (jarfile != null) {
			try{
				Enumeration<JarEntry> entries = jarfile.entries(); //gives ALL entries in jar
				List<Resource> retval = new ArrayList<Resource>();
				while(entries.hasMoreElements()) {

					JarEntry current = entries.nextElement();
					String name = current.getName();

					InJarResourceImpl inJarResource = new InJarResourceImpl(name, new URL("jar", url.getHost(),"file:"+jarfilename +"!/"+name));
					inJarResource.setEntry(current);
					retval.add(inJarResource);
				}

				return retval;
			}
			catch (Exception e){
				LOGGER.severe("Unable to look for resources in URL : " + url);
				e.printStackTrace();
			}

			return java.util.Collections.emptyList();
		}

		return super.getContents();
	}

	@Override
	public List<? extends Resource> getContents(Pattern pattern) {
		URL url = getURL();
		try{
			Enumeration<JarEntry> entries = jarfile.entries(); //gives ALL entries in jar
			List<Resource> retval = new ArrayList<Resource>();
			while(entries.hasMoreElements()) {
				JarEntry current = entries.nextElement();
				String name = current.getName();
				boolean accept = pattern.matcher(name).matches();
				if (accept) {
					InJarResourceImpl res = new InJarResourceImpl(name, new URL("jar", url.getHost(),"file:"+jarfilename +"!/"+name));
					res.setEntry(current);
					retval.add(res);
				}
			}

			return retval;
		}
		catch (Exception e){
			LOGGER.severe("Unable to look for resources in URL : " + url);
			e.printStackTrace();
		}

		return java.util.Collections.emptyList();
	}


	public List<? extends Resource> getContents(String startpath, Pattern pattern) {

		URL url = getURL();
		try{
			Enumeration<JarEntry> entries = jarfile.entries(); //gives ALL entries in jar
			List<Resource> retval = new ArrayList<Resource>();
			while(entries.hasMoreElements()) {
				JarEntry current = entries.nextElement();
				String name = current.getName();
				boolean accept = pattern.matcher(name).matches();
				if (name.startsWith(startpath) && accept) { //filter according to the path
					String entry = name.substring(startpath.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					InJarResourceImpl res = new InJarResourceImpl(name, new URL("jar", url.getHost(),"file:"+jarfilename +"!/"+name));
					res.setEntry(current);
					retval.add(res);
				}
			}

			return retval;
		}
		catch (Exception e){
			LOGGER.severe("Unable to look for resources in URL : " + url);
			e.printStackTrace();
		}

		return java.util.Collections.emptyList();

	}



	@Override
	public void setURI(String anURI) {
		return;
	}

	@Override
	public String getRelativePath() {
		return jarfilename;
		
	}

	public InputStream openInputStream(JarEntry entry){
		if (jarfile != null){
			try {
				return jarfile.getInputStream(entry);
			} catch (IOException e) {
				LOGGER.severe("Unable to access Resource");
				e.printStackTrace();
			}
		}
		return null;
	}

}
