package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.NGCSExporter;
import ctrmap.creativestudio.ngcs.io.NGCSImporter;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.fs.FSFile;
import xstandard.gui.components.tree.CustomJTreeCellRenderer;
import xstandard.gui.components.tree.CustomJTreeNode;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.ExtensionFilter;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class CSNode extends CustomJTreeNode {

	protected final CSJTree tree;

	private List<CSNodeAction> actions = new ArrayList<>();

	public CSNode(CSJTree tree) {
		this(tree, (CSNodeContentType) null);
	}

	public CSNode(CSJTree tree, CSNodeContentType type) {
		super();
		this.tree = tree;
		setupType(type);
	}

	public CSNode(CSJTree tree, CustomJTreeCellRenderer rnd) {
		super(rnd);
		this.tree = tree;
		setupType(getContentType());
	}

	public void setExpansionState(boolean state) {
		TreeNode[] path = getPath();
		TreePath tp = new TreePath(path);
		if (state) {
			tree.expandPath(tp);
		} else {
			tree.collapsePath(tp);
		}
	}

	public <T extends CSNode> T descend(Class<T> nodeClass) {
		TreeNode parentNode = (TreeNode) getParent();
		while (parentNode != null) {
			if (parentNode.getClass().isAssignableFrom(nodeClass)) {
				return (T) parentNode;
			}
			parentNode = parentNode.getParent();
		}
		return null;
	}

	public <T extends CSNode> T ascendByContent(Class<T> nodeClass, NamedResource content) {
		for (int i = 0; i < getChildCount(); i++) {
			TreeNode ch = getChildAt(i);
			if (ch instanceof CSNode) {
				CSNode chcs = (CSNode) ch;
				if (chcs.getClass().isAssignableFrom(nodeClass)) {
					if (chcs.getContent() == content) {
						return (T) chcs;
					}
				}
				T tryResult = chcs.ascendByContent(nodeClass, content);
				if (tryResult != null) {
					return tryResult;
				}
			}
		}
		return null;
	}

	public IEditor getEditor(NGEditorController editors) {
		return null;
	}

	protected boolean getAllowDefaultNodeActions() {
		return true;
	}

	private void setupType(CSNodeContentType type) {
		if (type == null) {
			type = getContentType();
		}

		if (getAllowDefaultNodeActions()) {
			if (type.checkCap(CSNodeContentCapabilities.CAP_IMPORT_REPLACE)) {
				if (type.canImport(getCS().getIOManager())) {
					registerAction("Replace", this::callReplace);
				}
			}
			if (type.checkCap(CSNodeContentCapabilities.CAP_EXPORT)) {
				if (type.canExport(getCS().getIOManager())) {
					registerAction("Export", this::callExport);
				}
			}
			registerAction("Remove", this::callRemove);
		}
	}

	public List<CSNodeAction> getActions() {
		return actions;
	}

	public void callAction(String command) {
		for (CSNodeAction a : actions) {
			if (a.name.equals(command)) {
				a.callback.run();
			}
		}
	}

	public final void registerActionPrepend(String actionButtonName, Runnable action) {
		actions.add(0, new CSNodeAction(actionButtonName, action));
	}

	public final void registerAction(String actionButtonName, Runnable action) {
		actions.add(new CSNodeAction(actionButtonName, action));
	}

	public abstract ListenableList getParentList();

	public void callRemove() {
		getParentList().remove(getContent());
		getCS().reloadEditor(); //will stop editing this object
	}

	public void replaceContent(NamedResource replacement) {
		ListenableList list = getParentList();
		if (list != null) {
			NamedResource content = getContent();
			int index = list.indexOf(content);
			if (index != -1) {
				replacement.setName(content.getName());
				setContent(replacement);
				list.setModify(index, replacement);
				onReplaceFinish(content);
			}
		}
	}

	public void callReplace() {
		NGCS cs = getCS();
		NGCSIOManager ioMgr = cs.getIOManager();
		CSNodeContentType type = getContentType();
		FSFile repl = XFileDialog.openFileDialog(type.getFiltersImport(ioMgr));
		if (repl != null) {
			G3DResource res = NGCSImporter.importFiles(cs, type.getFormats(ioMgr), repl);
			NamedResource replacement = getReplacement(res);
			if (replacement != null) {
				replaceContent(replacement);
			}
		}
	}

	public void callExport() {
		if (getContent() != null) {
			NGCS cs = getCS();
			NGCSIOManager ioMgr = cs.getIOManager();
			CSNodeContentType type = getContentType();
			ExtensionFilter[] filters = type.getFiltersExport(ioMgr);
			FSFile target = XFileDialog.openSaveFileDialog(null, getContent().getName(), filters);
			ExtensionFilter selFilter = ExtensionFilter.findByFileName(target, filters);
			if (target != null && selFilter != null) {
				IG3DFormatHandler hnd = IG3DFormatHandler.findByFilter(selFilter, IG3DFormatHandler.G3DFMT_EXPORT, type.getFormats(ioMgr));
				if (hnd != null) {
					G3DResource exportDmyRes = new G3DResource();
					putForExport(exportDmyRes);
					NGCSExporter.exportFiles(cs, hnd, new NGCSExporter.G3DResourceExportParam(exportDmyRes, target));
				}
			}
		}
	}

	public void putForExport(G3DResource dest) {

	}

	public NamedResource getReplacement(G3DResource source) {
		return null;
	}

	protected static Model getDmyModel(G3DResource rsc) {
		if (rsc.models.isEmpty()) {
			Model mdl = new Model();
			mdl.name = "Model";
			rsc.addModel(mdl);
			return mdl;
		}
		return rsc.models.get(0);
	}

	protected static SkeletalAnimation getDmySkelAnm(G3DResource rsc, float frameCount) {
		if (rsc.cameraAnimations.isEmpty()) {
			SkeletalAnimation skelAnm = new SkeletalAnimation();
			skelAnm.frameCount = frameCount;
			skelAnm.name = "SkeletalAnimation";
			rsc.addAnime(skelAnm);
			return skelAnm;
		}
		return rsc.skeletalAnimations.get(0);
	}

	protected static CameraAnimation getDmyCamAnm(G3DResource rsc, float frameCount) {
		if (rsc.cameraAnimations.isEmpty()) {
			CameraAnimation camAnm = new CameraAnimation();
			camAnm.frameCount = frameCount;
			camAnm.name = "CameraAnimation";
			rsc.addAnime(camAnm);
			return camAnm;
		}
		return rsc.cameraAnimations.get(0);
	}

	protected static <T extends NamedResource> T getFirst(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public boolean getShouldResetCamera() {
		return true;
	}

	public boolean getShouldResetLights() {
		return true;
	}

	public NGCS getCS() {
		return tree.getCS();
	}

	@Override
	public void removeAllChildren() {
		while (getChildCount() > 0) {
			removeChild((CSNode) getChildAt(0));
		}
	}

	public void addChild(CSNode ch) {
		addChild(getChildCount(), ch);
	}

	public void addChild(int index, CSNode ch) {
		tree.getModel().insertNodeInto(ch, this, index);
	}

	private void callChildRemoved(CSNode ch) {
		for (int i = 0; i < ch.getChildCount(); i++) {
			callChildRemoved((CSNode) ch.getChildAt(i));
		}
		ch.onNodeRemoved();
	}

	public void removeChild(CSNode ch) {
		callChildRemoved(ch);
		tree.getModel().removeNodeFromParent(ch);
	}

	@Override
	public void removeFromParent() {
		tree.getModel().removeNodeFromParent(this);
		setParent(null);
	}

	public void updateThis() {
		tree.stopEditing();
		tree.getModel().nodeChanged(this);
		updateCellUI();
	}

	public void onNodeRemoved() {

	}

	public void onReplaceFinish(Object lastContent) {
		getCS().reloadEditor();
	}

	public abstract NamedResource getContent();

	public abstract void setContent(NamedResource cnt);

	public abstract CSNodeContentType getContentType();

	public static class CSNodeAction {

		public String name;
		public Runnable callback;

		private CSNodeAction(String actionButtonName, Runnable action) {
			name = actionButtonName;
			callback = action;
		}
	}
}
