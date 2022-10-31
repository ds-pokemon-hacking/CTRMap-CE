package ctrmap.creativestudio.ngcs;

import ctrmap.CTRMapResources;
import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.creativestudio.dialogs.CSSplashScreen;
import ctrmap.creativestudio.dialogs.CameraSelectionDialog;
import ctrmap.creativestudio.dialogs.ModelSelectionDialog;
import ctrmap.util.gui.cameras.FPSCameraInputManager;
import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import ctrmap.creativestudio.ngcs.io.NGCSImporter;
import ctrmap.creativestudio.ngcs.plugins.NGCSCMIFPlugin;
import ctrmap.creativestudio.ngcs.plugins.NGCSColladaPlugin;
import ctrmap.creativestudio.ngcs.plugins.NGCSOBJPlugin;
import ctrmap.creativestudio.ngcs.plugins.NGCSSMDPlugin;
import ctrmap.creativestudio.ngcs.plugins.NGCSStandardIOPlugin;
import ctrmap.creativestudio.ngcs.rtldr.NGCSContentAccessor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietHelper;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.creativestudio.ngcs.rtldr.NGCSUIManager;
import ctrmap.creativestudio.ngcs.tree.CSNode;
import ctrmap.creativestudio.ngcs.tree.CSNodeContentType;
import ctrmap.creativestudio.ngcs.tree.LightNode;
import ctrmap.creativestudio.ngcs.tree.ModelNode;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimationController;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResourceType;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.DialogUtils;
import xstandard.gui.DnDHelper;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.tree.CheckboxTreeCell;
import xstandard.gui.components.tree.CustomJTreeCellRenderer;
import xstandard.gui.file.XFileDialog;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.util.ListenableList;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import rtldr.JRTLDRCore;
import xstandard.math.AABB6f;

public class NGCS extends javax.swing.JFrame implements NGCSContentAccessor {

	static {
		//Default NGCS plugins
		JRTLDRCore.loadExtensions(NGCSJulietIface.getInstance(),
			new NGCSStandardIOPlugin(),
			new NGCSColladaPlugin(),
			new NGCSCMIFPlugin(),
			new NGCSOBJPlugin(),
			new NGCSSMDPlugin()
		);
		String pluginRoot = JRTLDRCore.getPrefsNodeForExtensionManager("CreativeStudio").get("PluginRoot", null);
		if (pluginRoot != null) {
			File pluginRootDir = new File(pluginRoot);
			JRTLDRCore.loadExtensionDirectory(pluginRootDir);
		}
	}

	public static final int CS_IMAGE_ICON_DIM_MAX = 25;

	private Camera mainCamera;

	private boolean isCustomLight = false;
	private Light mainLight;

	private Scene scene = new Scene("CreativeStudioRoot");

	private ListenableList<CMIFFile.OtherFile> other = new ListenableList<>();
	private NGCSOtherSyncListener otherSync = new NGCSOtherSyncListener(scene.resource);

	private FPSCameraInputManager input;

	public Model currentModel;
	public G3DSceneTemplate currentTemplateSceneTemplate;
	public Scene currentTemplateScene;
	private CSNode lastNode;

	//Data tree
	private boolean disableTreeListeners = false;

	private NGCSSaveDataManager saveData;

	private CSSplashScreen splash = new CSSplashScreen();

	private NGEditorController editors;
	private NGCSIOManager ioMgr = NGCSIOManager.getInstance();
	private NGCSUIManager uiMgr;

	/**
	 * Creates new form NGCS
	 */
	public NGCS() {
		super();
		splash.setVisible(true);

		CTRMapResources.load();
		ComponentUtils.setSystemNativeLookAndFeel();
		initComponents();
		uiMgr = new NGCSUIManager(menuBar);
		NGCSJulietHelper.onCSWindowLoad(this);
		initToolbarShaderListeners();

		editorScrollPane.getVerticalScrollBar().setUnitIncrement(20);

		editors = new NGEditorController(this);
		other.addListener(otherSync);
		saveData = new NGCSSaveDataManager(this);
		input = new FPSCameraInputManager();
		input.setDisableMotionIfCtrlAlt(true);
		input.attachComponent(g3dViewport);
		input.addToScene(g3dViewport.scene);
		mainCamera = input.cam;
		dataTree.initCS(this);

		g3dViewport.scene.addChild(scene);
		mainLight = new Light("CreativeStudio");
		mainLight.setDirectionByOriginIllumPosition(new Vec3f(50f, 100f, 100f));
		mainLight.ambientColor = new RGBA(220, 220, 220, 255);
		mainLight.specular1Color = new RGBA(150, 150, 150, 255);
		changeLight(mainLight);
		dataTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		dataTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
			if (disableTreeListeners) {
				return;
			}
			if (lastNode != null) {
				lastNode.onNodeDeselected();
			}
			CSNode node = getSelectedNode();
			if (node != null) {
				node.onNodeSelected();

				if (node.getShouldResetCamera()) {
					if (changeCamera(mainCamera)) {
						resetCameraToModel();
					}
				}
				if (node.getShouldResetLights()) {
					changeLight(mainLight);
					resetCheckboxNodesAll(LightNode.class);
				}

				editors.switchEditorOpenObject(node.getEditor(editors), node);
			}
			lastNode = node;
		});
		editors.switchEditorOpenObject(null, null);
		dataTree.expandRow(0);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent componentEvent) {
				adjustSplitPanes();
			}
		});
		DnDHelper.addFileDropTarget(g3dViewport, new DnDHelper.FileDropListener() {
			@Override
			public void acceptDrop(List<File> files) {
				List<DiskFile> diskFiles = new ArrayList<>();
				for (File f : files) {
					diskFiles.add(new DiskFile(f));
				}
				importFiles(diskFiles);
			}
		});
		g3dViewport.scene.addSceneAnimationCallback(new SceneAnimationCallback() {
			@Override
			public void run(float frameAdvance) {
				anmControl.updateAllControls();
			}
		});
		anmControl.bindAnimationControllerList(scene.resourceAnimControllers);
		anmControl.bindAnimationControllerList(g3dViewport.scene.resourceAnimControllers);

		resetCameraToModel();
		ComponentUtils.maximize(this);
	}

	public NGCS(G3DResource embeddedSource, NGEmbeddedSaveDataManager.Callback saveCallback) {
		this(embeddedSource, saveCallback, false);
	}

	public NGCS(G3DResource embeddedSource, NGEmbeddedSaveDataManager.Callback saveCallback, boolean saveOnClose) {
		this();
		merge(embeddedSource);
		btnOpenCMIF.setEnabled(false);
		btnNewProject.setEnabled(false);
		setTitle("CTRMap Creative Studio - Embedded Mode");
		saveData = new NGEmbeddedSaveDataManager(this, saveCallback);
		if (saveOnClose) {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					saveData.callSave();
				}

				@Override
				public void windowClosed(WindowEvent e) {
					NGCSJulietHelper.onCSWindowClose(NGCS.this);
				}
			});
		}
	}

	@Override
	public void setVisible(boolean val) {
		super.setVisible(val);
		if (val && splash != null && splash.isVisible()) {
			splash.setVisible(false);
			toFront();
		}
	}

	private class ToolbarShaderSwitcherListener implements ActionListener {

		private final String shaderName;

		public ToolbarShaderSwitcherListener(String shaderName) {
			this.shaderName = shaderName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			g3dViewport.setAttrShader(shaderName);
		}

	}

	private void initToolbarShaderListeners() {
		btnShaderSetDefault.addActionListener(new ToolbarShaderSwitcherListener(null));
		btnShaderSetLighting.addActionListener(new ToolbarShaderSwitcherListener("Lighting.fsh_ext"));
		btnShaderSetNormal.addActionListener(new ToolbarShaderSwitcherListener("Normal.fsh_ext"));
		btnShaderSetTangent.addActionListener(new ToolbarShaderSwitcherListener("Tangent.fsh_ext"));
		btnShaderSetUV.addActionListener(new ToolbarShaderSwitcherListener("UVMap.fsh_ext"));
		btnShaderSetVCol.addActionListener(new ToolbarShaderSwitcherListener("VertexColour.fsh_ext"));
	}

	public NGCSIOManager getIOManager() {
		return ioMgr;
	}

	public NGCSUIManager getUIManager() {
		return uiMgr;
	}

	public Camera getMainCamera() {
		return mainCamera;
	}

	private void resetCheckboxNodesAll(Class<? extends CSNode> cls) {
		resetCheckboxNodes(cls, dataTree.getRootCSNode(), null);
	}

	private void resetCheckboxNodes(Class<? extends CSNode> cls, CSNode node, Object byContent) {
		if (cls.isAssignableFrom(node.getClass()) && (byContent == null || Objects.equals(node.getContent(), byContent))) {
			CustomJTreeCellRenderer rnd = node.getTreeCellComponent();
			if (rnd instanceof CheckboxTreeCell) {
				((CheckboxTreeCell) rnd).setChecked(false);
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			resetCheckboxNodes(cls, (CSNode) node.getChildAt(i), byContent);
		}
	}

	public void showLoadedModel(Model m) {
		boolean isAlreadyShown = false;

		stopShowingSceneTemplate();

		for (Model mdl : scene.resource.models) {
			if (mdl.isVisible && m == mdl) {
				//nothing to do
				isAlreadyShown = true;
				break;
			}
		}

		if (!isAlreadyShown) {
			//scene.stopAllAnimations();
			currentModel = m;
			for (Model mdl : scene.resource.models) {
				mdl.isVisible = m == mdl;
			}
			resetCameraToModel();
		}
	}

	public void stopShowingSceneTemplate() {
		if (currentTemplateScene != null) {
			unsetupSceneTemplateSceneFromCS(currentTemplateScene);
			g3dViewport.scene.removeChild(currentTemplateScene);
			g3dViewport.scene.addChild(scene);
			currentTemplateScene = null;
			currentTemplateSceneTemplate = null;
			for (Model mdl : scene.resource.models) {
				mdl.isVisible = mdl == currentModel;
			}
		}
	}

	public void showSceneTemplate(G3DSceneTemplate template) {
		if (currentTemplateSceneTemplate != template && template != null) {
			stopShowingSceneTemplate();
			currentTemplateScene = template.createScene(getResource());
			setupSceneTemplateSceneForCS(currentTemplateScene); //override with CS cameras
			currentTemplateSceneTemplate = template;
			for (Model mdl : currentTemplateScene.getAllModels()) {
				mdl.isVisible = true; //override CS model visibility
			}
			currentModel = null;
			g3dViewport.scene.removeChild(scene);
			g3dViewport.scene.addChild(currentTemplateScene);

			resetCameraToModel();
		}
	}

	private void unsetupSceneTemplateSceneFromCS(G3DResourceInstance scene) {
		anmControl.unbindAnimationControllerList(scene.resourceAnimControllers);
		for (G3DResourceInstance child : scene.getChildren()) {
			unsetupSceneTemplateSceneFromCS(child);
		}
	}

	private void setupSceneTemplateSceneForCS(G3DResourceInstance inst) {
		inst.cameraInstances.clear();
		anmControl.bindAnimationControllerList(inst.resourceAnimControllers);
		for (G3DResourceInstance child : inst.getChildren()) {
			setupSceneTemplateSceneForCS(child);
		}
	}

	public boolean changeCamera(Camera cam) {
		if (!g3dViewport.scene.getLocalResCamAnimControllers().isEmpty()) {
			//Do not change camera if an animation is playing
			return false;
		}
		//System.out.println("req change to camera " + cam.name);
		if (cam == input.cam) {
			return false;
		}
		input.deactivate();
		input.cam = cam;
		input.activate();
		return true;
	}

	public final void changeLight(Light light) {
		isCustomLight = light != mainLight;
		scene.deleteAllLightInstances();
		if (light != null) {
			//System.out.println("req change to light " + light.name);
			//System.out.println(light.position + " d " + light.direction);
			for (int i = 0; i < 4; i++) {
				Light l = new Light(light.name + "_set" + i);
				l.setIndex = i;
				l.position = light.position;
				l.direction = light.direction;
				l.directional = light.directional;
				l.diffuseColor = light.diffuseColor;
				l.ambientColor = light.ambientColor;
				l.specular0Color = light.specular0Color;
				l.specular1Color = light.specular1Color;
				scene.instantiateLight(l);
			}
		}
	}

	public void setCustomLightEnable(Light customLight, boolean value) {
		if (customLight != null) {
			if (value) {
				if (!isCustomLight) {
					changeLight(null);
				}
				scene.instantiateLight(customLight);
			} else {
				scene.deinstantiateLight(customLight);
				if (scene.lights.isEmpty()) {
					changeLight(mainLight);
				}
			}
		}
	}

	public void stopAllCameraAnime() {
		/*		DefaultMutableTreeNode camDir = g2dFactory.getCreateDirNode(DirectoryNode.DirectoryNodeType.CAMERAS, sceneDataRoot, (CSSceneNodeG2D) sceneDataRoot.getUserObject());
		for (int i = 0; i < camDir.getChildCount(); i++) {
			CSSceneNodeG2D n = NodeG2DFactory.getCSNode(camDir.getChildAt(i));
			if (n.getTypeIsCheckBox()) {
				((CSCheckBoxAndLabel) n.getLabel()).setChecked(false);
			}
		}
		g3dViewport.scene.stopCameraAnimations();*/
	}

	public Camera getCameraByNameOrDefault(String name) {
		Camera cam = (Camera) scene.resource.getNamedResource(name, G3DResourceType.CAMERA);
		if (cam != null) {
			return cam;
		}
		return input.cam;
	}

	public void playAnimation(AbstractAnimation anm) {
		if (anm instanceof CameraAnimation) {
			playCameraAnime((CameraAnimation) anm);
		} else {
			scene.playAnimation(anm);
		}
	}

	public void playCameraAnime(CameraAnimation anm) {
		g3dViewport.scene.stopCameraAnimations();
		if (!anm.transforms.isEmpty()) {
			String cameraName = anm.transforms.get(0).name;
			Camera cam = (Camera) scene.resource.getNamedResource(cameraName, G3DResourceType.CAMERA);
			if (cam != null) {
				changeCamera(cam);
			} else {
				cam = new Camera(input.cam);
				cam.name = cameraName;
				changeCamera(cam);
			}
		}
		g3dViewport.scene.resourceAnimControllers.add(new CameraAnimationController(anm));
	}

	public void resetJointEditorSkeletonModel() {
		editors.jointEditor.resetSkeletonModel();
	}

	public void resetCameraToModel() {
		changeCamera(mainCamera);

		AABB6f aabb = new AABB6f();

		if (currentTemplateScene == null) {
			if (btnDrawAllModels.isSelected()) {
				scene.resource.updateBBox(false);
				aabb = scene.calcAABB();
			} else {
				if (currentModel != null) {
					aabb = currentModel.boundingBox;
				} else {
					aabb.min.set(-50f);
					aabb.max.set(50f);
				}
			}
		} else {
			aabb = currentTemplateScene.calcAABB();
		}
		if (aabb.min.equals(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)) {
			aabb.min.set(-50f);
		}
		if (aabb.max.equals(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)) {
			aabb.max.set(50f);
		}

		input.cam.mode = Camera.Mode.PERSPECTIVE;
		setNearFar(aabb);
		setCameraTransRot(aabb);
	}

	public void setNearFar(AABB6f aabb) {
		float far = Math.max(aabb.min.getHighestAbsComponent(), aabb.max.getHighestAbsComponent());
		input.cam.zFar = Math.max(far * 4f, 300f);
		input.cam.zNear = Math.max(0.01f, far / 300f);
	}

	public void setCameraTransRot(AABB6f aabb) {
		input.setSpeed(Math.max(aabb.max.getHighestAbsComponent(), aabb.min.getHighestAbsComponent()) / 20f);
		input.overrideCamera((aabb.max.x + aabb.min.x) / 2f, aabb.max.y * 0.5f, Math.max(aabb.max.getHighestAbsComponent(), aabb.min.getHighestAbsComponent()) * 1.5f, 0f, 0f, 0f);
	}

	public CSG3DSurface getRenderer() {
		return g3dViewport;
	}

	private JScrollPane editorScrollPane = new JScrollPane();

	public void switchEditorUI(IEditor editor) {
		save();
		if (editor instanceof JComponent) {
			JComponent edComp = (JComponent) editor;
			editorScrollPane.setViewportView(edComp);
			sceneEditorSP.setRightComponent(editorScrollPane);
			adjustSplitPanes();
		} else {
			throw new IllegalArgumentException("<editor> is not a GUI component.");
		}
	}

	public void reloadEditor() {
		CSNode n = getSelectedNode();
		if (n != null) {
			editors.switchEditorOpenObject(n.getEditor(editors), n);
		}
		else {
			editors.switchEditorOpenObject(null, dataTree.getRootCSNode());
		}
	}

	private void adjustSplitPanes() {
		Component edComp = sceneEditorSP.getRightComponent();
		edComp.setSize(edComp.getPreferredSize());
		double loc = 1d - (double) (edComp.getPreferredSize().width + sceneEditorSP.getDividerSize() + editorScrollPane.getVerticalScrollBar().getWidth() - 3) / (double) sceneEditorSP.getWidth();
		if (loc < 0.1) {
			loc = 0.1d;
		}
		sceneEditorSP.setDividerLocation(loc);
	}

	private void importCommonSeq() {
		saveData.raiseSaveFlag();
	}

	public void clear() {
		stopShowingSceneTemplate();
		g3dViewport.scene.stopAllAnimations();
		scene.stopAllAnimations();
		scene.clear();
		other.clear();
		currentTemplateScene = null;
		currentTemplateSceneTemplate = null;
		currentModel = null;
		resetCameraToModel();
		changeLight(mainLight);
	}

	public void importFiles(List<? extends FSFile> src) {
		G3DResource res = doCSGenericImport(src.toArray(new FSFile[src.size()]));
		merge(res);
		if (!src.isEmpty()) {
			importCommonSeq();
		}
	}

	@Override
	public void importFile(FSFile f) {
		if (f != null) {
			merge(doCSGenericImport(f));
			importCommonSeq();
		}
	}

	@Override
	public void importResource(G3DResource res) {
		if (res != null) {
			merge(res);
			importCommonSeq();
		}
	}

	@Override
	public Model getSupplementaryModelForExport(boolean skelOnly) {
		Model model = null;
		List<Model> models = getModels();
		if (!models.isEmpty()) {
			if (models.size() == 1) {
				model = models.get(0);
				return model;
			}
			ModelSelectionDialog dlg = new ModelSelectionDialog(this, true, models, skelOnly);
			dlg.setVisible(true);
			model = dlg.getResult();
			return model;
		} else {
			DialogUtils.showErrorMessage(this, "No models available", "This action requires a model" + (skelOnly ? " skeleton" : "") + ", however, no models are currently loaded.");
		}
		return model;
	}

	public CSNode getSelectedNode() {
		Object node = dataTree.getLastSelectedPathComponent();
		if (node != null) {
			if (node instanceof CSNode) {
				return (CSNode) node;
			}
		}
		return null;
	}

	public void save() {
		editors.save();
	}

	public boolean stopAnimation(AbstractAnimation anm) {
		return scene.removeAnimation(anm)
			|| g3dViewport.scene.removeAnimation(anm); //camera animations are bound to the main viewport
	}

	private static void syncNamedResourceListWith(List toSync, List<NamedResource> syncFrom, Class clazz) {
		for (NamedResource in : syncFrom) {
			if (clazz.isAssignableFrom(in.getClass())) {
				if (!toSync.contains(in)) {
					toSync.add(clazz.cast(in));
				}
			}
		}

		for (int i = 0; i < toSync.size(); i++) {
			if (!syncFrom.contains((NamedResource) toSync.get(i))) {
				toSync.remove(i);
				i--;
			}
		}
	}

	public G3DResource doCSGenericImport(FSFile... f) {
		return NGCSImporter.importFiles(this, f);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shaderTypeBtnGroup = new javax.swing.ButtonGroup();
        sceneEditorSP = new javax.swing.JSplitPane();
        g2dg3dSP = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        dataTree = new ctrmap.creativestudio.ngcs.tree.CSJTree();
        g3dAnmCtrlSplit = new javax.swing.JSplitPane();
        g3dViewport = new ctrmap.creativestudio.ngcs.CSG3DSurface();
        anmControl = new ctrmap.creativestudio.ngcs.CSAnimationControlPanel();
        menuBar = new javax.swing.JMenuBar();
        projectMenu = new javax.swing.JMenu();
        btnNewProject = new javax.swing.JMenuItem();
        btnOpenCMIF = new javax.swing.JMenuItem();
        btnExportCMIF = new javax.swing.JMenuItem();
        btnSaveAs = new javax.swing.JMenuItem();
        importMenu = new javax.swing.JMenu();
        btnImportGeneric = new javax.swing.JMenuItem();
        btnMergeCMIF = new javax.swing.JMenuItem();
        btnImportTextures = new javax.swing.JMenuItem();
        exportMenu = new javax.swing.JMenu();
        g3dShaderChoiceMenu = new javax.swing.JMenu();
        btnShaderSetDefault = new javax.swing.JRadioButtonMenuItem();
        btnShaderSetLighting = new javax.swing.JRadioButtonMenuItem();
        btnShaderSetNormal = new javax.swing.JRadioButtonMenuItem();
        btnShaderSetTangent = new javax.swing.JRadioButtonMenuItem();
        btnShaderSetUV = new javax.swing.JRadioButtonMenuItem();
        btnShaderSetVCol = new javax.swing.JRadioButtonMenuItem();
        viewMenu = new javax.swing.JMenu();
        btnDrawAllModels = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CTRMap Creative Studio");
        setBackground(new java.awt.Color(255, 255, 255));
        setLocationByPlatform(true);

        sceneEditorSP.setDividerLocation(800);
        sceneEditorSP.setResizeWeight(0.9);

        g2dg3dSP.setDividerLocation(250);
        g2dg3dSP.setResizeWeight(0.1);

        dataTree.setImageSize(CS_IMAGE_ICON_DIM_MAX);
        dataTree.setRowHeight(CS_IMAGE_ICON_DIM_MAX);
        dataTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                dataTreeMousePressed(evt);
            }
        });
        treeScrollPane.setViewportView(dataTree);

        g2dg3dSP.setLeftComponent(treeScrollPane);

        g3dAnmCtrlSplit.setDividerLocation(360);
        g3dAnmCtrlSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        g3dAnmCtrlSplit.setResizeWeight(0.8);
        g3dAnmCtrlSplit.setTopComponent(g3dViewport);
        g3dAnmCtrlSplit.setRightComponent(anmControl);

        g2dg3dSP.setRightComponent(g3dAnmCtrlSplit);

        sceneEditorSP.setLeftComponent(g2dg3dSP);

        projectMenu.setText("Project");

        btnNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnNewProject.setText("New");
        btnNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProjectActionPerformed(evt);
            }
        });
        projectMenu.add(btnNewProject);

        btnOpenCMIF.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnOpenCMIF.setText("Open");
        btnOpenCMIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenCMIFActionPerformed(evt);
            }
        });
        projectMenu.add(btnOpenCMIF);

        btnExportCMIF.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnExportCMIF.setText("Save");
        btnExportCMIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportCMIFActionPerformed(evt);
            }
        });
        projectMenu.add(btnExportCMIF);

        btnSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnSaveAs.setText("Save As");
        btnSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAsActionPerformed(evt);
            }
        });
        projectMenu.add(btnSaveAs);

        menuBar.add(projectMenu);

        importMenu.setText("Import");

        btnImportGeneric.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnImportGeneric.setText("Generic");
        btnImportGeneric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportGenericActionPerformed(evt);
            }
        });
        importMenu.add(btnImportGeneric);

        btnMergeCMIF.setText("Merge CMIF scene");
        btnMergeCMIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMergeCMIFActionPerformed(evt);
            }
        });
        importMenu.add(btnMergeCMIF);

        btnImportTextures.setText("Texture(s)");
        btnImportTextures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportTexturesActionPerformed(evt);
            }
        });
        importMenu.add(btnImportTextures);

        menuBar.add(importMenu);

        exportMenu.setText("Export");
        menuBar.add(exportMenu);

        g3dShaderChoiceMenu.setText("Shaders");

        shaderTypeBtnGroup.add(btnShaderSetDefault);
        btnShaderSetDefault.setSelected(true);
        btnShaderSetDefault.setText("Default");
        g3dShaderChoiceMenu.add(btnShaderSetDefault);

        shaderTypeBtnGroup.add(btnShaderSetLighting);
        btnShaderSetLighting.setText("Lighting");
        g3dShaderChoiceMenu.add(btnShaderSetLighting);

        shaderTypeBtnGroup.add(btnShaderSetNormal);
        btnShaderSetNormal.setText("Normals");
        g3dShaderChoiceMenu.add(btnShaderSetNormal);

        shaderTypeBtnGroup.add(btnShaderSetTangent);
        btnShaderSetTangent.setText("Tangents");
        g3dShaderChoiceMenu.add(btnShaderSetTangent);

        shaderTypeBtnGroup.add(btnShaderSetUV);
        btnShaderSetUV.setText("UVs");
        g3dShaderChoiceMenu.add(btnShaderSetUV);

        shaderTypeBtnGroup.add(btnShaderSetVCol);
        btnShaderSetVCol.setText("Vertex colors");
        g3dShaderChoiceMenu.add(btnShaderSetVCol);

        menuBar.add(g3dShaderChoiceMenu);

        viewMenu.setText("View");

        btnDrawAllModels.setText("Show all models");
        btnDrawAllModels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDrawAllModelsActionPerformed(evt);
            }
        });
        viewMenu.add(btnDrawAllModels);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sceneEditorSP, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sceneEditorSP, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportCMIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportCMIFActionPerformed
		saveData.callSave();
    }//GEN-LAST:event_btnExportCMIFActionPerformed

    private void btnMergeCMIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMergeCMIFActionPerformed
		importCMIFSeq(XFileDialog.openFileDialog("Open a CMIF Scene file", CMIFFile.EXTENSION_FILTER), true);
    }//GEN-LAST:event_btnMergeCMIFActionPerformed

	public void resetResourceBBoxByModel(Model model) {
		for (G3DResourceInstance i : scene.getChildren()) {
			if (i.resource.models.contains(model)) {
				i.resource.updateBBox();
			}
		}
	}

    private void btnOpenCMIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenCMIFActionPerformed
		saveData.callOpen();
    }//GEN-LAST:event_btnOpenCMIFActionPerformed

    private void btnImportTexturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportTexturesActionPerformed
		List<DiskFile> files = XFileDialog.openMultiFileDialog();

		for (FSFile tex : files) {
			if (tex.exists()) {
				Texture t = TextureConverter.readTextureFromFile(tex);
				scene.addTexture(t);
			}
		}

		importCommonSeq();
    }//GEN-LAST:event_btnImportTexturesActionPerformed

    private void btnNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProjectActionPerformed
		saveData.callClear();
    }//GEN-LAST:event_btnNewProjectActionPerformed

    private void btnImportGenericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportGenericActionPerformed
		List<DiskFile> src = XFileDialog.openMultiFileDialog(CSNodeContentType.ALL.getFiltersImport(ioMgr));

		if (!src.isEmpty()) {
			importFiles(src);
		}
    }//GEN-LAST:event_btnImportGenericActionPerformed

    private void btnSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAsActionPerformed
		saveData.callSaveAs();
    }//GEN-LAST:event_btnSaveAsActionPerformed

    private void dataTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTreeMousePressed
		if (SwingUtilities.isRightMouseButton(evt)) {
			int selRow = dataTree.getRowForLocation(evt.getX(), evt.getY());
			TreePath selPath = dataTree.getPathForLocation(evt.getX(), evt.getY());
			dataTree.setSelectionPath(selPath);
			if (selRow > -1) {
				dataTree.setSelectionRow(selRow);
			}

			CSNode n = getSelectedNode();
			if (n != null) {
				NGCSNodePopupMenu m = new NGCSNodePopupMenu(n);
				if (m.makesSense()) {
					m.show(dataTree, evt.getX(), evt.getY());
				}
			}
		}
    }//GEN-LAST:event_dataTreeMousePressed

    private void btnDrawAllModelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDrawAllModelsActionPerformed
		if (btnDrawAllModels.isSelected()) {
			for (Model mdl : getModels()) {
				mdl.isVisible = true;
			}
		} else {
			for (Model mdl : getModels()) {
				mdl.isVisible = mdl == currentModel;
			}
		}
		resetCameraToModel();
    }//GEN-LAST:event_btnDrawAllModelsActionPerformed

	public void importCMIFSeq(FSFile cmifFile, boolean mergeMode) {
		if (cmifFile != null) {
			CMIFFile cmif = new CMIFFile(cmifFile);

			if (!mergeMode) {
				clear();
			}

			merge(cmif.toGeneric());

			if (mergeMode) {
				importCommonSeq();
			}
		}
	}

	public boolean isAllModelsVisible() {
		return btnDrawAllModels.isSelected();
	}

	public void merge(G3DResource rsc) {
		for (MetaDataValue v : rsc.metaData) {
			if (v.getType() == MetaDataValue.Type.RAW_BYTES) {
				other.add(new CMIFFile.OtherFile(v.getName(), v.byteArrValue()));
			}
		}
		scene.resource.mergeFull(rsc);
		if (rsc.models.size() == 1) {
			showLoadedModel(rsc.models.get(0));
			CSNode modelNode = dataTree.getRootCSNode().ascendByContent(ModelNode.class, rsc.models.get(0));
			if (modelNode != null) {
				dataTree.setSelectionPath(new TreePath(modelNode.getPath()));
				modelNode.setExpansionState(true);
			}
		}
		if (btnDrawAllModels.isSelected() && !rsc.models.isEmpty()) {
			resetCameraToModel();
		}
	}

	public void mergeModelJob(Model mdl) {
		if (mdl != null && currentModel != null) {
			Model target = currentModel;
			overwriteINamedList(mdl.materials, target.materials);
			overwriteINamedList(mdl.meshes, target.meshes);
			if (mdl.skeleton != null) {
				if (target.skeleton == null) {
					target.skeleton = new Skeleton();
				}
				overwriteINamedList(mdl.skeleton.getJoints(), target.skeleton.getJoints());
			}

			target.takeOwnMeshesAndMats();
		}
	}

	public static <T extends NamedResource> void overwriteINamedList(List<T> source, List<T> target) {
		for (T src : source) {
			T overwrite = (T) Scene.getNamedObject(src.getName(), target);
			int owIdx = -1;
			if (overwrite != null) {
				owIdx = target.indexOf(overwrite);
			}
			if (owIdx != -1) {
				target.set(owIdx, src);
			} else {
				target.add(src);
			}
		}
	}

	public Scene getScene() {
		return scene;
	}

	@Override
	public ListenableList<Model> getModels() {
		return scene.resource.models;
	}

	@Override
	public ListenableList<Texture> getTextures() {
		return scene.resource.textures;
	}

	@Override
	public ListenableList<Light> getLights() {
		return scene.resource.lights;
	}

	@Override
	public ListenableList<CMIFFile.OtherFile> getOthers() {
		return other;
	}

	@Override
	public List<AbstractAnimation> getAllAnimations() {
		return scene.resource.getAnimations();
	}

	@Override
	public ListenableList<MaterialAnimation> getMatAnime() {
		return scene.resource.materialAnimations;
	}

	@Override
	public ListenableList<SkeletalAnimation> getSklAnime() {
		return scene.resource.skeletalAnimations;
	}

	@Override
	public ListenableList<VisibilityAnimation> getVisAnime() {
		return scene.resource.visibilityAnimations;
	}

	@Override
	public ListenableList<CameraAnimation> getCamAnime() {
		return scene.resource.cameraAnimations;
	}

	@Override
	public ListenableList<Camera> getCameras() {
		return scene.resource.cameras;
	}

	@Override
	public ListenableList<G3DSceneTemplate> getSceneTemplates() {
		return scene.resource.sceneTemplates;
	}

	public CMIFFile getCMIF() {
		CMIFFile file = new CMIFFile(getResource());
		file.other.addAll(getOthers());
		return file;
	}

	@Override
	public G3DResource getResource() {
		return scene.resource;
	}

	public Camera callCameraSelect() {
		List<Camera> cameras = getCameras();
		if (!cameras.isEmpty()) {
			Camera camera = null;
			if (cameras.size() == 1) {
				camera = cameras.get(0);
				return camera;
			}
			CameraSelectionDialog dlg = new CameraSelectionDialog(this, true, cameras);
			dlg.setVisible(true);
			camera = dlg.getResult();
			return camera;
		} else {
			return null;
		}
	}

	/*
	Used by CreativeStudioChecker.
	 */
	public static void dummy() {

	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(() -> {
			ComponentUtils.setSystemNativeLookAndFeel();
			NGCS cs = new NGCS();
			cs.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			cs.setVisible(true);
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ctrmap.creativestudio.ngcs.CSAnimationControlPanel anmControl;
    private javax.swing.JCheckBoxMenuItem btnDrawAllModels;
    private javax.swing.JMenuItem btnExportCMIF;
    private javax.swing.JMenuItem btnImportGeneric;
    private javax.swing.JMenuItem btnImportTextures;
    private javax.swing.JMenuItem btnMergeCMIF;
    private javax.swing.JMenuItem btnNewProject;
    private javax.swing.JMenuItem btnOpenCMIF;
    private javax.swing.JMenuItem btnSaveAs;
    private javax.swing.JRadioButtonMenuItem btnShaderSetDefault;
    private javax.swing.JRadioButtonMenuItem btnShaderSetLighting;
    private javax.swing.JRadioButtonMenuItem btnShaderSetNormal;
    private javax.swing.JRadioButtonMenuItem btnShaderSetTangent;
    private javax.swing.JRadioButtonMenuItem btnShaderSetUV;
    private javax.swing.JRadioButtonMenuItem btnShaderSetVCol;
    private ctrmap.creativestudio.ngcs.tree.CSJTree dataTree;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JSplitPane g2dg3dSP;
    private javax.swing.JSplitPane g3dAnmCtrlSplit;
    private javax.swing.JMenu g3dShaderChoiceMenu;
    private ctrmap.creativestudio.ngcs.CSG3DSurface g3dViewport;
    private javax.swing.JMenu importMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JSplitPane sceneEditorSP;
    private javax.swing.ButtonGroup shaderTypeBtnGroup;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}
