package ctrmap.creativestudio.dialogs;

import ctrmap.formats.generic.collada.DAEExportSettings;
import xstandard.gui.DialogOptionRemember;
import java.awt.Frame;

public class DAESimpleExportDialog extends javax.swing.JDialog {

	private DAEExportSettings result = null;

	public DAESimpleExportDialog(Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		setLocationRelativeTo(parent);

		DialogOptionRemember.setRememberedCheckbox(btnAnmExportBake);
		DialogOptionRemember.setRememberedCheckbox(btnNoUseLookAt);
	}

	public DAEExportSettings getResult() {
		return result;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnExport = new javax.swing.JButton();
        btnAnmExportBake = new javax.swing.JCheckBox();
        btnNoUseLookAt = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Additional settings");
        setResizable(false);

        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnAnmExportBake.setSelected(true);
        btnAnmExportBake.setText("Bake animations");

        btnNoUseLookAt.setSelected(true);
        btnNoUseLookAt.setText("Do not use look-at transform");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNoUseLookAt)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAnmExportBake)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(btnExport)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnExport)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(btnAnmExportBake)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNoUseLookAt)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
		DialogOptionRemember.putRememberedCheckbox(btnAnmExportBake);
		DialogOptionRemember.putRememberedCheckbox(btnNoUseLookAt);

		result = new DAEExportSettings();
		result.bakeAnimations = btnAnmExportBake.isSelected();
		result.doNotUseLookAt = btnNoUseLookAt.isSelected();

		dispose();
    }//GEN-LAST:event_btnExportActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox btnAnmExportBake;
    private javax.swing.JButton btnExport;
    private javax.swing.JCheckBox btnNoUseLookAt;
    // End of variables declaration//GEN-END:variables
}
