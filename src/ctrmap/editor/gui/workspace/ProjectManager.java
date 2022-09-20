package ctrmap.editor.gui.workspace;

import xstandard.gui.file.CommonExtensionFilters;
import xstandard.gui.file.XFileDialog;
import ctrmap.editor.CTRMap;
import ctrmap.editor.system.juliet.CTRMapPluginDatabase;
import ctrmap.missioncontrol_base.McLogger;
import xstandard.fs.FSUtil;
import xstandard.gui.DialogUtils;
import xstandard.util.TripFlag;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.GameRegistryData;
import ctrmap.editor.system.workspace.ProjectRegistryData;
import ctrmap.missioncontrol_base.IMissionControl;
import xstandard.fs.FSFile;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import xstandard.formats.zip.ZipArchive;
import xstandard.thread.ThreadingUtils;

public class ProjectManager extends javax.swing.JFrame {

	public static final String CTRMAP_PROJECT_REGISTRY_DIR = "Projects";
	public static final String CTRMAP_GAME_REGISTRY_DIR = "Games";

	public ProjectRegistryData regData;
	public GameRegistryData gameRegData;

	public boolean launchCTRMapAsVisible = true;
	public PostLaunchAction postLaunchAction = null;

	public ProjectManager() {
		initComponents();
		setLocationRelativeTo(null);
		regData = new ProjectRegistryData(CTRMAP_PROJECT_REGISTRY_DIR);
		gameRegData = new GameRegistryData(CTRMAP_GAME_REGISTRY_DIR);

		for (ProjectRegistryData.ProjectRegistryEntry e : regData.entries) {
			projectList.addElement(new ProjectEntryUI(e, this));
		}

		for (String gamePath : gameRegData.entries.values()) {
			gameList.addElement(new GameEntryUI(gamePath, this));
		}

		for (CTRMapPluginDatabase.PluginEntry plg : CTRMapPluginDatabase.getPlugins()) {
			pluginList.addElement(new PluginEntryUI(plg, this));
		}

		projectsSP.getVerticalScrollBar().setUnitIncrement(15);
		gamesSP.getVerticalScrollBar().setUnitIncrement(15);
		pluginsSP.getVerticalScrollBar().setUnitIncrement(15);
	}

	public void removeProjectEntryAction(ProjectEntryUI e) {
		projectList.removeElement(e);
		regData.removeEntry(e.e);
	}

	public void removeGameEntryAction(GameEntryUI e) {
		gameList.removeElement(e);
		gameRegData.removeEntry(e.gamePath);
	}

	public void removePluginEntryAction(PluginEntryUI e) {
		pluginList.removeElement(e);
		CTRMapPluginDatabase.removePlugin(e.plugin.name);
	}

	public GameEntryUI findGameEntry(String path) {
		for (GameEntryUI entry : gameList) {
			if (entry.gamePath.equals(path)) {
				return entry;
			}
		}
		return null;
	}

	public void addGameEntryAction(GameEntryUI e) {
		gameList.addElement(e);
		gameRegData.putGamePath(e.gamePath);
	}

	public void addProjectEntryAction(ProjectEntryUI e) {
		projectList.addElement(0, e);
		regData.putNewEntry(e.e);
	}

	private final TripFlag launcherVerifyFlag = new TripFlag(true, true);

	public boolean isProjectLaunchSuccess() {
		return launcherVerifyFlag.peek();
	}

	public void launchCTRMapForEntryAction(ProjectEntryUI e) {
		e.e.lastUsed = System.currentTimeMillis();
		regData.updateEntry(e.e);
		launchCTRMapForProjectData(e.e.projectDataPath);
	}

	public CTRMap launchCTRMapForProjectData(String prjDataPath) {
		CTRMapInitDialog initDlg = new CTRMapInitDialog(this, true);
		initDlg.setLocationRelativeTo(this);

		final ProjectManager mInstance = this;

		SwingWorker worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				initDlg.getTextArea().append("Loading and verifying project " + FSUtil.getFileName(prjDataPath) + "...\n");
				CTRMapProject prj = new CTRMapProject(prjDataPath, mInstance);
				if (launcherVerifyFlag.get()) {
					CTRMap ctrmap = new CTRMap();

					if (!ctrmap.canLoadGame(prj.gameInfo)) {
						ThreadingUtils.runOnEDT(() -> {
							initDlg.getTextArea().append("Game not supported.\n");
							DialogUtils.showErrorMessage(ProjectManager.this, "Fatal error", "This project targets an unsupported game (" + prj.gameInfo.getSubGame().friendlyName + ").");
							initDlg.dispose();
						});
						prj.free();
						ctrmap.terminate();
						return null;
					}

					ctrmap.prepareMCForProject(prj);

					IMissionControl mc = ctrmap.getMissionControl();
					mc.log = new CTRMapProject.ProjectLogger(prj);
					McLogger initDlgLogger = initDlg.getLogger();
					mc.log.addChild(initDlgLogger);
					ctrmap.openProject(prj);

					initDlg.dispose();
					if (launchCTRMapAsVisible) {
						ctrmap.setVisible(true);
					}
					mc.log.removeChild(initDlgLogger);
					return ctrmap;
				} else {
					initDlg.dispose();
					return null;
				}
			}

			@Override
			public void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException ex) {
					DialogUtils.showExceptionTraceDialog(ex.getCause());
					initDlg.dispose();
				}
			}
		};
		worker.execute();
		initDlg.setVisible(true);
		CTRMap cm = null;
		try {
			cm = (CTRMap) worker.get();
			if (cm != null) {
				dispose();
				if (postLaunchAction != null) {
					postLaunchAction.postLaunch(cm);
				}
			}
		} catch (InterruptedException | ExecutionException ex) {
			Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return cm;
	}

	public void raiseLoadError(String errorDesc) {
		JOptionPane.showMessageDialog(this, errorDesc, "Load failed", JOptionPane.ERROR_MESSAGE);
		launcherVerifyFlag.raise();
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectManagerTabs = new javax.swing.JTabbedPane();
        prjPanel = new javax.swing.JPanel();
        recentProjectsLabel = new javax.swing.JLabel();
        projectsSP = new javax.swing.JScrollPane();
        projectList = new xstandard.gui.components.ComponentList();
        gamesPanel = new javax.swing.JPanel();
        btnAddGame = new javax.swing.JButton();
        gamesSP = new javax.swing.JScrollPane();
        gameList = new xstandard.gui.components.ComponentList<>();
        btnAddDSRom = new javax.swing.JButton();
        pluginsPanel = new javax.swing.JPanel();
        btnAddPluginJAR = new javax.swing.JButton();
        pluginsSP = new javax.swing.JScrollPane();
        pluginList = new xstandard.gui.components.ComponentList<>();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        btnNewProject = new javax.swing.JMenuItem();
        btnOpenProject = new javax.swing.JMenuItem();
        btnExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CTRMap Project Manager");
        setLocationByPlatform(true);

        recentProjectsLabel.setText("Recent");

        projectsSP.setViewportView(projectList);

        javax.swing.GroupLayout prjPanelLayout = new javax.swing.GroupLayout(prjPanel);
        prjPanel.setLayout(prjPanelLayout);
        prjPanelLayout.setHorizontalGroup(
            prjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(prjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(prjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(prjPanelLayout.createSequentialGroup()
                        .addComponent(recentProjectsLabel)
                        .addGap(0, 324, Short.MAX_VALUE))
                    .addComponent(projectsSP))
                .addContainerGap())
        );
        prjPanelLayout.setVerticalGroup(
            prjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(prjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recentProjectsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectsSP)
                .addContainerGap())
        );

        projectManagerTabs.addTab("Projects", prjPanel);

        btnAddGame.setText("Add Game directory");
        btnAddGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddGameActionPerformed(evt);
            }
        });

        gamesSP.setViewportView(gameList);

        btnAddDSRom.setText("Add ROM");
        btnAddDSRom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDSRomActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout gamesPanelLayout = new javax.swing.GroupLayout(gamesPanel);
        gamesPanel.setLayout(gamesPanelLayout);
        gamesPanelLayout.setHorizontalGroup(
            gamesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gamesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gamesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gamesPanelLayout.createSequentialGroup()
                        .addGap(0, 148, Short.MAX_VALUE)
                        .addComponent(btnAddDSRom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddGame))
                    .addComponent(gamesSP))
                .addContainerGap())
        );
        gamesPanelLayout.setVerticalGroup(
            gamesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gamesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gamesSP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gamesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddGame)
                    .addComponent(btnAddDSRom))
                .addContainerGap())
        );

        projectManagerTabs.addTab("Games", gamesPanel);

        btnAddPluginJAR.setText("Install plug-in");
        btnAddPluginJAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPluginJARActionPerformed(evt);
            }
        });

        pluginList.setLayout(new javax.swing.BoxLayout(pluginList, javax.swing.BoxLayout.LINE_AXIS));
        pluginsSP.setViewportView(pluginList);

        javax.swing.GroupLayout pluginsPanelLayout = new javax.swing.GroupLayout(pluginsPanel);
        pluginsPanel.setLayout(pluginsPanelLayout);
        pluginsPanelLayout.setHorizontalGroup(
            pluginsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pluginsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pluginsPanelLayout.createSequentialGroup()
                        .addGap(0, 261, Short.MAX_VALUE)
                        .addComponent(btnAddPluginJAR))
                    .addComponent(pluginsSP))
                .addContainerGap())
        );
        pluginsPanelLayout.setVerticalGroup(
            pluginsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginsSP, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAddPluginJAR)
                .addContainerGap())
        );

        projectManagerTabs.addTab("Plug-ins", pluginsPanel);

        fileMenu.setText("File");

        btnNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnNewProject.setText("Create New project");
        btnNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProjectActionPerformed(evt);
            }
        });
        fileMenu.add(btnNewProject);

        btnOpenProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnOpenProject.setText("Open Existing project");
        btnOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenProjectActionPerformed(evt);
            }
        });
        fileMenu.add(btnOpenProject);

        btnExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        fileMenu.add(btnExit);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectManagerTabs)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectManagerTabs)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private CanAddGameVerifier canAddGameVerifier = new CanAddGameVerifier() {
		@Override
		public boolean verifyGamePath(String path) {
			if (findGameEntry(path) != null) {
				DialogUtils.showErrorMessage(ProjectManager.this, "Duplicate game path", "This game path is already registered");
				return false;
			} else {
				return true;
			}
		}
	};

    private void btnAddGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGameActionPerformed
		AddGameDialog dlg = new AddGameDialog(this, true);
		final ProjectManager man = this;
		dlg.addOnGameAddSuccessListener((String gamePath) -> {
			addGameEntryAction(new GameEntryUI(gamePath, man));
		});
		dlg.addCanAddGameVerifier(canAddGameVerifier);
		dlg.setVisible(true);
    }//GEN-LAST:event_btnAddGameActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
		System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProjectActionPerformed
		if (!gameRegData.entries.isEmpty()) {
			AddProjectDialog dlg = new AddProjectDialog(this, true, gameRegData.entries.values());
			final ProjectManager man = this;
			dlg.addOnProjectAddListener((FSFile resultProjectFile) -> {
				addProjectEntryAction(new ProjectEntryUI(new ProjectRegistryData.ProjectRegistryEntry(resultProjectFile), man));
			});
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Please define some game paths in the Games tab first.", "No game definitions", JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_btnNewProjectActionPerformed

    private void btnOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenProjectActionPerformed
		FSFile prjFile = XFileDialog.openFileDialog("Open a CTRMap Project file", CTRMapProject.EXT_FILTER);
		if (prjFile != null) {
			addProjectEntryAction(new ProjectEntryUI(new ProjectRegistryData.ProjectRegistryEntry(prjFile), this));
			launchCTRMapForProjectData(prjFile.getPath());
		}
    }//GEN-LAST:event_btnOpenProjectActionPerformed

    private void btnAddPluginJARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPluginJARActionPerformed
		FSFile jar = XFileDialog.openFileDialog("Select a plug-in JAR file", CommonExtensionFilters.JAR);
		if (jar != null && ZipArchive.isZip(jar)) {
			String name = jar.getName();
			if (!CTRMapPluginDatabase.hasPlugin(name)) {
				CTRMapPluginDatabase.PluginEntry plg = CTRMapPluginDatabase.addPluginPath(jar.getName(), jar.getPath());
				pluginList.addElement(new PluginEntryUI(plg, this));
			} else {
				DialogUtils.showErrorMessage(this, "Duplicate plugin", "A plugin with name " + jar.getName() + " is already registered.");
			}
		}
    }//GEN-LAST:event_btnAddPluginJARActionPerformed

    private void btnAddDSRomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDSRomActionPerformed
		AddROMGameDialog dlg = new AddROMGameDialog(this, true);
		final ProjectManager man = this;
		dlg.addOnGameAddSuccessListener((String gamePath) -> {
			addGameEntryAction(new GameEntryUI(gamePath, man));
		});
		dlg.addCanAddGameVerifier(canAddGameVerifier);
		dlg.setVisible(true);
    }//GEN-LAST:event_btnAddDSRomActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			/* Create and display the form */
			java.awt.EventQueue.invokeLater(() -> {
				new ProjectManager().setVisible(true);
			});
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			Logger.getLogger(ProjectManager.class
				.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static interface PostLaunchAction {

		public void postLaunch(CTRMap cm);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDSRom;
    private javax.swing.JButton btnAddGame;
    private javax.swing.JButton btnAddPluginJAR;
    private javax.swing.JMenuItem btnExit;
    private javax.swing.JMenuItem btnNewProject;
    private javax.swing.JMenuItem btnOpenProject;
    private javax.swing.JMenu fileMenu;
    private xstandard.gui.components.ComponentList<GameEntryUI> gameList;
    private javax.swing.JPanel gamesPanel;
    private javax.swing.JScrollPane gamesSP;
    private javax.swing.JMenuBar menuBar;
    private xstandard.gui.components.ComponentList<PluginEntryUI> pluginList;
    private javax.swing.JPanel pluginsPanel;
    private javax.swing.JScrollPane pluginsSP;
    private javax.swing.JPanel prjPanel;
    private xstandard.gui.components.ComponentList projectList;
    private javax.swing.JTabbedPane projectManagerTabs;
    private javax.swing.JScrollPane projectsSP;
    private javax.swing.JLabel recentProjectsLabel;
    // End of variables declaration//GEN-END:variables
}
