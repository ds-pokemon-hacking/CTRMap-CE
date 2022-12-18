package ctrmap.renderer.backends.houston;

import ctrmap.renderer.backends.houston.common.GLExtensionSupport;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.BackendMaterialOverride;
import ctrmap.renderer.backends.base.flow.SceneRenderFlow;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.RenderTarget;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.backends.base.Framebuffer;
import ctrmap.renderer.backends.base.RenderCapabilities;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.generators.PlaneGenerator;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public abstract class GLBackendBase extends GLJPanel implements AbstractBackend, GLEventListener {

	private AWTGLReadBufferUtil readBufferUtil;

	private AnimatorBase animator;

	public Scene scene = new Scene("backend_root");

	protected RenderSettings settings;
	protected GLExtensionSupport extensions = new GLExtensionSupport();

	protected BackendMaterialOverride overrideMaterial = new BackendMaterialOverride();
	protected final Material postProcessMaterial = initPostProcessMaterial();

	protected SceneRenderFlow renderController;

	protected ShaderProgramManager programManager;

	protected final Framebuffer framebuffer = new Framebuffer();

	protected final RenderTarget SCREEN_RENDER_TARGET = new RenderTarget(RenderConstants.RENDERTARGET_SURFACE_MAIN, TextureFormatHandler.RGBA8, 0);

	protected static final Mesh FILL_SCREEN_QUAD = PlaneGenerator.generateQuadPlaneMesh(1f, 1f, 0f, false, true);

	protected Integer identity = hashCode();

	protected final Object FB_ACCESS_LOCK = new Object();

	static {
		FILL_SCREEN_QUAD.materialName = "Framebuffer";
		FILL_SCREEN_QUAD.createAndInvalidateBuffers();
	}

	protected static class DefaultCaps extends GLCapabilities {

		public DefaultCaps(GLProfile glp, RenderCapabilities source) throws GLException {
			super(glp);
			setHardwareAccelerated(true);
			setDoubleBuffered(source.doubleBuffered);
			setAlphaBits(source.alphaBits);
			setRedBits(source.redBits);
			setBlueBits(source.blueBits);
			setGreenBits(source.greenBits);
			setStencilBits(source.stencilBits);
		}
	}

	public GLBackendBase() {
		this(RenderSettings.DEFAULT_SETTINGS);
	}

	public GLBackendBase(RenderSettings settings) {
		this(settings, new RenderCapabilities());
	}

	public GLBackendBase(RenderSettings settings, RenderCapabilities caps) {
		this(new DefaultCaps(GLProfile.get(GLProfile.GL2), caps == null ? new RenderCapabilities() : caps));
		this.settings = settings;

		renderController = new SceneRenderFlow(this);

		super.addGLEventListener(this);
		animator = new FPSAnimator(this, settings.FRAMERATE_CAP);
	}

	//This constructor only exists because dumb Java does not allow us to call the supertype contructor and retain the DefaultCaps otherwise
	private GLBackendBase(GLCapabilities caps) {
		super(caps);
		readBufferUtil = new AWTGLReadBufferUtil(caps.getGLProfile(), true);
		programManager = createProgramManager();
		addRenderTarget(SCREEN_RENDER_TARGET);
	}

	public Integer getIdentity() {
		return identity;
	}

	protected abstract GLProfile createGLProfile();

	protected abstract ShaderProgramManager createProgramManager();

	@Override
	public void addNotify() {
		super.addNotify();
		animator.start();
	}

	public void setSettings(RenderSettings settings) {
		this.settings = settings;
	}
	
	@Override
	public ViewportInfo getViewportInfo() {
		return new ViewportInfo(getSize());
	}

	@Override
	public RenderSettings getSettings() {
		return settings;
	}

	public GLExtensionSupport getExtensions() {
		return extensions;
	}

	@Override
	public ShaderProgramManager getProgramManager() {
		return programManager;
	}

	protected abstract IRenderDriver getRenderDriver();

	@Override
	public final RenderTarget addRenderTarget(String renderTargetName, TextureFormatHandler renderTargetFormat) {
		RenderTarget rt = new RenderTarget(renderTargetName, renderTargetFormat, framebuffer.renderTargets.size());
		addRenderTarget(rt);
		return rt;
	}

	public RenderTarget getRenderTarget(String name) {
		for (RenderTarget rt : framebuffer.renderTargets) {
			if (rt.key.equals(name)) {
				return rt;
			}
		}
		return null;
	}

	public final void addRenderTarget(RenderTarget rt) {
		framebuffer.renderTargets.add(rt);
		programManager.addShaderDefinition(rt);
	}

	private static Material initPostProcessMaterial() {
		Material mat = new Material();
		mat.name = FILL_SCREEN_QUAD.materialName;
		mat.vertexShaderName = "framebuffer.vsh";
		mat.fragmentShaderName = "framebuffer.fsh";
		mat.fshType = MaterialParams.FragmentShaderType.USER_SHADER;
		return mat;
	}

	@Override
	public Material getPostprocessingMaterial() {
		return postProcessMaterial;
	}
	
	protected boolean createTempContext() {
		GLContext ctx = getContext();
		if (!ctx.isCurrent()) {
			ctx.makeCurrent();
			return true;
		}
		return false;
	}
	
	protected void closeTempContext(boolean tempContext) {
		if (tempContext) {
			getContext().release();
		}
	}

	private BufferedImage captureTmp;

	@Override
	public BufferedImage captureScreenshot() {
		synchronized (FB_ACCESS_LOCK) {
			try {
				Runnable runnable = () -> {
					GL gl = getGL();
					boolean tempCtx = createTempContext();
					captureTmp = readBufferUtil.readPixelsToBufferedImage(gl, true);
					closeTempContext(tempCtx);
				};
				if (SwingUtilities.isEventDispatchThread()) {
					runnable.run();
				} else {
					SwingUtilities.invokeAndWait(runnable);
				}
				return captureTmp;
			} catch (InterruptedException | InvocationTargetException ex) {
				Logger.getLogger(GLBackendBase.class.getName()).log(Level.SEVERE, null, ex);
			}
			return null;
		}
	}

	@Override
	public BackendMaterialOverride getOverrideMaterial() {
		return overrideMaterial;
	}

	private static final Material emptyMaterial = new Material();

	static {
		emptyMaterial.name = "empty";
	}

	@Override
	public void setScene(Scene scn) {
		renderController.resetVerticesNextPass.raise();
		scene = scn;
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public JComponent getGUI() {
		return this;
	}

	protected abstract void freeFramebuffers();
	
	private void sanityCheckAllFreed() {
		GL gl = getGL();
		int[] temp = new int[1];
		gl.glGenBuffers(1, temp, 0);
		if (temp[0] > 1) {
			System.err.println("Non-freed buffer count " + (temp[0] - 1));
		}
		gl.glDeleteBuffers(1, temp, 0);
		gl.glGenTextures(1, temp, 0);
		if (temp[0] > 1) {
			System.err.println("Non-freed texture count " + (temp[0] - 1));
		}
		gl.glDeleteTextures(1, temp, 0);
	}

	@Override
	public void free() {
		animator.stop();
		renderController.free();
		if (getGL() != null) {
			freeFramebuffers();
			getProgramManager().freeDriver(getRenderDriver());
			sanityCheckAllFreed();
			GLContext ctx = getContext();
			if (ctx != null) {
				ctx.destroy();
			}
		}
		setRealized(false);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	@Override
	public void waitForDisplay() {
		renderController.waitForDisplay.raise();
		while (renderController.waitForDisplay.peek()) {
			Thread.yield();
		}
	}

	protected static void setLineWidth(Mesh mesh, GL gl) {
		gl.glLineWidth(ReservedMetaData.getLineWidth(mesh.metaData, 1f));
	}

	@Override
	public void clearTextureCache() {
		renderController.clearTextureCacheNextPass.raise();
	}
}
