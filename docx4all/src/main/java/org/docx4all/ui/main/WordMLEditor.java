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

package org.docx4all.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.sf.vfsjfilechooser.utils.VFSURIParser;
import net.sf.vfsjfilechooser.utils.VFSUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.webdav.WebdavFileObject;
import org.bounce.text.xml.XMLDocument;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.docx4all.datatransfer.TransferHandler;
import org.docx4all.datatransfer.WordMLTransferable;
import org.docx4all.script.FxScriptUIHelper;
import org.docx4all.swing.WordMLTextPane;
import org.docx4all.swing.text.DocumentElement;
import org.docx4all.swing.text.FontManager;
import org.docx4all.swing.text.WordMLDocument;
import org.docx4all.swing.text.WordMLDocumentFilter;
import org.docx4all.swing.text.WordMLEditorKit;
import org.docx4all.ui.menu.ContentControlMenu;
import org.docx4all.ui.menu.EditMenu;
import org.docx4all.ui.menu.FileMenu;
import org.docx4all.ui.menu.FormatMenu;
import org.docx4all.ui.menu.HelpMenu;
import org.docx4all.ui.menu.HyperlinkMenu;
import org.docx4all.ui.menu.PlutextMenu;
import org.docx4all.ui.menu.ReviewMenu;
import org.docx4all.ui.menu.ViewMenu;
import org.docx4all.ui.menu.WindowMenu;
import org.docx4all.util.DocUtil;
import org.docx4all.util.SwingUtil;
import org.docx4all.xml.BodyML;
import org.docx4all.xml.DocumentML;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.IObjectFactory;
import org.docx4all.xml.ObjectFactory;
import org.docx4all.xml.SdtBlockML;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.plutext.client.Mediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jojada Tirtowidjojo - 13/11/2007
 */
public class WordMLEditor extends SingleFrameApplication {
	private static Logger log = LoggerFactory.getLogger(WordMLEditor.class);

	private ViewManager _viewManager;
	private JDesktopPane _desktop;
	private Map<String, JInternalFrame> _iframeMap;
	private InternalFrameListener _internalFrameListener;
	private MouseMotionListener _titleBarMouseListener;
	private ToolBarStates _toolbarStates;
	private JApplet _applet;

	public static String TRANSFORMER_FACTORY_DEFAULT;

	public static void main(String[] args) {

		// // Whenever we flush Preferences on Linux, we need *not* to use
		// // our Xalan jar (which we need
		// javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory.newInstance();
		// TRANSFORMER_FACTORY_DEFAULT = tfactory.getClass().getName();
		// log.debug("Set TRANSFORMER_FACTORY_DEFAULT to " + TRANSFORMER_FACTORY_DEFAULT);

		// If we launch via JNLP with Java 6 JAXB (as opposed to the JAXB RI),
		// we trip up with access denied on RuntimePermission accessDeclaredMembers
		// (is this because NamespacePrefixMapperSunInternal extends com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper?).
		// This is despite the JNLP file seeking all-permissions.
		// Workaround:
		if (System.getSecurityManager() == null) {
			System.out.println("Initial SecurityManager: null");
		} else {
			System.out.println("Initial SecurityManager: " + System.getSecurityManager().getClass().getName());
			System.setSecurityManager(null);
		}

		launch(WordMLEditor.class, args);
	}

	@Override
	protected void startup() {
		log.info("preStartup()...");
		preStartup(null);

		log.info("setting up createMenuBar");
		getMainFrame().setJMenuBar(createMenuBar());

		log.info("setting up createMainPanel");
		show(createMainPanel());
		log.info("startup() complete.");

	}

	void preStartup(JApplet applet) {
		_applet = applet;

		_viewManager = new ViewManager();

		_iframeMap = new HashMap<String, JInternalFrame>();

		log.info("setting up InternalFrameListener");
		_internalFrameListener = new InternalFrameListener();

		log.info("setting up TitleBarMouseListener");
		_titleBarMouseListener = new TitleBarMouseListener();

		log.info("setting up ToolBarStates");
		_toolbarStates = new ToolBarStates();
		Clipboard clipboard = getContext().getClipboard();
		clipboard.addFlavorListener(_toolbarStates);
		// As a FlavorListener, _toolbarStates will ONLY be notified
		// when there is a DataFlavor change in Clipboard.
		// Therefore, make sure that toolbarStates' _isPasteEnable property
		// is initialised correctly.
		boolean available = clipboard.isDataFlavorAvailable(WordMLTransferable.STRING_FLAVOR)
				|| clipboard.isDataFlavorAvailable(WordMLTransferable.WORDML_FRAGMENT_FLAVOR);
		_toolbarStates.setPasteEnabled(available);

		log.info("setting up VFSJFileChooser");
		initVFSJFileChooser();

		log.info("setting up WmlExitListener");
		addExitListener(new WmlExitListener());
	}

	/**
	 * Initialises VFSJFileChooser default bookmark entry which has to be a webdav folder pointed by
	 * Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI property.
	 */
	private void initVFSJFileChooser() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		String uri = rm.getString(Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI);
		System.out.println(Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI + ":" + uri);
		if (uri != null && uri.length() > 0) {
			uri = uri.trim();
			try {
				FileObject fo = VFSUtils.getFileSystemManager().resolveFile(uri);

				if (fo instanceof WebdavFileObject) {
					String name = rm.getString(Constants.VFSJFILECHOOSER_DEFAULT_WEBDAV_FOLDER_BOOKMARK_NAME);
					if (name == null || name.length() == 0) {
						name = "Default Webdav Folder";
					} else {
						name = name.trim();
					}

					VFSURIParser vup = new VFSURIParser(uri, false);
					org.docx4all.vfs.VFSUtil.addBookmarkEntry(name, vup);
				}

			} catch (FileSystemException exc) {
				exc.printStackTrace();
				StringBuilder sb = new StringBuilder();
				sb.append("Bad ");
				sb.append(Constants.VFSJFILECHOOSER_DEFAULT_FOLDER_URI);
				sb.append(" property.");
				throw new RuntimeException(sb.toString());
			}
		}
	} // initVFSJFileChooser()

	public void closeAllInternalFrames() {

		List<JInternalFrame> list = getAllInternalFrames();

		// Start from current editor's frame
		JInternalFrame currentFrame = getCurrentInternalFrame();
		list.remove(currentFrame);
		list.add(0, currentFrame);

		for (final JInternalFrame iframe : list) {
			final Runnable disposeRunnable = new Runnable() {
				@Override
				public void run() {
					iframe.dispose();
				}
			};

			if (getToolbarStates().isDocumentDirty(iframe)) {
				try {
					iframe.setSelected(true);
					iframe.setIcon(false);
				} catch (PropertyVetoException exc) {
					;// ignore
				}

				int answer = showConfirmClosingInternalFrame(iframe, "internalframe.close");
				if (answer == JOptionPane.CANCEL_OPTION) {
					break;
				}
			}

			SwingUtilities.invokeLater(disposeRunnable);
		}
	}

	public void closeInternalFrame(JInternalFrame iframe) {
		boolean canClose = true;

		if (getToolbarStates().isDocumentDirty(iframe)) {
			try {
				iframe.setSelected(true);
				iframe.setIcon(false);
			} catch (PropertyVetoException exc) {
				;// ignore
			}

			int answer = showConfirmClosingInternalFrame(iframe, "internalframe.close");
			canClose = (answer != JOptionPane.CANCEL_OPTION);
		}

		if (canClose) {
			WordMLTextPane editor = SwingUtil.getWordMLTextPane(iframe);
			if (editor != null) {
				editor.removeCaretListener(getToolbarStates());
				editor.removeFocusListener(getToolbarStates());
				editor.setTransferHandler(null);

				editor.getDocument().removeDocumentListener(getToolbarStates());

				WordMLEditorKit editorKit = (WordMLEditorKit) editor.getEditorKit();
				editorKit.removeInputAttributeListener(getToolbarStates());
				editor.getWordMLEditorKit().deinstall(editor);
			}
			iframe.dispose();
		}
	}

	public void createInternalFrame(FileObject f) {
		if (f == null) {
			return;
		}

		IObjectFactory factory = new ObjectFactory();

		log.info(VFSUtils.getFriendlyName(f.getName().getURI()));

		JInternalFrame iframe = _iframeMap.get(f.getName().getURI());
		if (iframe != null) {
			iframe.setVisible(true);

		} else {
			iframe = new JInternalFrame(f.getName().getBaseName(), true, true, true, true);
			iframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			iframe.addInternalFrameListener(_internalFrameListener);
			iframe.addInternalFrameListener(_toolbarStates);
			iframe.addPropertyChangeListener(WindowMenu.getInstance());

			if (iframe.getUI() instanceof BasicInternalFrameUI) {
				BasicInternalFrameUI ui = (BasicInternalFrameUI) iframe.getUI();
				javax.swing.JComponent northPane = ui.getNorthPane();
				if (northPane == null) {
					// Happens on Mac OSX: Google for "osx java getNorthPane"
					// Fix is from it.businesslogic.ireport.gui.JMDIFrame
					javax.swing.plaf.basic.BasicInternalFrameUI aUI = new javax.swing.plaf.basic.BasicInternalFrameUI(iframe);
					iframe.setUI(aUI);

					// Try again
					ui = (BasicInternalFrameUI) iframe.getUI();
					northPane = ui.getNorthPane();
				}
				northPane.addMouseMotionListener(_titleBarMouseListener);
			}

			JEditorPane editorView = createEditorView(f, factory);
			JPanel panel = FxScriptUIHelper.getInstance().createEditorPanel(editorView);

			iframe.getContentPane().add(panel);
			iframe.pack();
			_desktop.add(iframe);

			editorView.requestFocusInWindow();
			editorView.select(0, 0);

			String filePath = f.getName().getURI();
			iframe.putClientProperty(WordMLDocument.FILE_PATH_PROPERTY, filePath);
			_iframeMap.put(filePath, iframe);

			iframe.show();
		}

		try {
			iframe.setSelected(true);
			iframe.setIcon(false);
			iframe.setMaximum(true);
		} catch (PropertyVetoException exc) {
			// do nothing
		}
	}

	public void tileLayout(String filePath1, String filePath2) {
		if (_iframeMap.containsKey(filePath1) && _iframeMap.containsKey(filePath2)) {
			JInternalFrame[] frames = { _iframeMap.get(filePath1), _iframeMap.get(filePath2) };
			WindowMenu.tileLayout(frames);
		}
	}

	public void updateInternalFrame(FileObject oldFile, FileObject newFile) {
		if (oldFile.equals(newFile)) {
			return;
		}

		String fileUri = oldFile.getName().getURI();
		JInternalFrame iframe = _iframeMap.remove(fileUri);
		if (iframe != null) {
			fileUri = newFile.getName().getURI();
			iframe.putClientProperty(WordMLDocument.FILE_PATH_PROPERTY, fileUri);
			iframe.setTitle(newFile.getName().getBaseName());
		}
	}

	public String getPlutextWebdavUrlKeyword() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		return rm.getString(Constants.PLUTEXT_WEBDAV_URL_KEYWORD);
	}

	public JDesktopPane getDesktopPane() {
		return _desktop;
	}

	public ToolBarStates getToolbarStates() {
		return _toolbarStates;
	}

	public JInternalFrame getCurrentInternalFrame() {
		return _toolbarStates.getCurrentInternalFrame();
	}

	public JEditorPane getCurrentEditor() {
		return _toolbarStates.getCurrentEditor();
	}

	public JEditorPane getView(String viewTabTitle) {
		if (getCurrentInternalFrame() != null) {
			return getCurrentViewManager().getView(viewTabTitle);
		}
		return null;
	}

	public List<JInternalFrame> getAllInternalFrames() {
		return new ArrayList<JInternalFrame>(_iframeMap.values());
	}

	public String getEditorViewTabTitle() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		return rm.getString(Constants.EDITOR_VIEW_TAB_TITLE);
	}

	public String getSourceViewTabTitle() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		return rm.getString(Constants.SOURCE_VIEW_TAB_TITLE);
	}

	public String getContentControlHistoryViewTabTitle() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		return rm.getString(Constants.CONTENT_CONTROL_HISTORY_VIEW_TAB_TITLE);
	}

	public String getRecentChangesViewTabTitle() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		return rm.getString(Constants.RECENT_CHANGES_VIEW_TAB_TITLE);
	}

	public String getUntitledFileName() {
		ResourceMap rm = getContext().getResourceMap(WordMLEditor.class);
		String filename = rm.getString(Constants.UNTITLED_FILE_NAME);
		if (filename == null || filename.length() == 0) {
			filename = "Untitled";
		}
		return filename;
	}

	public Frame getWindowFrame() {
		if (_applet == null) {
			return getMainFrame();
		}

		Component c = _applet;
		while (c != null && !(c instanceof Frame)) {
			c = c.getParent();
		}

		return (Frame) c;
	}

	public JMenuBar getJMenuBar() {
		if (_applet == null) {
			return getMainFrame().getJMenuBar();
		}
		return _applet.getJMenuBar();
	}

	public int showConfirmDialog(String title, String message, int optionType, int messageType) {
		return JOptionPane.showConfirmDialog(getWindowFrame(), message, title, optionType, messageType);
	}

	public int showConfirmDialog(String title, String message, int optionType, int messageType, Object[] options, Object initialValue) {

		return JOptionPane.showOptionDialog(getWindowFrame(), message, title, optionType, messageType, null, options, initialValue);
	}

	public void showMessageDialog(String title, String message, int optionType) {
		JOptionPane.showMessageDialog(getWindowFrame(), message, title, optionType);
	}

	public void showViewInTab(final String tabTitle) {
		if (getCurrentInternalFrame() != null) {
			Cursor origCursor = getMainFrame().getCursor();
			getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			getCurrentViewManager().showViewTab(tabTitle);

			getMainFrame().setCursor(origCursor);
		}
	}

	public void closeViewTab(String tabTitle) {
		if (getCurrentInternalFrame() != null) {
			getCurrentViewManager().closeViewTab(tabTitle);
		}
	}

	private JEditorPane createSourceView(WordMLTextPane editorView) {
		// Create the Source View
		JEditorPane sourceView = new JEditorPane();

		MutableAttributeSet attrs = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attrs, FontManager.getInstance().getSourceViewFontFamilyName());
		StyleConstants.setFontSize(attrs, FontManager.getInstance().getSourceViewFontSize());

		// TODO - only do this if the font is available.
		Font font = new Font("Arial Unicode MS", Font.PLAIN, 12);

		System.out.println(font.getFamily());
		System.out.println(font.getFontName());
		System.out.println(font.getPSName());

		sourceView.setFont(font);
		// sourceView.setFont(FontManager.getInstance().getFontInAction(attrs));

		sourceView.setContentType("text/xml; charset=UTF-16");

		// Instantiate a XMLEditorKit with wrapping enabled.
		XMLEditorKit kit = new XMLEditorKit(true);
		// Set the wrapping style.
		kit.setWrapStyleWord(true);

		sourceView.setEditorKit(kit);

		WordMLDocument editorViewDoc = editorView.getDocument();

		try {
			editorViewDoc.readLock();

			editorView.getWordMLEditorKit().saveCaretText();

			DocumentElement elem = (DocumentElement) editorViewDoc.getDefaultRootElement();
			WordprocessingMLPackage wmlPackage = ((DocumentML) elem.getElementML()).getWordprocessingMLPackage();
			String filePath = (String) editorView.getDocument().getProperty(WordMLDocument.FILE_PATH_PROPERTY);

			// Do not include the last paragraph which is an extra paragraph.
			elem = (DocumentElement) elem.getElement(elem.getElementCount() - 1);
			ElementML paraML = elem.getElementML();
			ElementML bodyML = paraML.getParent();
			paraML.delete();

			Document doc = DocUtil.read(sourceView, wmlPackage);
			doc.putProperty(WordMLDocument.FILE_PATH_PROPERTY, filePath);
			doc.putProperty(WordMLDocument.WML_PACKAGE_PROPERTY, wmlPackage);
			doc.addDocumentListener(getToolbarStates());

			// Below are the properties used by bounce.jar library
			// See http://www.edankert.com/bounce/xmleditorkit.html
			doc.putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
			doc.putProperty(XMLDocument.AUTO_INDENTATION_ATTRIBUTE, Boolean.TRUE);
			doc.putProperty(XMLDocument.TAG_COMPLETION_ATTRIBUTE, Boolean.TRUE);

			// Remember to put 'paraML' as last paragraph
			bodyML.addChild(paraML);

		} finally {
			editorViewDoc.readUnlock();
		}

		kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.PLAIN);

		sourceView.addFocusListener(getToolbarStates());
		// sourceView.setDocument(doc);
		sourceView.putClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG, Boolean.TRUE);

		return sourceView;
	}

	private JEditorPane createContentControlHistoryView(WordMLTextPane editorView) {
		WordMLTextPane theView = new WordMLTextPane();

		editorView.saveCaretText();

		int pos = editorView.getCaretPosition();
		WordMLDocument editorViewDoc = editorView.getDocument();

		Mediator plutextClient = editorView.getWordMLEditorKit().getPlutextClient();

		try {
			editorViewDoc.readLock();

			DocumentElement elem = (DocumentElement) editorViewDoc.getSdtBlockMLElement(pos);
			SdtBlockML sdt = (SdtBlockML) elem.getElementML();
			String sdtId = sdt.getSdtProperties().getPlutextId();

			plutextClient.startSession();
			WordprocessingMLPackage wp = plutextClient.getVersionHistory(sdtId);
			org.docx4j.wml.Document wmlDoc = wp.getMainDocumentPart().getJaxbElement();

			WordMLDocument historyDoc = theView.getDocument();
			historyDoc.setDocumentFilter(new WordMLDocumentFilter());
			historyDoc.replaceBodyML(new BodyML(wmlDoc.getBody(), historyDoc.getElementMLFactory()));

		} catch (Exception exc) {
			exc.printStackTrace();

		} finally {
			plutextClient.endSession();
			editorViewDoc.readUnlock();
		}

		theView.setEditable(false);
		theView.addFocusListener(getToolbarStates());

		return theView;
	}

	private JEditorPane createRecentChangesView(WordMLTextPane editorView) {
		WordMLTextPane theView = new WordMLTextPane();

		editorView.saveCaretText();

		Mediator plutextClient = editorView.getWordMLEditorKit().getPlutextClient();

		try {
			plutextClient.startSession();
			WordprocessingMLPackage wp = plutextClient.getRecentChangesReport();
			org.docx4j.wml.Document wmlDoc = wp.getMainDocumentPart().getJaxbElement();

			WordMLDocument doc = theView.getDocument();
			doc.setDocumentFilter(new WordMLDocumentFilter());
			doc.replaceBodyML(new BodyML(wmlDoc.getBody(), doc.getElementMLFactory()));

		} catch (Exception exc) {
			exc.printStackTrace();

		} finally {
			plutextClient.endSession();
		}

		theView.setEditable(false);
		theView.addFocusListener(getToolbarStates());

		return theView;
	}

	private JEditorPane createEditorView(FileObject f, IObjectFactory factory) {
		String fileUri = f.getName().getURI();

		WordMLTextPane editorView = new WordMLTextPane();
		editorView.addFocusListener(_toolbarStates);
		editorView.addCaretListener(_toolbarStates);

		WordMLEditorKit editorKit = (WordMLEditorKit) editorView.getEditorKit();
		editorKit.addInputAttributeListener(_toolbarStates);

		WordMLDocument doc = null;

		try {
			if (f.exists()) {
				doc = editorKit.read(f, factory);
			}
		} catch (Exception exc) {
			exc.printStackTrace();

			ResourceMap rm = getContext().getResourceMap();
			String title = rm.getString(Constants.INIT_EDITOR_VIEW_IO_ERROR_DIALOG_TITLE);
			StringBuffer msg = new StringBuffer();
			msg.append(rm.getString(Constants.INIT_EDITOR_VIEW_IO_ERROR_MESSAGE));
			msg.append(Constants.NEWLINE);
			msg.append(VFSUtils.getFriendlyName(fileUri));
			showMessageDialog(title, msg.toString(), JOptionPane.ERROR_MESSAGE);
			doc = null;
		}

		if (doc == null) {
			doc = (WordMLDocument) editorKit.createDefaultDocument();
		}

		editorView.setTransferHandler(new TransferHandler(doc));

		doc.putProperty(WordMLDocument.FILE_PATH_PROPERTY, fileUri);
		doc.addDocumentListener(_toolbarStates);
		doc.setDocumentFilter(new WordMLDocumentFilter());
		editorView.setDocument(doc);
		editorView.putClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG, Boolean.TRUE);

		if (DocUtil.isSharedDocument(doc)) {
			editorKit.initPlutextClient(editorView);
		}

		return editorView;
	}

	JComponent createMainPanel() {
		_desktop = new JDesktopPane();
		_desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		_desktop.setBackground(Color.LIGHT_GRAY);

		JPanel toolbar = FxScriptUIHelper.getInstance().createToolBar();

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(toolbar, BorderLayout.NORTH);
		panel.add(_desktop, BorderLayout.CENTER);

		panel.setBorder(new EmptyBorder(0, 2, 2, 2)); // top, left, bottom, right
		panel.setPreferredSize(new Dimension(640, 480));

		return panel;
	}

	private int showConfirmClosingInternalFrame(JInternalFrame iframe, String resourceKeyPrefix) {
		int answer = JOptionPane.CANCEL_OPTION;

		String filePath = (String) iframe.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);

		ResourceMap rm = getContext().getResourceMap();
		String title = rm.getString(resourceKeyPrefix + ".dialog.title") + " "
				+ filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		String message = filePath + "\n" + rm.getString(resourceKeyPrefix + ".confirmMessage");
		Object[] options = { rm.getString(resourceKeyPrefix + ".confirm.saveNow"), rm.getString(resourceKeyPrefix + ".confirm.dontSave"),
				rm.getString(resourceKeyPrefix + ".confirm.cancel") };
		answer = showConfirmDialog(title, message, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, options, options[0]);
		if (answer == JOptionPane.CANCEL_OPTION) {
			;
		} else if (answer == JOptionPane.YES_OPTION) {
			boolean success = FileMenu.getInstance().save(iframe, null, FileMenu.SAVE_FILE_ACTION_NAME);
			if (success) {
				getToolbarStates().setDocumentDirty(iframe, false);
				getToolbarStates().setLocalEditsEnabled(iframe, false);
			} else {
				answer = JOptionPane.CANCEL_OPTION;
			}
		} else {
			// getToolbarStates().setDocumentDirty(iframe, false);
		}

		return answer;
	}

	private ViewManager getCurrentViewManager() {
		_viewManager.setOwner(getCurrentInternalFrame());
		return _viewManager;
	}

	JMenuBar createMenuBar() {
		JMenuBar menubar = new JMenuBar();

		JMenu fileMenu = FileMenu.getInstance().createJMenu();
		JMenu editMenu = EditMenu.getInstance().createJMenu();
		JMenu formatMenu = FormatMenu.getInstance().createJMenu();
		JMenu hyperlinkMenu = HyperlinkMenu.getInstance().createJMenu();
		JMenu contentControlMenu = ContentControlMenu.getInstance().createJMenu();
		JMenu reviewMenu = ReviewMenu.getInstance().createJMenu();
		JMenu viewMenu = ViewMenu.getInstance().createJMenu();
		JMenu plutextMenu = PlutextMenu.getInstance().createJMenu();
		JMenu windowMenu = WindowMenu.getInstance().createJMenu();
		JMenu helpMenu = HelpMenu.getInstance().createJMenu();

		menubar.add(fileMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(editMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(formatMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(hyperlinkMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(contentControlMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(reviewMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(viewMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(plutextMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(windowMenu);
		menubar.add(Box.createRigidArea(new Dimension(12, 0)));
		menubar.add(helpMenu);

		return menubar;
	}

	private class TitleBarMouseListener extends MouseMotionAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			JComponent titlePane = (JComponent) e.getSource();

			JInternalFrame frame = (JInternalFrame) titlePane.getParent();
			String tmp = frame.getTitle();
			FontMetrics fm = frame.getFontMetrics(frame.getFont());
			int width = fm.charsWidth(tmp.toCharArray(), 0, tmp.length());

			Rectangle tbounds = titlePane.getBounds();
			tbounds.width = width;

			if (tbounds.contains(e.getX(), e.getY())) {
				if (!tmp.startsWith(getUntitledFileName())) {
					tmp = (String) frame.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);
					// do not display password
					tmp = VFSUtils.getFriendlyName(tmp);
				}
				titlePane.setToolTipText(tmp);
			} else {
				titlePane.setToolTipText(null);
			}
		}
	}

	private class InternalFrameListener extends InternalFrameAdapter {

		@Override
		public void internalFrameIconified(InternalFrameEvent e) {
			// Sets JInternalFrame's maximum property to false.
			//
			// When a user clicks the minimize/maximize button of
			// JInternalFrame, its maximum property value remains
			// unchanged. This subsequently causes
			// JInternalFrame.setMaximum() not working.
			JInternalFrame frame = (JInternalFrame) e.getSource();
			try {
				frame.setMaximum(false);
			} catch (PropertyVetoException exc) {
				;// do nothing
			}
		}

		@Override
		public void internalFrameDeiconified(InternalFrameEvent e) {
			// Sets JInternalFrame's maximum property to false.
			//
			// When a user clicks the minimize/maximize button of
			// JInternalFrame, its maximum property value remains
			// unchanged. This subsequently causes
			// JInternalFrame.setMaximum() not working.
			JInternalFrame frame = (JInternalFrame) e.getSource();
			try {
				frame.setMaximum(true);
			} catch (PropertyVetoException exc) {
				;// do nothing
			}
		}

		@Override
		public void internalFrameOpened(InternalFrameEvent e) {
			JInternalFrame iframe = (JInternalFrame) e.getSource();
			WindowMenu.getInstance().addWindowMenuItem(iframe);
		}

		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			JInternalFrame iframe = (JInternalFrame) e.getSource();

			boolean canClose = true;

			if (getToolbarStates().isDocumentDirty(iframe)) {
				int answer = showConfirmClosingInternalFrame(iframe, "internalframe.close");
				canClose = (answer != JOptionPane.CANCEL_OPTION);
			}

			if (canClose) {
				WordMLTextPane editor = SwingUtil.getWordMLTextPane(iframe);
				if (editor != null) {
					editor.removeCaretListener(getToolbarStates());
					editor.removeFocusListener(getToolbarStates());
					editor.setTransferHandler(null);

					editor.getDocument().removeDocumentListener(getToolbarStates());

					WordMLEditorKit editorKit = (WordMLEditorKit) editor.getEditorKit();
					editorKit.removeInputAttributeListener(getToolbarStates());
					editor.getWordMLEditorKit().deinstall(editor);
				}
				iframe.dispose();
			}
		}

		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
			JInternalFrame iframe = (JInternalFrame) e.getSource();
			String filePath = (String) iframe.getClientProperty(WordMLDocument.FILE_PATH_PROPERTY);
			_iframeMap.remove(filePath);

			WindowMenu.getInstance().removeWindowMenuItem(iframe);
			_desktop.remove(iframe);
		}

		@Override
		public void internalFrameActivated(InternalFrameEvent e) {
			JInternalFrame iframe = (JInternalFrame) e.getSource();
			WindowMenu.getInstance().selectWindowMenuItem(iframe);
		}

		@Override
		public void internalFrameDeactivated(InternalFrameEvent e) {
			JInternalFrame iframe = (JInternalFrame) e.getSource();
			WindowMenu.getInstance().unSelectWindowMenuItem(iframe);
		}

	} // InternalFrameListener inner class

	private class ViewChangeListener implements ChangeListener {
		/*
		 * On View tab changes this method checks whether
		 * Editor View's content needs to be synchronized with
		 * that of Source View's
		 */
		@Override
		public void stateChanged(ChangeEvent event) {
			JTabbedPane pane = (JTabbedPane) event.getSource();
			int editorViewIdx = pane.indexOfTab(getEditorViewTabTitle());
			int sourceViewIdx = pane.indexOfTab(getSourceViewTabTitle());
			int historyViewIdx = pane.indexOfTab(getContentControlHistoryViewTabTitle());
			int recentViewIdx = pane.indexOfTab(getRecentChangesViewTabTitle());

			if (pane.getSelectedIndex() == editorViewIdx && editorViewIdx != -1) {
				changeToEditorView(pane, editorViewIdx, sourceViewIdx);

			} else if (pane.getSelectedIndex() == sourceViewIdx && sourceViewIdx != -1) {
				changeToSourceView(pane, editorViewIdx, sourceViewIdx);

			} else if (pane.getSelectedIndex() == historyViewIdx && historyViewIdx != -1) {
				changeToContentControlHistoryView(pane, editorViewIdx, historyViewIdx);

			} else if (pane.getSelectedIndex() == recentViewIdx && recentViewIdx != -1) {
				changeToRecentChangesView(pane, editorViewIdx, recentViewIdx);
			}
		}

		private void changeToEditorView(JTabbedPane pane, int editorViewIdx, int sourceViewIdx) {

			if (sourceViewIdx == -1) {
				// No Source View
				return;
			}

			final WordMLTextPane editorView = (WordMLTextPane) SwingUtil.getDescendantOfClass(WordMLTextPane.class,
					(Container) pane.getComponentAt(editorViewIdx), true);
			final JEditorPane sourceView = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class,
					(Container) pane.getComponentAt(sourceViewIdx), false);
			Boolean isSynched = (Boolean) sourceView.getClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG);
			if (!isSynched.booleanValue()) {
				// means that source view has been edited and
				// editorView has to be synchronised with source view.
				synchEditorView(editorView, sourceView);
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					editorView.requestFocusInWindow();
				}
			});
		}

		private void changeToSourceView(JTabbedPane pane, int editorViewIdx, int sourceViewIdx) {
			if (editorViewIdx == -1) {
				// No Editor View.
				// This should not happen.
				throw new IllegalStateException("No Editor View");
			}

			final WordMLTextPane editorView = (WordMLTextPane) SwingUtil.getDescendantOfClass(WordMLTextPane.class,
					(Container) pane.getComponentAt(editorViewIdx), true);
			final JEditorPane sourceView = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class,
					(Container) pane.getComponentAt(sourceViewIdx), false);
			Boolean isSynched = (Boolean) editorView.getClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG);
			if (!isSynched.booleanValue()) {
				synchSourceView(sourceView, editorView);
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sourceView.requestFocusInWindow();
				}
			});
		}

		private void changeToContentControlHistoryView(JTabbedPane pane, int editorViewIdx, int versionHistoryViewIdx) {
			if (editorViewIdx == -1) {
				// No Editor View.
				// This should not happen.
				throw new IllegalStateException("No Editor View");
			}
			final WordMLTextPane editorView = (WordMLTextPane) SwingUtil.getDescendantOfClass(WordMLTextPane.class,
					(Container) pane.getComponentAt(editorViewIdx), true);
			final JEditorPane historyView = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class,
					(Container) pane.getComponentAt(versionHistoryViewIdx), false);
			synchContentControlHistoryView(historyView, editorView);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					historyView.requestFocusInWindow();
				}
			});
		}

		private void changeToRecentChangesView(JTabbedPane pane, int editorViewIdx, int recentChangesViewIdx) {
			if (editorViewIdx == -1) {
				// No Editor View.
				// This should not happen.
				throw new IllegalStateException("No Editor View");
			}
			final WordMLTextPane editorView = (WordMLTextPane) SwingUtil.getDescendantOfClass(WordMLTextPane.class,
					(Container) pane.getComponentAt(editorViewIdx), true);
			final JEditorPane recentView = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class,
					(Container) pane.getComponentAt(recentChangesViewIdx), false);
			synchRecentChangesView(recentView, editorView);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					recentView.requestFocusInWindow();
				}
			});
		}

		private void synchEditorView(WordMLTextPane editorView, JEditorPane sourceView) {
			int caretPos = editorView.getCaretPosition();

			WordMLDocument editorViewDoc = editorView.getDocument();
			EditorKit kit = sourceView.getEditorKit();
			AbstractDocument sourceViewDoc = (AbstractDocument) sourceView.getDocument();
			try {
				sourceViewDoc.readLock();
				editorViewDoc.lockWrite();

				// Firstly, save source view content into WordprocessingMLPackage
				WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage) sourceViewDoc
						.getProperty(WordMLDocument.WML_PACKAGE_PROPERTY);
				DocUtil.write(kit, sourceViewDoc, wmlPackage, editorViewDoc.getElementMLFactory().getObjectFactory());

				// Now editorView's content has become invalid because
				// its WordprocessingMLPackage's main document part was
				// updated by DocUtil.write() above.
				// Need to refresh editor view.
				org.docx4j.wml.Document wmlDoc = wmlPackage.getMainDocumentPart().getJaxbElement();
				editorViewDoc.replaceBodyML(new BodyML(wmlDoc.getBody(), editorViewDoc.getElementMLFactory()));

				log.debug("stateChanged(): NEW Document Structure...");
				DocUtil.displayStructure(editorViewDoc);

				// reset LOCAL_VIEWS_SYNCHRONIZED_FLAG of source view
				sourceView.putClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG, Boolean.TRUE);

			} finally {
				editorViewDoc.unlockWrite();
				sourceViewDoc.readUnlock();

				editorView.validate();
				editorView.repaint();
				editorView.setCaretPosition(caretPos);
			}
		}

		private void synchSourceView(JEditorPane sourceView, WordMLTextPane editorView) {
			int caretPos = sourceView.getCaretPosition();

			JEditorPane newView = createSourceView(editorView);
			Document newDoc = newView.getDocument();

			sourceView.setDocument(newDoc);
			sourceView.validate();
			sourceView.repaint();
			sourceView.setCaretPosition(caretPos);

			// reset LOCAL_VIEWS_SYNCHRONIZED_FLAG of editor view
			editorView.putClientProperty(Constants.LOCAL_VIEWS_SYNCHRONIZED_FLAG, Boolean.TRUE);
		}

		private void synchContentControlHistoryView(JEditorPane historyView, WordMLTextPane editorView) {
			int caretPos = historyView.getCaretPosition();

			JEditorPane newView = createContentControlHistoryView(editorView);
			Document newDoc = newView.getDocument();

			historyView.setDocument(newDoc);
			historyView.validate();
			historyView.repaint();
			historyView.setCaretPosition(caretPos);
		}

		private void synchRecentChangesView(JEditorPane recentView, WordMLTextPane editorView) {
			int caretPos = recentView.getCaretPosition();

			JEditorPane newView = createRecentChangesView(editorView);
			Document newDoc = newView.getDocument();

			recentView.setDocument(newDoc);
			recentView.validate();
			recentView.repaint();
			recentView.setCaretPosition(caretPos);
		}
	} // ViewChangeListener inner class

	private class WmlExitListener implements ExitListener {
		@Override
		public boolean canExit(EventObject event) {
			boolean cancelExit = false;

			if (getToolbarStates().isAnyDocumentDirty()) {
				List<JInternalFrame> list = getAllInternalFrames();

				// Start from current editor's frame
				JInternalFrame currentFrame = getCurrentInternalFrame();
				list.remove(currentFrame);
				list.add(0, currentFrame);

				for (JInternalFrame iframe : list) {
					if (getToolbarStates().isDocumentDirty(iframe)) {
						try {
							iframe.setSelected(true);
							iframe.setIcon(false);
						} catch (PropertyVetoException exc) {
							;// ignore
						}

						int answer = showConfirmClosingInternalFrame(iframe, "Application.exit.saveFirst");
						if (answer == JOptionPane.CANCEL_OPTION) {
							cancelExit = true;
							break;
						}
					}
				} // for (iframe) loop
			} // if (getToolbarStates().isAnyDocumentDirty()

			boolean canExit = false;
			if (!cancelExit) {
				ResourceMap rm = getContext().getResourceMap();
				String title = rm.getString("Application.exit.dialog.title");
				String message = rm.getString("Application.exit.confirmMessage");
				int answer = showConfirmDialog(title, message, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				canExit = (answer == JOptionPane.YES_OPTION);
			} // if (!canExit)

			return canExit;
		} // canExit()

		@Override
		public void willExit(EventObject event) {
			;// not implemented
		}
	}// WMLExitListener inner class

	private class ViewManager {
		private JInternalFrame owner;
		private final ViewChangeListener viewChangeListener;

		ViewManager() {
			this.viewChangeListener = new ViewChangeListener();
			this.owner = null;
		}

		void setOwner(JInternalFrame iframe) {
			owner = iframe;
		}

		JTabbedPane getJTabbedPane() {
			checkOwner();

			if (owner.getContentPane().getComponent(0) instanceof JTabbedPane) {
				return (JTabbedPane) owner.getContentPane().getComponent(0);
			}
			return null;
		}

		JEditorPane getEditorView() {
			checkOwner();
			return getView(getEditorViewTabTitle());
		}

		JEditorPane getSourceView() {
			checkOwner();
			return getView(getSourceViewTabTitle());
		}

		JEditorPane getVersionHistoryView() {
			checkOwner();
			return getView(getContentControlHistoryViewTabTitle());
		}

		JEditorPane getRecentChangesView() {
			checkOwner();
			return getView(getRecentChangesViewTabTitle());
		}

		private JEditorPane getView(String viewTabTitle) {
			JEditorPane theView = null;

			JTabbedPane tabbedPane = getJTabbedPane();
			if (tabbedPane == null) {
				if (getEditorViewTabTitle().equals(viewTabTitle)) {
					theView = SwingUtil.getWordMLTextPane(owner);
				} else {
					// There should not be any other view.
					// Other views are always created in a Tabbed pane.
					theView = null;
				}
			} else {
				int idx = tabbedPane.indexOfTab(viewTabTitle);
				if (idx != -1) {
					theView = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class, (Container) tabbedPane.getComponentAt(idx),
							false);
				}
			}

			return theView;
		}

		void showViewTab(String tabTitle) {
			checkOwner();

			WordMLTextPane editorView = (WordMLTextPane) getEditorView();
			if (editorView == null) {
				throw new IllegalStateException("No Editor View");
			}

			JTabbedPane tabbedPane = getJTabbedPane();
			int tabIdx = -1;
			if (tabbedPane != null) {
				tabIdx = tabbedPane.indexOfTab(tabTitle);
			}

			JEditorPane view = null;
			if (tabIdx == -1) {
				// Add view tab
				if (getSourceViewTabTitle().equals(tabTitle)) {
					view = createSourceView(editorView);
					addViewTab(view, tabTitle);
					view.setCaretPosition(0);
				} else if (getContentControlHistoryViewTabTitle().equals(tabTitle)) {
					view = createContentControlHistoryView(editorView);
					addViewTab(view, tabTitle);
					view.setCaretPosition(0);
				} else if (getRecentChangesViewTabTitle().equals(tabTitle)) {
					view = createRecentChangesView(editorView);
					addViewTab(view, tabTitle);
					view.setCaretPosition(0);
				}
			} else {
				tabbedPane.setSelectedIndex(tabIdx);
				view = (JEditorPane) SwingUtil
						.getDescendantOfClass(JEditorPane.class, (Container) tabbedPane.getComponentAt(tabIdx), false);

			}

			if (view != null) {
				final JEditorPane ep = view;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ep.requestFocusInWindow();
					}
				});
			}
		}

		void closeViewTab(String tabTitle) {
			removeViewTab(tabTitle);
		}

		void removeViewTab(String tabTitle) {
			checkOwner();

			final JTabbedPane tabbedPane = getJTabbedPane();
			if (tabbedPane == null) {
				return;
			}

			if (getEditorViewTabTitle().equals(tabTitle)) {
				// Editor View should not be closed
				throw new IllegalArgumentException("Tab title=" + tabTitle);
			}

			int idx = tabbedPane.indexOfTab(getEditorViewTabTitle());
			if (idx == -1) {
				throw new IllegalStateException("No Editor View Tab");
			}

			// Disable tabbedPane's change listener.
			// This is needed to stop the view synchronisation process in it.
			tabbedPane.removeChangeListener(this.viewChangeListener);

			if (tabbedPane.getTabCount() > 2) {
				idx = tabbedPane.indexOfTab(tabTitle);
				tabbedPane.removeTabAt(idx);

				int lastTab = tabbedPane.getTabCount() - 1;
				tabbedPane.setSelectedIndex(lastTab);

				// Put change listener back on.
				tabbedPane.addChangeListener(ViewManager.this.viewChangeListener);

				final JEditorPane viewToFocus = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class,
						(Container) tabbedPane.getComponentAt(lastTab), false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						viewToFocus.requestFocusInWindow();
					}
				});

			} else if (tabbedPane.getTabCount() == 2) {
				// If Tabbed pane consists of Editor View and the view being closed
				JPanel viewPanel = (JPanel) tabbedPane.getComponentAt(idx);

				Rectangle bounds = owner.getBounds();
				owner.getContentPane().removeAll();

				// Move editorViewPanel from tabbedPane to owner's content
				tabbedPane.remove(idx);
				owner.getContentPane().add(viewPanel);

				owner.invalidate();
				owner.validate();
				owner.setBounds(bounds);

				final JEditorPane viewToFocus = (JEditorPane) SwingUtil.getDescendantOfClass(JEditorPane.class, viewPanel, false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						viewToFocus.requestFocusInWindow();
					}
				});

			} else {
				// should never happen.
			}

		} // removeViewTab()

		void selectViewTab(String tabTitle) {
			JTabbedPane tabbedPane = getJTabbedPane();
			if (tabbedPane == null) {
				throw new IllegalStateException("No Tabbed Pane.");
			}

			int idx = tabbedPane.indexOfTab(tabTitle);
			if (idx != -1) {
				tabbedPane.setSelectedIndex(idx);
			}
		}

		private void addViewTab(final JEditorPane view, String tabTitle) {
			JPanel viewPanel = FxScriptUIHelper.getInstance().createEditorPanel(view);

			Rectangle bounds = owner.getBounds();
			JTabbedPane tabbedPane = getJTabbedPane();
			if (tabbedPane == null) {
				// Create Tabbed Pane.
				// ADD Editor View Tab and new 'viewPanel' Tab
				JPanel editorViewPanel = (JPanel) owner.getContentPane().getComponent(0);

				owner.getContentPane().removeAll();

				tabbedPane = new JTabbedPane();
				tabbedPane.addTab(getEditorViewTabTitle(), editorViewPanel);
				tabbedPane.addTab(tabTitle, viewPanel);

				owner.getContentPane().add(tabbedPane);
				owner.invalidate();
				owner.validate();

			} else {
				// Disable tabbedPane's change listener.
				// This is needed to avoid unnecessary view synchronisation
				// process due to tab addition.
				tabbedPane.removeChangeListener(this.viewChangeListener);

				// Add Tab
				tabbedPane.addTab(tabTitle, viewPanel);
				tabbedPane.invalidate();
				tabbedPane.validate();
			}

			int i = tabbedPane.indexOfTab(tabTitle);
			tabbedPane.setSelectedIndex(i);
			// Put change listener back on
			tabbedPane.addChangeListener(this.viewChangeListener);
			owner.setBounds(bounds);
		}

		private void checkOwner() {
			if (owner == null) {
				throw new IllegalStateException("No Owner");
			}
		}
	} // TabbedPaneManager inner class

}// WordMLEditor class
