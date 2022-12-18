package ctrmap.renderer.backends.houston.gl2;

import ctrmap.renderer.backends.houston.HoustonResources;
import ctrmap.renderer.backends.houston.common.HoustonDefines;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.RenderTarget;
import ctrmap.renderer.backends.houston.gl2.shaderengine.GL2ProgramManager;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.RenderCapabilities;
import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.backends.base.flow.BufferObjectMemory;
import ctrmap.renderer.backends.base.shaderengine.ShaderDefinition;
import ctrmap.renderer.backends.base.shaderengine.UserShaderManager;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.houston.common.HoustonShaderAdapter;
import xstandard.res.ResourceAccess;
import java.nio.Buffer;
import ctrmap.renderer.backends.base.flow.IShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.backends.houston.GLBackendBase;
import ctrmap.renderer.backends.houston.common.HoustonUniforms;
import java.awt.Dimension;

import static ctrmap.renderer.backends.houston.common.HoustonConstants.*;

public class HoustonGL2 extends GLBackendBase {

	static {
		HoustonResources.load();
	}

	private final GL2RenderDriver driver = new GL2RenderDriver(this);

	public HoustonGL2() {
		super();
	}

	public HoustonGL2(RenderSettings settings) {
		super(settings);
	}

	public HoustonGL2(RenderSettings settings, RenderCapabilities caps) {
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
				GL2 gl = getGL().getGL2();
				driver.setGL(gl);
				boolean tempCtx = createTempContext();
				gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, framebuffer.name.get(driver));
				gl.glReadBuffer(GL2.GL_COLOR_ATTACHMENT0 + rt.renderTargetId);
				gl.glReadPixels(x, y, w, h, GL2Material.getGL2ExternalTextureFormat(rt.format.nativeFormat, extensions), GL2Material.getGL2TextureFormatDataType(rt.format.nativeFormat), dest);
				closeTempContext(tempCtx);
			}
		}
	}

	@Override
	public IShaderAdapter getShaderHandler() {
		return HoustonShaderAdapter.INSTANCE;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval(1);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glDepthMask(true);
		gl.glColorMask(true, true, true, true);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glClearDepth(1.0f);
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		extensions.init(gl);
		identity *= 7;
		System.out.println("Initialized OpenGL context: " + gl.glGetString(GL2.GL_VERSION) + " @ " + getClass().getSimpleName());
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		synchronized (FB_ACCESS_LOCK) {
			driver.setGL(drawable.getGL().getGL2());
			
			renderController.setGCFlagValue(BufferObjectMemory.GCFilter.PROGRAMS, settings.ENABLE_SHADER_GC);
			renderController.render(driver);
		}
	}

	private Dimension lastFramebufferSize = new Dimension(-1, -1);

	private void issueSimpleFBTexCommands(GL2 gl) {
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	}

	public void setUpFramebuffer(GL2 gl, IRenderDriver driver, RenderState state) {
		int fbname = framebuffer.name.get(driver);
		if (fbname == -1) {
			//Create the framebuffer
			int[] fb = new int[1];
			gl.glGenFramebuffers(1, fb, 0);
			fbname = fb[0];
			framebuffer.name.set(driver, fbname);
		}
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbname);

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
				gl.glBindTexture(GL2.GL_TEXTURE_2D, rtTexName);

				driver.texImage2D(rt.format, windowSize.width, windowSize.height, null);

				issueSimpleFBTexCommands(gl);
			}
		}

		boolean needsUpdateDepth = needsUpdateAllTex;

		int depthName = framebuffer.depthName.get(driver);

		if (depthName == -1) {
			depthName = driver.genTexture();
			framebuffer.depthName.set(driver, depthName);
			needsUpdateDepth = true;
		}

		if (needsUpdateDepth) {
			gl.glBindTexture(GL2.GL_TEXTURE_2D, depthName);

			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT24, windowSize.width, windowSize.height, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, null);

			issueSimpleFBTexCommands(gl);
		}

		int rtAttachmentMax = 0;

		for (RenderTarget rt : framebuffer.renderTargets) {
			int rtTexName = rt.getPointer(driver);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + rt.renderTargetId, GL2.GL_TEXTURE_2D, rtTexName, 0);
			if (rt.renderTargetId + 1 > rtAttachmentMax) {
				rtAttachmentMax = rt.renderTargetId + 1;
			}
		}

		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, depthName, 0);

		int[] drawbuffers = new int[rtAttachmentMax];
		for (RenderTarget rt : framebuffer.renderTargets) {
			int id = rt.renderTargetId;
			drawbuffers[id] = GL2.GL_COLOR_ATTACHMENT0 + rt.renderTargetId;
		}

		gl.glDrawBuffers(drawbuffers.length, drawbuffers, 0);

		if (RenderConstants.RENDER_DEBUG) {
			int fbStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
			if (fbStatus != GL2.GL_FRAMEBUFFER_COMPLETE) {
				System.err.println("INCOMPLETE FRAMEBUFFER ! ! ! - " + fbStatus);
			}
		}

		gl.glViewport(0, 0, lastFramebufferSize.width, lastFramebufferSize.height);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	}

	public void flushFramebuffer(GL2 gl, IRenderDriver driver, RenderState state) {
		/*gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, framebuffer.name);
		gl.glReadBuffer(GL2.GL_COLOR_ATTACHMENT1);

		Texture result = new Texture(lastFramebufferSize.width, lastFramebufferSize.height, TextureFormatHandler.RGBA8);
		ByteBuffer buf = ByteBuffer.allocate(result.width * result.height * result.format.getNativeBPP());
		gl.glReadPixels(0, 0, result.width, result.height, GL2Material.getGL2ExternalTextureFormat(result.format.originFormat, extensions), GL2Material.getGL2TextureFormatDataType(result.format.nativeFormat), buf);
		result.data = buf.array();
		TextureConverter.writeTextureToFile(new File("D:\\_REWorkspace\\shader\\debug\\fb.png"), "png", result);*/

		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, lastFramebufferSize.width, lastFramebufferSize.height);

		renderController.setUpMaterialState(postProcessMaterial, driver);

		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, SCREEN_RENDER_TARGET.getPointer(driver));

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
	protected ShaderProgramManager createProgramManager() {
		return initProgramManager(new GL2ProgramManager());
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
		GL2 gl = getGL().getGL2();
		driver.setGL(gl);

		int[] singleDelete = new int[1];

		singleDelete[0] = framebuffer.name.get(driver);
		if (singleDelete[0] != -1) {
			gl.glDeleteFramebuffers(1, singleDelete, 0);
		}

		int[] texToDelete = new int[framebuffer.renderTargets.size() + 1];

		texToDelete[0] = framebuffer.depthName.get(driver);

		int index = 1;
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
