package ctrmap.editor.gui.editors.common.tools.worldobj;

import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalController;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectiveIAModel extends ModelInstance {

	private ListenableList<? extends WorldObject> list;
	private Map<WorldObject, Joint> jointMap = new HashMap<>();
	private MaterialProvider matProvider;

	private Model model = new Model();
	private SkeletalAnimation anime = new SkeletalAnimation();
	private SkeletalController animeCtrl = new SkeletalController(anime, model.skeleton);

	ListenableList.ElementChangeListener listener = (ListenableList.ElementChangeEvent evt) -> {
		createModel();
	};

	public CollectiveIAModel(ListenableList<? extends WorldObject> list, MaterialProvider provider) {
		if (true) {
			throw new UnsupportedOperationException("DO NOT USE!");
		}
		this.list = list;
		matProvider = provider;
		list.addListener(listener);
		initModel();
		createModel();
		setResource(new G3DResource(model));
		anime.frameCount = 1f;
		resourceAnimControllers.add(animeCtrl);
	}

	public void destroy() {
		list.removeListener(listener);
	}

	private void initModel() {
		model.name = "CollectiveIAModel";
		model.addMaterial(matProvider.getLineMaterial());
	}

	public void createModel() {
		Mesh meshLine = new Mesh();
		meshLine.name = "IA_Mesh_Line";
		meshLine.materialName = WorldObjInstanceAdapter.G3D_LINE_MAT_NAME;
		meshLine.metaData.putValue(ReservedMetaData.LINE_WIDTH, 3);
		meshLine.hasBoneIndices = true;
		meshLine.hasBoneWeights = true;
		meshLine.primitiveType = PrimitiveType.LINES;

		syncJoints();

		for (int i = 0; i < list.size(); i++) {
			WorldObject wobj = list.get(i);
			WorldObject next = i + 1 < list.size() ? list.get(i + 1) : null;
			if (next != null) {
				Vertex v0 = new Vertex();
				Vertex v1 = new Vertex();
				v0.position = new Vec3f();
				v1.position = new Vec3f();
				v0.boneIndices.add(model.skeleton.getJointIndex(jointMap.get(wobj)));
				v1.boneIndices.add(model.skeleton.getJointIndex(jointMap.get(next)));
				v0.weights.add(1f);
				v1.weights.add(1f);

				meshLine.vertices.add(v0);
				meshLine.vertices.add(v1);
			}
		}

		model.meshes.clear();
		model.addMesh(meshLine);
		updateAnime();
	}

	public void updateAnime() {
		Vec3f vec000 = Vec3f.ZERO();
		Vec3f vec111 = Vec3f.ONE();

		for (WorldObject obj : list) {
			String name = Integer.toHexString(obj.hashCode());
			SkeletalBoneTransform bt = (SkeletalBoneTransform) anime.getBoneTransform(name);
			if (bt == null) {
				bt = new SkeletalBoneTransform();
				bt.name = name;
				anime.bones.add(bt);
			}
			bt.pushFullBakedFrame(0f, obj.getWPos(), vec000, vec111);
		}
	}

	public void syncJoints() {
		List<WorldObject> invalid = new ArrayList<>();
		for (WorldObject existing : jointMap.keySet()) {
			if (!list.contains(existing)) {
				invalid.add(existing);
			}
		}
		for (WorldObject o : invalid) {
			model.skeleton.getJoints().remove(jointMap.get(o));
			jointMap.remove(o);
		}

		for (WorldObject wo : list) {
			Joint j = jointMap.getOrDefault(wo, new Joint());
			if (!model.skeleton.getJoints().contains(j)) {
				j.name = Integer.toHexString(wo.hashCode());
				model.skeleton.addJoint(j);
				jointMap.put(wo, j);
			}
		}
	}
}
