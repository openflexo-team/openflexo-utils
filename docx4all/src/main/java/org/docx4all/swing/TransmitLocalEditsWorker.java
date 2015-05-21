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

package org.docx4all.swing;

import java.awt.Cursor;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.docx4all.swing.text.WordMLDocument;
import org.docx4all.ui.main.WordMLEditor;
import org.docx4all.xml.BodyML;
import org.plutext.client.Mediator;

/**
 * @author Jojada Tirtowidjojo - 11/09/2008
 */
public class TransmitLocalEditsWorker extends SwingWorker<Boolean, Void> implements IProgressBarWorker {
	private final WordMLEditor wmlEditor;
	private final Mediator plutextClient;
	private final Hashtable<Integer, String> messageTable;

	public TransmitLocalEditsWorker(Mediator plutextClient, WordMLEditor wmlEditor) {
		this.wmlEditor = wmlEditor;
		this.plutextClient = plutextClient;
		this.messageTable = new Hashtable<Integer, String>(8);
	}

	/*
	 * Main task. Executed in background thread.
	 */
	@Override
	public Boolean doInBackground() {
		WordMLTextPane editor = this.plutextClient.getWordMLTextPane();
		WordMLDocument doc = editor.getDocument();

		Cursor origCursor = editor.getCursor();
		editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					preTransmit();
				}
			});
		} catch (Exception exc) {
			exc.printStackTrace();
			setProgress(TransmitProgress.DONE, "There was a transmission error . Please see logs for details.");
			editor.setCursor(origCursor);
			return Boolean.FALSE;
		}

		Boolean success = Boolean.TRUE;

		try {
			doc.readLock();

			plutextClient.startSession();
			success = Boolean.valueOf(plutextClient.transmitLocalChanges(this));

		} catch (Exception exc) {
			exc.printStackTrace();
			setProgress(TransmitProgress.DONE, "There was a transmission error . Please see logs for details.");
			success = Boolean.FALSE;
		} finally {
			plutextClient.endSession();
			doc.readUnlock();
			editor.setCursor(origCursor);
		}

		return success;
	}

	/*
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {
		try {
			Boolean endResult = get();
			firePropertyChange("endResult", null, endResult);
		} catch (Exception exc) {
			;// ignore
		}
	}

	public void preTransmit() {
		setProgress(TransmitProgress.START, "Cleaning Content Controls");

		WordMLTextPane editor = this.plutextClient.getWordMLTextPane();
		WordMLDocument doc = editor.getDocument();

		try {
			org.docx4j.wml.Document wmlDoc = org.plutext.client.Util.preTransmit(this.plutextClient);
			doc.replaceBodyML(new BodyML(wmlDoc.getBody(), doc.getElementMLFactory()));

		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	public void setProgress(TransmitProgress progress, String message) {
		setProgress(progress.value(), message);
	}

	public void setProgress(Integer progress, String message) {
		// String old = this.messageTable.get(progress);
		this.messageTable.put(progress, message);
		// firePropertyChange("message", old, message);

		setProgress(progress.intValue());
	}

	public String getProgressMessage(TransmitProgress progress) {
		return getProgressMessage(progress.value());
	}

	@Override
	public String getProgressMessage(Integer progressValue) {
		return this.messageTable.get(progressValue);
	}

	@Override
	public JComponent getInsertedEndMessage() {
		return null;
	}

	public enum TransmitProgress {
		START(10),
		INSPECTING_LOCAL_DOC_STRUCTURE(20),
		FETCHING_REMOTE_DOC_STRUCTURE(30),
		IDENTIFYING_STRUCTURAL_CHANGES(40),
		IDENTIFYING_UPDATED_TEXT(50),
		TRANSMITTING_MESSAGE(60),
		INTERPRETING_TRANSMISSION_RESULT(70),

		DONE(100);

		private final Integer value;

		TransmitProgress(int value) {
			this.value = Integer.valueOf(value);
		}

		public Integer value() {
			return value;
		}
	}
}// TransmitLocalEditsWorker class

