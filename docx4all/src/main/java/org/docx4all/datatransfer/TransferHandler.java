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

package org.docx4all.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.docx4all.swing.WordMLTextPane;
import org.docx4all.swing.text.BadSelectionException;
import org.docx4all.swing.text.TextSelector;
import org.docx4all.swing.text.WordMLDocument;
import org.docx4all.swing.text.WordMLFragment;

/**
 *	@author Jojada Tirtowidjojo - 16/01/2008
 */
public class TransferHandler extends javax.swing.TransferHandler {
    public TransferHandler() {
    }

    public boolean importData(JComponent c, Transferable t) {
    	DataFlavor[] flavors = t.getTransferDataFlavors();
    	
        if (!canImport(c, flavors)
        	|| !(c instanceof WordMLTextPane)) {
            return false;
        }
        
        WordMLFragment wmlFragment = getFragment(t, flavors);        
        if (wmlFragment != null) {
        	((WordMLTextPane) c).replaceSelection(wmlFragment);
        }
        
        //Do not worry about the returned value for now
        return true;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == MOVE) {
        	final JEditorPane editor = (JEditorPane) c;
			WordMLDocument doc = (WordMLDocument) editor.getDocument();
			final int start = editor.getSelectionStart();
			int length = editor.getSelectionEnd() - start;
			try {
				doc.remove(start, length);
				
				Runnable r = new Runnable() {
					public void run() {
						editor.setCaretPosition(start);
					}
				};
				SwingUtilities.invokeLater(r);
				
			} catch (BadLocationException exc) {
				;//ignore
			}
        }
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
    	boolean canImport = false;
    	for (int i=0; i < flavors.length && !canImport; i++) {
    		canImport = WordMLTransferable.isSupported(flavors[i]);
    	}
    	
    	return canImport;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    protected Transferable createTransferable(JComponent c) {
    	Transferable theObj = null;    	
    	if (c instanceof WordMLTextPane) {
    		WordMLTextPane editor = (WordMLTextPane) c;
			editor.saveCaretText();
			WordMLDocument doc = (WordMLDocument) editor.getDocument();
			int start = editor.getSelectionStart();
			int length = editor.getSelectionEnd() - start;
			try {
				TextSelector ts = new TextSelector(doc, start, length);
				WordMLFragment frag = new WordMLFragment(ts);
				theObj = new WordMLTransferable(frag);
			} catch (BadSelectionException exc) {
				;// ignore
			}
    	}
        return theObj;
    }

    private boolean contains(DataFlavor[] flavors, DataFlavor checkee) {
    	boolean contained = false;
    	
        for (int i = 0; i < flavors.length && !contained; i++) {
            if (flavors[i].equals(checkee)) {
            	contained = true;
            }
        }
        return contained;
    }

    private WordMLFragment getFragment(Transferable t, DataFlavor[] flavors) {
        WordMLFragment wmlFragment = null;
        try {
            if (contains(flavors, WordMLTransferable.WORDML_FRAGMENT_FLAVOR)) {
            	wmlFragment = 
            		(WordMLFragment) 
            			t.getTransferData(
            				WordMLTransferable.WORDML_FRAGMENT_FLAVOR);
            } else if (contains(flavors, WordMLTransferable.STRING_FLAVOR)) {
				String s = 
					(String) t.getTransferData(WordMLTransferable.STRING_FLAVOR);
            	wmlFragment = new WordMLFragment(s);
            }
        } catch (UnsupportedFlavorException exc) {
            exc.printStackTrace();
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return wmlFragment;
    }
}// TransferHandler class



















