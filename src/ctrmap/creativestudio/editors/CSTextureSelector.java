package ctrmap.creativestudio.editors;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.util.ListenableList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class CSTextureSelector extends JComboBox<String> {

	private Scene scene;
	private boolean wantLUT;

	/**
	 * Creates new form CSTextureSelector
	 */
	public CSTextureSelector() {
		setEditable(true);
		setModel(new DefaultComboBoxModel<>());
	}

	public void setLUTOrNot(boolean v) {
		wantLUT = v;
	}

	public void bindScene(Scene scene) {
		this.scene = scene;
		if (scene != null) {
			scene.resource.textures.addListener((ListenableList.ElementChangeEvent evt) -> {
				buildSuggestions();
			});
		}
	}

	private void buildSuggestions() {
		Object selItem = getSelectedItem();
		removeAllItems();
		for (Texture tex : scene.resource.textures) {
			if (!ReservedMetaData.isLUT(tex) ^ wantLUT) {
				addItem(tex.name);
			}
		}
		setSelectedItem(selItem);
	}

	public void setTextureName(String name) {
		setSelectedItem(name);
	}

	public String getTextureName() {
		String n = (String) getSelectedItem();
		if (n != null && n.length() > 0) {
			return n;
		}
		return null;
	}
}
