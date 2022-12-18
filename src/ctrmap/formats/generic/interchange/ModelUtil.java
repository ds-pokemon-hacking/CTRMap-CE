package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.model.Joint;
import xstandard.io.InvalidMagicException;
import xstandard.math.vec.Vec3f;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.PointerTable;
import xstandard.io.structs.TemporaryOffset;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelUtil {

	public static final ExtensionFilter JOINT_EXTENSION_FILTER = new ExtensionFilter("Bone", "*.ifbn");

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Model", "*.ifmd");

	public static final String MODEL_MAGIC = "IFMD";
	public static final String SKELETON_JOINT_MAGIC = "SKLJ";

	public static Model readModel(File f) {
		return readModel(new DiskFile(f));
	}

	public static Model readModel(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, MODEL_MAGIC);

			Model mdl = readModel(l0.io, l0.fileVersion);

			l0.io.close();
			return mdl;
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Model readModel(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, MODEL_MAGIC)) {
			throw new InvalidMagicException("Invalid model magic.");
		}
		Model m = new Model();
		m.name = StringIO.readStringWithAddress(dis);

		if (fileVersion >= Revisions.REV_META_DATA) {
			m.metaData.putValues(MetaDataUtil.readMetaData(dis, fileVersion).getValues());
		}

		PointerTable bonesPT = new PointerTable(dis);
		PointerTable meshesPT = new PointerTable(dis);
		PointerTable materialsPT = new PointerTable(dis);
		PointerTable visgroupsPT = null;
		if (fileVersion >= Revisions.REV_VISGROUPS) {
			visgroupsPT = new PointerTable(dis);
		}

		Map<Joint, Integer> parentIndices = new HashMap<>();

		bonesPT.forEach(((io) -> {
			if (!StringIO.checkMagic(io, SKELETON_JOINT_MAGIC)) {
				throw new InvalidMagicException("Invalid joint magic.");
			}

			Joint j = new Joint();
			j.name = StringIO.readStringWithAddress(io);
			int parentIdx = io.readInt();
			if (parentIdx != -1) {
				parentIndices.put(j, parentIdx);
			}
			j.position = new Vec3f(io);
			j.rotation = new Vec3f(io);
			j.scale = new Vec3f(io);

			if (fileVersion >= Revisions.REV_JNT_EXTRA) {
				j.flags = io.readInt();
				j.kinematicsRole = Skeleton.KinematicsRole.VALUES[io.read()];
			}

			m.skeleton.addJoint(j);
		}));

		for (Map.Entry<Joint, Integer> pie : parentIndices.entrySet()) {
			pie.getKey().parentName = m.skeleton.getJoint(pie.getValue()).name;
		}
		if (m.skeleton != null) {
			m.skeleton.buildTransforms();
		}

		materialsPT.forEach(((io) -> {
			m.addMaterial(MaterialUtil.readMaterial(io, fileVersion));
		}));

		meshesPT.forEach(((io) -> {
			m.addMesh(MeshUtil.readMesh(io, fileVersion));
		}));

		if (visgroupsPT != null) {
			visgroupsPT.forEach(((io) -> {
				MeshVisibilityGroup visgroup = new MeshVisibilityGroup(io.readStringWithAddress());
				visgroup.isVisible = io.readBoolean();
				m.addVisGroup(visgroup);
			}));
		}

		return m;
	}

	public static void writeModel(Model m, File f) {
		writeModel(m, new DiskFile(f));
	}

	public static void writeModel(Model m, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(MODEL_MAGIC);
			writeModel(m, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeModel(Model m, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(MODEL_MAGIC);		//STRUCT MAGIC
		dos.writeString(m.name);	//Name offset
		MetaDataUtil.writeMetaData(m.metaData, dos);

		//ugh, moooore pointer tables
		List<TemporaryOffset> bonesPointerTable = PointerTable.allocatePointerTable(m.skeleton.getJoints().size(), dos);
		List<TemporaryOffset> meshesPointerTable = PointerTable.allocatePointerTable(m.meshes.size(), dos);
		List<TemporaryOffset> materialsPointerTable = PointerTable.allocatePointerTable(m.materials.size(), dos);
		List<TemporaryOffset> visgroupsPointerTable = PointerTable.allocatePointerTable(m.visGroups.size(), dos);

		//align
		dos.pad(CMIFFile.IF_PADDING);

		//SKELETON
		for (int i = 0; i < m.skeleton.getJoints().size(); i++) {
			bonesPointerTable.get(i).setHere();

			Joint j = m.skeleton.getJoint(i);
			dos.writeStringUnterminated(SKELETON_JOINT_MAGIC);		//MAGIC
			dos.writeString(j.name);								//Nameofs
			dos.writeInt(m.skeleton.getJointIndex(j.parentName));	//Parent index
			//BEGIN TRANSFORMS
			j.position.write(dos);									//Position vector
			j.rotation.write(dos);									//Rotation vector in eulers
			j.scale.write(dos);										//Scaling vector (usually 1/1/1)

			//Flags
			dos.writeInt(j.flags);							//Billboard matrix flags
			dos.writeEnum(j.kinematicsRole);						//IK role (chain/joint/effector)
		}

		dos.pad(CMIFFile.IF_PADDING);

		//MATERIALS
		for (int i = 0; i < m.materials.size(); i++) {
			materialsPointerTable.get(i).setHere();

			Material mat = m.materials.get(i);
			MaterialUtil.writeMaterial(mat, dos);

			dos.pad(CMIFFile.IF_PADDING);
		}

		//MESHES
		for (int i = 0; i < m.meshes.size(); i++) {
			Mesh mesh = m.meshes.get(i);
			meshesPointerTable.get(i).setHere();

			MeshUtil.writeMesh(m, mesh, dos);
		}

		//VISGROUPS
		for (int i = 0; i < m.visGroups.size(); i++) {
			MeshVisibilityGroup visgroup = m.visGroups.get(i);
			visgroupsPointerTable.get(i).setHere();

			dos.writeString(visgroup.name);
			dos.writeBoolean(visgroup.isVisible);
		}
	}
}
