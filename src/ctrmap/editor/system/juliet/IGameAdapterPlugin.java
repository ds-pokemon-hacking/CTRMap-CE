package ctrmap.editor.system.juliet;

import ctrmap.editor.gui.editors.common.IGameAdapter;
import ctrmap.missioncontrol_base.IMissionControl;
import rtldr.RExtensionBase;

public interface IGameAdapterPlugin extends RExtensionBase<GameAdapterRegistry> {

	/**
	 * Gets the class of the MissionControl engine that this adapter provides.
	 *
	 * @return
	 */
	public Class<? extends IMissionControl> getEngineClass();

	/**
	 * Creates a new instance of the runtime engine game adapter.
	 *
	 * @return
	 */
	public IGameAdapter createGameAdapter();
}
