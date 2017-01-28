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

package org.docx4all.ui.menu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.xml.bind.Marshaller;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;
import org.docx4all.swing.NewShareDialog;
import org.docx4all.swing.PDFViewer;
import org.docx4all.swing.WordMLTextPane;
import org.docx4all.swing.text.DocumentElement;
import org.docx4all.swing.text.WordMLDocument;
import org.docx4all.swing.text.WordMLDocumentFilter;
import org.docx4all.swing.text.WordMLEditorKit;
import org.docx4all.ui.main.Constants;
import org.docx4all.ui.main.ToolBarStates;
import org.docx4all.ui.main.WordMLEditor;
import org.docx4all.util.DocUtil;
import org.docx4all.util.PreferenceUtil;
import org.docx4all.util.SwingUtil;
import org.docx4all.util.XmlUtil;
import org.docx4all.vfs.FileNameExtensionFilter;
import org.docx4all.xml.DocumentML;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.ObjectFactory;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
//import org.docx4j.convert.out.html.HtmlExporter;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToVFSZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.vfsjfilechooser.VFSJFileChooser;
import net.sf.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;
import net.sf.vfsjfilechooser.VFSJFileChooser.SELECTION_MODE;
import net.sf.vfsjfilechooser.accessories.DefaultAccessoriesPanel;
import net.sf.vfsjfilechooser.utils.VFSUtils;

/**
 * @author Jojada Tirtowidjojo - 27/11/2007
 */
public class FileMenu extends UIMenu {
	private static Logger log = LoggerFactory.getLogger(FileMenu.class);

	private final static FileMenu _instance = new FileMenu();

	/**
	 * The binding key used for this FileMenu object when passed into scripting environment
	 */
	public final static String SCRIPT_BINDING_KEY = "fileMenu:org.docx4all.ui.menu.FileMenu";

	// ==========
	// MENU Names
	// ==========
	// Used as an argument to JMenu.setName().
	// Therefore it can be used in .properties file
	// to configure File Menu property in the menu bar
	/**
	 * The name of JMenu object that hosts File menu in the menu bar
	 */
	public final static String FILE_MENU_NAME = "fileMenu";

	// ============
	// ACTION Names
	// ============
	// The string value of each action name must be the same as
	// the method name annotated by @Action tag.
	// Action name is used to configure Menu/Button Action property
	// in .properties file and get an Action object out of
	// Spring Application Framework

	/**
	 * The action name of New file menu
	 */
	public final static String NEW_FILE_ACTION_NAME = "newFile";

	/**
	 * The action name of Open file menu
	 */
	public final static String OPEN_FILE_ACTION_NAME = "openFile";

	/**
	 * The action name of Save file menu
	 */
	public final static String SAVE_FILE_ACTION_NAME = "saveFile";

	/**
	 * The action name of Save As file menu
	 */
	public final static String SAVE_AS_DOCX_ACTION_NAME = "saveAsDocx";

	/**
	 * The action name of Save As file menu
	 */
	public final static String SAVE_ALL_FILES_ACTION_NAME = "saveAllFiles";

	/**
	 * The action name of Export As Html menu
	 */
	public final static String EXPORT_AS_HTML_ACTION_NAME = "exportAsHtml";

	/**
	 * The action name of Save As Shared Document menu
	 */
	public final static String EXPORT_AS_SHARED_DOC_ACTION_NAME = "exportAsSharedDoc";

	/**
	 * The action name of Export As Non Shared Document menu
	 */
	public final static String EXPORT_AS_NON_SHARED_DOC_ACTION_NAME = "exportAsNonSharedDoc";

	/**
	 * The action name of Print Preview menu
	 */
	public final static String PRINT_PREVIEW_ACTION_NAME = "printPreview";

	/**
	 * The action name of Close menu
	 */
	public final static String CLOSE_FILE_ACTION_NAME = "closeFile";

	/**
	 * The action name of Close All menu
	 */
	public final static String CLOSE_ALL_FILES_ACTION_NAME = "closeAllFiles";

	/**
	 * The action name of Exit menu
	 */
	public final static String EXIT_ACTION_NAME = "exit";

	private static final String[] _menuItemActionNames = { NEW_FILE_ACTION_NAME, OPEN_FILE_ACTION_NAME, SEPARATOR_CODE,
			SAVE_FILE_ACTION_NAME, SAVE_AS_DOCX_ACTION_NAME, SAVE_ALL_FILES_ACTION_NAME, SEPARATOR_CODE, EXPORT_AS_HTML_ACTION_NAME,
			EXPORT_AS_SHARED_DOC_ACTION_NAME, EXPORT_AS_NON_SHARED_DOC_ACTION_NAME, SEPARATOR_CODE, PRINT_PREVIEW_ACTION_NAME,
			SEPARATOR_CODE, CLOSE_FILE_ACTION_NAME, CLOSE_ALL_FILES_ACTION_NAME, SEPARATOR_CODE, EXIT_ACTION_NAME };

	public static FileMenu getInstance() {
		return _instance;
	}

	private short _untitledFileNumber = 0;

	private FileMenu() {
		;// singleton
	}

	@Override
	public String[] getMenuItemActionNames() {
		String[] names = new String[_menuItemActionNames.length];
		System.arraycopy(_menuItemActionNames, 0, names, 0, _menuItemActionNames.length);
		return names;
	}

	@Override
	public String getMenuName() {
		return FILE_MENU_NAME;
	}

	@Override
	protected JMenuItem createMenuItem(String actionName) {
		JMenuItem theItem = super.createMenuItem(actionName);

		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ToolBarStates toolbarStates = editor.getToolbarStates();

		if (SAVE_FILE_ACTION_NAME.equals(actionName) || SAVE_AS_DOCX_ACTION_NAME.equals(actionName)) {
			theItem.setEnabled(false);
			toolbarStates.addPropertyChangeListener(ToolBarStates.DOC_DIRTY_PROPERTY_NAME, new EnableOnEqual(theItem, Boolean.TRUE));

		}
		else if (SAVE_ALL_FILES_ACTION_NAME.equals(actionName)) {
			theItem.setEnabled(false);
			toolbarStates.addPropertyChangeListener(ToolBarStates.ANY_DOC_DIRTY_PROPERTY_NAME, new EnableOnEqual(theItem, Boolean.TRUE));

		}
		else if (EXPORT_AS_SHARED_DOC_ACTION_NAME.equals(actionName)) {
			theItem.setEnabled(false);
			toolbarStates.addPropertyChangeListener(ToolBarStates.DOC_SHARED_PROPERTY_NAME, new DisableOnEqual(theItem, Boolean.TRUE));

		}
		else if (EXPORT_AS_NON_SHARED_DOC_ACTION_NAME.equals(actionName)) {
			theItem.setEnabled(false);
			toolbarStates.addPropertyChangeListener(ToolBarStates.DOC_SHARED_PROPERTY_NAME, new EnableOnEqual(theItem, Boolean.TRUE));

		}
		else if (PRINT_PREVIEW_ACTION_NAME.equals(actionName) || EXPORT_AS_HTML_ACTION_NAME.equals(actionName)
				|| CLOSE_FILE_ACTION_NAME.equals(actionName) || CLOSE_ALL_FILES_ACTION_NAME.equals(actionName)) {
			theItem.setEnabled(false);
			toolbarStates.addPropertyChangeListener(ToolBarStates.IFRAME_NUMBERS_PROPERTY_NAME, new EnableOnPositive(theItem));
		}

		return theItem;
	}

	@Action
	public void newFile() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);

		FileObject fo = null;
		try {
			// Prepare the new file name
			StringBuffer newFile = new StringBuffer();
			newFile.append(editor.getUntitledFileName());
			newFile.append(++_untitledFileNumber);
			newFile.append(".docx");

			// put new file in same directory as last opened local file's.
			// if there isn't last opened local file, put it in the
			// swing file chooser's default directory.
			String lastFile = prefs.get(Constants.LAST_OPENED_LOCAL_FILE, Constants.EMPTY_STRING);
			if (lastFile.length() == 0) {
				fo = VFSUtils.getFileSystemManager().toFileObject(FileSystemView.getFileSystemView().getDefaultDirectory());
			}
			else {
				fo = VFSUtils.getFileSystemManager().resolveFile(lastFile).getParent();
			}
			fo = fo.resolveFile(newFile.toString());

		} catch (FileSystemException exc) {
			exc.printStackTrace();
			ResourceMap rm = editor.getContext().getResourceMap(getClass());
			String title = rm.getString(FileMenu.NEW_FILE_ACTION_NAME + ".Action.text");
			String message = rm.getString(FileMenu.NEW_FILE_ACTION_NAME + ".Action.errorMessage");
			editor.showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		editor.createInternalFrame(fo);
	}

	@Action
	public void exportAsSharedDoc(ActionEvent actionEvent) {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		JEditorPane view = editor.getCurrentEditor();
		if (view instanceof WordMLTextPane) {
			WordMLTextPane wmlTextPane = (WordMLTextPane) view;
			WordMLDocument doc = wmlTextPane.getDocument();
			if (DocUtil.getChunkingStrategy(doc) == null) {
				displaySetupFirstMessage();
				return;
			}

			NewShareDialog d = new NewShareDialog(editor.getWindowFrame());
			d.pack();
			d.setLocationRelativeTo(editor.getWindowFrame());
			d.setVisible(true);
			if (d.getValue() == NewShareDialog.NEXT_BUTTON_TEXT) {
				DocumentElement root = (DocumentElement) doc.getDefaultRootElement();
				DocumentML docML = (DocumentML) root.getElementML();
				WordprocessingMLPackage wmlPackage = docML.getWordprocessingMLPackage();
				XmlUtil.setPlutextCheckinMessageEnabledProperty(wmlPackage, d.isCommentOnEveryChange());

				RETURN_TYPE val = saveAsFile(EXPORT_AS_SHARED_DOC_ACTION_NAME, actionEvent, Constants.DOCX_STRING);

				Cursor origCursor = wmlTextPane.getCursor();
				wmlTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				if (val != RETURN_TYPE.APPROVE) {
					// Cancelled or Error
					XmlUtil.removePlutextProperty(wmlPackage, Constants.PLUTEXT_CHECKIN_MESSAGE_ENABLED_PROPERTY_NAME);

				}
				else if (!DocUtil.isSharedDocument(doc)) {
					// Because user has saved to places other than predefined server.
					XmlUtil.removeSharedDocumentProperties(wmlPackage);
					// TODO: Display a message saying a shared document can only
					// be created in predefined server and ask user to try again.
				}
				else {

					String filepath = (String) doc.getProperty(WordMLDocument.FILE_PATH_PROPERTY);
					try {
						FileObject fo = VFSUtils.getFileSystemManager().resolveFile(filepath);
						doc = wmlTextPane.getWordMLEditorKit().read(fo, new ObjectFactory());

						doc.putProperty(WordMLDocument.FILE_PATH_PROPERTY, filepath);
						doc.addDocumentListener(editor.getToolbarStates());
						doc.setDocumentFilter(new WordMLDocumentFilter());

						wmlTextPane.setDocument(doc);
						wmlTextPane.putClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG, Boolean.TRUE);

						if (DocUtil.isSharedDocument(doc)) {
							wmlTextPane.getWordMLEditorKit().initPlutextClient(wmlTextPane);
						}
					} catch (IOException exc) {
						exc.printStackTrace();
						// TODO:Display an IO error message and ask user to close the document
						// and try to reopen it.
					}

				}

				wmlTextPane.setCursor(origCursor);
			}
			d.dispose();
		}
	}

	private void displaySetupFirstMessage() {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(getClass());
		String title = rm.getString("exportAsSharedDoc.Action.text");
		String message = rm.getString("exportAsSharedDoc.Action.setupFirstMessage");
		editor.showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
	}

	@Action
	public void exportAsNonSharedDoc(ActionEvent actionEvent) {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		JEditorPane view = editor.getCurrentEditor();
		if (view instanceof WordMLTextPane) {
			WordMLTextPane textpane = (WordMLTextPane) view;
			WordMLDocument doc = textpane.getDocument();
			if (DocUtil.isSharedDocument(doc)) {
				textpane.saveCaretText();
				saveAsFile(EXPORT_AS_NON_SHARED_DOC_ACTION_NAME, actionEvent, Constants.DOCX_STRING);
			}
		}
	}

	@Action
	public void openFile(ActionEvent actionEvent) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(WordMLEditor.class);

		String lastFileUri = prefs.get(Constants.LAST_OPENED_FILE, Constants.EMPTY_STRING);
		FileObject dir = null;
		if (lastFileUri.length() > 0) {
			try {
				dir = VFSUtils.getFileSystemManager().resolveFile(lastFileUri).getParent();
			} catch (FileSystemException exc) {
				dir = null;
			}
		}

		VFSJFileChooser chooser = createFileChooser(rm, dir, Constants.DOCX_STRING);

		RETURN_TYPE returnVal = chooser.showOpenDialog((Component) actionEvent.getSource());

		if (returnVal == RETURN_TYPE.APPROVE) {
			FileObject file = getSelectedFile(chooser, Constants.DOCX_STRING);
			if (file != null) {
				lastFileUri = file.getName().getURI();

				prefs.put(Constants.LAST_OPENED_FILE, lastFileUri);
				if (file.getName().getScheme().equals(("file"))) {
					prefs.put(Constants.LAST_OPENED_LOCAL_FILE, lastFileUri);
				}
				PreferenceUtil.flush(prefs);
				log.info("\n\n Opening " + VFSUtils.getFriendlyName(lastFileUri));
				editor.createInternalFrame(file);
			}
		}
	}

	@Action
	public void saveFile(ActionEvent actionEvent) {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		if (editor.getToolbarStates().isDocumentDirty()) {
			JInternalFrame iframe = editor.getCurrentInternalFrame();
			if (iframe.getTitle().startsWith(editor.getUntitledFileName())) {
				saveAsDocx(actionEvent);

			}
			else {
				WordMLTextPane textPane = SwingUtil.getWordMLTextPane(iframe);
				if (textPane != null && textPane.getWordMLEditorKit().getPlutextClient() != null && editor.getCurrentEditor() != textPane) {

					ResourceMap rm = editor.getContext().getResourceMap(getClass());
					String title = rm.getString("saveFile.Action.text");
					String message = rm.getString("saveFile.Action.goto.editorView.infoMessage");
					editor.showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);

					return;
				}

				boolean success = save(iframe, null, SAVE_FILE_ACTION_NAME);
				if (success) {
					editor.getToolbarStates().setDocumentDirty(iframe, false);
					editor.getToolbarStates().setLocalEditsEnabled(iframe, false);
				}
			}
		}
	}

	@Action
	public void exportAsHtml(ActionEvent actionEvent) {
		saveAsFile(EXPORT_AS_HTML_ACTION_NAME, actionEvent, Constants.HTML_STRING);
	}

	@Action
	public void saveAsDocx(ActionEvent actionEvent) {
		saveAsFile(SAVE_AS_DOCX_ACTION_NAME, actionEvent, Constants.DOCX_STRING);
	}

	private RETURN_TYPE saveAsFile(String callerActionName, ActionEvent actionEvent, String fileType) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		ResourceMap rm = editor.getContext().getResourceMap(getClass());

		JInternalFrame iframe = editor.getCurrentInternalFrame();
		String oldFilePath = (String) iframe.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);

		VFSJFileChooser chooser = createFileChooser(rm, callerActionName, iframe, fileType);

		RETURN_TYPE returnVal = chooser.showSaveDialog((Component) actionEvent.getSource());
		if (returnVal == RETURN_TYPE.APPROVE) {
			FileObject selectedFile = getSelectedFile(chooser, fileType);

			boolean error = false;
			boolean newlyCreatedFile = false;

			if (selectedFile == null) {

				// Should never happen, whether the file exists or not

			}
			else {

				// Check selectedFile's existence and ask user confirmation when needed.
				try {
					boolean selectedFileExists = selectedFile.exists();
					if (!selectedFileExists) {
						FileObject parent = selectedFile.getParent();
						String uri = UriParser.decode(parent.getName().getURI());

						if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1
								&& parent.getName().getScheme().startsWith("file") && !parent.isWriteable()
								&& (uri.indexOf("/Documents") > -1 || uri.indexOf("/My Documents") > -1)) {
							// TODO: Check whether we still need this workaround.
							// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4939819
							// Re: File.canWrite() returns false for the "My Documents" directory (win)
							String localpath = org.docx4j.utils.VFSUtils.getLocalFilePath(parent);
							File f = new File(localpath);
							f.setWritable(true, true);
						}

						selectedFile.createFile();
						newlyCreatedFile = true;

					}
					else if (!selectedFile.getName().getURI().equalsIgnoreCase(oldFilePath)) {
						String title = rm.getString(callerActionName + ".Action.text");
						String message = VFSUtils.getFriendlyName(selectedFile.getName().getURI()) + "\n"
								+ rm.getString(callerActionName + ".Action.confirmMessage");
						int answer = editor.showConfirmDialog(title, message, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (answer != JOptionPane.YES_OPTION) {
							selectedFile = null;
						}
					} // if (!selectedFileExists)

				} catch (FileSystemException exc) {
					exc.printStackTrace();// ignore
					log.error("Couldn't create new file or assure file existence. File = " + selectedFile.getName().getURI());
					selectedFile = null;
					error = true;
				}
			}

			// Check whether there has been an error, cancellation by user
			// or may proceed to saving file.
			if (selectedFile != null) {
				// Proceed to saving file
				String selectedPath = selectedFile.getName().getURI();
				if (log.isDebugEnabled()) {
					log.debug("saveAsFile(): selectedFile = " + VFSUtils.getFriendlyName(selectedPath));
				}

				prefs.put(Constants.LAST_OPENED_FILE, selectedPath);
				if (selectedFile.getName().getScheme().equals("file")) {
					prefs.put(Constants.LAST_OPENED_LOCAL_FILE, selectedPath);
				}
				PreferenceUtil.flush(prefs);

				boolean success = false;
				if (EXPORT_AS_NON_SHARED_DOC_ACTION_NAME.equals(callerActionName)) {
					log.info("saveAsFile(): Exporting as non shared document to " + VFSUtils.getFriendlyName(selectedPath));
					success = export(iframe, selectedPath, callerActionName);
					if (success) {
						prefs.put(Constants.LAST_OPENED_FILE, selectedPath);
						if (selectedPath.startsWith("file:")) {
							prefs.put(Constants.LAST_OPENED_LOCAL_FILE, selectedPath);
						}
						PreferenceUtil.flush(prefs);
						log.info("saveAsFile(): Opening " + VFSUtils.getFriendlyName(selectedPath));
						editor.createInternalFrame(selectedFile);
					}
				}
				else {
					success = save(iframe, selectedPath, callerActionName);
					if (success) {
						if (Constants.DOCX_STRING.equals(fileType) || Constants.FLAT_OPC_STRING.equals(fileType)) {
							// If saving as .docx then update the document dirty flag
							// of toolbar states as well as internal frame title.
							editor.getToolbarStates().setDocumentDirty(iframe, false);
							editor.getToolbarStates().setLocalEditsEnabled(iframe, false);
							FileObject file = null;
							try {
								file = VFSUtils.getFileSystemManager().resolveFile(oldFilePath);
								editor.updateInternalFrame(file, selectedFile);
							} catch (FileSystemException exc) {
								;// ignore
							}
						}
						else {
							// Because document dirty flag is not cleared
							// and internal frame title is not changed,
							// we present a success message.
							String title = rm.getString(callerActionName + ".Action.text");
							String message = VFSUtils.getFriendlyName(selectedPath) + "\n"
									+ rm.getString(callerActionName + ".Action.successMessage");
							editor.showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}

				if (!success && newlyCreatedFile) {
					try {
						selectedFile.delete();
					} catch (FileSystemException exc) {
						log.error("saveAsFile(): Saving failure and cannot remove the newly created file = " + selectedPath);
						exc.printStackTrace();
					}
				}
			}
			else if (error) {
				log.error("saveAsFile(): selectedFile = NULL");
				String title = rm.getString(callerActionName + ".Action.text");
				String message = rm.getString(callerActionName + ".Action.errorMessage");
				editor.showMessageDialog(title, message, JOptionPane.ERROR_MESSAGE);
			}

		} // if (returnVal == JFileChooser.APPROVE_OPTION)

		return returnVal;
	}// saveAsFile()

	/**
	 * If user types in a filename JFileChooser does not append the file extension displayed by its FileFilter to it. This method appends
	 * desiredFileType parameter as file extension if JFileChooser's selected file does not have it.
	 * 
	 * @param chooser
	 *            JFileChooser instance
	 * @param desiredFileType
	 *            File extension
	 * @return a File whose extension is desiredFileType.
	 */
	public FileObject getSelectedFile(VFSJFileChooser chooser, String desiredFileType) {
		FileObject theFile = chooser.getSelectedFile();

		StringBuffer uri = new StringBuffer(theFile.getName().getURI());
		if (desiredFileType.equalsIgnoreCase(theFile.getName().getExtension())) {
			// user may type in the file extension in the JFileChooser dialog.
			// Therefore worth checking file extension case insensitively
			// int dot = uri.lastIndexOf(Constants.DOT);
			// uri = new StringBuffer(uri.substring(0, dot));

			// Do nothing

		}
		else if (Constants.DOCX_STRING.equals(desiredFileType) // also allow .xml
				&& Constants.FLAT_OPC_STRING.equalsIgnoreCase(theFile.getName().getExtension())) {

			// Do nothing

		}
		else {
			uri.append(Constants.DOT);
			uri.append(desiredFileType);
		}
		try {
			theFile = VFSUtils.getFileSystemManager().resolveFile(uri.toString());
		} catch (FileSystemException exc) {
			exc.printStackTrace();
			theFile = null;
		}

		return theFile;
	}

	@Action
	public void saveAllFiles() {
		WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
		for (JInternalFrame iframe : wmlEditor.getAllInternalFrames()) {
			if (wmlEditor.getToolbarStates().isDocumentDirty(iframe) && save(iframe, null, SAVE_ALL_FILES_ACTION_NAME)) {
				wmlEditor.getToolbarStates().setDocumentDirty(iframe, false);
				wmlEditor.getToolbarStates().setLocalEditsEnabled(iframe, false);
			}
		}
	}

	@Action
	public void printPreview() {
		WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
		JEditorPane editor = wmlEditor.getCurrentEditor();

		WordMLEditorKit kit = (WordMLEditorKit) editor.getEditorKit();
		kit.saveCaretText();

		Document doc = editor.getDocument();
		String filePath = (String) doc.getProperty(WordMLDocument.FILE_PATH_PROPERTY);
		DocumentElement elem = (DocumentElement) doc.getDefaultRootElement();
		DocumentML rootML = (DocumentML) elem.getElementML();

		// Do not include the last paragraph when saving or printing.
		// we'll put it back when the job is done.
		elem = (DocumentElement) elem.getElement(elem.getElementCount() - 1);
		ElementML paraML = elem.getElementML();
		ElementML bodyML = paraML.getParent();
		paraML.delete();

		try {
			WordprocessingMLPackage wordMLPackage = rootML.getWordprocessingMLPackage();

			// XmlPackage worker = new XmlPackage(wordMLPackage);
			// org.docx4j.xmlPackage.Package result = worker.get();
			// boolean suppressDeclaration = true;
			// boolean prettyprint = true;
			// System.out.println(
			// org.docx4j.XmlUtils.
			// marshaltoString(result, suppressDeclaration, prettyprint,
			// org.docx4j.jaxb.Context.jcXmlPackage) );

			// Create temporary .pdf file.
			// Remember that filePath is in Commons-VFS format which
			// uses '/' as separator char.
			String tmpName = filePath.substring(filePath.lastIndexOf("/"), filePath.lastIndexOf(Constants.DOT));
			File tmpFile = File.createTempFile(tmpName + ".tmp", ".pdf");
			// Delete the temporary file when program exits.
			tmpFile.deleteOnExit();

			OutputStream os = new java.io.FileOutputStream(tmpFile);

			// Could write to a ByteBuffer and avoid the temp file if:
			// 1. com.sun.pdfview.PDFViewer had an appropriate open method
			// 2. We knew how big to make the buffer
			// java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(15000);
			// //15kb
			// OutputStream os = newOutputStream(buf);

			// Deprecated => http://stackoverflow.com/questions/20840051/docx4j-convert-to-pdf-deprecated
			/* PdfConversion c = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
			// can change from viaHTML to viaIText or viaXSLFO
			PdfSettings settings = new PdfSettings();
			c.output(os, settings); */
			Docx4J.toPDF(wordMLPackage, os);

			os.close();

			PDFViewer pv = new PDFViewer(true);
			// pv.openFile(buf, "some name"); // requires modified
			// com.sun.pdfview.PDFViewer
			pv.openFile(tmpFile);

		} catch (Exception exc) {
			exc.printStackTrace();
			ResourceMap rm = wmlEditor.getContext().getResourceMap(getClass());
			String title = rm.getString(PRINT_PREVIEW_ACTION_NAME + ".Action.text");
			String message = rm.getString(PRINT_PREVIEW_ACTION_NAME + ".Action.errorMessage") + "\n" + VFSUtils.getFriendlyName(filePath);
			wmlEditor.showMessageDialog(title, message, JOptionPane.ERROR_MESSAGE);

		} finally {
			// Remember to put 'paraML' as last paragraph
			bodyML.addChild(paraML);
		}

	} // printPreview()

	@Action
	public void closeFile() {
		WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
		wmlEditor.closeInternalFrame(wmlEditor.getCurrentInternalFrame());
	}

	@Action
	public void closeAllFiles() {
		WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
		wmlEditor.closeAllInternalFrames();
	}

	@Action
	public void exit() {
		WordMLEditor editor = WordMLEditor.getInstance(WordMLEditor.class);
		editor.exit();
	}

	private VFSJFileChooser createFileChooser(ResourceMap resourceMap, String callerActionName, JInternalFrame iframe,
			String filteredFileExtension) {

		String filePath = null;
		if (EXPORT_AS_SHARED_DOC_ACTION_NAME.equals(callerActionName)) {
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			filePath = prefs.get(Constants.LAST_OPENED_FILE, Constants.EMPTY_STRING);
		}
		else {
			filePath = (String) iframe.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);
		}

		FileObject file = null;
		FileObject dir = null;
		try {
			file = VFSUtils.getFileSystemManager().resolveFile(filePath);
			dir = file.getParent();
		} catch (FileSystemException exc) {
			;// ignore
		}

		return createFileChooser(resourceMap, dir, filteredFileExtension);
	}

	private VFSJFileChooser createFileChooser(ResourceMap resourceMap, FileObject showedDir, String filteredFileExtension) {

		VFSJFileChooser chooser = new VFSJFileChooser();
		chooser.setAccessory(new DefaultAccessoriesPanel(chooser));
		chooser.setFileHidingEnabled(false);
		chooser.setMultiSelectionEnabled(false);

		chooser.setFileSelectionMode(SELECTION_MODE.FILES_ONLY);

		String desc = null;
		if (Constants.HTML_STRING.equals(filteredFileExtension)) {
			desc = resourceMap.getString(Constants.VFSJFILECHOOSER_HTML_FILTER_DESC);
			if (desc == null || desc.length() == 0) {
				desc = "Html Files (.html)";
			}
		}
		else {
			desc = resourceMap.getString(Constants.VFSJFILECHOOSER_DOCX_FILTER_DESC);
			if (desc == null || desc.length() == 0) {
				desc = "Docx Files (.docx, .xml)";
			}
		}

		if (showedDir == null) {
			String s = resourceMap.getString(Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI);
			if (s != null && s.length() > 0) {
				try {
					showedDir = VFSUtils.getFileSystemManager().resolveFile(s);
				} catch (FileSystemException exc) {
					StringBuilder sb = new StringBuilder();
					sb.append("Bad ");
					sb.append(Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI);
					sb.append(" property.");
					throw new RuntimeException(sb.toString());
				}
			}
			else {
				showedDir = null;
			}
		}

		FileNameExtensionFilter filter;
		if (Constants.DOCX_STRING.equals(filteredFileExtension)) {
			// Add .xml as well
			filter = new FileNameExtensionFilter(desc, filteredFileExtension, Constants.FLAT_OPC_STRING);
		}
		else {
			filter = new FileNameExtensionFilter(desc, filteredFileExtension);
		}
		chooser.addChoosableFileFilter(filter);
		chooser.setCurrentDirectory(showedDir);

		return chooser;
	}

	public void createInFileSystem(FileObject file) throws FileSystemException {
		FileObject parent = file.getParent();
		String uri = UriParser.decode(parent.getName().getURI());

		if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1 && parent.getName().getScheme().startsWith("file")
				&& !parent.isWriteable() && (uri.indexOf("/Documents") > -1 || uri.indexOf("/My Documents") > -1)) {
			// TODO: Check whether we still need this workaround.
			// See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4939819
			// Re: File.canWrite() returns false for the "My Documents" directory (win)
			String localpath = org.docx4j.utils.VFSUtils.getLocalFilePath(parent);
			File f = new File(localpath);
			f.setWritable(true, true);
		}

		file.createFile();
	}

	/**
	 * Saves editor documents to a file.
	 * 
	 * Internal frame may have two editors for presenting two different views to user namely editor view and source view. WordMLTextPane is
	 * used for editor view and JEditorPane for source view. The contents of these two editors are synchronized when user switches from one
	 * view to the other. Therefore, there will be ONLY ONE editor that is dirty and has to be saved by this method.
	 * 
	 * @param iframe
	 * @param saveAsFilePath
	 * @param callerActionName
	 * @return
	 */
	public boolean save(JInternalFrame iframe, String saveAsFilePath, String callerActionName) {
		boolean success = true;

		if (saveAsFilePath == null) {
			saveAsFilePath = (String) iframe.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);
		}

		if (log.isDebugEnabled()) {
			log.debug("save(): filePath=" + VFSUtils.getFriendlyName(saveAsFilePath));
		}

		WordMLTextPane editorView = SwingUtil.getWordMLTextPane(iframe);
		JEditorPane sourceView = SwingUtil.getSourceEditor(iframe);

		if (sourceView != null && !((Boolean) sourceView.getClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG)).booleanValue()) {
			// signifies that Source View is not synchronised with Editor View yet.
			// Therefore, it is dirty and has to be saved.
			if (editorView != null && editorView.getWordMLEditorKit().getPlutextClient() != null) {
				// Document has to be saved from editor view
				// by committing local edits
				success = false;
			}
			else {
				EditorKit kit = sourceView.getEditorKit();
				WordMLDocument doc = (WordMLDocument) sourceView.getDocument();
				WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage) doc.getProperty(WordMLDocument.WML_PACKAGE_PROPERTY);

				DocUtil.write(kit, doc, wmlPackage, doc.getElementMLFactory().getObjectFactory());
				success = save(wmlPackage, saveAsFilePath, callerActionName);

				if (success) {
					if (saveAsFilePath.endsWith(Constants.DOCX_STRING) || saveAsFilePath.endsWith(Constants.FLAT_OPC_STRING)) {
						doc.putProperty(WordMLDocument.FILE_PATH_PROPERTY, saveAsFilePath);
						iframe.putClientProperty(WordMLDocument.FILE_PATH_PROPERTY, saveAsFilePath);
					}
				}
			}
			return success;
		}
		sourceView = null;

		if (editorView == null) {
			;// pass

		}
		else if (editorView.getWordMLEditorKit().getPlutextClient() != null) {
			if (saveAsFilePath.equals(editorView.getDocument().getProperty(WordMLDocument.FILE_PATH_PROPERTY))) {
				success = commitLocalChanges(editorView, callerActionName);

			}
			else {
				// TODO: Enable saving Plutext document as a new file.
				WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
				ResourceMap rm = wmlEditor.getContext().getResourceMap(getClass());
				String title = rm.getString(callerActionName + ".Action.text");
				StringBuilder message = new StringBuilder();
				message.append("File ");
				message.append(saveAsFilePath);
				message.append(Constants.NEWLINE);
				message.append(rm.getString(callerActionName + ".Action.wrong.fileName.infoMessage"));
				wmlEditor.showMessageDialog(title, message.toString(), JOptionPane.INFORMATION_MESSAGE);

				success = false;
			}
		}
		else {
			WordMLEditorKit kit = (WordMLEditorKit) editorView.getEditorKit();
			kit.saveCaretText();

			Document doc = editorView.getDocument();
			DocumentElement elem = (DocumentElement) doc.getDefaultRootElement();
			DocumentML rootML = (DocumentML) elem.getElementML();

			// Do not include the last paragraph when saving.
			// After saving we put it back.
			elem = (DocumentElement) elem.getElement(elem.getElementCount() - 1);
			ElementML paraML = elem.getElementML();
			ElementML bodyML = paraML.getParent();
			paraML.delete();

			success = save(rootML.getWordprocessingMLPackage(), saveAsFilePath, callerActionName);

			// Remember to put 'paraML' as last paragraph
			bodyML.addChild(paraML);

			if (success) {
				if (saveAsFilePath.endsWith(Constants.DOCX_STRING)) {
					doc.putProperty(WordMLDocument.FILE_PATH_PROPERTY, saveAsFilePath);
					iframe.putClientProperty(WordMLDocument.FILE_PATH_PROPERTY, saveAsFilePath);
				}
			}
		}

		return success;
	}

	private boolean commitLocalChanges(WordMLTextPane editor, String callerActionName) {
		return PlutextMenu.getInstance().commitLocalEdits(editor, callerActionName);
	}

	protected boolean save(WordprocessingMLPackage wmlPackage, String saveAsFilePath, String callerActionName) {
		boolean success = true;
		try {
			if (saveAsFilePath.endsWith(Constants.DOCX_STRING)) {
				SaveToVFSZipFile saver = new SaveToVFSZipFile(wmlPackage);
				saver.save(saveAsFilePath);
			}
			else if (saveAsFilePath.endsWith(Constants.FLAT_OPC_STRING)) {

				FlatOpcXmlCreator xmlPackageCreator = new FlatOpcXmlCreator(wmlPackage);
				org.docx4j.xmlPackage.Package flatOPC = xmlPackageCreator.get();

				FileObject fo = VFSUtils.getFileSystemManager().resolveFile(saveAsFilePath);
				OutputStream fos = fo.getContent().getOutputStream();

				Marshaller m = Context.jcXmlPackage.createMarshaller();
				try {
					NamespacePrefixMapperUtils.setProperty(m, NamespacePrefixMapperUtils.getPrefixMapper());

					m.setProperty("jaxb.formatted.output", true);
				} catch (javax.xml.bind.PropertyException cnfe) {
					log.error(cnfe.getMessage(), cnfe);
				}
				m.marshal(flatOPC, fos);
				try {
					// just in case
					fos.close();
				} catch (IOException exc) {
					;// ignore
				}

			}
			else if (saveAsFilePath.endsWith(Constants.HTML_STRING)) {
				FileObject fo = VFSUtils.getFileSystemManager().resolveFile(saveAsFilePath);
				OutputStream fos = fo.getContent().getOutputStream();
				javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(fos);
				// wmlPackage.html(result);
				AbstractHtmlExporter exporter = new HtmlExporterNG2();

				// .. the HtmlSettings object
				HtmlSettings htmlSettings = new HtmlSettings();
				htmlSettings.setImageDirPath(saveAsFilePath + "_files");
				htmlSettings.setImageTargetUri(saveAsFilePath.substring(saveAsFilePath.lastIndexOf("/") + 1) + "_files");

				exporter.html(wmlPackage, result, htmlSettings);

				try {
					// just in case
					fos.close();
				} catch (IOException exc) {
					;// ignore
				}
			}
			else {
				throw new Docx4JException("Invalid filepath = " + VFSUtils.getFriendlyName(saveAsFilePath));
			}
		} catch (Exception exc) {
			exc.printStackTrace();

			success = false;
			WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);
			ResourceMap rm = wmlEditor.getContext().getResourceMap(getClass());

			String title = rm.getString(callerActionName + ".Action.text");
			String message = rm.getString(callerActionName + ".Action.errorMessage") + "\n" + VFSUtils.getFriendlyName(saveAsFilePath);
			wmlEditor.showMessageDialog(title, message, JOptionPane.ERROR_MESSAGE);
		}

		return success;
	}

	public boolean export(JInternalFrame iframe, String saveAsFilePath, String callerActionName) {
		log.debug("export(): filePath=" + VFSUtils.getFriendlyName(saveAsFilePath));

		WordprocessingMLPackage srcPackage = null;

		WordMLTextPane editorView = SwingUtil.getWordMLTextPane(iframe);
		JEditorPane sourceView = SwingUtil.getSourceEditor(iframe);

		if (sourceView != null && !((Boolean) sourceView.getClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG)).booleanValue()) {
			// signifies that Source View is not synchronised with Editor View yet.
			// Therefore, export from Source View.
			EditorKit kit = sourceView.getEditorKit();
			WordMLDocument doc = (WordMLDocument) sourceView.getDocument();
			srcPackage = DocUtil.write(kit, doc, null, doc.getElementMLFactory().getObjectFactory());
			editorView = null;
		}
		sourceView = null;

		if (editorView == null) {
			;// pass
		}
		else {
			WordMLDocument doc = editorView.getDocument();
			DocumentElement root = (DocumentElement) doc.getDefaultRootElement();
			DocumentML docML = (DocumentML) root.getElementML();
			srcPackage = docML.getWordprocessingMLPackage();
		}

		boolean success = save(XmlUtil.export(srcPackage, editorView.getDocument().getElementMLFactory().getObjectFactory()),
				saveAsFilePath, callerActionName);

		log.debug("export(): success=" + success + " filePath=" + VFSUtils.getFriendlyName(saveAsFilePath));

		return success;
	}

}// FileMenu class
