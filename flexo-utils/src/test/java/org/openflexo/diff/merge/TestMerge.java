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

package org.openflexo.diff.merge;

import java.io.File;
import java.io.IOException;

import org.openflexo.diff.DiffSource;
import org.openflexo.diff.merge.MergeChange.MergeChangeSource;
import org.openflexo.diff.merge.MergeChange.MergeChangeType;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

import junit.framework.TestCase;

public class TestMerge extends TestCase {

	public void test0() throws IOException {
		// Unused ResourceLocator rl =
		ResourceLocator.getResourceLocator();
		File original = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge0-original.java.txt"))).getFile();
		File left = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge0-left.java.txt"))).getFile();
		File right = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge0-right.java.txt"))).getFile();
		Merge merge = new Merge(new DiffSource(original), new DiffSource(left), new DiffSource(right), DefaultMergedDocumentType.JAVA);
		assertEquals(merge.getChanges().size(), 0);
		assertFalse(merge.isReallyConflicting());
	}

	public void test1() throws IOException {
		// Unused ResourceLocator rl =
		ResourceLocator.getResourceLocator();
		File original = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge1-original.java.txt"))).getFile();
		File left = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge1-left.java.txt"))).getFile();
		File right = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge1-right.java.txt"))).getFile();
		Merge merge = new Merge(new DiffSource(original), new DiffSource(left), new DiffSource(right), DefaultMergedDocumentType.JAVA);
		assertEquals(merge.getChanges().size(), 9);
		assertFalse(merge.isReallyConflicting());
		assertChange(merge.getChanges().get(0), MergeChangeSource.Left, MergeChangeType.Removal, 23, 22, 23, 33, 23, 33);
		assertChange(merge.getChanges().get(1), MergeChangeSource.Left, MergeChangeType.Addition, 25, 25, 36, 35, 36, 35);
		assertChange(merge.getChanges().get(2), MergeChangeSource.Right, MergeChangeType.Addition, 91, 90, 101, 100, 101, 105);
		assertChange(merge.getChanges().get(3), MergeChangeSource.Right, MergeChangeType.Modification, 98, 98, 108, 108, 113, 114);
		assertChange(merge.getChanges().get(4), MergeChangeSource.Left, MergeChangeType.Addition, 100, 100, 110, 109, 116, 115);
		assertChange(merge.getChanges().get(5), MergeChangeSource.Right, MergeChangeType.Removal, 103, 103, 112, 112, 118, 117);
		assertChange(merge.getChanges().get(6), MergeChangeSource.Left, MergeChangeType.Modification, 106, 106, 115, 115, 120, 120);
		assertChange(merge.getChanges().get(7), MergeChangeSource.Left, MergeChangeType.Modification, 130, 130, 139, 139, 144, 144);
		assertChange(merge.getChanges().get(8), MergeChangeSource.Left, MergeChangeType.Modification, 134, 134, 143, 143, 148, 148);
	}

	public void test2() throws IOException {
		// Unused ResourceLocator rl =
		ResourceLocator.getResourceLocator();
		File original = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge2-original.java.txt"))).getFile();
		File left = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge2-left.java.txt"))).getFile();
		File right = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge2-right.java.txt"))).getFile();
		Merge merge = new Merge(new DiffSource(original), new DiffSource(left), new DiffSource(right), DefaultMergedDocumentType.JAVA);
		assertEquals(merge.getChanges().size(), 14);
		assertTrue(merge.isReallyConflicting());
		assertChange(merge.getChanges().get(0), MergeChangeSource.Left, MergeChangeType.Removal, 7, 6, 7, 7, 7, 7);
		assertChange(merge.getChanges().get(1), MergeChangeSource.Right, MergeChangeType.Addition, 10, 9, 11, 10, 11, 11);
		assertChange(merge.getChanges().get(2), MergeChangeSource.Conflict, MergeChangeType.Modification, 13, 13, 14, 13, 15, 15);
		assertChange(merge.getChanges().get(3), MergeChangeSource.Left, MergeChangeType.Addition, 19, 19, 19, 18, 21, 20);
		assertChange(merge.getChanges().get(4), MergeChangeSource.Right, MergeChangeType.Addition, 35, 34, 34, 33, 36, 39);
		assertChange(merge.getChanges().get(5), MergeChangeSource.Conflict, MergeChangeType.Modification, 79, 79, 78, 77, 84, 85);
		assertChange(merge.getChanges().get(6), MergeChangeSource.Left, MergeChangeType.Addition, 86, 86, 84, 83, 92, 91);
		assertChange(merge.getChanges().get(7), MergeChangeSource.Right, MergeChangeType.Addition, 102, 101, 99, 98, 107, 109);
		assertChange(merge.getChanges().get(8), MergeChangeSource.Left, MergeChangeType.Modification, 129, 129, 126, 126, 137, 137);
		assertChange(merge.getChanges().get(9), MergeChangeSource.Conflict, MergeChangeType.Modification, 135, 138, 132, 135, 143, 146);
		assertChange(merge.getChanges().get(10), MergeChangeSource.Right, MergeChangeType.Addition, 140, 139, 137, 136, 148, 148);
		assertChange(merge.getChanges().get(11), MergeChangeSource.Left, MergeChangeType.Addition, 141, 141, 138, 137, 150, 149);
		assertChange(merge.getChanges().get(12), MergeChangeSource.Conflict, MergeChangeType.Modification, 143, 148, 139, 144, 151, 156);
		assertChange(merge.getChanges().get(13), MergeChangeSource.Right, MergeChangeType.Addition, 156, 155, 152, 151, 164, 164);
	}

	public void test3() throws IOException {
		// Unused ResourceLocator rl =
		ResourceLocator.getResourceLocator();
		File original = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge3-original.java.txt"))).getFile();
		File left = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge3-left.java.txt"))).getFile();
		File right = ((FileResourceImpl) (ResourceLocator.locateResource("TestMerge/TestMerge3-right.java.txt"))).getFile();
		Merge merge = new Merge(new DiffSource(original), new DiffSource(left), new DiffSource(right), DefaultMergedDocumentType.JAVA);
		assertEquals(merge.getChanges().size(), 9);
		assertTrue(merge.isReallyConflicting());
		assertChange(merge.getChanges().get(0), MergeChangeSource.Conflict, MergeChangeType.Modification, 2, 4, 2, 4, 2, 4);
		assertChange(merge.getChanges().get(1), MergeChangeSource.Right, MergeChangeType.Modification, 7, 7, 7, 7, 7, 7);
		assertChange(merge.getChanges().get(2), MergeChangeSource.Left, MergeChangeType.Modification, 17, 17, 17, 17, 17, 17);
		assertChange(merge.getChanges().get(3), MergeChangeSource.Left, MergeChangeType.Addition, 23, 23, 23, 22, 23, 22);
		assertChange(merge.getChanges().get(4), MergeChangeSource.Conflict, MergeChangeType.Modification, 27, 32, 26, 31, 26, 31);
		assertChange(merge.getChanges().get(5), MergeChangeSource.Right, MergeChangeType.Addition, 35, 34, 34, 33, 34, 37);
		assertChange(merge.getChanges().get(6), MergeChangeSource.Left, MergeChangeType.Addition, 77, 80, 76, 75, 80, 79);
		assertChange(merge.getChanges().get(7), MergeChangeSource.Conflict, MergeChangeType.Modification, 154, 154, 149, 149, 153, 153);
		assertChange(merge.getChanges().get(8), MergeChangeSource.Conflict, MergeChangeType.Modification, 159, 159, 154, 154, 158, 158);
	}

	private static void assertChange(MergeChange change, MergeChangeSource changeSource, MergeChangeType changeType, int first0, int last0,
			int first1, int last1, int first2, int last2) {
		assertEquals(change.getMergeChangeSource(), changeSource);
		assertEquals(change.getMergeChangeType(), changeType);
		assertEquals(first0, change.getFirst0());
		assertEquals(first1, change.getFirst1());
		assertEquals(first2, change.getFirst2());
		assertEquals(last0, change.getLast0());
		assertEquals(last1, change.getLast1());
		assertEquals(last2, change.getLast2());
	}

	/*public static void main(String[] args) 
	{
		File original0 = new FileResource("TestMerge/TestMerge0-original.java.txt");
		File left0 = new FileResource("TestMerge/TestMerge0-left.java.txt");
		File right0 = new FileResource("TestMerge/TestMerge0-right.java.txt");
		File original1 = new FileResource("TestMerge/TestMerge1-original.java.txt");
		File left1 = new FileResource("TestMerge/TestMerge1-left.java.txt");
		File right1 = new FileResource("TestMerge/TestMerge1-right.java.txt");
		File original2 = new FileResource("TestMerge/TestMerge2-original.java.txt");
		File left2 = new FileResource("TestMerge/TestMerge2-left.java.txt");
		File right2 = new FileResource("TestMerge/TestMerge2-right.java.txt");
		File original3 = new FileResource("TestMerge/TestMerge3-original.java.txt");
		File left3 = new FileResource("TestMerge/TestMerge3-left.java.txt");
		File right3 = new FileResource("TestMerge/TestMerge3-right.java.txt");
		File original4 = new FileResource("TestMerge/TestMerge4-original.java.txt");
		File left4 = new FileResource("TestMerge/TestMerge4-left.java.txt");
		File right4 = new FileResource("TestMerge/TestMerge4-right.java.txt");
		File original5 = new FileResource("TestMerge/TestMerge5-original.java.txt");
		File left5 = new FileResource("TestMerge/TestMerge5-left.java.txt");
		File right5 = new FileResource("TestMerge/TestMerge5-right.java.txt");
		File original6 = new FileResource("TestMerge/TestMerge6-original.java.txt");
		File left6 = new FileResource("TestMerge/TestMerge6-left.java.txt");
		File right6 = new FileResource("TestMerge/TestMerge6-right.java.txt");
		File original7 = new FileResource("TestMerge/TestMerge7-original.java.txt");
		File left7 = new FileResource("TestMerge/TestMerge7-left.java.txt");
		File right7 = new FileResource("TestMerge/TestMerge7-right.java.txt");
		File original8 = new FileResource("TestMerge/TestMerge8-original.xml");
		File left8 = new FileResource("TestMerge/TestMerge8-left.xml");
		File right8 = new FileResource("TestMerge/TestMerge8-right.xml");
	
		final JDialog dialog = new JDialog((Frame)null,true);
	
		JPanel panel = new JPanel(new BorderLayout());
	
		final JTabbedPane tabbedPane = new JTabbedPane();
	
		tabbedPane.add(makeMergeTabbedPane(original0, left0, right0,DefaultMergedDocumentType.JAVA),"Merge-0");
		tabbedPane.add(makeMergeTabbedPane(original1, left1, right1,DefaultMergedDocumentType.JAVA),"Merge-1");
		tabbedPane.add(makeMergeTabbedPane(original2, left2, right2,DefaultMergedDocumentType.JAVA),"Merge-2");
		tabbedPane.add(makeMergeTabbedPane(original3, left3, right3,DefaultMergedDocumentType.JAVA),"Merge-3");
		tabbedPane.add(makeMergeTabbedPane(original4, left4, right4,DefaultMergedDocumentType.JAVA),"Merge-4");
		tabbedPane.add(makeMergeTabbedPane(original5, left5, right5,DefaultMergedDocumentType.JAVA),"Merge-5");
		tabbedPane.add(makeMergeTabbedPane(original6, left6, right6,DefaultMergedDocumentType.JAVA),"Merge-6");
		tabbedPane.add(makeMergeTabbedPane(original7, left7, right7,DefaultMergedDocumentType.JAVA),"Merge-7");
		tabbedPane.add(makeMergeTabbedPane(original8, left8, right8,DefaultMergedDocumentType.XML),"Merge-8");
	
		panel.add(tabbedPane,BorderLayout.CENTER);
	
		JButton closeButton = new JButton("Exit");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				System.exit(0);
			}
		});
	
		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTabbedPane mergeTabbedPane = (JTabbedPane)tabbedPane.getSelectedComponent();
				DefaultMergePanel currentMergePanel = (DefaultMergePanel)mergeTabbedPane.getComponentAt(0);
				editMerge((Merge)currentMergePanel.getMerge());
			}
		});
	
		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(closeButton);
		controlPanel.add(editButton);
		
		panel.add(controlPanel,BorderLayout.SOUTH);
	
		dialog.setPreferredSize(new Dimension(1000,800));
		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	private static JTabbedPane makeMergeTabbedPane(File original, File left, File right, MergedDocumentType docType)
	{
		Merge merge = null;
		DiffReport diffLeft = null;
		DiffReport diffRight = null;
		try {
			merge = new Merge(new DiffSource(original),new DiffSource(left),new DiffSource(right),docType);
			diffLeft = ComputeDiff.diff(left,original);
			diffRight = ComputeDiff.diff(original,right);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(new DefaultMergePanel(merge,docType.getStyle()),"Merge");
		tabbedPane.add(new DiffPanel(diffLeft,docType.getStyle()),"Diffs between left and original");
		tabbedPane.add(new DiffPanel(diffRight,docType.getStyle()),"Diffs between original and right");
	
		return tabbedPane;
	}
	
	public static void editMerge(Merge merge)
	{
		final JDialog dialog = new JDialog((Frame)null,true);
	
		JPanel panel = new JPanel(new BorderLayout());
		MergeEditor editor = new MergeEditor(merge) {
			@Override
			public void done() {
				dialog.dispose();
			}
		};
		panel.add(editor,BorderLayout.CENTER);
	
		dialog.setPreferredSize(new Dimension(1000,800));
		dialog.getContentPane().add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}
	*/
}
