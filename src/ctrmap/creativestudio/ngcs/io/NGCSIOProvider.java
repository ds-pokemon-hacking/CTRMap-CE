package ctrmap.creativestudio.ngcs.io;

import ctrmap.creativestudio.dialogs.CameraSelectionDialog;
import ctrmap.creativestudio.dialogs.ModelSelectionDialog;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.gui.DialogUtils;
import java.awt.Frame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ctrmap.formats.common.FormatIOExConfig;

public class NGCSIOProvider implements G3DIOProvider {

	private final NGCS cs;
	private boolean skeletonSelectAlreadyFailed = false;
	private boolean cameraSelectAlreadyFailed = false;

	private Model model;
	private Camera camera;

	private boolean batchMode;
	private boolean isExport;
	private boolean isCacheReady = true;

	public NGCSIOProvider(NGCS cs, boolean batchMode) {
		this.cs = cs;
		this.batchMode = batchMode;
	}

	@Override
	public Skeleton getSkeleton() {
		Model mdl = popupModelOrSkelSelectDialog(true);
		if (mdl == null) {
			return null;
		}
		return mdl.skeleton;
	}

	@Override
	public Camera getRequestCamera() {
		return popupCameraSelectDialog();
	}

	@Override
	public G3DResource getAll() {
		return cs.getScene().resource;
	}

	@Override
	public Frame getGUIParent() {
		return cs;
	}

	public Model popupModelOrSkelSelectDialog(boolean skelOnly) {
		if (skeletonSelectAlreadyFailed) {
			return null;
		}
		if (isCacheReady && model != null) {
			return model;
		}
		isCacheReady = true;
		model = cs.getSupplementaryModelForExport(skelOnly);
		if (model == null) {
			skeletonSelectAlreadyFailed = true;
		}
		return model;
	}

	public Camera popupCameraSelectDialog() {
		if (cameraSelectAlreadyFailed) {
			return null;
		}
		if (isCacheReady && camera != null) {
			return camera;
		}
		isCacheReady = true;
		camera = cs.callCameraSelect();
		if (camera == null) {
			DialogUtils.showErrorMessage(cs, "No cameras available", "This action requires a camera, however, no cameras are currently present in the scene.");
			cameraSelectAlreadyFailed = true;
		}
		return camera;
	}

	@Override
	public Model getModel() {
		return popupModelOrSkelSelectDialog(false);
	}

	@Override
	public void resetExdataCache() {
		if (!batchMode) {
			isCacheReady = false;
			camera = null;
			model = null;
		}
	}
	
	private Map<IG3DFormatExHandler, FormatIOExConfig> lastExConfigs = new HashMap<>();

	@Override
	public FormatIOExConfig getExCfg(IG3DFormatExHandler exHandler, boolean export) {
		if (exHandler != null) {
			FormatIOExConfig cfg = lastExConfigs.get(exHandler);
			if (cfg != null) {
				return cfg;
			}
			cfg = export ? exHandler.popupExportExConfigDialog(getGUIParent()) : exHandler.popupImportExConfigDialog(getGUIParent());
			if (cfg != null) {
				lastExConfigs.put(exHandler, cfg);
			}
			return cfg;
		}
		return null;
	}
}
