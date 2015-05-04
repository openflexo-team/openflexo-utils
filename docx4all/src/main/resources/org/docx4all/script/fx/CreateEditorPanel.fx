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

package org.docx4all.script.fx;

import org.docx4all.swing.WordMLTextPane;

import org.docx4all.script.fx.ui.EditorPanel;
import org.docx4all.script.fx.ui.widget.ScrollableEditorPane;

import javafx.ui.HorizontalScrollBarPolicy;

var editorView = editorView:<<javax.swing.JEditorPane>>;

var editorPanel = EditorPanel {
    editorPane: ScrollableEditorPane {
        editor: editorView
        horizontalScrollBarPolicy: NEVER:HorizontalScrollBarPolicy
    }
};

return editorPanel.getComponent();



