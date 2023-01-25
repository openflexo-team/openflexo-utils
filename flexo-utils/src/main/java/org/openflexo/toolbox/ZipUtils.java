/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexoutils, a component of the software infrastructure 
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

package org.openflexo.toolbox;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	public static final String VALID_ENTRY_NAME_REGEXP = "\\p{ASCII}+";
	public static final Pattern VALID_ENTRY_NAME_PATTERN = Pattern.compile(VALID_ENTRY_NAME_REGEXP);

	/**
	 * This method makes a zip file on file <code>zipOutput</code>out of the given <code>fileToZip</code>
	 * 
	 * @param zipOutput
	 *            - the output where to write the zip
	 * @param fileToZip
	 *            the file to zip (wheter it is a file or a directory)
	 * @throws IOException
	 */
	public static void makeZip(File zipOutput, File fileToZip, FileFilter filter, int level) throws IOException {
		FileUtils.createNewFile(zipOutput);
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipOutput))) {
			zos.setLevel(level);
			if (fileToZip.isDirectory()) {
				// progress.resetSecondaryProgress(FileUtils.countFilesInDirectory(fileToZip, true) + 1);
				zipDir(fileToZip.getParentFile().getAbsolutePath().length() + 1, fileToZip, zos, filter);
			}
			else {
				zipFile(fileToZip, zos);
			}
		}
	}

	/**
	 * This method makes a zip on the outputsream <code>zos</code>out of the given <code>dirToZip</code>
	 * 
	 * @param dirToZip
	 *            the directory to zip
	 * @param zos
	 *            the output stream where to write the zip data
	 * @throws IOException
	 */
	public static void zipDir(File dirToZip, ZipOutputStream zos) throws IOException {
		zipDir(dirToZip.getParentFile().getAbsolutePath().length() + 1, dirToZip, zos, null);
	}

	/**
	 * This method makes a zip on the outputsream <code>zos</code>out of the given <code>fileToZip</code>
	 * 
	 * @param fileToZip
	 *            the file to zip
	 * @param zos
	 *            the output stream where to write the zip data
	 * @throws IOException
	 */

	public static void zipFile(File fileToZip, ZipOutputStream zos) throws IOException {
		zipFile(fileToZip.getParentFile().getAbsolutePath().length() + 1, fileToZip, zos);
	}

	public static void zipDir(int pathPrefixSize, File dirToZip, ZipOutputStream zos, FileFilter filter) throws IOException {
		String[] dirList = dirToZip.list();
		for (int i = 0; i < dirList.length; i++) {
			File f = new File(dirToZip, dirList[i]);
			if (filter == null || f != null && filter.accept(f)) {
				if (f.isDirectory()) {
					zipDir(pathPrefixSize, f, zos, filter);
				}
				else {
					zipFile(pathPrefixSize, f, zos);
				}
			}
		}
	}

	private static void zipFile(int pathPrefixSize, File fileToZip, ZipOutputStream zos) throws IOException {
		if (!fileToZip.exists()) {
			return;
		}
		byte[] readBuffer = new byte[4096];
		int bytesIn = 0;
		// progress.setSecondaryProgress("zipping_file" + " " + fileToZip.getAbsolutePath().substring(pathPrefixSize));
		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			ZipEntry anEntry = new ZipEntry(fileToZip.getAbsolutePath().substring(pathPrefixSize).replace('\\', '/'));
			// place the zip entry in the ZipOutputStream object
			zos.putNextEntry(anEntry);
			// now write the content of the file to the ZipOutputStream
			while ((bytesIn = fis.read(readBuffer)) != -1) {
				zos.write(readBuffer, 0, bytesIn);
			}
		}
	}

	public static void unzipFile(String source, String destDirectory) throws IOException {
		FileInputStream zipFile = new FileInputStream(source);

		try (ArchiveInputStream i = new ZipArchiveInputStream(zipFile, "UTF-8", false, true)) {
			ArchiveEntry entry = null;
			while ((entry = i.getNextEntry()) != null) {
				if (!i.canReadEntryData(entry)) {
					System.out.println("Can't read entry: " + entry);
					continue;
				}
				String name = destDirectory + File.separator + entry.getName();
				File f = new File(name);
				if (entry.isDirectory()) {
					if (!f.isDirectory() && !f.mkdirs()) {
						throw new IOException("failed to create directory " + f);
					}
				} else {
					File parent = f.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("failed to create directory " + parent);
					}
					try (OutputStream o = Files.newOutputStream(f.toPath())) {
						IOUtils.copy(i, o);
					}
				}
			}
		}
	}
}
