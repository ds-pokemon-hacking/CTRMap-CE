package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.dialogs.BatchExportFormatSelector;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.NGCSExporter;
import ctrmap.creativestudio.ngcs.io.NGCSImporter;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.ExtensionFilter;
import xstandard.text.FormattingUtils;
import xstandard.util.ArraysEx;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ContainerNode extends CSNode {

	public static final int RESID_DEFAULT = 1;

	private String name;
	private int resId;

	private final DummyResource dmyRes;

	private ListenableList list;
	private final CSNodeContentType childCntType;

	public ContainerNode(String name, CSNodeContentType childCntType, ListenableList<? extends NamedResource> list, int resid, CSJTree tree) {
		super(tree, childCntType);
		this.list = list;
		this.name = name;
		this.childCntType = childCntType;
		this.resId = resid;
		this.dmyRes = new DummyResource();
		
		NGCSIOManager ioMgr = getCS().getIOManager();

		registerAction("Add", this::callAdd);
		registerAction("Import", this::callImportAllNoReplace);
		if (childCntType.canExport(ioMgr)) {
			registerAction("Export All", this::callExportAll);
		}
		if (childCntType.canImport(ioMgr)) {
			registerAction("Replace All", this::callReplaceAll);
		}
		registerAction("Clear", this::callClear);
	}
	
	protected void rebind(ListenableList<? extends NamedResource> list) {
		this.list = list;
	}

	@Override
	protected boolean getAllowDefaultNodeActions() {
		return false;
	}

	private void callAdd() {
		NamedResource newElem = null;

		switch (childCntType) {
			case ANIMATION_C:
				newElem = new CameraAnimation();
				break;
			case ANIMATION_M:
				newElem = new MaterialAnimation();
				break;
			case ANIMATION_S:
				newElem = new SkeletalAnimation();
				break;
			case ANIMATION_V:
				newElem = new VisibilityAnimation();
				break;
			case CAMERA:
				newElem = new Camera();
				break;
			case LIGHT:
				newElem = new Light("Light");
				break;
			case MODEL:
				newElem = new Model();
				break;
			case TEXTURE:
				newElem = new Texture(16, 16);
				break;
			case OTHER:
				newElem = new CMIFFile.OtherFile();
				((CMIFFile.OtherFile)newElem).data = new byte[0];
				break;
			case JOINT:
				newElem = new Joint();
				break;
			case MATERIAL:
				newElem = new Material();
				break;
			case MESH:
				newElem = new Mesh();
				break;
			case SCENE_TEMPLATE:
				newElem = new G3DSceneTemplate();
				break;
			case VISGROUP:
				newElem = new MeshVisibilityGroup("VisGroup");
				break;
		}

		if (newElem != null) {
			newElem.setName(childCntType.name);
			G3DResource.addListPrededupe(list, ArraysEx.asList(newElem), null);
			setExpansionState(true);
		}
	}
	
	private void callClear() {
		int cc = getChildCount();
		for (int i = 0; i < cc; i++) {
			((CSNode)getChildAt(0)).callRemove();
		}
	}

	private void callImportAllNoReplace() {
		callImportAll(false);
	}

	private void callReplaceAll() {
		callImportAll(true);
	}

	public void callImportAll(boolean replace) {
		NGCSIOManager ioMgr = getCS().getIOManager();
		List<DiskFile> sourceFiles = XFileDialog.openMultiFileDialog(childCntType.getFiltersImport(ioMgr));

		G3DResource imported = NGCSImporter.importFiles(getCS(), childCntType.getFormats(ioMgr), sourceFiles.toArray(new DiskFile[sourceFiles.size()]));

		List<? extends NamedResource> sourceList = null;

		switch (childCntType) {
			case ANIMATION_C:
				sourceList = imported.cameraAnimations;
				break;
			case ANIMATION_M:
				sourceList = imported.materialAnimations;
				break;
			case ANIMATION_S:
				sourceList = imported.skeletalAnimations;
				break;
			case ANIMATION_V:
				sourceList = imported.visibilityAnimations;
				break;
			case CAMERA:
				sourceList = imported.cameras;
				break;
			case LIGHT:
				sourceList = imported.lights;
				break;
			case MODEL:
				sourceList = imported.models;
				break;
			case TEXTURE:
				sourceList = imported.textures;
				break;
			case OTHER:
				List<CMIFFile.OtherFile> l = new ArrayList<>();
				for (MetaDataValue val : imported.metaData) {
					if (val.getType() == MetaDataValue.Type.RAW_BYTES) {
						l.add(new CMIFFile.OtherFile(val.getName(), val.byteArrValue()));
					}
				}
				sourceList = l;
				break;
			case JOINT: {
				List<Joint> allJoints = new ArrayList<>();
                for (Model mdl : imported.models) {
					if (mdl.materials != null) {
                    	allJoints.addAll(mdl.skeleton.getJoints());
					}
                }
                sourceList = allJoints;
                break;
			}
			case MATERIAL: {
				List<Material> allMaterials = new ArrayList<>();
                for (Model mdl : imported.models) {
                    allMaterials.addAll(mdl.materials);
                }
                sourceList = allMaterials;
                break;
			}
			case MESH:
				List<Mesh> allMeshes = new ArrayList<>();
                for (Model mdl : imported.models) {
                    allMeshes.addAll(mdl.meshes);
                }
                sourceList = allMeshes;
				break;
		}

		if (sourceList != null && !sourceList.isEmpty()) {
			HashSet<String> names = new HashSet<>();
			for (NamedResource r : sourceList) {
				String name = r.getName();
				if (!names.contains(name)) {
					names.add(name);
				}
			}
			if (replace) {
				for (int i = 0; i < list.size(); i++) {
					if (names.contains(((NamedResource) list.get(i)).getName())) {
						list.remove(i);
						i--;
					}
				}
			}

			G3DResource.addListPrededupe(list, sourceList, childCntType.name);
			
			setExpansionState(true);
		}
	}

	public void callExportAll() {
		FSFile targetDir = XFileDialog.openDirectoryDialog("Select a directory for the exported files");
		if (targetDir != null) {
			NGCSIOManager ioMgr = getCS().getIOManager();
			BatchExportFormatSelector formatSelector = new BatchExportFormatSelector(getCS(), true, childCntType.getFiltersExport(ioMgr));
			formatSelector.setVisible(true);
			ExtensionFilter result = formatSelector.getResult();
			if (result != null) {
				IG3DFormatHandler formatHandler = IG3DFormatHandler.findByFilter(result, IG3DFormatHandler.G3DFMT_EXPORT, childCntType.getFormats(ioMgr));
				if (formatHandler != null) {
					String extension = result.getPrimaryExtension();
					NGCSExporter.G3DResourceExportParam exportParams[] = new NGCSExporter.G3DResourceExportParam[list.size()];
					targetDir.mkdir();
					for (int i = 0; i < exportParams.length; i++) {
						G3DResource res = new G3DResource();
						((CSNode)getChildAt(i)).putForExport(res);
						exportParams[i] = new NGCSExporter.G3DResourceExportParam(
							res,
							targetDir.getChild(FormattingUtils.getStrForValidFileName(((NamedResource)list.get(i)).getName()) + extension)
						);
					}
					NGCSExporter.exportFiles(getCS(), formatHandler, exportParams);
				}
				else {
					System.err.println("CRITICAL: Could not match export handler for filter " + result);
				}
			}
		}
	}

	@Override
	public int getIconResourceID() {
		return resId == -1 ? RESID_DEFAULT : resId;
	}

	@Override
	public String getNodeName() {
		return name;
	}

	@Override
	public NamedResource getContent() {
		return dmyRes;
	}

	@Override
	public ListenableList getParentList() {
		return new ListenableList(); //can not be removed
	}

	@Override
	public CSNodeContentType getContentType() {
		return childCntType;
	}

	@Override
	public void setContent(NamedResource cnt) {

	}

	private class DummyResource implements NamedResource {

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			//ContainerNode.this.name = name;
			throw new UnsupportedOperationException();
		}

	}
}
