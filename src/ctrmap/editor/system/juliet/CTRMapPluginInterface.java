package ctrmap.editor.system.juliet;

import ctrmap.editor.CTRMap;
import ctrmap.editor.CTRMapUIManager;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.settings.SettingsPanel;
import javax.swing.JMenuItem;
import rtldr.JExtensionStateListener;
import rtldr.JGarbageCollector;
import rtldr.JRTLDRCore;
import xstandard.thread.ThreadingUtils;
import rtldr.JExtensionReceiver;

public class CTRMapPluginInterface implements JExtensionReceiver<ICTRMapPlugin> {

	private final CTRMap cm;

	private final JGarbageCollector<Class<? extends AbstractPerspective>> perspectiveGc = new JGarbageCollector<>();
	private final JGarbageCollector<Class<? extends AbstractToolbarEditor>> toolbarEditorGc = new JGarbageCollector<>();
	private final JGarbageCollector<Class<? extends AbstractTabbedEditor>> tabbedEditorGc = new JGarbageCollector<>();
	private final JGarbageCollector<JMenuItem> menuItemGc = new JGarbageCollector<>();
	private final JGarbageCollector<Class<? extends SettingsPanel>> settingPanelGc = new JGarbageCollector<>();
	private final JGarbageCollector<String> aboutDialogStringsGc = new JGarbageCollector<>();

	public CTRMapPluginInterface(CTRMap cm) {
		this.cm = cm;
	}

	void ready() {
		JRTLDRCore.bindExtensionManager("CTRMapPlugin", this, new JExtensionStateListener<ICTRMapPlugin>() {
			@Override
			public void onExtensionLoaded(ICTRMapPlugin ext) {
				aboutDialogStringsGc.startListening(ext);
				perspectiveGc.startListening(ext);
				ext.registPerspectives(CTRMapPluginInterface.this);
				perspectiveGc.stopListening();
				JGarbageCollector.startListening(ext, toolbarEditorGc, tabbedEditorGc, settingPanelGc);
				ext.registEditors(CTRMapPluginInterface.this);
				JGarbageCollector.stopListening(toolbarEditorGc, tabbedEditorGc, settingPanelGc);
				menuItemGc.startListening(ext);
				ext.registUI(CTRMapPluginInterface.this);
				menuItemGc.stopListening();
				aboutDialogStringsGc.stopListening();
			}

			@Override
			public void onExtensionUnloaded(ICTRMapPlugin ext) {
				ThreadingUtils.runOnEDT((() -> {
					perspectiveGc.collect(ext, (t) -> {
						cm.getEditorManager().unregistPerspective(t);
					});
					menuItemGc.collect(ext, (t) -> {
						cm.getUIManager().removeMenuItem(t);
					});
					toolbarEditorGc.collect(ext, (t) -> {
						cm.getEditorManager().unregistToolbarEditor(t);
					});
					tabbedEditorGc.collect(ext, (t) -> {
						cm.getEditorManager().unregistTabbedEditor(t);
					});
					settingPanelGc.collect(ext, (t) -> {
						cm.getEditorManager().unregistSettingsPane(t);
					});
					aboutDialogStringsGc.collect(ext, (t) -> {
						cm.getEditorManager().removeAboutDialogString(t);
					});
				}));
			}
		});
	}

	void free() {
		JRTLDRCore.unregistExtensionManager(this);
	}

	/**
	 * Registers a class as a root editor perspective.
	 *
	 * @param perspective
	 */
	public void rmoRegistPerspective(Class<? extends AbstractPerspective> perspective) {
		perspective = perspectiveGc.regGc(perspective);
		if (perspective != null) {
			cm.getEditorManager().registPerspective(perspective);
		}
	}

	/**
	 * Registers a sub-editor as a perspective's toolbar editor.
	 *
	 * @param parent The perspective in which this editor is used.
	 * @param editor The editor class.
	 */
	public void rmoRegistToolbarEditor(Class<? extends AbstractPerspective> parent, Class<? extends AbstractToolbarEditor> editor) {
		editor = toolbarEditorGc.regGc(editor);
		if (editor != null) {
			cm.getEditorManager().registToolbarEditor(parent, editor);
		}
	}

	/**
	 * Registers multiple sub-editors as a perspective's toolbar editors.
	 *
	 * @param parent The perspective in which these editor are used.
	 * @param editors The editor classes.
	 */
	public void rmoRegistToolbarEditors(Class<? extends AbstractPerspective> parent, Class<? extends AbstractToolbarEditor>... editors) {
		for (Class<? extends AbstractToolbarEditor> e : editors) {
			rmoRegistToolbarEditor(parent, e);
		}
	}

	/**
	 * Registers a sub-editor as a perspective's tabbed pane editor.
	 *
	 * @param parent The perspective in which this editor is used.
	 * @param editor The editor class.
	 */
	public void rmoRegistTabbedEditor(Class<? extends AbstractPerspective> parent, Class<? extends AbstractTabbedEditor> editor) {
		editor = tabbedEditorGc.regGc(editor);
		if (editor != null) {
			cm.getEditorManager().registTabbedEditor(parent, editor);
		}
	}

	/**
	 * Registers multiple sub-editors as a perspective's tabbed pane editors.
	 *
	 * @param parent The perspective in which these editor are used.
	 * @param editors The editor classes.
	 */
	public void rmoRegistTabbedEditors(Class<? extends AbstractPerspective> parent, Class<? extends AbstractTabbedEditor>... editors) {
		for (Class<? extends AbstractTabbedEditor> e : editors) {
			rmoRegistTabbedEditor(parent, e);
		}
	}

	/**
	 * Registers a settings panel class for use in the editor's Settings dialog.
	 *
	 * @param cls Class of the settings panel.
	 */
	public void rmoRegistSettingsPanel(Class<? extends SettingsPanel> cls) {
		cls = settingPanelGc.regGc(cls);
		if (cls != null) {
			cm.getEditorManager().registSettingsPanel(cls);
		}
	}

	/**
	 * Creates a menu item with an arbitrary user callback in the editor menu bar,
	 *
	 * @param menuName Name of the root menu group.
	 * @param itemName Name of the menu item.
	 * @param callback The callback to execute when clicked.
	 * @return The menu item instance for further manipulation.
	 */
	public JMenuItem rmoAddMenuItem(String menuName, String itemName, CTRMapUIManager.ActionCallback callback) {
		if (menuItemGc.isListening()) {
			return menuItemGc.regGc(cm.getUIManager().addMenuItem(menuName, itemName, callback));
		}
		return null;
	}

	/**
	 * Adds a line of credit to the About dialog.
	 *
	 * @param credit Text formatted as person - credit.
	 */
	public void rmoAddAboutDialogCredit(String credit) {
		if ((credit = aboutDialogStringsGc.regGc(credit)) != null) {
			cm.getEditorManager().addAboutDialogCredit(credit);
		}
	}

	/**
	 * Adds multiple lines of credit to the About dialog.
	 *
	 * @param credits Text formatted as person - credit.
	 */
	public void rmoAddAboutDialogCredits(String... credits) {
		for (String credit : credits) {
			rmoAddAboutDialogCredit(credit);
		}
	}

	/**
	 * Adds a line of special thanks to the About dialog.
	 *
	 * @param specialThanks An expression of thanks.
	 */
	public void rmoAddAboutDialogSpecialThanks(String specialThanks) {
		if ((specialThanks = aboutDialogStringsGc.regGc(specialThanks)) != null) {
			cm.getEditorManager().addAboutDialogSpecialThanks(aboutDialogStringsGc.regGc(specialThanks));
		}
	}

	/**
	 * Adds multiple lines of special thanks to the About dialog.
	 *
	 * @param specialThanks Several expressions of thanks.
	 */
	public void rmoAddAboutDialogSpecialThanks(String... specialThanks) {
		for (String st : specialThanks) {
			rmoAddAboutDialogSpecialThanks(st);
		}
	}
}
