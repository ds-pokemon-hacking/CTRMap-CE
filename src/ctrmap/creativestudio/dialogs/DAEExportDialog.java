package ctrmap.creativestudio.dialogs;

import ctrmap.formats.generic.collada.DAEExportSettings;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Model;
import xstandard.gui.DialogOptionRemember;
import xstandard.gui.components.ComponentUtils;
import xstandard.util.ArraysEx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DAEExportDialog extends javax.swing.JDialog {

	private CSDAEExportSettings result = null;

	private final List[] anmLists;

	public DAEExportDialog(java.awt.Frame parent, boolean modal, List<Model> models, List<SkeletalAnimation> anmS, List<CameraAnimation> anmC, List<VisibilityAnimation> anmV) {
		super(parent, modal);
		anmLists = new List[]{
			anmS,
			anmC,
			anmV
		};
		initComponents();
		setLocationRelativeTo(parent);

		DialogOptionRemember.selectRememberedComboBox("DAEExportAnmTypeBox", anmTypeBox);
		DialogOptionRemember.setRememberedCheckbox(btnIsExportMdl);
		DialogOptionRemember.setRememberedCheckbox(btnIsExportTex);
		DialogOptionRemember.setRememberedCheckbox(btnIsExportAnm);
		DialogOptionRemember.setRememberedCheckbox(btnIsExportCam);
		DialogOptionRemember.setRememberedCheckbox(btnIsExportLight);
		DialogOptionRemember.setRememberedCheckbox(btnSeparateTex);
		DialogOptionRemember.setRememberedCheckbox(btnAnmExportAllSepDir);
		DialogOptionRemember.setRememberedCheckbox(btnBakeTransforms);
		DialogOptionRemember.setRememberedCheckbox(btnTexExportMappedOnly);
		DialogOptionRemember.setRememberedCheckbox(btnNoUseLookAt);
		DialogOptionRemember.setRememberedCheckbox(btnBlenderMorphs);
		DialogOptionRemember.selectRememberedRBtnPos(mdlExportTypeGroup);
		DialogOptionRemember.selectRememberedRBtnPos(anmExportTypeGroup);

		btnMdlExportOne.setSelected(btnMdlExportOne.isSelected() && !models.isEmpty());

		setTexUI();
		setMdlUI();
		setAnmUI();
		mdlSelect.loadModels(models);
		loadAnms();

		mdlSelect.restoreComboBoxValue("DAESingleModelName");
		anmSelect.restoreComboBoxValue("DAESingleAnimeName");

		ActionListener mdlUIListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMdlUI();
			}
		};
		ActionListener anmUIListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setAnmUI();
			}
		};

		btnMdlExportAllAsOne.addActionListener(mdlUIListener);
		btnMdlExportOne.addActionListener(mdlUIListener);
		btnAnmExportAllMoreFiles.addActionListener(anmUIListener);
		btnAnmExportOne.addActionListener(anmUIListener);
		btnIsExportMdl.addActionListener(mdlUIListener);
		btnIsExportAnm.addActionListener(anmUIListener);
	}

	public CSDAEExportSettings getResult() {
		return result;
	}

	private void loadAnms() {
		List l = anmLists[anmTypeBox.getSelectedIndex()];
		anmSelect.loadAnimations(l);
	}

	private void setTexUI() {
		btnSeparateTex.setEnabled(btnIsExportTex.isSelected());
		btnTexExportMappedOnly.setEnabled(btnIsExportMdl.isSelected() && btnIsExportTex.isSelected());
	}

	private void setMdlUI() {
		boolean exportMdl = btnIsExportMdl.isSelected();
		mdlSelect.setModelBoxEnabled(btnMdlExportOne.isSelected() && exportMdl);
		btnTexExportMappedOnly.setEnabled(exportMdl && btnIsExportTex.isSelected());
		ComponentUtils.setComponentsEnabled(exportMdl, btnMdlExportOne, btnMdlExportAllAsOne);
	}

	private void setAnmUI() {
		boolean exportAnm = btnIsExportAnm.isSelected();
		anmSelect.setAnmBoxEnabled(btnAnmExportOne.isSelected() && exportAnm);
		anmTypeBox.setEnabled(btnAnmExportOne.isSelected() && exportAnm);
		btnAnmExportAllSepDir.setEnabled(btnAnmExportAllMoreFiles.isSelected() && exportAnm);
		ComponentUtils.setComponentsEnabled(exportAnm, btnAnmExportAllMoreFiles, btnAnmExportAllOneFile, btnAnmExportOne);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mdlExportTypeGroup = new javax.swing.ButtonGroup();
        anmExportTypeGroup = new javax.swing.ButtonGroup();
        btnSeparateTex = new javax.swing.JCheckBox();
        btnExport = new javax.swing.JButton();
        btnIsExportTex = new javax.swing.JCheckBox();
        btnMdlExportAllAsOne = new javax.swing.JRadioButton();
        btnMdlExportOne = new javax.swing.JRadioButton();
        mdlSelect = new ctrmap.creativestudio.dialogs.ModelSelectionPanel();
        texSep = new javax.swing.JSeparator();
        anmSep = new javax.swing.JSeparator();
        btnAnmExportAllMoreFiles = new javax.swing.JRadioButton();
        btnAnmExportOne = new javax.swing.JRadioButton();
        anmTypeLabel = new javax.swing.JLabel();
        anmTypeBox = new javax.swing.JComboBox<>();
        anmSelect = new ctrmap.creativestudio.dialogs.AnimeSelectionPanel();
        btnAnmExportAllSepDir = new javax.swing.JCheckBox();
        btnIsExportMdl = new javax.swing.JCheckBox();
        btnIsExportAnm = new javax.swing.JCheckBox();
        camSep = new javax.swing.JSeparator();
        btnIsExportCam = new javax.swing.JCheckBox();
        lightSep = new javax.swing.JSeparator();
        btnIsExportLight = new javax.swing.JCheckBox();
        exportBtnSep = new javax.swing.JSeparator();
        btnAnmExportAllOneFile = new javax.swing.JRadioButton();
        btnTexExportMappedOnly = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        btnBakeTransforms = new javax.swing.JCheckBox();
        btnNoUseLookAt = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        btnBlenderMorphs = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export COLLADA");
        setResizable(false);

        btnSeparateTex.setSelected(true);
        btnSeparateTex.setText("Separate texture directory");

        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnIsExportTex.setSelected(true);
        btnIsExportTex.setText("Export textures");
        btnIsExportTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIsExportTexActionPerformed(evt);
            }
        });

        mdlExportTypeGroup.add(btnMdlExportAllAsOne);
        btnMdlExportAllAsOne.setSelected(true);
        btnMdlExportAllAsOne.setText("All models");

        mdlExportTypeGroup.add(btnMdlExportOne);
        btnMdlExportOne.setText("Single model");

        anmExportTypeGroup.add(btnAnmExportAllMoreFiles);
        btnAnmExportAllMoreFiles.setSelected(true);
        btnAnmExportAllMoreFiles.setText("All animations (separate files)");

        anmExportTypeGroup.add(btnAnmExportOne);
        btnAnmExportOne.setText("Single animation");

        anmTypeLabel.setText("Type:");

        anmTypeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Skeletal animation", "Camera animation", "Visibility animation" }));
        anmTypeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anmTypeBoxActionPerformed(evt);
            }
        });

        btnAnmExportAllSepDir.setSelected(true);
        btnAnmExportAllSepDir.setText("Separate animation directory");

        btnIsExportMdl.setSelected(true);
        btnIsExportMdl.setText("Export models");

        btnIsExportAnm.setSelected(true);
        btnIsExportAnm.setText("Export animations");

        btnIsExportCam.setSelected(true);
        btnIsExportCam.setText("Export cameras");

        btnIsExportLight.setSelected(true);
        btnIsExportLight.setText("Export lights");

        anmExportTypeGroup.add(btnAnmExportAllOneFile);
        btnAnmExportAllOneFile.setText("All animations (one file)");

        btnTexExportMappedOnly.setSelected(true);
        btnTexExportMappedOnly.setText("Mapped textures only");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Transform mode"));

        btnBakeTransforms.setSelected(true);
        btnBakeTransforms.setText("Bake transforms");

        btnNoUseLookAt.setSelected(true);
        btnNoUseLookAt.setText("Do not use look-at transform");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBakeTransforms)
                    .addComponent(btnNoUseLookAt))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBakeTransforms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNoUseLookAt)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Hacks"));

        btnBlenderMorphs.setText("Headless vertex morphs");
        btnBlenderMorphs.setToolTipText("<html> If checked, this will ignore vertex morphs by skin controllers.<br> This is AGAINST the COLLADA specification and will cause problems in most programs,<br> but is required to recognize vertex morphs in certain poorly designed software (note: Blender). </html>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBlenderMorphs)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBlenderMorphs)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnIsExportLight)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(exportBtnSep)
                            .addComponent(lightSep)
                            .addComponent(anmSep)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(anmSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(anmTypeLabel)
                                                .addGap(27, 27, 27)
                                                .addComponent(anmTypeBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btnAnmExportAllMoreFiles)
                                            .addComponent(btnAnmExportAllOneFile)
                                            .addComponent(btnAnmExportOne)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addComponent(btnAnmExportAllSepDir)))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addComponent(camSep, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(texSep)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(mdlSelect, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btnMdlExportOne)
                                            .addComponent(btnMdlExportAllAsOne))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnExport))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnIsExportAnm)
                                    .addComponent(btnIsExportCam)
                                    .addComponent(btnIsExportMdl)
                                    .addComponent(btnIsExportTex)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btnTexExportMappedOnly)
                                            .addComponent(btnSeparateTex))))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnIsExportMdl)
                .addGap(2, 2, 2)
                .addComponent(btnMdlExportAllAsOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMdlExportOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mdlSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(texSep, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIsExportTex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSeparateTex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTexExportMappedOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(anmSep, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIsExportAnm)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnmExportAllMoreFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnmExportAllSepDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnmExportAllOneFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnmExportOne)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(anmTypeLabel)
                    .addComponent(anmTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anmSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(camSep, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIsExportCam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lightSep, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIsExportLight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportBtnSep, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnExport)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
		DialogOptionRemember.putRememberedRBtnPos(mdlExportTypeGroup);
		DialogOptionRemember.putRememberedCheckbox(btnIsExportMdl);
		DialogOptionRemember.putRememberedCheckbox(btnIsExportTex);
		DialogOptionRemember.putRememberedCheckbox(btnSeparateTex);
		DialogOptionRemember.putRememberedCheckbox(btnIsExportAnm);
		DialogOptionRemember.putRememberedRBtnPos(anmExportTypeGroup);
		DialogOptionRemember.putRememberedCheckbox(btnAnmExportAllSepDir);
		DialogOptionRemember.putRememberedCheckbox(btnTexExportMappedOnly);
		DialogOptionRemember.putRememberedCheckbox(btnNoUseLookAt);
		DialogOptionRemember.putRememberedComboBox("DAEExportAnmTypeBox", anmTypeBox);
		DialogOptionRemember.putRememberedCheckbox(btnIsExportCam);
		DialogOptionRemember.putRememberedCheckbox(btnIsExportLight);
		DialogOptionRemember.putRememberedCheckbox(btnBakeTransforms);
		DialogOptionRemember.putRememberedCheckbox(btnBlenderMorphs);
		mdlSelect.saveComboBoxValue("DAESingleModelName");
		anmSelect.saveComboBoxValue("DAESingleAnimeName");

		result = new CSDAEExportSettings();
		result.exportMdl = btnIsExportMdl.isSelected();
		result.exportAnm = btnIsExportAnm.isSelected();
		result.exportTex = btnIsExportTex.isSelected();
		result.exportCam = btnIsExportCam.isSelected();
		result.exportLight = btnIsExportLight.isSelected();
		result.dirSepTex = btnSeparateTex.isSelected();
		result.dirSepAnm = btnAnmExportAllSepDir.isSelected();
		result.texMappedOnly = btnTexExportMappedOnly.isSelected() && result.exportMdl;
		result.bakeTransforms = btnBakeTransforms.isSelected();
		result.doNotUseLookAt = btnNoUseLookAt.isSelected();
		result.blenderMorphs = btnBlenderMorphs.isSelected();

		if (result.exportMdl) {
			if (btnMdlExportOne.isSelected()) {
				result.modelsToExport.add(mdlSelect.getSelectedModel());
			} else {
				result.modelsToExport.addAll(mdlSelect.getAllModels());
			}
		}

		if (result.exportAnm) {
			if (btnAnmExportOne.isSelected()) {
				AbstractAnimation a = anmSelect.getSelectedAnimation();
				ArraysEx.addIfNotNullOrContains(result.animeToExport, a);
			} else {
				for (List l : anmLists) {
					result.animeToExport.addAll(l);
				}
			}
		}

		dispose();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnIsExportTexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIsExportTexActionPerformed
		setTexUI();
    }//GEN-LAST:event_btnIsExportTexActionPerformed

    private void anmTypeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anmTypeBoxActionPerformed
		loadAnms();
    }//GEN-LAST:event_anmTypeBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup anmExportTypeGroup;
    private ctrmap.creativestudio.dialogs.AnimeSelectionPanel anmSelect;
    private javax.swing.JSeparator anmSep;
    private javax.swing.JComboBox<String> anmTypeBox;
    private javax.swing.JLabel anmTypeLabel;
    private javax.swing.JRadioButton btnAnmExportAllMoreFiles;
    private javax.swing.JRadioButton btnAnmExportAllOneFile;
    private javax.swing.JCheckBox btnAnmExportAllSepDir;
    private javax.swing.JRadioButton btnAnmExportOne;
    private javax.swing.JCheckBox btnBakeTransforms;
    private javax.swing.JCheckBox btnBlenderMorphs;
    private javax.swing.JButton btnExport;
    private javax.swing.JCheckBox btnIsExportAnm;
    private javax.swing.JCheckBox btnIsExportCam;
    private javax.swing.JCheckBox btnIsExportLight;
    private javax.swing.JCheckBox btnIsExportMdl;
    private javax.swing.JCheckBox btnIsExportTex;
    private javax.swing.JRadioButton btnMdlExportAllAsOne;
    private javax.swing.JRadioButton btnMdlExportOne;
    private javax.swing.JCheckBox btnNoUseLookAt;
    private javax.swing.JCheckBox btnSeparateTex;
    private javax.swing.JCheckBox btnTexExportMappedOnly;
    private javax.swing.JSeparator camSep;
    private javax.swing.JSeparator exportBtnSep;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator lightSep;
    private javax.swing.ButtonGroup mdlExportTypeGroup;
    private ctrmap.creativestudio.dialogs.ModelSelectionPanel mdlSelect;
    private javax.swing.JSeparator texSep;
    // End of variables declaration//GEN-END:variables

	public static class CSDAEExportSettings extends DAEExportSettings {

		public boolean exportMdl;
		public boolean exportTex;
		public boolean exportAnm;
		public boolean exportCam;
		public boolean exportLight;

		public List<Model> modelsToExport = new ArrayList<>();
		public List<AbstractAnimation> animeToExport = new ArrayList<>();

		public boolean animeToSeparateFiles;

		public boolean texMappedOnly;

		public boolean dirSepAnm;
		public boolean dirSepTex;
	}
}
