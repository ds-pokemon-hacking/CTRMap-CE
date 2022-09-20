package ctrmap.editor.gui.editors.common;

import ctrmap.formats.common.GameInfo;
import ctrmap.missioncontrol_base.IMissionControl;

public interface IGameAdapter<M extends IMissionControl> {
	public String getName();
	public Class<M> getEngineClass();
	
	public boolean supportsGame(GameInfo game);
	
	public void startup();
	public M getMC();
}
