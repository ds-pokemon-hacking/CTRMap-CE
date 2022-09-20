package ctrmap.editor.system.juliet;

import ctrmap.missioncontrol_base.IMissionControl;
import java.util.HashMap;
import java.util.Map;
import ctrmap.editor.gui.editors.common.IGameAdapter;
import ctrmap.formats.common.GameInfo;
import rtldr.JExtensionStateListener;
import rtldr.JRTLDRCore;
import rtldr.JExtensionReceiver;

public class GameAdapterRegistry implements JExtensionReceiver<IGameAdapterPlugin> {

	private final Map<Class<? extends IMissionControl>, IGameAdapter> adapters = new HashMap<>();

	public GameAdapterRegistry() {
		//Each instance of CTRMap will have its own instance of GameAdapterRegistry
		//They will all load plugins separately in parallel so that the underlying
		//instances of Mission Control do not clash
		JRTLDRCore.bindExtensionManager("GameAdapterPlugin", this, new JExtensionStateListener<IGameAdapterPlugin>() {
			@Override
			public void onExtensionLoaded(IGameAdapterPlugin ext) {
				adapters.put(ext.getEngineClass(), ext.createGameAdapter());
			}

			@Override
			public void onExtensionUnloaded(IGameAdapterPlugin ext) {
				adapters.remove(ext.getEngineClass());
			}
		});
	}

	public void free() {
		JRTLDRCore.unregistExtensionManager(this);
	}

	private IGameAdapter getGameAdapterSafe(GameInfo game) {
		for (IGameAdapter a : adapters.values()) {
			if (a.supportsGame(game)) {
				return a;
			}
		}
		return null;
	}

	public IGameAdapter getGameAdapter(GameInfo game) {
		IGameAdapter a = getGameAdapterSafe(game);
		if (a == null) {
			throw new RuntimeException("Game not supported: " + game.getSubGame());
		}
		return a;
	}

	public <M extends IMissionControl> IGameAdapter<M> getGameAdapter(Class<M> cls) {
		IGameAdapter<M> adapter = adapters.get(cls);
		if (adapter != null) {
			return adapter;
		}
		throw new RuntimeException("Could not find game adapter for " + cls);
	}

	public boolean hasGameAdapter(GameInfo game) {
		return getGameAdapterSafe(game) != null;
	}
}
