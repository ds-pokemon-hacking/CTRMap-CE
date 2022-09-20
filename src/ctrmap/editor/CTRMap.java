package ctrmap.editor;

import ctrmap.CTRMapResources;
import ctrmap.Launc;
import xstandard.fs.FSFile;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.common.GameInfoListener;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.AbstractSubEditor;
import ctrmap.missioncontrol_base.AudioSettings;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.util.gui.CMGUI;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.editor.system.workspace.backup.CTRMapBackupSystem;
import ctrmap.editor.gui.workspace.ProjectManager;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.system.script.CTRMapIDEHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.missioncontrol_base.IMissionControl;
import ctrmap.renderer.util.ObjectSelection;
import xstandard.gui.DialogUtils;
import xstandard.gui.LoadingDialog;
import xstandard.gui.SwingWorkerUtils;
import xstandard.res.ResourceAccess;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.common.IGameAdapter;
import ctrmap.editor.system.juliet.CTRMapPluginControl;
import ctrmap.editor.system.juliet.CTRMapPluginDatabase;
import ctrmap.editor.system.juliet.CTRMapPluginInterface;
import ctrmap.editor.system.juliet.GameAdapterRegistry;
import ctrmap.missioncontrol_base.McLogger;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import java.awt.Component;
import java.io.File;
import xstandard.math.vec.RGBA;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Objects;
import rtldr.JRTLDRCore;
import xstandard.gui.components.ComponentUtils;
import xstandard.thread.ThreadingUtils;

public class CTRMap extends JFrame {

	public static final Launc.SubprocessStarter STARTER = () -> {
		ProjectManager.main(null);
		return true;
	};

	public static final String GEVENT_FLAG_CHANGE_ID = "ChangeProjectFlag";

	public static final boolean CTRMAP_DEBUG = true;

	public static final boolean CTRMAP_LOG_STDERR = false;
	private static boolean CTRMAP_LOG_STDERR_HOOK_REGIST = false;

	private CTRMapProject project;
	private CTRMapBackupSystem backupSystem;
	public CTRMapIDEHelper ideHelper;

	private CTRMapPluginInterface plugins;
	private final GameAdapterRegistry gameAdapters = new GameAdapterRegistry();
	private CTRMapEditorManager editorMgr;

	public JTabbedPane tabs = new JTabbedPane();

	public JPanel worldEditorPanel = new JPanel(new BorderLayout());
	public JSplitPane worldEditorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	public JScrollPane worldEditorScrollPane = new JScrollPane();

	private CTRMapUIManager uiMgr;

	private final ToolActionListener toolListener = new ToolActionListener();

	//Menu and toolbars
	public JMenuBar menubar = new JMenuBar();

	public JComboBox editorSelector = new JComboBox();
	public JToolBar toolbar = new JToolBar();
	public ButtonGroup toolBtnGroup = new ButtonGroup();
	public List<AbstractButton> toolButtons = new ArrayList<>();

	public IMissionControl mcInUse;

	public CTRMap() {
		this(true);
	}

	public CTRMap(boolean initGUI) {
		super();
		CTRMapResources.load();

		for (CTRMapPluginDatabase.PluginEntry plg : CTRMapPluginDatabase.getPlugins()) {
			JRTLDRCore.loadJarExt(new File(plg.path));
		}

		if (initGUI) {
			initSwingGUI();
		}
	}

	public final void terminate() {
		if (project != null) {
			project.free();
		}
		gameAdapters.free();
		CTRMapPluginControl.freeInterface(plugins);
	}

	public final void initSwingGUI() {
		setTitle("CTRMap Editor");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		uiMgr = new CTRMapUIManager(this);
		editorMgr = new CTRMapEditorManager(this, new EditorManagerListener());
		plugins = new CTRMapPluginInterface(this);

		editorSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<AbstractPerspective> editors = getPerspectives();
				String selectedEditor = (String) editorSelector.getSelectedItem();
				for (AbstractPerspective are : editors) {
					if (are.getName().equals(selectedEditor)) {
						openPerspective(are);
						break;
					}
				}
			}
		});

		toolbar.addSeparator();
		editorSelector.setMaximumSize(editorSelector.getPreferredSize());
		toolbar.add(editorSelector);
		toolbar.addSeparator();

		toolbar.setFloatable(false);

		worldEditorPanel.add(toolbar, BorderLayout.NORTH);
		worldEditorPanel.add(worldEditorSplitPane, BorderLayout.CENTER);

		CTRMapMenuActions.initMenuActions(this);

		add(tabs);
		setJMenuBar(menubar);

		worldEditorSplitPane.setResizeWeight(1f);
		worldEditorSplitPane.setRightComponent(worldEditorScrollPane);
		worldEditorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		worldEditorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		worldEditorScrollPane.getVerticalScrollBar().setUnitIncrement(8);

		pack();
		setMinimumSize(new Dimension(1280, 720));
		setSize(getMinimumSize());
		ComponentUtils.maximize(this);

		CTRMapKeyActions.initActionMap(this);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (checkSaveAll()) {
					terminate();
					System.exit(0);
				}
			}

			@Override
			public void windowOpened(WindowEvent e) {
				if (backupSystem != null) {
					backupSystem.setupBackupRestore();
				}
			}
		});

		if (CTRMAP_LOG_STDERR) {
			if (!CTRMAP_LOG_STDERR_HOOK_REGIST) {
				Runtime.getRuntime().addShutdownHook(STDERR_FLUSH_HOOK);
				CTRMAP_LOG_STDERR_HOOK_REGIST = true;
			}
		}

		CTRMapPluginControl.readyInterface(plugins);
	}

	private static final Thread STDERR_FLUSH_HOOK = new Thread((() -> {
		System.err.flush();
	}));

	public boolean canLoadGame(GameInfo game) {
		return gameAdapters.hasGameAdapter(game);
	}

	public IMissionControl getMissionControl() {
		return mcInUse;
	}

	public <M extends IMissionControl> M getMissionControl(Class<M> cls) {
		return gameAdapters.getGameAdapter(cls).getMC();
	}

	public CTRMapUIManager getUIManager() {
		return uiMgr;
	}

	public CTRMapEditorManager getEditorManager() {
		return editorMgr;
	}

	public <T extends AbstractSubEditor> T getEditor(Class<T> cls) {
		for (AbstractPerspective p : editorMgr.getPerspectives()) {
			T e = p.getEditor(cls);
			if (e != null) {
				return e;
			}
		}
		return null;
	}

	private void setupMCForCTRMap(IMissionControl mc) {
		mc.addBackendChangeListener((AbstractBackend newBackend) -> {
			worldEditorSplitPane.setLeftComponent(newBackend.getGUI());
			//Add Discovery resource root
			newBackend.getProgramManager().getUserShManager().addIncludeDirectory(ResourceAccess.getResourceFile("discovery"));
			ObjectSelection.enableObjSelSHA(newBackend);
		});
		//Simple clear color animator by day time
		mc.mcScene.addSceneAnimationCallback(new SceneAnimationCallback() {
			final float TWENTYOCLOCK_SECS = 20 * 60 * 60;
			final float TWENTYTWOOCLOCK_SECS = 22 * 60 * 60;
			final float FIVEOCLOCK_SECS = 5 * 60 * 60;
			final float SEVENOCLOCK_SECS = 7 * 60 * 60;

			@Override
			public void run(float frameAdvance) {
				float mul = 1f;
				float daySecond = LocalDateTime.now().toLocalTime().toSecondOfDay();
				if (daySecond > TWENTYTWOOCLOCK_SECS || daySecond < FIVEOCLOCK_SECS) {
					mul = 0f;
				} else if (daySecond > SEVENOCLOCK_SECS && daySecond < TWENTYOCLOCK_SECS) {
					mul = 1f;
				} else if (daySecond >= TWENTYOCLOCK_SECS) {
					mul = 1 - (daySecond - TWENTYOCLOCK_SECS) / (TWENTYTWOOCLOCK_SECS - TWENTYOCLOCK_SECS);
				} else if (daySecond >= FIVEOCLOCK_SECS) {
					mul = (daySecond - FIVEOCLOCK_SECS) / (SEVENOCLOCK_SECS - FIVEOCLOCK_SECS);
				}
				mc.videoSettings.CLEAR_COLOR = new RGBA(0, 0, 0, 255).lerp(new RGBA(103, 174, 255, 255), mul);
			}
		});
	}

	private void setupMissionControlForGame(GameInfo game) {
		IGameAdapter adapter = gameAdapters.getGameAdapter(game);
		System.out.println("Using runtime engine " + adapter.getName());
		mcInUse = adapter.getMC();
		setupMCForCTRMap(mcInUse);
		mcInUse.clearGameInfoListeners();

		tabs.removeAll();
		tabs.add("World Editor", worldEditorPanel);

		for (AbstractPerspective p : editorMgr.getPerspectives()) {
			if (p.isGameSupported(game)) {
				p.load();
			} else {
				p.release();
			}
		}

		mcInUse.addGameInfoListener(new CTRMapGameInfoListener());
	}

	public CTRMapProject getProject() {
		return project;
	}

	public CTRMapBackupSystem getBackupSystem() {
		return backupSystem;
	}

	public boolean requestEditor(AbstractPerspective editor) {
		if (currentPerspective != editor) {
			DialogUtils.showErrorMessage(this, "Editor mismatch", "This action requires the " + editor.getName() + " editor.");
			return false;
		}
		return true;
	}

	public List<AbstractPerspective> getPerspectives() {
		return editorMgr.getPerspectives();
	}

	public AbstractPerspective currentPerspective = null;

	public void openPerspective(AbstractPerspective perspective) {
		if (!perspective.isLoaded()) {
			throw new RuntimeException("Tried to open non-loaded perspective " + perspective);
		}
		if (currentPerspective == perspective) {
			return;
		}
		if (currentPerspective != null) {
			currentPerspective.onEditorDeactivated();
		}
		currentPerspective = perspective;

		if (perspective.tool != null) {
			//worldEditorSplitPane.setRightComponent(editor.tool.getGUI());
			JComponent gui = perspective.tool.getGUI();
			setToolGUI(gui);
		} else {
			worldEditorScrollPane.setViewportView(null);
		}

		for (AbstractButton btn : toolButtons) {
			toolBtnGroup.remove(btn);
			toolbar.remove(btn);
		}
		toolButtons.clear();
		while (tabs.getTabCount() > 1) { //remove all but the root tab
			tabs.remove(1);
		}

		for (AbstractTool tool : perspective.getTools()) {
			addToolButton(tool);
		}

		for (AbstractTabbedEditor tab : perspective.getTabPanels()) {
			addEditorTab(tab);
		}

		toolbar.revalidate();
		perspective.onEditorActivated();
		perspective.onDCCCameraChanged();
	}

	private AbstractButton getButtonForTool(AbstractTool tool) {
		for (AbstractButton b : toolButtons) {
			if (Objects.equals(b.getActionCommand(), tool.getFriendlyName())) {
				return b;
			}
		}
		return null;
	}

	private void removeEditorFromToolbar(AbstractToolbarEditor e) {
		for (AbstractTool tool : e.getTools()) {
			AbstractButton b = getButtonForTool(tool);
			if (b != null) {
				toolButtons.remove(b);
				toolBtnGroup.remove(b);
				toolbar.remove(b);
			}
		}
	}

	private void removePerspective(AbstractPerspective p) {
		if (p == currentPerspective) {
			int index = getPerspectiveIndex(p);
			if (index != editorSelector.getItemCount()) {
				editorSelector.removeItem(index);
				ComponentUtils.setSelectedIndexSafe(editorSelector, index);
			}
		}
	}

	public GameInfo getGame() {
		if (mcInUse == null || mcInUse.game == null) {
			return null;
		}
		return mcInUse.game;
	}

	public UserData getUserData() {
		return project.userData;
	}

	public boolean checkSaveAll() {
		return saveData(true);
	}

	public boolean saveData() {
		return saveData(false);
	}

	public boolean saveData(boolean dialog) {
		boolean b = true;
		if (getMissionControl() != null) {
			for (AbstractPerspective p : getPerspectives()) {
				if (p.isGameSupported(getMissionControl().game)) {
					b &= p.store(dialog);
				}
			}
		}
		return b;
	}

	public void closeProject() {
		mcInUse.unload();
		mcInUse.log = new McLogger.StdOutLogger();
		for (AbstractPerspective perspective : getPerspectives()) {
			if (perspective.isGameSupported(mcInUse.game)) {
				perspective.onProjectUnloaded(project);
				perspective.release();
			}
		}
		mcInUse = null;
		project.free();
		project = null;
	}

	public void prepareMCForProject(CTRMapProject prj) {
		if (mcInUse == null) {
			if (project != null) {
				project.saveProjectData();
			} else {
				setupMissionControlForGame(prj.gameInfo);
			}
		}
	}

	public void openProject(CTRMapProject prj) {
		prepareMCForProject(prj);
		project = prj;

		if (backupSystem == null) {
			backupSystem = new CTRMapBackupSystem(this);
		}

		backupSystem.stopBackupTimer();

		ideHelper = new CTRMapIDEHelper(prj);

		if (!(mcInUse.log instanceof CTRMapProject.ProjectLogger)) {
			mcInUse.log = new CTRMapProject.ProjectLogger(prj);
		}
		if (CTRMAP_LOG_STDERR) {
			System.setErr(new PrintStream(project.userData.getUserDataFile(UserData.UsrFile.ERROR_LOG).getNativeOutputStream()));
		}

		mcInUse.mcInit(prj.wsfs, prj.gameInfo, RenderSettings.getDefaultSettings(), AudioSettings.defaultSettings);
		gameAdapters.getGameAdapter(prj.gameInfo).startup();

		SwingUtilities.invokeLater(() -> {
			getMissionControl().callGameInfoListeners();

			AbstractPerspective firstAvailEditor = null;

			for (AbstractPerspective rootEditor : getPerspectives()) {
				if (rootEditor.isGameSupported(prj.gameInfo)) {
					if (firstAvailEditor == null) {
						firstAvailEditor = rootEditor;
					}
					rootEditor.onProjectLoaded(prj);
				}
			}
			currentPerspective = null;

			if (editorSelector.getItemCount() > 0) {
				editorSelector.setSelectedIndex(0);
			}

			backupSystem.resetBackupTimer();
		});
	}

	public static CTRMap openCTRMapWithMostRecentProject() {
		ProjectManager man = new ProjectManager();
		return man.launchCTRMapForProjectData(man.regData.getNewest().projectDataPath);
	}

	public void packVFSArc(FSFile arc) {
		if (project.wsfs.isArcFile(arc)) {
			project.wsfs.vfs.applyOvFS(arc.getPath());
		} else {
			System.err.println("Archive origin is not an ArcFile (this should not happen!?)!");
		}
	}

	public void applyOvFS() {
		LoadingDialog ld = new LoadingDialog(this, true);
		SwingWorker worker = SwingWorkerUtils.prepareJob(new Runnable() {
			@Override
			public void run() {
				project.wsfs.vfs.applyOvFS("", ld);
				ld.close();
			}
		});
		worker.execute();
		ld.showDialog();
		try {
			worker.get();
		} catch (InterruptedException | ExecutionException ex) {
			Logger.getLogger(CTRMap.class.getName()).log(Level.SEVERE, null, ex);
			ld.close();
		}
	}

	public AbstractTool lookupToolByFriendlyName(String name) {
		for (AbstractTool tool : currentPerspective.getTools()) {
			if (tool.getFriendlyName().equals(name)) {
				return tool;
			}
		}
		return null;
	}

	private void setToolGUI(JComponent gui) {
		if (gui != worldEditorScrollPane.getViewport().getView()) {
			worldEditorScrollPane.setViewportView(gui);
			//revalidate();
			if (worldEditorScrollPane.getVerticalScrollBar().isVisible()) {
				Dimension ps = gui.getPreferredSize();
				ps.width += worldEditorScrollPane.getVerticalScrollBar().getWidth();
				worldEditorScrollPane.setPreferredSize(ps);
			}
			worldEditorSplitPane.resetToPreferredSizes();
		}
	}

	private int getPerspectiveIndex(AbstractPerspective p) {
		for (int index = 0; index < editorSelector.getItemCount(); index++) {
			if (Objects.equals(p.getName(), editorSelector.getItemAt(index))) {
				return index;
			}
		}
		return -1;
	}

	private void addPerspective(AbstractPerspective p) {
		if (getPerspectiveIndex(p) == -1) {
			GameInfo game = getGame();
			if (game != null) {
				if (p.isGameSupported(game)) {
					editorSelector.addItem(p.getName());
				}
			}
		}
	}

	private void addToolButton(AbstractTool tool) {
		if (getButtonForTool(tool) == null) {
			if (tool.getEditor() == null) {
				System.err.println("ERROR TOOL EDITOR NULL " + tool);
			} else if ((!tool.getEditor().isDebugOnly() || CTRMAP_DEBUG) && tool.getEditor().isGameSupported(getGame())) {
				JRadioButton btn = CMGUI.createGraphicalToolButton(tool.getResGroup());
				btn.setActionCommand(tool.getFriendlyName());
				btn.setToolTipText(tool.getFriendlyName() + " tool");
				btn.addActionListener(toolListener);
				toolBtnGroup.add(btn);
				toolbar.add(btn);
				toolButtons.add(btn);
				if (tool == currentPerspective.tool) {
					btn.setSelected(true);
				}
			}
		}
	}

	private void addEditorTab(AbstractTabbedEditor editor) {
		if ((!editor.isDebugOnly() || CTRMAP_DEBUG) && editor.isGameSupported(getGame())) {
			tabs.add(editor.getTabName(), (JComponent) editor);
		}
	}

	public void broadcastGlobalEvent(String string, Object... params) {
		if (currentPerspective != null) {
			currentPerspective.callGlobalEvent(string, params);
		}
	}

	private class CTRMapGameInfoListener implements GameInfoListener {

		@Override
		public void onGameInfoChange(GameInfo newGameInfo) {
			ThreadingUtils.runOnEDT(() -> {
				long start = System.currentTimeMillis();
				for (AbstractPerspective p : editorMgr.getPerspectives()) {
					if (p.isGameSupported(newGameInfo)) {
						addPerspective(p);
					} else {
						removePerspective(p);
					}
				}
				System.out.println("Initializing perspectives took " + (System.currentTimeMillis() - start) + "ms.");
				editorSelector.setMaximumSize(editorSelector.getPreferredSize());
				toolbar.revalidate();
			});
		}
	}

	/*private class CTRMapMCDebugger extends McDebugger {

		public CTRMapMCDebugger() {
			fieldDebugger = new FieldDebugger();
			fieldDebugger.freeCam = levelEditor.dcc;
			fieldDebugger.areaDebuggers.add(levelEditor.mCamEditForm);
			fieldDebugger.areaDebuggers.add(levelEditor.zones);
			fieldDebugger.areaDebuggers.add(levelEditor.newMatrixEditor);
			fieldDebugger.areaDebuggers.add(levelEditor.lightingEditor);
			fieldDebugger.playerDebuggers.add(levelEditor.m3DInput);
			fieldDebugger.playerDebuggers.add(levelEditor.mWarpEditForm);
			fieldDebugger.playerDebuggers.add(levelEditor.mPlayerControlForm);
			fieldDebugger.playerDebuggers.add(levelEditor.mNPCEditForm);
			fieldDebugger.mapDebuggers.add(levelEditor.mTileEditForm);
			fieldDebugger.mapDebuggers.add(levelEditor.collEditor);
			fieldDebugger.mapDebuggers.add(levelEditor.matrixEditor);
			fieldDebugger.mapDebuggers.add(levelEditor.railEditor);
			fieldDebugger.mapDebuggers.add(levelEditor.mPropEditForm);
			fieldDebugger.mapDebuggers.add(levelEditor.newMatrixEditor);
			fieldDebugger.mapDebuggers.add(levelEditor.encEditor);
			//These have to take priority because they have to have the zones loaded before other forms make use of them
			fieldDebugger.zoneDebuggers.add(levelEditor.objUIDAssigner);
			fieldDebugger.zoneDebuggers.add(levelEditor.ideStateMng);
			fieldDebugger.zoneDebuggers.add(levelEditor.mNPCEditForm.scriptPnl);
			fieldDebugger.zoneDebuggers.add(levelEditor.mTriggerEditForm.scriptPnl);
			fieldDebugger.zoneDebuggers.add(levelEditor.mScriptingAssistant);
			//End of priority forms
			fieldDebugger.zoneDebuggers.add(levelEditor.zones);
			fieldDebugger.zoneDebuggers.add(levelEditor.scriptEditor);
			fieldDebugger.zoneDebuggers.add(levelEditor.mNPCEditForm);
			fieldDebugger.zoneDebuggers.add(levelEditor.mFurnitureEditForm);
			fieldDebugger.zoneDebuggers.add(levelEditor.mTriggerEditForm);
			fieldDebugger.zoneDebuggers.add(levelEditor.mWarpEditForm);
			fieldDebugger.zoneDebuggers.add(levelEditor.itemInjector);
			fieldDebugger.zoneDebuggers.add(levelEditor.trainerInjector);
			fieldDebugger.zoneDebuggers.add(levelEditor.encEditor);
			fieldDebugger.zoneDebuggers.add(levelEditor.diveEditor);
			if (skyTripEditor.lze != null) {
				fieldDebugger.zoneDebuggers.add(skyTripEditor.lze);
			}
			skyTripDebugger = new SkyTripDebugger();
			skyTripDebugger.camDebugger = skyTripEditor.cam;
			skyTripDebugger.mapDebuggers.add(levelEditor.collEditor);
			skyTripDebugger.mapDebuggers.add(skyTripEditor.lze);
			skyTripDebugger.mapDebuggers.add(skyTripEditor.cam);
			skyTripDebugger.dataDebuggers.add(skyTripEditor.stge);
			skyTripDebugger.mapDebuggers.add(skyTripEditor.stoe);
			seqDebugger = new SequenceDebugger();
			seqDebugger.playbackDebuggers.add(seqEditor.seqControl);
			seqDebugger.camDebugger = seqEditor.debugCamera;
		}
	}*/
	private class ToolActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (currentPerspective.tool != null) {
				currentPerspective.tool.onToolShutdown();
			}
			AbstractTool tool = lookupToolByFriendlyName(e.getActionCommand());
			currentPerspective.changeTool(tool);
			setToolGUI(tool.getGUI());
		}
	}

	private class EditorManagerListener implements CTRMapEditorManager.Listener {

		@Override
		public void notifyPerspective(AbstractPerspective p) {
			ThreadingUtils.runOnEDT(() -> {
				addPerspective(p);
			});
		}

		@Override
		public void notifyPerspectiveGone(AbstractPerspective p) {
			ThreadingUtils.runOnEDT(() -> {
				removePerspective(p);
			});
		}

		@Override
		public void notifyToolbarEditor(AbstractPerspective p, Class<? extends AbstractToolbarEditor> e) {
			if (p == currentPerspective) {
				AbstractToolbarEditor editor = p.getEditor(e);
				if (editor != null) {
					for (AbstractTool tool : editor.getTools()) {
						addToolButton(tool);
					}
				}
			}
		}

		@Override
		public void notifyToolbarEditorGone(AbstractPerspective p, AbstractToolbarEditor e) {
			if (p == currentPerspective) {
				removeEditorFromToolbar(e);
			}
		}

		@Override
		public void notifyTabbedEditor(AbstractPerspective p, Class<? extends AbstractTabbedEditor> e) {
			if (p == currentPerspective) {
				addEditorTab(p.getEditor(e));
			}
		}

		@Override
		public void notifyTabbedEditorGone(AbstractPerspective p, AbstractTabbedEditor e) {
			if (p == currentPerspective) {
				tabs.remove((Component) e);
			}
		}
	}
}
