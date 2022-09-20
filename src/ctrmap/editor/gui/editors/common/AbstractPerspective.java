package ctrmap.editor.gui.editors.common;

import ctrmap.editor.CTRMap;
import ctrmap.editor.CTRMapEditorManager;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.gui.editors.common.input.CM3DInputManager;
import ctrmap.editor.gui.editors.common.input.DCCManager;
import ctrmap.editor.gui.editors.common.input.LevelEditor2DInputManager;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.missioncontrol_base.IMissionControl;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.GameInfoListener;
import ctrmap.formats.common.collision.ICollisionProvider;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import xstandard.math.vec.Vec3f;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractPerspective {

	private boolean loaded;
	
	protected CTRMap ctrmap;

	public DCCManager dcc;
	public LevelEditor2DInputManager tilemapInput;
	public CM3DInputManager m3DInput;
	public EditorScene scn;

	public AbstractTool tool;

	private final List<Class<? extends AbstractToolbarEditor>> toolbarEditorClasses = new ArrayList<>();
	private final List<Class<? extends AbstractTabbedEditor>> tabbedEditorClasses = new ArrayList<>();

	private final List<AbstractToolbarEditor> toolbarEditors = new ArrayList<>();
	private final List<AbstractTabbedEditor> tabbedEditors = new ArrayList<>();

	private final RootGameInfoListener giListener = new RootGameInfoListener();

	public AbstractPerspective(CTRMap cm) {
		this.ctrmap = cm;
		scn = new EditorScene(this);
	}

	public <T extends AbstractSubEditor> T getEditor(Class<T> cls) {
		for (AbstractSubEditor e : getAllEditors()) {
			if (e.getClass() == cls) {
				return (T) e;
			}
		}
		return null;
	}

	private <T> Constructor<T> getFirstSafeConstructor(Class<T> cls, Class... paramClasses) {
		for (Class other : paramClasses) {
			try {
				return cls.getConstructor(other);
			} catch (NoSuchMethodException | SecurityException ex) {

			}
		}
		return null;
	}

	private <T> T instantiateBaseEditor(Class<T> cls) {
		Constructor<T> ctor = getFirstSafeConstructor(cls, getClass(), CTRMap.class, AbstractPerspective.class);
		if (ctor != null) {
			try {
				Class paramCls = ctor.getParameterTypes()[0];
				Object param = null;
				if (AbstractPerspective.class.isAssignableFrom(paramCls)) {
					param = this;
				}
				else if (paramCls == CTRMap.class) {
					param = ctrmap;
				}
				return ctor.newInstance(param);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				System.err.println("Could not instantiate editor: " + cls);
				Logger.getLogger(CTRMapEditorManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return null;
	}

	public void load() {
		m3DInput = new CM3DInputManager(this);
		tilemapInput = new LevelEditor2DInputManager(this);
		dcc = new DCCManager(this);
		for (Class<? extends AbstractToolbarEditor> c : toolbarEditorClasses) {
			AbstractToolbarEditor e = instantiateBaseEditor(c);
			if (e != null) {
				toolbarEditors.add(e);
			}
		}
		for (Class<? extends AbstractTabbedEditor> c : tabbedEditorClasses) {
			AbstractTabbedEditor e = instantiateBaseEditor(c);
			if (e != null) {
				tabbedEditors.add(e);
			}
		}
		IMissionControl mc = getCurrentMC();
		mc.addGameInfoListener(giListener);
		for (IMCDebugger dbg : getDebuggers()) {
			mc.getDebuggerManager().registDebugger(dbg);
		}
		loaded = true;
	}

	public void release() {
		m3DInput = null;
		tilemapInput = null;
		dcc = null;
		toolbarEditors.clear();
		tabbedEditors.clear();
		IMissionControl mc = getCurrentMC();
		mc.removeGameInfoListener(giListener);
		for (IMCDebugger dbg : getDebuggers()) {
			mc.getDebuggerManager().unregistDebugger(dbg);
		}
		loaded = false;
	}
	
	public boolean isLoaded() {
		return loaded;
	}

	public void onEditorActivated() {
		//This order is important - the second input manager relies on tileSelector position, which is given by the 1st one
		//reverse order would result in one of them lagging behind
		ctrmap.getMissionControl().addSurfaceListener(tilemapInput);
		ctrmap.getMissionControl().addSurfaceListener(m3DInput);
	}

	public void onEditorDeactivated() {
		ctrmap.getMissionControl().removeSurfaceListener(m3DInput);
		ctrmap.getMissionControl().removeSurfaceListener(tilemapInput);
	}

	public abstract boolean isGameSupported(GameInfo game);

	public final void onProjectLoaded(CTRMapProject proj) {
		for (AbstractSubEditor e : getAllEditors()) {
			e.onProjectLoaded(proj);
		}
	}

	public final void onProjectUnloaded(CTRMapProject proj) {
		for (AbstractSubEditor e : getAllEditors()) {
			e.onProjectUnloaded(proj);
		}
	}

	public abstract void onDCCCameraChanged();

	public final List<AbstractToolbarEditor> getToolbarEditors() {
		return toolbarEditors;
	}

	public final List<AbstractTabbedEditor> getTabPanels() {
		return tabbedEditors;
	}

	public final List<AbstractSubEditor> getAllEditors() {
		List<AbstractSubEditor> l = new ArrayList<>(toolbarEditors.size() + tabbedEditors.size());
		l.addAll(toolbarEditors);
		l.addAll(tabbedEditors);
		return l;
	}

	public final List<? extends IMCDebugger> getDebuggers() {
		List<IMCDebugger> l = new ArrayList<>();
		l.add(dcc);
		for (AbstractSubEditor e : getAllEditors()) {
			if (e instanceof IMCDebugger) {
				l.add((IMCDebugger)e);
			}
		}
		l.addAll(getExtraDebuggers());
		return l;
	}
	
	public List<? extends IMCDebugger> getExtraDebuggers() {
		return new ArrayList<>();
	}

	public abstract String getName();

	public Vec3f getIdealCenterCameraPosByZeroPlane() {
		if (dcc.getDebugCameraEnabled() && dcc.getCurrentDcc() == m3DInput) {
			Camera cam = dcc.getDebugCamera();
			Vec3f dirVec = cam.getRotationEulerToDir();
			Vec3f pos = Vec3f.hitY(cam.translation, dirVec, 0f, new Vec3f());
			ICollisionProvider coll = getWorldCollisionProvider();
			if (coll != null) {
				pos.y = coll.getHeightAtWorldLoc(pos.x, pos.y, pos.z);
			}
			return pos;
		} else {
			return tilemapInput.getCenterCameraPos();
		}
	}

	public List<AbstractTool> getTools() {
		List<AbstractTool> tools = new ArrayList<>();
		for (AbstractToolbarEditor edt : getToolbarEditors()) {
			if (edt != null) {
				ArraysEx.addAllIfNotNullOrContains(tools, edt.getTools());
			}
		}
		return tools;
	}

	public CTRMap getCTRMap() {
		return ctrmap;
	}

	public GameInfo getGameInfo() {
		return ctrmap.getGame();
	}

	public IMissionControl getCurrentMC() {
		return ctrmap.mcInUse;
	}

	public AbstractBackend getRenderer() {
		return ctrmap.getMissionControl().backend;
	}
	
	public void handleGlobalEvent(String eventName, Object... params) {
		
	}

	public void reinitTool() {
		if (tool != null) {
			tool.onToolInit();
		}
	}

	public boolean store() {
		return store(false);
	}

	public final boolean store(boolean dialog) {
		for (AbstractSubEditor e : getAllEditors()) {
			if (!e.store(dialog)) {
				return false;
			}
		}
		return true;
	}

	public abstract Scene getInjectionScene();

	public abstract ICollisionProvider getWorldCollisionProvider();
	
	public void callGlobalEvent(String eventId, Object... params) {
		handleGlobalEvent(eventId, params);
		for (AbstractSubEditor e : getAllEditors()) {
			e.handleGlobalEvent(eventId, params);
		}
	}

	public void changeTool(AbstractTool tool) {
		this.tool = tool;
		tool.onToolInit();
		scn.onToolChange(tool);
	}

	public void addToolbarEditor(Class<? extends AbstractToolbarEditor> e) {
		ArraysEx.addIfNotNullOrContains(toolbarEditorClasses, e);
	}

	public void addTabbedEditor(Class<? extends AbstractTabbedEditor> e) {
		ArraysEx.addIfNotNullOrContains(tabbedEditorClasses, e);
	}

	public void removeToolbarEditor(AbstractToolbarEditor e) {
		toolbarEditorClasses.remove(e.getClass());
		toolbarEditors.remove(e);
	}

	public void removeTabbedEditor(AbstractTabbedEditor e) {
		tabbedEditorClasses.remove(e.getClass());
		tabbedEditors.remove(e);
	}

	private class RootGameInfoListener implements GameInfoListener {

		@Override
		public void onGameInfoChange(GameInfo newGameInfo) {
			for (AbstractSubEditor e : getAllEditors()) {
				if (e.isGameSupported(newGameInfo)) {
					for (GameInfoListener subListener : e.getGameInfoListeners()) {
						subListener.onGameInfoChange(newGameInfo);
					}
				}
			}
		}
	}
}
