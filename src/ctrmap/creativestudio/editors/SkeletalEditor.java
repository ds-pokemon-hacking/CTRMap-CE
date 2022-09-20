package ctrmap.creativestudio.editors;

import ctrmap.creativestudio.dialogs.Skel2CamDialog;
import ctrmap.creativestudio.dialogs.Skel2CamDialogLookat;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.tree.CSNode;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.AnimeProcessor;
import ctrmap.renderer.util.camcvt.Skel2CamLookat;
import xstandard.gui.components.ComponentUtils;

public class SkeletalEditor extends javax.swing.JPanel implements IEditor {

	/**
	 * Creates new form SkeletalEditor
	 */
	public SkeletalEditor() {
		initComponents();

		ComponentUtils.setNFValueClass(Float.class, tempo, scale);
	}

	private CSNode node;
	private SkeletalAnimation anime;

	@Override
	public void handleObject(Object o) {
		reload();
		if (IEditor.checkIsCompatibleNG(o, SkeletalAnimation.class)) {
			node = (CSNode) o;
			anime = (SkeletalAnimation) node.getContent();
			generalEditor.loadAnime(anime, node);
		} else {
			node = null;
			anime = null;
			generalEditor.loadAnime(null, node);
		}
	}

	@Override
	public void save() {
		//Nothing else. We want the user to explicitly carry out the irreversible operations with "Apply".
	}

	public void reload() {
		tempo.setValue(1f);
		scale.setValue(1f);
		optResult.setText("--");
		generalEditor.loadAnime(null, node);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tempoPanel = new javax.swing.JPanel();
        tempo = new javax.swing.JFormattedTextField();
        btnApplyTempo = new javax.swing.JButton();
        scalePanel = new javax.swing.JPanel();
        scale = new javax.swing.JFormattedTextField();
        btnApplyScale = new javax.swing.JButton();
        optPanel = new javax.swing.JPanel();
        btnOptKeyframes = new javax.swing.JButton();
        optResult = new javax.swing.JLabel();
        btnWholeFrames = new javax.swing.JButton();
        btnOptSkeletalRotations = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnSkel2Cam = new javax.swing.JButton();
        btnSkel2CamLookat = new javax.swing.JButton();
        btnRemoveIK = new javax.swing.JButton();
        generalEditor = new ctrmap.creativestudio.editors.AnimeGeneralEditor();

        tempoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tempo"));

        tempo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        btnApplyTempo.setText("Apply");
        btnApplyTempo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyTempoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tempoPanelLayout = new javax.swing.GroupLayout(tempoPanel);
        tempoPanel.setLayout(tempoPanelLayout);
        tempoPanelLayout.setHorizontalGroup(
            tempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tempoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tempo)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tempoPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnApplyTempo)))
                .addContainerGap())
        );
        tempoPanelLayout.setVerticalGroup(
            tempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tempoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnApplyTempo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scalePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Animation skeleton scale"));

        scale.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        btnApplyScale.setText("Apply");
        btnApplyScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyScaleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout scalePanelLayout = new javax.swing.GroupLayout(scalePanel);
        scalePanel.setLayout(scalePanelLayout);
        scalePanelLayout.setHorizontalGroup(
            scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scale)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scalePanelLayout.createSequentialGroup()
                        .addGap(0, 209, Short.MAX_VALUE)
                        .addComponent(btnApplyScale)))
                .addContainerGap())
        );
        scalePanelLayout.setVerticalGroup(
            scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnApplyScale)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Optimization"));

        btnOptKeyframes.setText("Remove redundant keyframes");
        btnOptKeyframes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptKeyframesActionPerformed(evt);
            }
        });

        optResult.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        optResult.setText("--");

        btnWholeFrames.setText("Make keyframes integers");
        btnWholeFrames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWholeFramesActionPerformed(evt);
            }
        });

        btnOptSkeletalRotations.setText("Linearize rotations");
        btnOptSkeletalRotations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptSkeletalRotationsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optPanelLayout = new javax.swing.GroupLayout(optPanel);
        optPanel.setLayout(optPanelLayout);
        optPanelLayout.setHorizontalGroup(
            optPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optPanelLayout.createSequentialGroup()
                .addGroup(optPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, optPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnOptSkeletalRotations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(optPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnOptKeyframes, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                    .addComponent(optResult, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(optPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnWholeFrames, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 58, Short.MAX_VALUE))
        );
        optPanelLayout.setVerticalGroup(
            optPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnOptKeyframes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optResult)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnWholeFrames)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOptSkeletalRotations)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tools"));

        btnSkel2Cam.setText("Create Camera animation (Viewpoint)");
        btnSkel2Cam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkel2CamActionPerformed(evt);
            }
        });

        btnSkel2CamLookat.setText("Create Camera animation (LookAt)");
        btnSkel2CamLookat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSkel2CamLookatActionPerformed(evt);
            }
        });

        btnRemoveIK.setText("Strip IKs");
        btnRemoveIK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveIKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSkel2Cam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSkel2CamLookat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemoveIK, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSkel2Cam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSkel2CamLookat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoveIK)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tempoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scalePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(optPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(generalEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalEditor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tempoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scalePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnApplyTempoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyTempoActionPerformed
		if (anime != null) {
			float v = 1f / (Float) tempo.getValue();
			if (v != 0) {
				AnimeProcessor.applyTempoScale(anime, v);
			}
			tempo.setValue(1f);
			generalEditor.loadAnime(anime, node);
		}
    }//GEN-LAST:event_btnApplyTempoActionPerformed

    private void btnApplyScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyScaleActionPerformed
		if (anime != null) {
			float v = (Float) scale.getValue();
			if (v != 0) {
				AnimeProcessor.scaleSklAnimeTra(anime, v);
			}
			scale.setValue(1f);
		}
    }//GEN-LAST:event_btnApplyScaleActionPerformed

    private void btnOptKeyframesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptKeyframesActionPerformed
		if (anime != null) {
			int rsl = AnimeProcessor.optimizeSkaKeyframes(anime);
			optResult.setText("Optimized out " + rsl + " keyframes!");
		}
    }//GEN-LAST:event_btnOptKeyframesActionPerformed

    private void btnWholeFramesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWholeFramesActionPerformed
		if (anime != null) {
			AnimeProcessor.makeAnimeWholeFrames(anime);
		}
    }//GEN-LAST:event_btnWholeFramesActionPerformed

    private void btnSkel2CamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkel2CamActionPerformed
		if (anime != null) {
			NGCS cs = node.getCS();
			if (Skel2CamDialog.checkApplicableOpenDialog(cs, anime)) {
				Skel2CamDialog dlg = new Skel2CamDialog(cs, true, anime, cs.getModels());
				dlg.setVisible(true);

				if (dlg.getConfirmed()) {
					CameraAnimation camAnm = AnimeProcessor.skeletalToCamera(
						anime,
						dlg.getResultJointName(),
						dlg.getResultCamName(),
						dlg.getRefModel() != null ? dlg.getRefModel().skeleton : null,
						true,
						dlg.getRootRotAdjustment()
					);

					cs.importResource(new G3DResource(camAnm));
				}
			}
		}
    }//GEN-LAST:event_btnSkel2CamActionPerformed

    private void btnOptSkeletalRotationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptSkeletalRotationsActionPerformed
		if (anime != null) {
			AnimeProcessor.optimizeSkeletalAnimation(anime, true);
		}
    }//GEN-LAST:event_btnOptSkeletalRotationsActionPerformed

    private void btnSkel2CamLookatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSkel2CamLookatActionPerformed
		if (anime != null) {
			NGCS cs = node.getCS();
			if (Skel2CamDialog.checkApplicableOpenDialog(cs, anime)) {
				Skel2CamDialogLookat dlg = new Skel2CamDialogLookat(cs, true, anime, cs.getModels());
				dlg.setVisible(true);

				if (dlg.getConfirmed()) {
					Skel2CamLookat.Skel2CamLookatInput in = new Skel2CamLookat.Skel2CamLookatInput();

					in.anm = anime;
					in.refModel = dlg.getRefModel();
					in.bakeModeGlobal = in.refModel != null;
					in.posSrc = dlg.getCamposSource();
					in.tgtSrc = dlg.getCamtgtSource();
					in.rollSrc = dlg.getCamrollSource();
					in.cameraName = dlg.getResultCamName();

					CameraAnimation camAnm = Skel2CamLookat.convertSkeletalAnimation(in);

					cs.importResource(new G3DResource(camAnm));
				}
			}
		}
    }//GEN-LAST:event_btnSkel2CamLookatActionPerformed

    private void btnRemoveIKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveIKActionPerformed
		if (anime != null) {
			AnimeProcessor.stripIKs(anime);
		}
    }//GEN-LAST:event_btnRemoveIKActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyScale;
    private javax.swing.JButton btnApplyTempo;
    private javax.swing.JButton btnOptKeyframes;
    private javax.swing.JButton btnOptSkeletalRotations;
    private javax.swing.JButton btnRemoveIK;
    private javax.swing.JButton btnSkel2Cam;
    private javax.swing.JButton btnSkel2CamLookat;
    private javax.swing.JButton btnWholeFrames;
    private ctrmap.creativestudio.editors.AnimeGeneralEditor generalEditor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel optPanel;
    private javax.swing.JLabel optResult;
    private javax.swing.JFormattedTextField scale;
    private javax.swing.JPanel scalePanel;
    private javax.swing.JFormattedTextField tempo;
    private javax.swing.JPanel tempoPanel;
    // End of variables declaration//GEN-END:variables
}
