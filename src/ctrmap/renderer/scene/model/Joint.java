package ctrmap.renderer.scene.model;

import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.util.List;

public class Joint implements NamedResource {

	public static final int BB_AXIS_X = (1 << 0);
	public static final int BB_AXIS_Y = (1 << 1);
	public static final int BB_AXIS_Z = (1 << 2);
	public static final int SCALE_COMPENSATE = (1 << 4);
	public static final int BB_AIM = (1 << 31);

	public Skeleton parentSkeleton;

	public Skeleton.KinematicsRole kinematicsRole = Skeleton.KinematicsRole.NONE;
	public int flags = 0;

	public String name = "Joint";
	public String parentName;

	public Vec3f position = new Vec3f();
	public Vec3f rotation = new Vec3f();
	public Vec3f scale = Vec3f.ONE();

	public Joint() {
	}

	public Joint(Joint j) {
		kinematicsRole = j.kinematicsRole;
		name = j.name;
		flags = j.flags;
		parentName = j.parentName;
		position = new Vec3f(j.position);
		rotation = new Vec3f(j.rotation);
		scale = new Vec3f(j.scale);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public boolean isFlag(int flag) {
		return (flags & flag) != 0;
	}

	public void setFlag(int flag, boolean value) {
		flags = flags & (~flag) | (value ? flag : 0);
	}

	public boolean isBBX() {
		return isFlag(BB_AXIS_X);
	}

	public boolean isBBY() {
		return isFlag(BB_AXIS_Y);
	}

	public boolean isBBZ() {
		return isFlag(BB_AXIS_Z);
	}

	public boolean isBBAim() {
		return isFlag(BB_AIM);
	}

	public boolean isScaleCompensate() {
		return isFlag(SCALE_COMPENSATE);
	}

	public boolean isBillboard() {
		return isBBX() || isBBY() || isBBZ();
	}

	public void setBBX(boolean value) {
		setFlag(BB_AXIS_X, value);
	}

	public void setBBY(boolean value) {
		setFlag(BB_AXIS_Y, value);
	}

	public void setBBZ(boolean value) {
		setFlag(BB_AXIS_Z, value);
	}

	public void setBBAim(boolean value) {
		setFlag(BB_AIM, value);
	}

	public void setScaleCompensate(boolean value) {
		setFlag(SCALE_COMPENSATE, value);
	}

	public List<Joint> getChildren() {
		if (parentSkeleton == null) {
			System.err.println("Joint " + name + " has no parent skeleton!!!");
			return null;
		}
		return parentSkeleton.getChildrenOf(this);
	}

	public Joint getParent() {
		return parentSkeleton.getJoint(parentName);
	}

	public int getIndex() {
		return parentSkeleton.getJointIndex(this);
	}

	public Joint getChildByType(Skeleton.KinematicsRole type) {
		if (this.kinematicsRole == type) {
			return this;
		} else {
			for (Joint ch : getChildren()) {
				Joint j = ch.getChildByType(type);
				if (j != null) {
					return j;
				}
			}
		}
		return null;
	}
	
	public Matrix4 getLocalMatrix() {
		return getLocalMatrix(new Matrix4());
	}

	public Matrix4 getLocalMatrix(Matrix4 dest) {
		dest.translation(position);
		dest.rotate(rotation);
		dest.scale(scale);
		if (isScaleCompensate() && parentName != null) {
			Joint parent = getParent();
			if (parent != null) {
				dest.scale(parent.scale.clone().recip());
			}
		}
		return dest;
	}

}
