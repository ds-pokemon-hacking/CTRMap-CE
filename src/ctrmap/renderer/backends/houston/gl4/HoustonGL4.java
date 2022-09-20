package ctrmap.renderer.backends.houston.gl4;

import ctrmap.renderer.backends.houston.HoustonResources;
import ctrmap.renderer.backends.houston.common.HoustonDefines;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.RenderTarget;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.backends.base.flow.BufferObjectMemory;
import ctrmap.renderer.backends.base.shaderengine.ShaderDefinition;
import ctrmap.renderer.backends.base.shaderengine.UserShaderManager;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import xstandard.res.ResourceAccess;
import java.nio.Buffer;
import ctrmap.renderer.backends.base.flow.IShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.backends.houston.GLBackendBase;
import ctrmap.renderer.backends.houston.common.HoustonUniforms;
import java.awt.Dimension;

import static ctrmap.renderer.backends.houston.common.HoustonConstants.*;
import ctrmap.renderer.backends.houston.gl4.shaderengine.GL4ProgramManager;

public class HoustonGL4 extends GLBackendBase {

	static {
		HoustonResources.load();
	}

	private GL4RenderDriver driver = new GL4RenderDriver(this);

	public HoustonGL4() {
		super();
	}

	public HoustonGL4(RenderSettings settings) {
		super(settings);
	}

	public HoustonGL4(RenderSettings settings, GLCapabilities caps) {
		super(settings, caps);
	}

	@Override
	protected GLProfile createGLProfile() {
		return GLProfile.get(GLProfile.GL4);
	}

	@Override
	public void readPixels(int x, int y, int w, int h, String rtName, Buffer dest) {
		synchronized (FB_ACCESS_LOCK) {
			RenderTarget rt = getRenderTarget(rtName);
			if (rt != null) {
				GL4 gl = getGL().getGL4();
				driver.setGL(gl);
				boolean tempCtx = createTempContext();
				gl.glBindFramebuffer(GL4.GL_READ_FRAMEBUFFER, framebuffer.name.get(driver));
				gl.glReadBuffer(GL4.GL_COLOR_ATTACHMENT0 + rt.renderTargetId);
				gl.glReadPixels(x, y, w, h, GL4Material.getGL4ExternalTextureFormat(rt.format.nativeFormat, extensions), GL4Material.getGL4TextureFormatDataType(rt.format.nativeFormat), dest);
				closeTempContext(tempCtx);
			}
		}
	}

	@Override
	public IShaderAdapter getShaderHandler() {
		return GL4AlphaTestHoustonShaderAdapter.INSTANCE;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		HoustonResources.load();

		GL4 gl = drawable.getGL().getGL4();
		gl.setSwapInterval(1);
		gl.glDepthMask(true);
		gl.glColorMask(true, true, true, true);
		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glDepthFunc(GL4.GL_LEQUAL);
		gl.glClearDepth(1.0f);
		gl.glClear(GL4.GL_DEPTH_BUFFER_BIT | GL4.GL_COLOR_BUFFER_BIT | GL4.GL_STENCIL_BUFFER_BIT);
		gl.glEnable(GL4.GL_TEXTURE_2D);
		extensions.init(gl);
		identity *= 7;
		System.out.println("Initialized OpenGL context: " + gl.glGetString(GL4.GL_VERSION) + " @ " + getClass().getSimpleName());
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		synchronized (FB_ACCESS_LOCK) {
			driver.setGL(drawable.getGL().getGL4());

			renderController.setGCFlagValue(BufferObjectMemory.GCFilter.PROGRAMS, settings.ENABLE_SHADER_GC);
			renderController.render(driver);
		}
	}

	private Dimension lastFramebufferSize = new Dimension(-1, -1);

	private void issueSimpleFBTexCommands(GL4 gl) {
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_EDGE);
	}

	public void setUpFramebuffer(GL4 gl, IRenderDriver driver, RenderState state) {
		int fbname = framebuffer.name.get(driver);
		if (fbname == -1) {
			//Create the framebuffer
			int[] fb = new int[1];
			gl.glGenFramebuffers(1, fb, 0);
			fbname = fb[0];
			framebuffer.name.set(driver, fbname);
		}
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, fbname);

		Dimension windowSize = getSize();
		boolean needsUpdateAllTex = !windowSize.equals(lastFramebufferSize);

		lastFramebufferSize = windowSize;

		for (RenderTarget rt : framebuffer.renderTargets) {
			boolean needsUpdate = needsUpdateAllTex;

			int rtTexName = rt.getPointer(driver);
			if (rtTexName == -1) {
				rtTexName = driver.genTexture();
				rt.setPointer(driver, rtTexName);
				needsUpdate = true;
			}

			if (needsUpdate) {
				driver.bindTexture(rtTexName);

				driver.texImage2D(rt.format, windowSize.width, windowSize.height, null);

				issueSimpleFBTexCommands(gl);
			}
		}

		boolean needsUpdateDepth = needsUpdateAllTex;

		int depthFb = framebuffer.depthName.get(driver);
		if (depthFb == -1) {
			int[] depthRenderBuffer = new int[1];
			gl.glGenRenderbuffers(1, depthRenderBuffer, 0);
			depthFb = depthRenderBuffer[0];
			framebuffer.depthName.set(driver, depthFb);
			needsUpdateDepth = true;
		}

		if (needsUpdateDepth) {
			gl.glBindRenderbuffer(GL4.GL_RENDERBUFFER, depthFb);
			gl.glRenderbufferStorage(GL4.GL_RENDERBUFFER, GL4.GL_DEPTH24_STENCIL8, windowSize.width, windowSize.height);
		}

		int rtAttachmentMax = 0;

		for (RenderTarget rt : framebuffer.renderTargets) {
			int rtTexName = rt.getPointer(driver);
			gl.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0 + rt.renderTargetId, GL4.GL_TEXTURE_2D, rtTexName, 0);
			if (rt.renderTargetId + 1 > rtAttachmentMax) {
				rtAttachmentMax = rt.renderTargetId + 1;
			}
		}

		gl.glFramebufferRenderbuffer(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_STENCIL_ATTACHMENT, GL4.GL_RENDERBUFFER, depthFb);

		int[] drawbuffers = new int[rtAttachmentMax];
		for (RenderTarget rt : framebuffer.renderTargets) {
			int id = rt.renderTargetId;
			drawbuffers[id] = GL4.GL_COLOR_ATTACHMENT0 + rt.renderTargetId;
		}

		gl.glDrawBuffers(drawbuffers.length, drawbuffers, 0);

		int fbStatus = gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER);
		if (fbStatus != GL4.GL_FRAMEBUFFER_COMPLETE) {
			System.err.println("INCOMPLETE FRAMEBUFFER ! ! ! - " + fbStatus);
		}

		gl.glViewport(0, 0, lastFramebufferSize.width, lastFramebufferSize.height);
		gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT | GL4.GL_STENCIL_BUFFER_BIT);
	}

	public void flushFramebuffer(GL4 gl, IRenderDriver driver, RenderState state) {
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, lastFramebufferSize.width, lastFramebufferSize.height);

		renderController.setUpMaterialState(postProcessMaterial, driver);

		gl.glEnable(GL4.GL_TEXTURE_2D);
		gl.glActiveTexture(GL4.GL_TEXTURE0);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, SCREEN_RENDER_TARGET.getPointer(driver));

		ShaderProgram program = programManager.getShaderProgram(driver, postProcessMaterial);
		if (program != null) {
			gl.glUseProgram(program.handle.get(driver));
			gl.glUniform1iv(program.getUniformLocation(HoustonUniforms.TEX_SAMPLERS, driver), 1, new int[]{0}, 0);
			gl.glUniform1i(program.getUniformLocation(HoustonUniforms.ASH_TIME, driver), (int) System.currentTimeMillis());
			gl.glUniform2f(program.getUniformLocation(HoustonUniforms.ASH_SCREEN_DIM, driver), lastFramebufferSize.width, lastFramebufferSize.height);
		} else {
			System.err.println("COULD NOT BIND FRAMEBUFFER RENDER PROGRAM ! ! !");
			gl.glUseProgram(0);
		}

		driver.drawMesh(FILL_SCREEN_QUAD);

		gl.glUseProgram(0);
	}

	@Override
	public ViewportInfo getViewportInfo() {
		return new ViewportInfo(getSize(), settings.Z_NEAR, settings.Z_FAR);
	}

	@Override
	protected ShaderProgramManager createProgramManager() {
		return initProgramManager(new GL4ProgramManager());
	}

	protected final ShaderProgramManager initProgramManager(ShaderProgramManager mng) {
		UserShaderManager vshMan = mng.getUserShManager();

		vshMan.addIncludeDirectory(ResourceAccess.getResourceFile(HOUSTON_ROOT));
		vshMan.setDefaultShaderName(HOUSTON_VSH_FILE);

		mng.addShaderDefinition(new ShaderDefinition(HoustonDefines.KEY_TEXTURE_MAX, HoustonDefines.TEXTURE_MAX));
		mng.addShaderDefinition(new ShaderDefinition(HoustonDefines.KEY_LUT_MAX, HoustonDefines.LUT_MAX));
		mng.addShaderDefinition(new ShaderDefinition(HoustonDefines.KEY_LIGHT_MAX, HoustonDefines.LIGHT_MAX));
		mng.addShaderDefinition(new ShaderDefinition(HoustonDefines.KEY_SHADING_STAGE_MAX, HoustonDefines.SHADING_STAGE_MAX));

		for (RenderTarget rt : framebuffer.renderTargets) {
			mng.addShaderDefinition(rt);
		}
		return mng;
	}

	@Override
	protected void freeFramebuffers() {
		if (getGL() == null) {
			return;
		}
		GL4 gl = getGL().getGL4();
		driver.setGL(gl);

		int[] singleDelete = new int[1];

		singleDelete[0] = framebuffer.name.get(driver);
		if (singleDelete[0] != -1) {
			gl.glDeleteFramebuffers(1, singleDelete, 0);
		}
		singleDelete[0] = framebuffer.depthName.get(driver);
		if (singleDelete[0] != -1) {
			gl.glDeleteRenderbuffers(1, singleDelete, 0);
		}

		int[] texToDelete = new int[framebuffer.renderTargets.size()];

		int index = 0;
		for (RenderTarget rt : framebuffer.renderTargets) {
			int ptr = rt.getPointer(driver);
			texToDelete[index++] = ptr;
		}

		gl.glDeleteTextures(texToDelete.length, texToDelete, 0);
	}

	@Override
	protected IRenderDriver getRenderDriver() {
		return driver;
	}
}
