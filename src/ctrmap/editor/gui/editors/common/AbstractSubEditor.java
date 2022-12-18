package ctrmap.editor.gui.editors.common;

import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.GameInfoListener;
import ctrmap.missioncontrol_base.debug.IMCDebugger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface AbstractSubEditor {

	public default boolean isGameSupported(GameInfo game) {
		return true;
	}

	public default boolean isDebugOnly() {
		return false;
	}
	
	public default boolean isSharedInstance() {
		return false;
	}

	public default List<GameInfoListener> getGameInfoListeners() {
		return new ArrayList<>();
	}
	
	public default Collection<? extends IMCDebugger> getExtraDebuggers() {
		return new ArrayList<>();
	}
	
	public default void prepareForSave() {
		
	}

	public default boolean store(boolean dialog) {
		return true;
	}

	public default void handleGlobalEvent(String eventId, Object... params) {

	}

	public default void onProjectLoaded(CTRMapProject proj) {

	}

	public default void onProjectUnloaded(CTRMapProject proj) {

	}
}
