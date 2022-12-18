package ctrmap.editor.system.juliet;

import ctrmap.formats.common.GameInfo;
import rtldr.RExtensionBase;

public interface ICTRMapPlugin extends RExtensionBase<CTRMapPluginInterface> {

	/**
	 * Registers all root perspectives provided by this plug-in.
	 *
	 * @param j
	 */
	public default void registPerspectives(CTRMapPluginInterface j) {

	}

	/**
	 * Registers all sub-editors provided by this plug-in.
	 *
	 * @param j
	 */
	public default void registEditors(CTRMapPluginInterface j) {

	}

	/**
	 * Adds optional UI elements to the main editor.
	 *
	 * @param j
	 */
	public default void registUI(CTRMapPluginInterface j, GameInfo game) {

	}
}
