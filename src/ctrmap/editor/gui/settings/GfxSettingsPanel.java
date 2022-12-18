package ctrmap.editor.gui.settings;

import ctrmap.CTRMapResources;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.missioncontrol_base.IMissionControl;
import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.RenderSettings;
import xstandard.formats.msgtxt.MsgTxt;
import xstandard.gui.components.ComponentUtils;

public class GfxSettingsPanel extends javax.swing.JPanel implements SettingsPanel {

	/**
	 * Creates new form GfxSettingsPanel
	 */
	private static final MsgTxt gfxSettings_res = new MsgTxt(CTRMapResources.ACCESSOR.getStream("message/renderer_settings_res.msgtxt"));

	private static final String DESC_RENDERER = "renderer_desc";
	private static final String DESC_CLIP = "clipping_desc";
	private static final String DESC_FPS = "framecap_desc";
	private static final String DESC_FOV = "fov_desc";
	private static final String DESC_ANIME_SPEED = "anime_speed_desc";
	private static final String DESC_ANIME_OPT = "anime_interpolate_opt_desc";
	private static final String DESC_ANIME_30FPS_M = "anime_interpolate_m_desc";
	private static final String DESC_ANIME_30FPS_S = "anime_interpolate_s_desc";
	private static final String DESC_TEXTURE_FORMATS = "native_texture_formats_desc";
	private static final String DESC_SHADER_GC = "shader_gc_desc";
	private static final String DESC_BFC = "back_face_culling_desc";

	private SettingsForm parent;

	public GfxSettingsPanel() {
		initComponents();
		ComponentUtils.setNFValueClass(Float.class, fov, zNear, zFar, animeSpeed);
		ComponentUtils.setNFValueClass(Integer.class, fpsCap);

		renderer.setSelectedIndex(getIndexForRendererString());
		zNear.setValue(RenderSettings.Defaults.Z_NEAR);
		zFar.setValue(RenderSettings.Defaults.Z_FAR);
		fpsCap.setValue(RenderSettings.Defaults.FRAMERATE_CAP);
		fov.setValue(RenderSettings.Defaults.FOV);
		animeSpeed.setValue(RenderSettings.Defaults.FRAME_SCALE);
		optimizeIntp.setSelected(RenderSettings.Defaults.ANIMATION_OPTIMIZE);
		interpolateM.setSelected(!RenderSettings.Defaults.ANIMATION_USE_30FPS_MAT);
		interpolateS.setSelected(!RenderSettings.Defaults.ANIMATION_USE_30FPS_SKL);
		useNativeTextureFormats.setSelected(RenderSettings.Defaults.USE_NATIVE_TEXTURE_FORMATS);
		enableShaderGC.setSelected(RenderSettings.Defaults.ENABLE_SHADER_GC);
		btnBFC.setSelected(!RenderSettings.Defaults.BACKFACE_CULLING);
	}

	@Override
	public void attachParent(SettingsForm parent) {
		this.parent = parent;
	}

	public static int getIndexForRendererString() {
		switch (RenderSettings.Defaults.BACKEND_DEFAULT) {
			case RenderConstants.BACKEND_OGL2_HOUSTON:
				return 0;
			case RenderConstants.BACKEND_OGL4_HOUSTON:
				return 2;
			case RenderConstants.BACKEND_OGL2_HOUSTON_UBER:
				return 1;
		}
		return 0;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rendererLabel = new javax.swing.JLabel();
        renderer = new javax.swing.JComboBox<>();
        rendererSettingsPanel = new javax.swing.JPanel();
        clippingLabel = new javax.swing.JLabel();
        nearClipLabel = new javax.swing.JLabel();
        farClipLabel = new javax.swing.JLabel();
        zNear = new javax.swing.JFormattedTextField();
        zFar = new javax.swing.JFormattedTextField();
        fpsCapLabel = new javax.swing.JLabel();
        fpsCap = new javax.swing.JFormattedTextField();
        fpsCapUnitLabel = new javax.swing.JLabel();
        fovLabel = new javax.swing.JLabel();
        fov = new javax.swing.JFormattedTextField();
        fovUnitLabel = new javax.swing.JLabel();
        viewportSettingsLabel = new javax.swing.JLabel();
        hwSettingsLabel = new javax.swing.JLabel();
        useNativeTextureFormats = new javax.swing.JCheckBox();
        btnBFC = new javax.swing.JCheckBox();
        enableShaderGC = new javax.swing.JCheckBox();
        animeSettingsPanel = new javax.swing.JPanel();
        animeSpeedLabel = new javax.swing.JLabel();
        animeSpeed = new javax.swing.JFormattedTextField();
        animeSpeedUnitLabel = new javax.swing.JLabel();
        interpolationLabel = new javax.swing.JLabel();
        interpolateM = new javax.swing.JCheckBox();
        interpolateS = new javax.swing.JCheckBox();
        optimizeIntp = new javax.swing.JCheckBox();
        btnSave = new javax.swing.JButton();

        rendererLabel.setText("Rendering engine:");

        renderer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Houston (GL2)", "Houston (GL2/Ubershaders)", "Houston (GL4)" }));
        renderer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rendererMouseEntered(evt);
            }
        });

        rendererSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Common renderer properties"));

        clippingLabel.setText("Clipping");

        nearClipLabel.setText("Near:");

        farClipLabel.setText("Far:");

        zNear.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        zNear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zNearMouseEntered(evt);
            }
        });

        zFar.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        zFar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                zFarMouseEntered(evt);
            }
        });

        fpsCapLabel.setText("Framelimit:");

        fpsCap.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        fpsCap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fpsCapMouseEntered(evt);
            }
        });

        fpsCapUnitLabel.setText("FPS");

        fovLabel.setText("FOV:");

        fov.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        fov.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fovMouseEntered(evt);
            }
        });

        fovUnitLabel.setText("°");

        viewportSettingsLabel.setText("Viewport");

        hwSettingsLabel.setText("Hardware utilization");

        useNativeTextureFormats.setText("Use native texture formats");
        useNativeTextureFormats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                useNativeTextureFormatsMouseEntered(evt);
            }
        });

        btnBFC.setText("Disable backface culling");
        btnBFC.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnBFCMouseEntered(evt);
            }
        });

        enableShaderGC.setText("Shader garbage collection");
        enableShaderGC.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                enableShaderGCMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout rendererSettingsPanelLayout = new javax.swing.GroupLayout(rendererSettingsPanel);
        rendererSettingsPanel.setLayout(rendererSettingsPanelLayout);
        rendererSettingsPanelLayout.setHorizontalGroup(
            rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(farClipLabel)
                                    .addComponent(nearClipLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(zFar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(zNear, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(clippingLabel))
                        .addGap(50, 50, 50)
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewportSettingsLabel)
                            .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fpsCapLabel)
                                    .addComponent(fovLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(fov, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fovUnitLabel))
                                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(fpsCap, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fpsCapUnitLabel))))))
                    .addComponent(hwSettingsLabel)
                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableShaderGC)
                            .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                                .addComponent(useNativeTextureFormats)
                                .addGap(18, 18, 18)
                                .addComponent(btnBFC)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        rendererSettingsPanelLayout.setVerticalGroup(
            rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clippingLabel)
                    .addComponent(viewportSettingsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nearClipLabel)
                            .addComponent(zNear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(farClipLabel)
                            .addComponent(zFar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(rendererSettingsPanelLayout.createSequentialGroup()
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fpsCap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fpsCapUnitLabel)
                            .addComponent(fpsCapLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fovLabel)
                            .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(fov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(fovUnitLabel)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hwSettingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rendererSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useNativeTextureFormats)
                    .addComponent(btnBFC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableShaderGC))
        );

        animeSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Animation settings (not recommended for most users)"));

        animeSpeedLabel.setText("Animation speed multiplier:");

        animeSpeed.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        animeSpeed.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                animeSpeedMouseEntered(evt);
            }
        });

        animeSpeedUnitLabel.setText("x");

        interpolationLabel.setText("Interpolate animations");

        interpolateM.setText("Material");
        interpolateM.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                interpolateMMouseEntered(evt);
            }
        });

        interpolateS.setText("Skeletal");
        interpolateS.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                interpolateSMouseEntered(evt);
            }
        });

        optimizeIntp.setText("Optimize");
        optimizeIntp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                optimizeIntpMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout animeSettingsPanelLayout = new javax.swing.GroupLayout(animeSettingsPanel);
        animeSettingsPanel.setLayout(animeSettingsPanelLayout);
        animeSettingsPanelLayout.setHorizontalGroup(
            animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(animeSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(animeSpeedLabel)
                    .addComponent(interpolationLabel)
                    .addGroup(animeSettingsPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(animeSettingsPanelLayout.createSequentialGroup()
                                .addComponent(animeSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(animeSpeedUnitLabel))
                            .addComponent(interpolateS)
                            .addGroup(animeSettingsPanelLayout.createSequentialGroup()
                                .addComponent(interpolateM)
                                .addGap(155, 155, 155)
                                .addComponent(optimizeIntp)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        animeSettingsPanelLayout.setVerticalGroup(
            animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(animeSettingsPanelLayout.createSequentialGroup()
                .addComponent(animeSpeedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(animeSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(animeSpeedUnitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(interpolationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(animeSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(interpolateM)
                    .addComponent(optimizeIntp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interpolateS)
                .addContainerGap(8, Short.MAX_VALUE))
        );

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(animeSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rendererLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(renderer, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave))
                    .addComponent(rendererSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rendererLabel)
                    .addComponent(renderer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rendererSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(animeSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void showHint(String msg) {
		parent.showHint(this, msg);
	}

	@Override
	public MsgTxt getMsgTxt() {
		return gfxSettings_res;
	}

	@Override
	public SettingsForm getParentForm() {
		return parent;
	}

    private void rendererMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rendererMouseEntered
		showHint(DESC_RENDERER);
    }//GEN-LAST:event_rendererMouseEntered

    private void zNearMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zNearMouseEntered
		showHint(DESC_CLIP);
    }//GEN-LAST:event_zNearMouseEntered

    private void zFarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zFarMouseEntered
		showHint(DESC_CLIP);
    }//GEN-LAST:event_zFarMouseEntered

    private void fpsCapMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fpsCapMouseEntered
		showHint(DESC_FPS);
    }//GEN-LAST:event_fpsCapMouseEntered

    private void fovMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fovMouseEntered
		showHint(DESC_FOV);
    }//GEN-LAST:event_fovMouseEntered

    private void animeSpeedMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_animeSpeedMouseEntered
		showHint(DESC_ANIME_SPEED);
    }//GEN-LAST:event_animeSpeedMouseEntered

    private void interpolateMMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_interpolateMMouseEntered
		showHint(DESC_ANIME_30FPS_M);
    }//GEN-LAST:event_interpolateMMouseEntered

    private void interpolateSMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_interpolateSMouseEntered
		showHint(DESC_ANIME_30FPS_S);
    }//GEN-LAST:event_interpolateSMouseEntered

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		save();
    }//GEN-LAST:event_formWindowClosing

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		save();
    }//GEN-LAST:event_btnSaveActionPerformed

	@Override
	public void save() {
		IMissionControl mc = parent.cm.getMissionControl();
		String newBackend = defaultBackEndEnumToString();
		int newFPS = (Integer) fpsCap.getValue();
		if (newFPS > 1000) {
			newFPS = 1000;
			fpsCap.setValue(newFPS);
		}
		boolean reloadMC = !newBackend.equals(RenderSettings.Defaults.BACKEND_DEFAULT) || newFPS != RenderSettings.Defaults.FRAMERATE_CAP;
		boolean reloadTextures = false;
		RenderSettings.Defaults.BACKEND_DEFAULT = newBackend;
		RenderSettings.Defaults.ANIMATION_USE_30FPS_MAT = !interpolateM.isSelected();
		RenderSettings.Defaults.ANIMATION_USE_30FPS_SKL = !interpolateS.isSelected();
		RenderSettings.Defaults.ANIMATION_OPTIMIZE = optimizeIntp.isSelected();
		RenderSettings.Defaults.FOV = (Float) fov.getValue();
		RenderSettings.Defaults.FRAMERATE_CAP = newFPS;
		RenderSettings.Defaults.FRAME_SCALE = (Float) animeSpeed.getValue();
		RenderSettings.Defaults.Z_NEAR = (Float) zNear.getValue();
		RenderSettings.Defaults.Z_FAR = (Float) zFar.getValue();
		if (useNativeTextureFormats.isSelected() != RenderSettings.Defaults.USE_NATIVE_TEXTURE_FORMATS) {
			reloadTextures = true;
		}
		RenderSettings.Defaults.USE_NATIVE_TEXTURE_FORMATS = useNativeTextureFormats.isSelected();
		RenderSettings.Defaults.ENABLE_SHADER_GC = enableShaderGC.isSelected();
		RenderSettings.Defaults.BACKFACE_CULLING = !btnBFC.isSelected();

		RenderSettings.DEFAULT_SETTINGS.loadDefaults();
		RenderSettings.Defaults.saveDefaults();
		mc.backend.getSettings().loadDefaults();
		if (reloadMC) {
			mc.updateVideoBackend();
		}
		if (reloadTextures) {
			mc.backend.clearTextureCache();
		}
		for (AbstractPerspective edt : parent.cm.getPerspectives()) {
			if (edt.m3DInput != null) {
				edt.m3DInput.fpsCamera.cam.zFar = RenderSettings.DEFAULT_SETTINGS.Z_FAR;
				edt.m3DInput.fpsCamera.cam.zNear = RenderSettings.DEFAULT_SETTINGS.Z_NEAR;
				edt.m3DInput.fpsCamera.cam.FOV = RenderSettings.Defaults.FOV;
			}
		}
	}

    private void optimizeIntpMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optimizeIntpMouseEntered
		showHint(DESC_ANIME_OPT);
    }//GEN-LAST:event_optimizeIntpMouseEntered

    private void useNativeTextureFormatsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_useNativeTextureFormatsMouseEntered
		showHint(DESC_TEXTURE_FORMATS);
    }//GEN-LAST:event_useNativeTextureFormatsMouseEntered

    private void btnBFCMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBFCMouseEntered
		showHint(DESC_BFC);
    }//GEN-LAST:event_btnBFCMouseEntered

    private void enableShaderGCMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_enableShaderGCMouseEntered
		showHint(DESC_SHADER_GC);
    }//GEN-LAST:event_enableShaderGCMouseEntered

	private String defaultBackEndEnumToString() {
		switch (renderer.getSelectedIndex()) {
			case 0:
				return RenderConstants.BACKEND_OGL2_HOUSTON;
			case 1:
				return RenderConstants.BACKEND_OGL2_HOUSTON_UBER;
			case 2:
				return RenderConstants.BACKEND_OGL4_HOUSTON;
		}
		return null;
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel animeSettingsPanel;
    private javax.swing.JFormattedTextField animeSpeed;
    private javax.swing.JLabel animeSpeedLabel;
    private javax.swing.JLabel animeSpeedUnitLabel;
    private javax.swing.JCheckBox btnBFC;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel clippingLabel;
    private javax.swing.JCheckBox enableShaderGC;
    private javax.swing.JLabel farClipLabel;
    private javax.swing.JFormattedTextField fov;
    private javax.swing.JLabel fovLabel;
    private javax.swing.JLabel fovUnitLabel;
    private javax.swing.JFormattedTextField fpsCap;
    private javax.swing.JLabel fpsCapLabel;
    private javax.swing.JLabel fpsCapUnitLabel;
    private javax.swing.JLabel hwSettingsLabel;
    private javax.swing.JCheckBox interpolateM;
    private javax.swing.JCheckBox interpolateS;
    private javax.swing.JLabel interpolationLabel;
    private javax.swing.JLabel nearClipLabel;
    private javax.swing.JCheckBox optimizeIntp;
    private javax.swing.JComboBox<String> renderer;
    private javax.swing.JLabel rendererLabel;
    private javax.swing.JPanel rendererSettingsPanel;
    private javax.swing.JCheckBox useNativeTextureFormats;
    private javax.swing.JLabel viewportSettingsLabel;
    private javax.swing.JFormattedTextField zFar;
    private javax.swing.JFormattedTextField zNear;
    // End of variables declaration//GEN-END:variables

	@Override
	public String getTitle() {
		return "Video";
	}
}
