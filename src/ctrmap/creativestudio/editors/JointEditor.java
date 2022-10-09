package ctrmap.creativestudio.editors;

import ctrmap.creativestudio.ngcs.CSG3DSurface;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.tree.JointNode;
import ctrmap.creativestudio.ngcs.tree.ModelNode;
import ctrmap.renderer.backends.base.shaderengine.ShaderUniform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.util.generators.SkeletonModelGenerator;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.combobox.ComboBoxExInternal;
import xstandard.util.ListenableList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JointEditor extends javax.swing.JPanel implements ISceneBound, IEditor {

	private NGCS cs;

	private final ListenableList.ElementChangeListener skelChangeListener = new ListenableList.ElementChangeListener() {
		@Override
		public void onEntityChange(ListenableList.ElementChangeEvent evt) {
			save();
		}
	};

	/**
	 * Creates new form JointEditor
	 */
	public JointEditor(NGCS cs) {
		initComponents();

		this.cs = cs;
		jointParent.addListener(new ComboBoxExInternal.ComboBoxListener() {
			@Override
			public void itemSelected(Object selectedItem) {
				if (loaded) {
					checkSaveParent();
				}
			}
		});

		ActionListener saveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		};

		cs.getRenderer().getProgramManager().addExtraUniform(highLightJoint);

		tra.addActionListener(saveListener);
		rot.addActionListener(saveListener);
		sca.addActionListener(saveListener);
		
		nameField.addListener((oldName, newName) -> {
			if (lastSkeleton != null) {
				for (Joint child : lastSkeleton.getJoints()) {
					if (Objects.equals(child.parentName, oldName)) {
						child.parentName = newName;
					}
				}
			}
		});
	}

	private Joint joint;
	private JointNode node;
	private boolean loaded = false;

	private Skeleton lastSkeleton;
	public G3DResourceInstance skeletonModel = new G3DResourceInstance();

	private ShaderUniform highLightJoint = new ShaderUniform("highLightBoneNum", -1);

	@Override
	public void handleObject(Object j) {
		loaded = false;
		if (IEditor.checkIsCompatibleNG(j, Joint.class)) {
			node = (JointNode) j;
			node.getCS().showLoadedModel(node.descend(ModelNode.class).getContent());
			joint = (Joint) node.getContent();
			CSG3DSurface renderer = cs.getRenderer();

			if (joint.parentSkeleton != lastSkeleton) {
				if (lastSkeleton != null) {
					lastSkeleton.getJoints().removeListener(skelChangeListener);
				}
				lastSkeleton = joint.parentSkeleton;
				lastSkeleton.getJoints().addListener(skelChangeListener);
				resetSkeletonModel();
			}
			skeletonModel.setVisible(true);
			cs.getScene().addChild(skeletonModel);

			highLightJoint.intValue = joint.getIndex();

			reloadParentDropdown(joint.parentSkeleton, joint);
			showParent();
			tra.loadVec(joint.position);
			rot.loadVec(joint.rotation);
			sca.loadVec(joint.scale);
			ikRole.setSelectedIndex(joint.kinematicsRole.ordinal());
			bbX.setSelected(joint.isBBX());
			bbY.setSelected(joint.isBBY());
			bbZ.setSelected(joint.isBBZ());
			bbAim.setSelected(joint.isBBAim());
			btnCompensateScale.setSelected(joint.isScaleCompensate());
		} else {
			skeletonModel.setVisible(false);
			if (lastSkeleton != null) {
				lastSkeleton.getJoints().removeListener(skelChangeListener);
			}
			lastSkeleton = null;
			highLightJoint.intValue = -1;
			joint = null;
			tra.loadVec(null);
			rot.loadVec(null);
			sca.loadVec(null);
			jointParent.removeAllItems();
			ikRole.setSelectedIndex(0);
			ComponentUtils.clearComponents(bbAim, bbX, bbY, bbZ, btnCompensateScale);
		}
		nameField.loadNode(node);
		loaded = true;
	}

	public void resetSkeletonModel() {
		skeletonModel.setResource(new G3DResource(SkeletonModelGenerator.generateSkeletonModel(lastSkeleton)));
	}

	public void updateSkeletonModel() {
		SkeletonModelGenerator.updateSkeletonModel(skeletonModel.resource);
	}

	@Override
	public void save() {
		if (joint != null) {
			joint.parentSkeleton.buildTransforms();
			if (cs.currentModel != null) {
				updateSkeletonModel();
			} else {
				skeletonModel.setResource(null);
			}
		}
	}

	private void reloadParentDropdown(Skeleton skl, Joint jnt) {
		List<String> values = new ArrayList<>();
		values.add("<none>");
		for (Joint j : skl.getJoints()) {
			if (j != jnt) {
				values.add(j.getName());
			}
		}
		jointParent.loadValues(values);
	}

	private void checkSaveParent() {
		if (joint != null) {
			Joint newParent = joint.parentSkeleton.getJoint((String) jointParent.getSelectedItem());
			if (newParent != joint) {
				joint.parentName = newParent == null ? null : newParent.name;
				node.changeParentToJoint(newParent);
			}
			showParent();
		}
	}

	private void showParent() {
		if (joint.parentName != null) {
			int index = joint.parentSkeleton.getJointIndex(joint.parentName);
			if (index > joint.parentSkeleton.getJointIndex(joint.name)) {
				index--;
			}
			jointParent.setSelectedIndex(index + 1);
		}
		else {
			jointParent.setSelectedIndex(0);
		}
	}

	@Override
	public void onSceneUpdate() {
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generalPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        parentLabel = new javax.swing.JLabel();
        nameField = new ctrmap.creativestudio.editors.NameTextField();
        jointParent = new xstandard.gui.components.combobox.ACComboBox();
        transformPanel = new javax.swing.JPanel();
        tra = new xstandard.gui.components.Vec3fEditor();
        traLabel = new javax.swing.JLabel();
        rotLabel = new javax.swing.JLabel();
        rot = new xstandard.gui.components.Vec3fEditor();
        scaLabel = new javax.swing.JLabel();
        sca = new xstandard.gui.components.Vec3fEditor();
        jPanel3 = new javax.swing.JPanel();
        ikRoleLabel2 = new javax.swing.JLabel();
        ikRole = new javax.swing.JComboBox<>();
        billboardModeLabel = new javax.swing.JLabel();
        bbX = new javax.swing.JCheckBox();
        bbY = new javax.swing.JCheckBox();
        bbZ = new javax.swing.JCheckBox();
        bbAim = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        btnCompensateScale = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(350, 357));

        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        nameLabel.setText("Name");

        parentLabel.setText("Parent");

        jointParent.setUseEqualsAnyway(true);

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(parentLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jointParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jointParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        transformPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transform"));

        traLabel.setText("Translation:");

        rotLabel.setText("Rotation:");

        scaLabel.setText("Scale:");

        javax.swing.GroupLayout transformPanelLayout = new javax.swing.GroupLayout(transformPanel);
        transformPanel.setLayout(transformPanelLayout);
        transformPanelLayout.setHorizontalGroup(
            transformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(transformPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(traLabel)
                    .addComponent(tra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotLabel)
                    .addComponent(rot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaLabel)
                    .addComponent(sca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        transformPanelLayout.setVerticalGroup(
            transformPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(transformPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(traLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Other"));

        ikRoleLabel2.setText("IK role");

        ikRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Effector", "Joint", "Chain" }));
        ikRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ikRoleActionPerformed(evt);
            }
        });

        billboardModeLabel.setText("Billboard:");

        bbX.setForeground(new java.awt.Color(255, 0, 0));
        bbX.setText("X");
        bbX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbXActionPerformed(evt);
            }
        });

        bbY.setForeground(new java.awt.Color(0, 153, 0));
        bbY.setText("Y");
        bbY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbYActionPerformed(evt);
            }
        });

        bbZ.setForeground(new java.awt.Color(0, 0, 255));
        bbZ.setText("Z");
        bbZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbZActionPerformed(evt);
            }
        });

        bbAim.setText("Aim");
        bbAim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbAimActionPerformed(evt);
            }
        });

        jLabel1.setText("Misc.");

        btnCompensateScale.setText("Compensate parent scale");
        btnCompensateScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompensateScaleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(bbX)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bbZ)
                        .addGap(27, 27, 27)
                        .addComponent(bbAim))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(ikRoleLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ikRole, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(billboardModeLabel)
                    .addComponent(jLabel1)
                    .addComponent(btnCompensateScale))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ikRoleLabel2)
                    .addComponent(ikRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(billboardModeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbX)
                    .addComponent(bbY)
                    .addComponent(bbZ)
                    .addComponent(bbAim))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCompensateScale)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transformPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transformPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ikRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ikRoleActionPerformed
		if (joint != null) {
			joint.kinematicsRole = Skeleton.KinematicsRole.values()[ikRole.getSelectedIndex()];
		}
    }//GEN-LAST:event_ikRoleActionPerformed

    private void bbXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbXActionPerformed
		if (joint != null) {
			joint.setBBX(bbX.isSelected());
		}
    }//GEN-LAST:event_bbXActionPerformed

    private void bbYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbYActionPerformed
		if (joint != null) {
			joint.setBBY(bbY.isSelected());
		}
    }//GEN-LAST:event_bbYActionPerformed

    private void bbZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbZActionPerformed
		if (joint != null) {
			joint.setBBZ(bbZ.isSelected());
		}
    }//GEN-LAST:event_bbZActionPerformed

    private void bbAimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbAimActionPerformed
		if (joint != null) {
			joint.setBBAim(bbAim.isSelected());
		}
    }//GEN-LAST:event_bbAimActionPerformed

    private void btnCompensateScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompensateScaleActionPerformed
		if (joint != null) {
			joint.setScaleCompensate(btnCompensateScale.isSelected());
			save();
		}
    }//GEN-LAST:event_btnCompensateScaleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox bbAim;
    private javax.swing.JCheckBox bbX;
    private javax.swing.JCheckBox bbY;
    private javax.swing.JCheckBox bbZ;
    private javax.swing.JLabel billboardModeLabel;
    private javax.swing.JCheckBox btnCompensateScale;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JComboBox<String> ikRole;
    private javax.swing.JLabel ikRoleLabel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private xstandard.gui.components.combobox.ACComboBox jointParent;
    private ctrmap.creativestudio.editors.NameTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel parentLabel;
    private xstandard.gui.components.Vec3fEditor rot;
    private javax.swing.JLabel rotLabel;
    private xstandard.gui.components.Vec3fEditor sca;
    private javax.swing.JLabel scaLabel;
    private xstandard.gui.components.Vec3fEditor tra;
    private javax.swing.JLabel traLabel;
    private javax.swing.JPanel transformPanel;
    // End of variables declaration//GEN-END:variables
}
