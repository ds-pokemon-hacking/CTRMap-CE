package ctrmap.renderer.scene.model;

import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.Scene;
import xstandard.math.vec.Matrix4;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class Skeleton implements NamedResource, Iterable<Joint> {

	public String name = "Skeleton";

	private ListenableList<Joint> bones = new ListenableList<>();

	public List<Matrix4> bindTransforms = new ArrayList<>();
	
	public Skeleton(){
		bones.addListener(new ListenableList.ElementChangeListener() {
			@Override
			public void onEntityChange(ListenableList.ElementChangeEvent evt) {
				if (evt.type == ListenableList.ElementChangeType.ADD) {
					Joint j = (Joint) evt.element;
					if (j != null) {
						j.parentSkeleton = Skeleton.this;
					}
				}
			}
		});
	}
	
	public Skeleton(Skeleton skl){
		this();
		this.name = skl.name;
		for (Joint j : skl.bones){
			addJoint(new Joint(j));
		}
	}

	public Joint getJoint(String name) {
		return (Joint) Scene.getNamedObject(name, bones);
	}

	public Joint getJoint(int index) {
		if (index >= 0 && index < bones.size()) {
			return bones.get(index);
		}
		return null;
	}

	public List<Joint> getChildrenOf(Joint j) {
		List<Joint> l = new ArrayList<>();
		for (Joint ch : bones) {
			if (j.name.equals(ch.parentName)) {
				l.add(ch);
			}
		}
		return l;
	}

	public ListenableList<Joint> getJoints() {
		return bones;
	}

	public int getJointCount() {
		return bones.size();
	}

	public void addJoints(Collection<Joint> l) {
		for (Joint j : l) {
			addJoint(j);
		}
	}

	public void addJoint(Joint j) {
		bones.add(j);
		bindTransforms.add(getAbsoluteJointBindPoseMatrix(j));
	}

	/**
	 * DO NOT USE
	 */
	public void sortBones() {
		List<Joint> newJoints = new ArrayList<>();
		addChildJoints(newJoints, null);
		bones.clear();
		bones.addAll(newJoints);
	}

	private List<Joint> addChildJoints(List<Joint> jointsNew, String parentName) {
		for (Joint j : bones) {
			if ((parentName == null && j.parentName == null) || (j.parentName != null && j.parentName.equals(parentName))) {
				jointsNew.add(j);
				addChildJoints(jointsNew, j.name);
			}
		}
		return jointsNew;
	}

	public void updateFrom(Skeleton skl) {
		for (Joint j : skl.bones) {
			Joint local = getJoint(j.name);
			if (local != null) {
				local.position = j.position;
				local.rotation = j.rotation;
				local.scale = j.scale;
			}
		}
	}

	public void buildTransforms() {
		bindTransforms.clear();
		for (Joint j : bones) {
			bindTransforms.add(getAbsoluteJointBindPoseMatrix(j));
		}
	}

	public Matrix4 getAbsoluteJointBindPoseMatrix(Joint j) {
		Stack<Integer> transforms = new Stack<>();

		int pid = bones.indexOf(j);
		while (pid != -1) {
			transforms.push(pid);
			pid = getJointIndex(bones.get(pid).parentName);
		}

		Matrix4 target = new Matrix4();

		while (!transforms.empty()) {
			target.mul(bones.get(transforms.pop()).getLocalMatrix());
		}

		return target;
	}

	public int getJointIndex(String name) {
		for (int i = 0; i < bones.size(); i++) {
			if (Objects.equals(bones.get(i).name, name)) {
				return i;
			}
		}
		return -1;
	}

	public int getJointIndex(Joint j) {
		return bones.indexOf(j);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("Skeleton names should remain default.");
	}

	@Override
	public Iterator<Joint> iterator() {
		return bones.iterator();
	}


	public static enum KinematicsRole {
		NONE,
		EFFECTOR,
		JOINT,
		CHAIN;
		
		public static final KinematicsRole[] VALUES = values();
	}
}
