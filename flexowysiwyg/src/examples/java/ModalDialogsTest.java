/*
 * ModalDialogsTest.java
 *
 * Created on 21 marzo 2006, 12.17
 */

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import sferyx.administration.editors.HTMLEditor;
/**
 *
 * @author  Vassil Boyadjiev
 */
public class ModalDialogsTest extends javax.swing.JApplet {
    
    /** Creates new form ModalDialogsTest */
    public ModalDialogsTest() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jButton1 = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.FlowLayout());

        jButton1.setText("Start Editor");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        getContentPane().add(jButton1);

    }//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Add your handling code here:
        JDialog dialog=new JDialog((Frame)SwingUtilities.windowForComponent(this),"Editor dialog",true);
       
        dialog.getContentPane().setLayout(new BorderLayout());
        HTMLEditor htmlEditor=new HTMLEditor();
        dialog.getContentPane().add(htmlEditor);
        CustomHyperlinkDialog imageBrowsable=new CustomHyperlinkDialog((Frame)SwingUtilities.windowForComponent(this),true);
        //let's make it a bit different
        imageBrowsable.setBackground(java.awt.Color.red);
        htmlEditor.setImageCustomBrowsableComponent(imageBrowsable);
        dialog.setSize(300,300);
        dialog.setLocation(200,200);
        dialog.show();
        
    }//GEN-LAST:event_jButton1ActionPerformed
    public static void main(String[] ars)
    {
        JFrame frame=new JFrame();
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new ModalDialogsTest());
        frame.setSize(400,400);
        frame.show();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
    
}
