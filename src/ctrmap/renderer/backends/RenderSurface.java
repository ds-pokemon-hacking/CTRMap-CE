package ctrmap.renderer.backends;

import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.BackendMaterialOverride;
import ctrmap.renderer.backends.base.RenderCapabilities;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.RenderTarget;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.backends.base.flow.IShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import ctrmap.renderer.util.AspectRatioSyncCallback;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class RenderSurface extends JPanel implements AbstractBackend {

	private AbstractBackend backend;
	
	private boolean syncAspectRatio = false;
	private final SceneAnimationCallback aspectSyncCallback = new AspectRatioSyncCallback.Default(this);

	public RenderSurface() {
		this(RenderSettings.createDefaultRenderer());
	}

	public RenderSurface(RenderSettings settings) {
		this(settings, null);
	}
	
	public RenderSurface(RenderSettings settings, RenderCapabilities caps) {
		this(settings.createRenderer(caps));
	}

	private RenderSurface(AbstractBackend backend) {
		this.backend = backend;

		JComponent gui = backend.getGUI();

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(gui, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(gui, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	public final void setSyncAspectRatio(boolean enable) {
		if (enable) {
			getScene().addSceneAnimationCallback(aspectSyncCallback);
		}
		else {
			getScene().removeSceneAnimationCallback(aspectSyncCallback);
		}
		syncAspectRatio = enable;
	}

	@Override
	public void setScene(Scene scn) {
		boolean aspectSync = syncAspectRatio;
		setSyncAspectRatio(false); //remove callback from old scene
		backend.setScene(scn);
		setSyncAspectRatio(aspectSync);
	}

	@Override
	public Scene getScene() {
		return backend.getScene();
	}

	@Override
	public JComponent getGUI() {
		return this;
	}

	@Override
	public RenderSettings getSettings() {
		return backend.getSettings();
	}

	@Override
	public ViewportInfo getViewportInfo() {
		return backend.getViewportInfo();
	}

	@Override
	public ShaderProgramManager getProgramManager() {
		return backend.getProgramManager();
	}

	@Override
	public BackendMaterialOverride getOverrideMaterial() {
		return backend.getOverrideMaterial();
	}

	@Override
	public Material getPostprocessingMaterial() {
		return backend.getPostprocessingMaterial();
	}

	@Override
	public void clearTextureCache() {
		backend.clearTextureCache();
	}

	@Override
	public BufferedImage captureScreenshot() {
		return backend.captureScreenshot();
	}

	@Override
	public RenderTarget addRenderTarget(String name, TextureFormatHandler format) {
		return backend.addRenderTarget(name, format);
	}

	@Override
	public void readPixels(int x, int y, int w, int h, String renderTargetName, Buffer dest) {
		backend.readPixels(x, y, w, h, renderTargetName, dest);
	}

	@Override
	public void waitForDisplay() {
		backend.waitForDisplay();
	}

	@Override
	public IShaderAdapter getShaderHandler() {
		return backend.getShaderHandler();
	}

	@Override
	public void free() {
		backend.free();
	}
}
