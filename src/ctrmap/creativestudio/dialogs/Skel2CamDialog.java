package ctrmap.creativestudio.dialogs;

import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Model;
import xstandard.gui.DialogUtils;
import xstandard.math.MathEx;
import java.util.List;
import javax.swing.JFrame;

public class Skel2CamDialog extends javax.swing.JDialog {

	private boolean confirmed;
	private String transformJointName;
	private String camTgtName;
	private Model refModel;
	private float rootRotAdjustment;
		
	public Skel2CamDialog(java.awt.Frame parent, boolean modal, SkeletalAnimation anm, List<Model> models) {
		super(parent, modal);
		initComponents();
		setLocationRelativeTo(parent);
		bakeSkeletonSelect.setModelBoxEnabled(false);
		
		for (SkeletalBoneTransform bt : anm.bones){
			joint.addItem(bt.name);
		}
	}
	
	public static boolean checkApplicableOpenDialog(JFrame parent, SkeletalAnimation anm){
		if (anm.bones.isEmpty()){
			DialogUtils.showErrorMessage(parent, "Pointless operation", "This animation does not contain any joint transforms.");
			return false;
		}
		return true;
	}
	
	public boolean getConfirmed(){
		return confirmed;
	}
	
	public String getResultJointName(){
		return transformJointName;
	}
	
	public String getResultCamName(){
		return camTgtName;
	}

	public Model getRefModel(){
		return refModel;
	}
	
	public float getRootRotAdjustment() {
		return rootRotAdjustment;
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        camTgtBtnGroup = new javax.swing.ButtonGroup();
        convModeGroup = new javax.swing.ButtonGroup();
        jointLabel = new javax.swing.JLabel();
        joint = new javax.swing.JComboBox<>();
        camTgtLabel = new javax.swing.JLabel();
        btnCamNameMatchJoint = new javax.swing.JRadioButton();
        btnCamNameCustom = new javax.swing.JRadioButton();
        btnCamNameCustomField = new javax.swing.JTextField();
        btnConvert = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        convModeLabel = new javax.swing.JLabel();
        btnConvLocal = new javax.swing.JRadioButton();
        btnConvGlobal = new javax.swing.JRadioButton();
        optionsLabel = new javax.swing.JLabel();
        btnRotateRootXFixup = new javax.swing.JCheckBox();
        bakeSkeletonSelect = new ctrmap.creativestudio.dialogs.ModelSelectionPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Skeletal To Camera");
        setResizable(false);

        jointLabel.setText("Transform joint:");

        joint.setMaximumRowCount(30);

        camTgtLabel.setText("Target camera name:");

        camTgtBtnGroup.add(btnCamNameMatchJoint);
        btnCamNameMatchJoint.setSelected(true);
        btnCamNameMatchJoint.setText("Same as transform joint");
        btnCamNameMatchJoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCamNameMatchJointActionPerformed(evt);
            }
        });

        camTgtBtnGroup.add(btnCamNameCustom);
        btnCamNameCustom.setText("Custom");
        btnCamNameCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCamNameCustomActionPerformed(evt);
            }
        });

        btnCamNameCustomField.setEnabled(false);

        btnConvert.setText("Convert");
        btnConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        convModeLabel.setText("Conversion mode:");

        convModeGroup.add(btnConvLocal);
        btnConvLocal.setSelected(true);
        btnConvLocal.setText("Local (direct)");
        btnConvLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvLocalActionPerformed(evt);
            }
        });

        convModeGroup.add(btnConvGlobal);
        btnConvGlobal.setText("Global (baked)");
        btnConvGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvGlobalActionPerformed(evt);
            }
        });

        optionsLabel.setText("Options");

        btnRotateRootXFixup.setText("Rotate root X +Pi/2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConvert))
                    .addComponent(joint, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnRotateRootXFixup))
                            .addComponent(jointLabel)
                            .addComponent(convModeLabel)
                            .addComponent(camTgtLabel)
                            .addComponent(optionsLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(bakeSkeletonSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCamNameCustom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCamNameCustomField))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnConvGlobal)
                                    .addComponent(btnConvLocal)
                                    .addComponent(btnCamNameMatchJoint))
                                .addGap(0, 111, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jointLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(joint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(convModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConvLocal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConvGlobal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bakeSkeletonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(camTgtLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCamNameMatchJoint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCamNameCustom)
                    .addComponent(btnCamNameCustomField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(optionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRotateRootXFixup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConvert)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
		dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertActionPerformed
		confirmed = true;
		transformJointName = (String)joint.getSelectedItem();
		refModel = bakeSkeletonSelect.getSelectedModel();
		if (btnConvLocal.isSelected()){
			refModel = null;
		}
		String camTgt = btnCamNameCustom.isSelected() ? btnCamNameCustomField.getText() : transformJointName;
		if (camTgt == null || camTgt.isEmpty()){
			DialogUtils.showErrorMessage(this, "Input error", "Camera name can not be empty");
			return;
		}
		camTgtName = camTgt;
		rootRotAdjustment = btnRotateRootXFixup.isSelected() ? MathEx.HALF_PI : 0f;
		dispose();
    }//GEN-LAST:event_btnConvertActionPerformed

    private void btnCamNameMatchJointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamNameMatchJointActionPerformed
		btnCamNameCustomField.setEnabled(false);
    }//GEN-LAST:event_btnCamNameMatchJointActionPerformed

    private void btnCamNameCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamNameCustomActionPerformed
		btnCamNameCustomField.setEnabled(true);
    }//GEN-LAST:event_btnCamNameCustomActionPerformed

    private void btnConvLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvLocalActionPerformed
		bakeSkeletonSelect.setModelBoxEnabled(false);
    }//GEN-LAST:event_btnConvLocalActionPerformed

    private void btnConvGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvGlobalActionPerformed
		if (!bakeSkeletonSelect.hasAnyModel()){
			DialogUtils.showErrorMessage("No model", "Can not bake global transforms without a model in the scene.");
			btnConvLocal.setSelected(true);
		}
		else {
			bakeSkeletonSelect.setModelBoxEnabled(true);
		}
    }//GEN-LAST:event_btnConvGlobalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ctrmap.creativestudio.dialogs.ModelSelectionPanel bakeSkeletonSelect;
    private javax.swing.JRadioButton btnCamNameCustom;
    private javax.swing.JTextField btnCamNameCustomField;
    private javax.swing.JRadioButton btnCamNameMatchJoint;
    private javax.swing.JButton btnCancel;
    private javax.swing.JRadioButton btnConvGlobal;
    private javax.swing.JRadioButton btnConvLocal;
    private javax.swing.JButton btnConvert;
    private javax.swing.JCheckBox btnRotateRootXFixup;
    private javax.swing.ButtonGroup camTgtBtnGroup;
    private javax.swing.JLabel camTgtLabel;
    private javax.swing.ButtonGroup convModeGroup;
    private javax.swing.JLabel convModeLabel;
    private javax.swing.JComboBox<String> joint;
    private javax.swing.JLabel jointLabel;
    private javax.swing.JLabel optionsLabel;
    // End of variables declaration//GEN-END:variables
}
