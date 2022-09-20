package ctrmap.renderer.backends.base;

import ctrmap.renderer.backends.base.flow.IShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import javax.swing.JComponent;

public interface AbstractBackend {

	public void setScene(Scene scn);

	public Scene getScene();

	public JComponent getGUI();

	public RenderSettings getSettings();

	public ViewportInfo getViewportInfo();
	
	public IShaderAdapter getShaderHandler();
	
	public ShaderProgramManager getProgramManager();
	
	public BackendMaterialOverride getOverrideMaterial();
	
	public Material getPostprocessingMaterial();

	public void clearTextureCache();

	public BufferedImage captureScreenshot();
	
	public RenderTarget addRenderTarget(String name, TextureFormatHandler format);
	
	public void readPixels(int x, int y, int w, int h, String renderTargetName, Buffer dest);

	public void waitForDisplay();

	public void free();
}
