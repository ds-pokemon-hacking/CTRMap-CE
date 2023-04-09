package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import static ctrmap.creativestudio.ngcs.tree.CSNodeContentCapabilities.*;
import xstandard.gui.file.ExtensionFilter;
import java.util.ArrayList;
import java.util.List;

public enum CSNodeContentType {
	MODEL("Model", CAP_ALL, CSG3DIOContentType.MODEL),
	MESH("Mesh", CAP_ALL, CSG3DIOContentType.MESH),
	MATERIAL("Material", CAP_ALL, CSG3DIOContentType.MATERIAL),
	TEXTURE("Texture", CAP_ALL, CSG3DIOContentType.TEXTURE),
	ANIMATION_S("SkeletalAnimation", CAP_ALL, CSG3DIOContentType.ANIMATION_SKL),
	ANIMATION_M("MaterialAnimation", CAP_ALL, CSG3DIOContentType.ANIMATION_MAT),
	ANIMATION_C("CameraAnimation", CAP_ALL, CSG3DIOContentType.ANIMATION_CAM),
	ANIMATION_V("VisibilityAnimation", CAP_ALL, CSG3DIOContentType.ANIMATION_VIS),
	ANIMATION_TARGET("AnimationTarget", 0),
	ANIMATION_TARGET_S("BoneTarget", CAP_ALL, CSG3DIOContentType.ANIMATION_CURVE_SKL),
	ANIMATION_TARGET_C("CameraTarget", CAP_ALL, CSG3DIOContentType.ANIMATION_CURVE_CAM),
	CAMERA("Camera", CAP_ALL, CSG3DIOContentType.CAMERA),
	LIGHT("Light", CAP_ALL, CSG3DIOContentType.LIGHT),
	JOINT("Joint", 0),
	VISGROUP("VisGroup", 0),
	VERTEX_MORPH("VertexMorph", CAP_ALL, CSG3DIOContentType.MESH),

	SCENE_TEMPLATE("SceneTemplate", CAP_ALL, CSG3DIOContentType.SCENE_TEMPLATE),
	SCENE_TEMPLATE_NODE("SceneTemplateNode", 0),

	OTHER("File", CAP_ALL, CSG3DIOContentType.UNKNOWN),
	ALL("All", CAP_ALL, new CSG3DIOContentType[]{null});

	public final String name;
	private final int caps;
	public final CSG3DIOContentType[] ioTypes;

	private CSNodeContentType(String friendlyName, int caps, CSG3DIOContentType... ioTypes) {
		this.name = friendlyName;
		this.caps = caps;
		this.ioTypes = ioTypes;
	}

	public boolean checkCap(int cap) {
		return (caps & cap) != 0;
	}

	public IG3DFormatHandler[] getFormats(NGCSIOManager io) {
		List<IG3DFormatHandler> out = new ArrayList<>();
		for (CSG3DIOContentType t : ioTypes) {
			for (IG3DFormatHandler h : io.getFormatHandlers(t)) {
				if (!out.contains(h)) {
					out.add(h);
				}
			}
		}
		return out.toArray(new IG3DFormatHandler[out.size()]);
	}

	public boolean canImport(NGCSIOManager io) {
		for (IG3DFormatHandler f : getFormats(io)) {
			if (f.canImport()) {
				return true;
			}
		}
		return false;
	}

	public boolean canExport(NGCSIOManager io) {
		for (IG3DFormatHandler f : getFormats(io)) {
			if (f.canExport()) {
				return true;
			}
		}
		return false;
	}

	public ExtensionFilter[] getFiltersImport(NGCSIOManager io) {
		List<ExtensionFilter> l = new ArrayList<>();
		for (IG3DFormatHandler h : getFormats(io)) {
			if (h.canImport()) {
				l.add(h.getExtensionFilter());
			}
		}
		return l.toArray(new ExtensionFilter[l.size()]);
	}

	public ExtensionFilter[] getFiltersExport(NGCSIOManager io) {
		List<ExtensionFilter> l = new ArrayList<>();
		for (IG3DFormatHandler h : getFormats(io)) {
			if (h.canExport()) {
				l.add(h.getExtensionFilter());
			}
		}
		return l.toArray(new ExtensionFilter[l.size()]);
	}
}
