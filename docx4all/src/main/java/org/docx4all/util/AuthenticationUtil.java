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

package org.docx4all.util;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import net.sf.vfsjfilechooser.utils.VFSURIParser;
import net.sf.vfsjfilechooser.utils.VFSUtils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.webdav.WebdavClientFactory;
import org.apache.commons.vfs.provider.webdav.WebdavFileObject;
import org.apache.commons.vfs.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.webdav.WebdavMethodRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4all.swing.UserAuthenticationDialog;
import org.docx4all.ui.main.WordMLEditor;
import org.jdesktop.application.ResourceMap;

/**
 *	@author Jojada Tirtowidjojo - 03/12/2008
 */
public class AuthenticationUtil {
	private static Logger log = LoggerFactory.getLogger(AuthenticationUtil.class);

	/**
	 * Display a user authentication form dialog.
	 * The username and password fields will be used to authenticate against 
	 * webdavUrl path.
	 * If the authentication is successful then a FileObject that
	 * represents resource at webdavUrl will be returned otherwise a null
	 * value is returned.
	 * The returned FileObject does not guarantee that the resource it represents 
	 * exists. User has to check the resource existence separately.
	 * 
	 * User has three chances to retry before authentication is considered to
	 * be unsuccessful.
	 *  
	 * @param editor
	 * @param webdavUrl
	 * @param dialogTitle
	 * @return A FileObject that represents resource at webdavUrl;
	 *         Null, otherwise.
	 * @throws FileSystemException is thrown when there is an underlying 
	 * communication error or VFS file system error.
	 */
	public final static FileObject userAuthenticationChallenge(
		WordMLEditor editor, 
		String webdavUrl,
		String dialogTitle)
		throws FileSystemException {
		
		WebdavFileObject theFile = null;
		
		ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);
		
		UserAuthenticationDialog dialog = 
			new UserAuthenticationDialog(editor, webdavUrl);
		dialog.pack();
		dialog.setLocationRelativeTo(editor.getMainFrame());
		dialog.setSize(new Dimension(400, 250));

		int retry = 1;
		int status = 401;
		VFSURIParser url = new VFSURIParser(webdavUrl, false);
		
		while (status != 200 && retry <= 3) {
			dialog.setVisible(true);

			if (dialog.getValue() == UserAuthenticationDialog.CANCEL_BUTTON_TEXT) {
				dialog.setVisible(false);
				dialog.dispose();
				retry = 100; // break
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("webdav://");
				sb.append(dialog.getUsername());
				sb.append(":");
				sb.append(dialog.getPassword());
				sb.append("@");
				sb.append(url.getHostname());
				if (url.getPortnumber() != null && url.getPortnumber().length() > 0) {
					sb.append(":");
					sb.append(url.getPortnumber());
				}
				sb.append(url.getPath());

				theFile =
					(WebdavFileObject)
						VFSUtils.getFileSystemManager().resolveFile(
							sb.toString());
				status = getAuthorisationStatus(theFile);
				
				log.info("userAuthenticationChallenge(): retry=" + retry + " status=" + status);
				
				if (status != 200) {
					theFile = null;
					status = 401;
					
					String msg = rm.getString("UserAuthentication.tryAgain.message");
					editor.showMessageDialog(
						dialogTitle, msg, JOptionPane.INFORMATION_MESSAGE);
					retry++;
				}
			}
		} //while (status != 200 && retry <= 3);
		
		if (status == 200) {
			;//pass
		} else if (retry == 4) {
			//authentication failure
			String msg = rm.getString("UserAuthentication.failure.message");
			editor.showMessageDialog(
				dialogTitle, msg, JOptionPane.INFORMATION_MESSAGE);
		} else {
			;//User must have cancelled the dialog
		}
		
		return theFile;
	}
	
	public final static int getAuthorisationStatus(WebdavFileObject fo) throws FileSystemException {
		int status = 401; //unauthorised and retry.
		
		org.apache.webdav.lib.methods.OptionsMethod optionsMethod = null;
		try {
			String urlCharset = 
				WebdavFileSystemConfigBuilder.getInstance().getUrlCharset(
						fo.getFileSystem().getFileSystemOptions());
			URLFileName urlFileName = (URLFileName) fo.getName();
			optionsMethod = 
				new org.apache.webdav.lib.methods.OptionsMethod(
						urlFileName.getPathQueryEncoded(urlCharset));
		
			optionsMethod.setMethodRetryHandler(
					WebdavMethodRetryHandler.getInstance());
			optionsMethod.setFollowRedirects(true);

			char[] username = null;
			if (urlFileName.getUserName() != null) {
				username = urlFileName.getUserName().toCharArray();
			}
			char[] password = null;
			if (urlFileName.getPassword() != null) {
				password = urlFileName.getPassword().toCharArray();
			}
			
			WebdavClientFactory factory = new WebdavClientFactory();
			HttpClient client = 
				factory.createConnection(
						urlFileName.getHostName(),
						urlFileName.getPort(),
						username,
						password,
						fo.getFileSystem().getFileSystemOptions());
			status = client.executeMethod(optionsMethod);
		} catch (Exception exc) {
			throw new FileSystemException("Cannot get authorisation status", exc);
		} finally {
			if (optionsMethod != null) {
				optionsMethod.releaseConnection();
			}
		}
	
		return status;
	}

	private AuthenticationUtil() {
		;//uninstantiable
	}
	
}// AuthenticationUtil class



















