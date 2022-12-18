package ctrmap.editor.gui.editors.scenegraph;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.CreativeStudioChecker;
import ctrmap.creativestudio.editors.MaterialEditor;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.scenegraph.editors.AnimationControllerEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.CameraEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.IScenegraphEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.JointEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.KinematicsEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.LightEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.MeshInfoViewer;
import ctrmap.editor.gui.editors.scenegraph.editors.ResourceInfoViewer;
import ctrmap.editor.gui.editors.scenegraph.editors.SceneInstanceEditor;
import ctrmap.editor.gui.editors.scenegraph.editors.TextureViewer;
import ctrmap.editor.gui.editors.scenegraph.tree.ScenegraphExplorerNode;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.editor.gui.editors.scenegraph.tools.ScenegraphTool;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.scene.Scene;
import xstandard.gui.components.tree.CustomJTreeNode;
import xstandard.util.ArraysEx;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.system.workspace.CTRMapProject;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class ScenegraphExplorer extends javax.swing.JPanel implements AbstractToolbarEditor {

	public final LightEditor lightEditor = new LightEditor();
	public final AnimationControllerEditor animeCtrlEditor = new AnimationControllerEditor();
	public final SceneInstanceEditor instanceEditor = new SceneInstanceEditor();
	public final JointEditor jointEditor = new JointEditor();
	public final KinematicsEditor kinematicsEditor = new KinematicsEditor();
	public final CameraEditor cameraEditor = new CameraEditor();
	public final TextureViewer textureViewer = new TextureViewer();
	public final ResourceInfoViewer resViewer = new ResourceInfoViewer();
	public final MeshInfoViewer meshViewer = new MeshInfoViewer();
	public final IScenegraphEditor materialEditor;

	private ScenegraphTool tool;
	
	private AbstractBackend backend;
		
	public ScenegraphExplorer(AbstractPerspective p) {
		this(p.getRenderer());
	}

	public ScenegraphExplorer(AbstractBackend backend) {
		initComponents();

		tool = new ScenegraphTool(this);

		loadBackend(backend);

		if (CreativeStudioChecker.isCreativeStudioPresent()) {
			materialEditor = new ScenegraphExplorerNgcsEditorAdapter(new MaterialEditor());
		} else {
			materialEditor = null;
		}

		sceneTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object obj = sceneTree.getLastSelectedPathComponent();
				if (obj instanceof ScenegraphExplorerNode) {
					((ScenegraphExplorerNode) obj).onSelected(ScenegraphExplorer.this);
				}
			}
		});

		sceneTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int selRow = sceneTree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = sceneTree.getPathForLocation(e.getX(), e.getY());
					sceneTree.setSelectionPath(selPath);
					if (selRow > -1) {
						sceneTree.setSelectionRow(selRow);
					}

					Object obj = sceneTree.getLastSelectedPathComponent();
					if (obj instanceof CustomJTreeNode) {
						((CustomJTreeNode) obj).onNodePopupInvoke(e);
					}
				}
			}
		});

		editSPContainer.getVerticalScrollBar().setUnitIncrement(8);
	}

	public void loadScene(Scene s) {
		sceneTree.loadRootScene(s);
	}

	public void loadBackend(AbstractBackend backend) {
		this.backend = backend;
		if (backend != null) {
			loadScene(backend.getScene());
		}
	}

	public void loadObjToEditor(IScenegraphEditor edt, Object o) {
		editSPContainer.setViewportView(edt == null ? null : edt.getGUI());
		if (edt != null) {
			edt.load(o);
		}
	}

	@Override
	public List<AbstractTool> getTools() {
		return ArraysEx.asList(tool);
	}

	@Override
	public boolean isDebugOnly() {
		return true;
	}
	
	@Override
	public boolean isSharedInstance() {
		return true;
	}
	
	@Override
	public void onProjectLoaded(CTRMapProject proj) {
		if (backend != null) {
			loadScene(backend.getScene());
		}
	}
	
	@Override
	public void onProjectUnloaded(CTRMapProject proj) {
		loadScene(new Scene("Dummy"));
	}

	private static class ScenegraphExplorerNgcsEditorAdapter implements IScenegraphEditor {
		private final IEditor ngcsEditor;
		
		public ScenegraphExplorerNgcsEditorAdapter(IEditor ngcsEditor) {
			this.ngcsEditor = ngcsEditor;
		}

		@Override
		public void load(Object o) {
			ngcsEditor.handleObject(o);
		}
		
		@Override
		public Component getGUI() {
			return (Component) ngcsEditor;
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeSP = new javax.swing.JScrollPane();
        sceneTree = new ctrmap.editor.gui.editors.scenegraph.tree.ScenegraphJTree();
        editSPContainer = new javax.swing.JScrollPane();

        treeSP.setViewportView(sceneTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(editSPContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .addComponent(treeSP))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(treeSP, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editSPContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editSPContainer;
    private ctrmap.editor.gui.editors.scenegraph.tree.ScenegraphJTree sceneTree;
    private javax.swing.JScrollPane treeSP;
    // End of variables declaration//GEN-END:variables
}
