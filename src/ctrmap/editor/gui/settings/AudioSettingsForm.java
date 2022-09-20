package ctrmap.editor.gui.settings;

import ctrmap.CTRMapResources;
import ctrmap.missioncontrol_base.AudioSettings;
import xstandard.formats.msgtxt.MsgTxt;

public class AudioSettingsForm extends javax.swing.JPanel implements SettingsPanel {

	private static final MsgTxt audioSettings_res = new MsgTxt(CTRMapResources.ACCESSOR.getStream("message/audio_settings_res.msgtxt"));
	private static final String DESC_ENABLE = "enable_desc";
	private static final String DESC_VOLUME = "master_volume_desc";

	private SettingsForm parent;

	/**
	 * Creates new form AudioSettings
	 */
	public AudioSettingsForm() {
		initComponents();

		audioEnabled.setSelected(AudioSettings.Defaults.DEF_ON);
		masterVol.setValue((int) AudioSettings.Defaults.DEF_VOLUME);
	}

	@Override
	public void attachParent(SettingsForm parent) {
		this.parent = parent;
		
		SettingsPanel.batchAddMouseOverListener(this, DESC_ENABLE, audioEnabled);
		SettingsPanel.batchAddMouseOverListener(this, DESC_VOLUME, masterVolLabel, masterVol);
	}

	@Override
	public MsgTxt getMsgTxt() {
		return audioSettings_res;
	}

	@Override
	public SettingsForm getParentForm() {
		return parent;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        audioEnabled = new javax.swing.JCheckBox();
        masterVolLabel = new javax.swing.JLabel();
        masterVol = new javax.swing.JSlider();

        setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

        audioEnabled.setText("Enabled");
        audioEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audioEnabledActionPerformed(evt);
            }
        });

        masterVolLabel.setText("Master volume");

        masterVol.setMajorTickSpacing(25);
        masterVol.setPaintLabels(true);
        masterVol.setPaintTicks(true);
        masterVol.setValue(100);
        masterVol.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                masterVolStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(audioEnabled)
                    .addComponent(masterVolLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(masterVol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(178, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(audioEnabled)
                .addGap(18, 18, 18)
                .addComponent(masterVolLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(masterVol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void audioEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audioEnabledActionPerformed
		AudioSettings.Defaults.DEF_ON = audioEnabled.isSelected();
		AudioSettings.Defaults.save();
		AudioSettings.defaultSettings.loadDefaults();
    }//GEN-LAST:event_audioEnabledActionPerformed

    private void masterVolStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_masterVolStateChanged
		AudioSettings.Defaults.DEF_VOLUME = masterVol.getValue();
		AudioSettings.Defaults.save();
		AudioSettings.defaultSettings.loadDefaults();
    }//GEN-LAST:event_masterVolStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox audioEnabled;
    private javax.swing.JSlider masterVol;
    private javax.swing.JLabel masterVolLabel;
    // End of variables declaration//GEN-END:variables
}
