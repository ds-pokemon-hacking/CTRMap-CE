package ctrmap.missioncontrol_base;

import ctrmap.formats.common.GameInfoListener;
import ctrmap.formats.common.GameInfo;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.missioncontrol_base.debug.MCDebuggerManager;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.Scene;
import xstandard.fs.FSManager;
import ctrmap.renderer.backends.RenderSurface;
import xstandard.res.ResourceAccess;
import java.util.ArrayList;
import java.util.List;
import xstandard.util.ArraysEx;

public abstract class IMissionControl {

	public FSManager fsManager;
	public GameInfo game;

	public AbstractBackend backend;

	public final Scene mcScene = new Scene("MissionControl_RootScene");

	public InputManager input = new InputManager();

	public McLogger log = new McLogger.StdOutLogger();
	public RenderSettings videoSettings;
	public AudioSettings audioSettings;
	
	private MCDebuggerManager debuggers = new MCDebuggerManager();

	protected List<IMCSurfaceListener> surfaceListeners = new ArrayList<>();
	protected List<BackendChangeListener> bcListeners = new ArrayList<>();
	protected List<GameInfoListener> giListeners = new ArrayList<>();

	protected IMissionControl() {
		mcScene.addSceneAnimationCallback((float frameAdvance) -> {
			input.updateInput();
		});
	}
	
	public boolean mcInit(FSManager filesystem, GameInfo gameMan, RenderSettings vSettings, AudioSettings aSettings) {
		mcScene.clear(true);
		mcScene.addSceneAnimationCallback((frameAdvance) -> {
			mcScene.setAllCameraAspectRatio(backend.getViewportInfo().getAspectRatio());
		});
		this.videoSettings = vSettings;
		this.audioSettings = aSettings;
		List<String> errors = new ArrayList<>();
		if (filesystem != null) {
			fsManager = filesystem;
		} else {
			errors.add("FileSystem is null");
		}
		errors.addAll(updateVideoBackend());
		if (gameMan != null) {
			game = gameMan;
		} else {
			errors.add("GameManager is null");
		}
		for (String s : errors) {
			log.err("Initialization error: " + s);
		}
		if (errors.isEmpty()) {
			onInit();
			return true;
		}
		return false;
	}
	
	protected void onInit() {
		
	}
	
	public void attachDebugger(IMCDebugger debugger) {
		debuggers.registDebugger(debugger);
	}

	public void callGameInfoListeners() {
		for (int i = 0; i < giListeners.size(); i++) {
			giListeners.get(i).onGameInfoChange(game);
		}
	}

	public void addBackendChangeListener(BackendChangeListener l) {
		ArraysEx.addIfNotNullOrContains(bcListeners, l);
	}

	public void clearGameInfoListeners() {
		giListeners.clear();
	}

	public void addGameInfoListener(GameInfoListener l) {
		ArraysEx.addIfNotNullOrContains(giListeners, l);
	}
	
	public void removeGameInfoListener(GameInfoListener l) {
		giListeners.remove(l);
	}

	public void addSurfaceListener(IMCSurfaceListener listener) {
		if (!surfaceListeners.contains(listener)) {
			surfaceListeners.add(listener);
			listener.attach(backend.getGUI());
		}
	}

	public void removeSurfaceListener(IMCSurfaceListener man) {
		if (surfaceListeners.remove(man)) {
			man.detach(backend.getGUI());
		}
	}

	private void detachSurfaceListeners() {
		if (backend != null) {
			for (IMCSurfaceListener l : surfaceListeners) {
				l.detach(backend.getGUI());
			}
		}
	}

	public void unload() {
		mcScene.clear(true);
		detachSurfaceListeners();
		debuggers.closeAll();
	}
	
	public MCDebuggerManager getDebuggerManager() {
		return debuggers;
	}

	public List<String> updateVideoBackend() {
		List<String> errors = new ArrayList<>();
		detachSurfaceListeners();
		if (backend != null) {
			backend.free();
		}
		backend = new RenderSurface(videoSettings);

		if (errors.isEmpty()) {
			backend.setScene(mcScene);
			for (IMCSurfaceListener l : surfaceListeners) {
				l.attach(backend.getGUI());
			}
		}
		for (BackendChangeListener bcl : bcListeners) {
			bcl.onBackendChange(backend);
		}
		return errors;
	}
}
