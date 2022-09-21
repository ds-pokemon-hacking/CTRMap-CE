package ctrmap.renderer.backends.base.flow;

import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.BackendMaterialOverride;
import static ctrmap.renderer.backends.base.BackendMaterialOverride.OverrideCapability.*;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.*;
import xstandard.util.EnumBitflags;
import xstandard.util.TripFlag;
import java.util.ArrayList;
import java.util.List;

public class SceneRenderFlow {

	public static final int RENDER_LAYER_MAX = 8;

	private final AbstractBackend backend;

	public TripFlag resetVerticesNextPass = new TripFlag();
	public TripFlag clearTextureCacheNextPass = new TripFlag();
	public TripFlag waitForDisplay = new TripFlag();

	public long frametime_last = 0;
	private List<Long> frameTS = new ArrayList<>();

	private BufferObjectMemory lastBufState;
	private BufferObjectMemory nowBufState;
	
	private final EnumBitflags<BufferObjectMemory.GCFilter> gcFilter = new EnumBitflags<>(
		BufferObjectMemory.GCFilter.BUFFERS, 
		BufferObjectMemory.GCFilter.TEXTURES, 
		BufferObjectMemory.GCFilter.PROGRAMS
	);

	public SceneRenderFlow(AbstractBackend backend) {
		this.backend = backend;
	}
	
	public void free() {
		if (lastBufState != null) {
			lastBufState.gc(new EnumBitflags<>(BufferObjectMemory.GCFilter.BUFFERS, BufferObjectMemory.GCFilter.TEXTURES, BufferObjectMemory.GCFilter.PROGRAMS));
		}
	}

	protected void debugPrint(String str) {
		if (RenderConstants.RENDER_DEBUG) {
			System.out.println(str);
		}
	}

	public void setGCFlag(BufferObjectMemory.GCFilter f) {
		gcFilter.set(f);
	}
	
	public void clearGCFlag(BufferObjectMemory.GCFilter f) {
		gcFilter.unset(f);
	}
	
	public void setGCFlagValue(BufferObjectMemory.GCFilter f, boolean enabled) {
		gcFilter.setValue(f, enabled);
	}
	
	public void render(IRenderDriver driver) {
		long displayStart = System.currentTimeMillis();

		nowBufState = new BufferObjectMemory(driver);

		Scene scene = backend.getScene();
		RenderSettings settings = backend.getSettings();

		if (resetVerticesNextPass.get()) {
			scene.resetVertexData(driver.getIdentity());
		}

		if (clearTextureCacheNextPass.get()) {
			scene.requestAllTextureReupload();
		}

		if (RenderConstants.RENDER_DEBUG) {
			debugPrint("Total vertices/indices to render: " + scene.getTotalVertexCount() + ", raw vertices amount: " + scene.getTotalVertexCountVBO());
		}

		if (frametime_last == 0) {
			frametime_last = System.currentTimeMillis();
		}

		float frametime = 0f;

		long renderBeginTime = System.currentTimeMillis();
		frametime = (renderBeginTime - frametime_last);
		frametime_last = System.currentTimeMillis();

		frameTS.add(frametime_last);
		for (int i = 0; i < frameTS.size(); i++) {
			if (frametime_last - frameTS.get(i) > 1000) {
				frameTS.remove(i);
				i--;
			}
		}

		debugPrint("FRAMETIME: " + frametime + " FPS: " + frameTS.size());

		float anmFrameStep = frametime * settings.FRAME_SCALE * (30f / 1000f);
		scene.advanceAllAnimationCallbacks(anmFrameStep);
		scene.notifyBeginDraw(settings);
		scene.advanceAllAnimations(anmFrameStep, settings);

		driver.clearColor(backend.getSettings().CLEAR_COLOR);
		driver.clearDepth(1.0f);
		driver.clearFramebuffer();

		RenderState state = new RenderState();

		long beginDraw = System.currentTimeMillis();

		driver.beginDrawEx(state);

		renderScene(scene, driver, state);

		driver.finishDrawEx(state);

		if (lastBufState != null) {
			BufferObjectMemory diff = nowBufState.diff(lastBufState);

			//Objects left in the diff have been removed since last draw and can be garbage collected.
			diff.gc(gcFilter);
		}
		lastBufState = nowBufState;

		debugPrint("Scene rendering took " + (System.currentTimeMillis() - beginDraw) + " ms");

		//</editor-fold>
		driver.flush();
		scene.runAllCallbacks();
		waitForDisplay.get();
		debugPrint("Display loop took " + (System.currentTimeMillis() - displayStart));
	}

	public void renderScene(Scene s, IRenderDriver gl, RenderState state) {
		G3DResourceState rootState = new G3DResourceState(s);

		for (int i = 0; i < RENDER_LAYER_MAX; i++) {
			state.setLayer(i);
			renderG3DResInstanceState(rootState, gl, state);
		}
	}
	
	private Model bboxDebugModel = new Model();

	private void renderG3DResInstanceState(G3DResourceState resourceState, IRenderDriver gl, RenderState state) {
		G3DResourceInstance instance = resourceState.instance;
		if (instance.visible) {
			if (!state.hasFlag(RenderState.Flag.NO_DRAW)) {				
				G3DResource g3d = instance.resource;
				if (g3d != null) {
					for (Model model : g3d.models) {
						if (model.isVisible) {
							renderG3DResMdl(resourceState, model, state, gl);
						}
					}
				}

				for (G3DResourceState child : resourceState.children) {
					renderG3DResInstanceState(child, gl, state);
				}
			}
		}
	}

	private static final Material emptyMaterial = new Material();

	static {
		emptyMaterial.name = "empty";
	}

	protected <T> T decideOverrideParam(T param, T overrideParam, BackendMaterialOverride.OverrideCapability cap) {
		if (backend.getOverrideMaterial().hasOverrideCap(cap)) {
			return overrideParam;
		}
		return param;
	}

	public void setUpMaterialState(Material mat, IRenderDriver driver) {
		driver.resetMaterialState();

		if (mat == null) {
			mat = emptyMaterial;
		}

		BackendMaterialOverride overrideMaterial = backend.getOverrideMaterial();

		boolean isMatOverriden = overrideMaterial.enabled();

		if (!isMatOverriden) {
			driver.setUpBlend(mat.blendOperation);
			driver.setUpStencilTest(mat.stencilTest, mat.stencilOperation);
			driver.setUpDepthTest(mat.depthColorMask);
			driver.setUpAlphaTest(mat.alphaTest);
			driver.setUpFaceCulling(mat.faceCulling);
		} else {
			driver.setUpBlend(decideOverrideParam(mat.blendOperation, overrideMaterial.blendOperation, ALPHA_BLEND));
			driver.setUpStencilTest(
				decideOverrideParam(mat.stencilTest, overrideMaterial.stencilTest, STENCIL_TEST),
				decideOverrideParam(mat.stencilOperation, overrideMaterial.stencilOperation, STENCIL_OPERATION)
			);
			driver.setUpDepthTest(decideOverrideParam(mat.depthColorMask, overrideMaterial.depthColorMask, DEPTH_TEST));
			driver.setUpAlphaTest(decideOverrideParam(mat.alphaTest, overrideMaterial.alphaTest, ALPHA_TEST));
			driver.setUpFaceCulling(
				decideOverrideParam(mat.faceCulling, overrideMaterial.faceCulling, FACE_CULLING)
			);
		}
	}

	protected void renderG3DResMdl(G3DResourceState resourceState, Model mdl, RenderState state, IRenderDriver gl) {
		G3DResourceInstance instance = resourceState.instance;
		if (instance == null || mdl == null) {
			return;
		}

		for (Mesh mesh : mdl.meshes) {
			boolean doRender = state.renderLayer == mesh.renderLayer && resourceState.getMeshVisibility(mdl, mesh);

			if (!doRender) {
				continue;
			}

			setUpMaterialState(mesh.getMaterial(mdl), gl);

			MeshRenderFlow.setUpDrawForModelMesh(
				resourceState,
				mdl,
				mesh,
				state,
				backend,
				nowBufState,
				gl
			);

			//long actualRenderCall = System.currentTimeMillis();
			gl.drawMesh(mesh);
			//System.out.println("Mesh " + mesh.name + " draw took " + (System.currentTimeMillis() - meshBegin) + ", since renderModelMesh call " + (System.currentTimeMillis() - actualRenderCall));

			nowBufState.registMesh(mesh);
		}
	}
}
