package ctrmap.editor;

import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.settings.SettingsPanel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CTRMapEditorManager {

	private final CTRMap cm;
	private final Listener listener;

	private final List<AbstractPerspective> perspectives = new ArrayList<>();
	private final List<AbstractToolbarEditor> toolbarEditors = new ArrayList<>();
	private final List<AbstractTabbedEditor> tabbedEditors = new ArrayList<>();
	
	private final List<SettingsPanel> settingsPanels = new ArrayList<>();
	
	private final List<String> aboutDialogCredits = new ArrayList<>();
	private final List<String> aboutDialogThanks = new ArrayList<>();

	public CTRMapEditorManager(CTRMap cm, Listener listener) {
		this.listener = listener;
		this.cm = cm;
	}

	public <T extends AbstractPerspective> T getPerspective(Class<T> cls) {
		for (AbstractPerspective p : perspectives) {
			if (cls.isAssignableFrom(p.getClass())) {
				return (T) p;
			}
		}
		return null;
	}

	public void registPerspective(Class<? extends AbstractPerspective> perspectiveClass) {
		if (getPerspective(perspectiveClass) == null) {
			try {
				Constructor<? extends AbstractPerspective> c = perspectiveClass.getConstructor(CTRMap.class);
				AbstractPerspective p = c.newInstance(cm);
				perspectives.add(p);
				listener.notifyPerspective(p);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

			}
		}
	}

	public void unregistPerspective(Class<? extends AbstractPerspective> perspectiveClass) {
		AbstractPerspective p = getPerspective(perspectiveClass);
		if (p != null) {
			perspectives.remove(p);
			listener.notifyPerspectiveGone(p);
		}
	}

	List<AbstractPerspective> getPerspectives() {
		return perspectives;
	}

	void notifyOfAllPerspectives() {
		for (AbstractPerspective p : perspectives) {
			listener.notifyPerspective(p);
		}
	}
	
	public void registToolbarEditor(Class<? extends AbstractPerspective> parent, Class<? extends AbstractToolbarEditor> editor) {
		AbstractPerspective p = getPerspective(parent);
		if (p != null) {
			p.addToolbarEditor(editor);
			listener.notifyToolbarEditor(p, editor);
		}
	}
	
	public void unregistToolbarEditor(Class<? extends AbstractToolbarEditor> cls) {
		for (AbstractPerspective p : perspectives) {
			for (AbstractToolbarEditor e : p.getToolbarEditors()) {
				if (e.getClass() == cls) {
					p.removeToolbarEditor(e);
					listener.notifyToolbarEditorGone(p, e);
				}
			}
		}
	}
	
	public void registTabbedEditor(Class<? extends AbstractPerspective> parent, Class<? extends AbstractTabbedEditor> editor) {
		AbstractPerspective p = getPerspective(parent);
		if (p != null) {
			p.addTabbedEditor(editor);
			listener.notifyTabbedEditor(p, editor);
		}
	}

	public void unregistTabbedEditor(Class<? extends AbstractTabbedEditor> cls) {
		for (AbstractPerspective p : perspectives) {
			for (AbstractTabbedEditor e : p.getTabPanels()) {
				if (e.getClass() == cls) {
					p.removeTabbedEditor(e);
					listener.notifyTabbedEditorGone(p, e);
				}
			}
		}
	}
	
	public void registSettingsPanel(Class<? extends SettingsPanel> cls) {
		for (SettingsPanel p : settingsPanels) {
			if (p.getClass() == cls) {
				return;
			}
		}
		try {
			settingsPanels.add(cls.newInstance());
		} catch (InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(CTRMapEditorManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void unregistSettingsPane(Class<? extends SettingsPanel> cls) {
		for (SettingsPanel p : settingsPanels) {
			if (p.getClass() == cls) {
				settingsPanels.remove(p);
				break;
			}
		}
	}

	public Collection<? extends SettingsPanel> getSettingsPanels() {
		return settingsPanels;
	}
	
	public void addAboutDialogCredit(String credit) {
		aboutDialogCredits.add(credit);
	}
	
	public void addAboutDialogSpecialThanks(String specialThanks) {
		aboutDialogThanks.add(specialThanks);
	}
	
	public void removeAboutDialogString(String str) {
		aboutDialogCredits.remove(str);
		aboutDialogThanks.remove(str);
	}

	List<String> getCredits() {
		return aboutDialogCredits;
	}
	
	List<String> getSpecialThanks() {
		return aboutDialogThanks;
	}
	
	public static interface Listener {

		public void notifyPerspective(AbstractPerspective p);

		public void notifyPerspectiveGone(AbstractPerspective p);
		
		public void notifyToolbarEditor(AbstractPerspective p, Class<? extends AbstractToolbarEditor> e);
		
		public void notifyToolbarEditorGone(AbstractPerspective p, AbstractToolbarEditor e);
		
		public void notifyTabbedEditor(AbstractPerspective p, Class<? extends AbstractTabbedEditor> e);
		
		public void notifyTabbedEditorGone(AbstractPerspective p, AbstractTabbedEditor e);
	}
}
