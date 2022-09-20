package ctrmap.formats.generic.interchange;

import xstandard.io.InvalidMagicException;
import ctrmap.renderer.scene.Camera;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.LoopMode;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraLookAtBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.material.RGBAKeyFrameGroup;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnimeUtil {

	public static final ExtensionFilter MA_EXTENSION_FILTER = new ExtensionFilter("Material animation", "*.ifma");
	public static final ExtensionFilter SA_EXTENSION_FILTER = new ExtensionFilter("Skeletal animation", "*.ifsa");
	public static final ExtensionFilter CA_EXTENSION_FILTER = new ExtensionFilter("Camera animation", "*.ifca");

	public static final String MAT_ANIME_MAGIC = "IFMA";
	public static final String SKL_ANIME_MAGIC = "IFSA";
	public static final String CAM_ANIME_MAGIC = "IFCA";

	public static SkeletalAnimation readSkeletalAnime(File f) {
		return readSkeletalAnime(new DiskFile(f));
	}

	public static SkeletalAnimation readSkeletalAnime(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, SKL_ANIME_MAGIC);

			SkeletalAnimation sa = readSkeletalAnime(l0.io, l0.fileVersion);

			l0.io.close();
			return sa;
		} catch (IOException ex) {
			Logger.getLogger(AnimeUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static MaterialAnimation readMaterialAnime(File f) {
		return readMaterialAnime(new DiskFile(f));
	}

	public static MaterialAnimation readMaterialAnime(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, MAT_ANIME_MAGIC);

			MaterialAnimation sa = readMaterialAnime(l0.io, l0.fileVersion);

			l0.io.close();
			return sa;
		} catch (IOException ex) {
			Logger.getLogger(AnimeUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static CameraAnimation readCameraAnime(File f) {
		return readCameraAnime(new DiskFile(f));
	}

	public static CameraAnimation readCameraAnime(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, CAM_ANIME_MAGIC);

			CameraAnimation ca = readCameraAnime(l0.io, l0.fileVersion);

			l0.io.close();
			return ca;
		} catch (IOException ex) {
			Logger.getLogger(AnimeUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static SkeletalAnimation readSkeletalAnime(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, SKL_ANIME_MAGIC)) {
			throw new InvalidMagicException("Invalid skeletal animation magic.");
		}

		SkeletalAnimation a = new SkeletalAnimation();

		readAnimeCommonHeader(a, dis, fileVersion);

		int bonesCount = dis.readShort();
		for (int i = 0; i < bonesCount; i++) {
			SkeletalBoneTransform bt = new SkeletalBoneTransform();
			bt.name = StringIO.readStringWithAddress(dis);

			int elemCount = dis.readByte();

			for (int e = 0; e < elemCount; e++) {
				int elem = dis.read();
				KeyFrameList kfg = readFloatKeyFrameGroup(dis, fileVersion);
				switch (SklAnimKeyFrameType.values()[elem]) {
					case TX:
						bt.tx = kfg;
						break;
					case TY:
						bt.ty = kfg;
						break;
					case TZ:
						bt.tz = kfg;
						break;
					case RX:
						bt.rx = kfg;
						break;
					case RY:
						bt.ry = kfg;
						break;
					case RZ:
						bt.rz = kfg;
						break;
					case SX:
						bt.sx = kfg;
						break;
					case SY:
						bt.sy = kfg;
						break;
					case SZ:
						bt.sz = kfg;
						break;
				}
			}

			a.bones.add(bt);
		}

		return a;
	}

	public static MaterialAnimation readMaterialAnime(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, MAT_ANIME_MAGIC)) {
			throw new InvalidMagicException("Invalid material animation magic.");
		}

		MaterialAnimation a = new MaterialAnimation();
		readAnimeCommonHeader(a, dis, fileVersion);

		int bonesCount = dis.readShort();
		for (int i = 0; i < bonesCount; i++) {
			MatAnimBoneTransform bt = new MatAnimBoneTransform();

			bt.name = StringIO.readStringWithAddress(dis);
			int elemCount = dis.readByte();
			for (int e = 0; e < elemCount; e++) {
				int uvIndex = dis.readByte();
				MatAnimKeyFrameType type = MatAnimKeyFrameType.values()[dis.readByte()];

				if (type == MatAnimKeyFrameType.TEX) {
					int count = dis.readShort();

					for (int n = 0; n < count; n++) {
						float frame = dis.readFloat();
						String name = StringIO.readStringWithAddress(dis);

						if (!bt.textureNames.contains(name)) {
							bt.textureNames.add(name);
						}
						bt.textureIndices[uvIndex].add(new KeyFrame(frame, bt.textureNames.indexOf(name), 0, 0, KeyFrame.InterpolationMethod.STEP));
					}
				} else {
					KeyFrameList kfg = readFloatKeyFrameGroup(dis, fileVersion);

					switch (type) {
						case TX:
							bt.mtx[uvIndex] = kfg;
							break;
						case TY:
							bt.mty[uvIndex] = kfg;
							break;
						case R:
							bt.mrot[uvIndex] = kfg;
							break;
						case SX:
							bt.msx[uvIndex] = kfg;
							break;
						case SY:
							bt.msy[uvIndex] = kfg;
							break;
					}
				}
			}

			if (fileVersion >= Revisions.REV_CCOL_ANIMATION) {
				int colorCount = dis.read();

				for (int colIndex = 0; colIndex < colorCount; colIndex++) {
					RGBAKeyFrameGroup kfg = new RGBAKeyFrameGroup();
					bt.materialColors[dis.read()] = kfg;
					boolean hasR = dis.readBoolean();
					boolean hasG = dis.readBoolean();
					boolean hasB = dis.readBoolean();
					boolean hasA = dis.readBoolean();
					if (hasR) {
						kfg.r = readFloatKeyFrameGroup(dis, fileVersion);
					}
					if (hasG) {
						kfg.g = readFloatKeyFrameGroup(dis, fileVersion);
					}
					if (hasB) {
						kfg.b = readFloatKeyFrameGroup(dis, fileVersion);
					}
					if (hasA) {
						kfg.a = readFloatKeyFrameGroup(dis, fileVersion);
					}
				}
			}

			a.bones.add(bt);
		}
		return a;
	}

	public static CameraAnimation readCameraAnime(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, CAM_ANIME_MAGIC)) {
			throw new InvalidMagicException("Invalid camera animation magic.");
		}

		CameraAnimation a = new CameraAnimation();
		readAnimeCommonHeader(a, dis, fileVersion);

		boolean isRotationRad = false;
		if (fileVersion < Revisions.REV_CAM_DEG_RAD_FIX) {
			isRotationRad = !dis.readBoolean();
		}

		int transformCount = dis.readShort();
		for (int i = 0; i < transformCount; i++) {
			String name = StringIO.readStringWithAddress(dis);
			Camera.Mode mode = Camera.Mode.values()[dis.read()];
			if (fileVersion >= Revisions.REV_CAM_DEG_RAD_FIX) {
				isRotationRad = dis.readBoolean();
			}

			CameraBoneTransform bt = null;

			switch (mode) {
				case LOOKAT:
					bt = new CameraLookAtBoneTransform();
					break;
				case ORTHO:
				case PERSPECTIVE:
					bt = new CameraViewpointBoneTransform();
					break;
			}

			if (bt == null) {
				System.err.println("Invalid camera transform type ! !");
				continue;
			}
			bt.isRadians = isRotationRad;

			int elemCount = dis.read();

			for (int j = 0; j < elemCount; j++) {
				CamAnimKeyFrameType type = CamAnimKeyFrameType.values()[dis.read()];
				KeyFrameList kfl = readFloatKeyFrameGroup(dis, fileVersion);

				switch (type) {
					case FOV:
						switch (mode) {
							case LOOKAT:
								((CameraLookAtBoneTransform) bt).fov = kfl;
								break;
							case PERSPECTIVE:
								((CameraViewpointBoneTransform) bt).fov = kfl;
								break;
						}
						break;
					case TGTX:
						((CameraLookAtBoneTransform) bt).targetTX = kfl;
						break;
					case TGTY:
						((CameraLookAtBoneTransform) bt).targetTY = kfl;
						break;
					case TGTZ:
						((CameraLookAtBoneTransform) bt).targetTZ = kfl;
						break;
					case UPX:
						((CameraLookAtBoneTransform) bt).upX = kfl;
						break;
					case UPY:
						((CameraLookAtBoneTransform) bt).upY = kfl;
						break;
					case UPZ:
						((CameraLookAtBoneTransform) bt).upZ = kfl;
						break;
					case RX:
						((CameraViewpointBoneTransform) bt).rx = kfl;
						break;
					case RY:
						((CameraViewpointBoneTransform) bt).ry = kfl;
						break;
					case RZ:
						((CameraViewpointBoneTransform) bt).rz = kfl;
						break;
					case TX:
						bt.tx = kfl;
						break;
					case TY:
						bt.ty = kfl;
						break;
					case TZ:
						bt.tz = kfl;
						break;
				}
			}

			bt.name = name;

			a.transforms.add(bt);
		}
		return a;
	}

	private static void kflToDegrees(List<KeyFrame> kfl) {
		for (KeyFrame kf : kfl) {
			kf.value = (float) Math.toDegrees(kf.value);
		}
	}

	private static void readAnimeCommonHeader(AbstractAnimation a, DataIOStream dis, int fileVersion) throws IOException {
		a.name = StringIO.readStringWithAddress(dis);
		a.frameCount = dis.readFloat();
		a.isLooped = dis.readBoolean();
		if (fileVersion >= Revisions.REV_ANM_METADATA) {
			a.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}
	}

	public static void writeMatAnime(MaterialAnimation mta, File f) {
		writeMatAnime(mta, new DiskFile(f));
	}

	public static void writeMatAnime(MaterialAnimation mta, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(MAT_ANIME_MAGIC);
			writeMatAnime(mta, dos);

			dos.close();
			f.setBytes(dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeMatAnime(MaterialAnimation a, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(MAT_ANIME_MAGIC);
		writeAnimeCommonHeader(a, dos);

		dos.writeShort((short) a.bones.size());
		for (MatAnimBoneTransform bt : a.bones) {
			dos.writeString(bt.name);		//Target material name

			dos.write(getDeepExistingElementCount(bt.mtx, bt.mty, bt.mrot, bt.msx, bt.msy, bt.textureIndices));

			for (int i = 0; i < 3; i++) {
				writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType.TX, i, bt.mtx, dos);
				writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType.TY, i, bt.mty, dos);

				writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType.R, i, bt.mrot, dos);

				writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType.SX, i, bt.msx, dos);
				writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType.SY, i, bt.msy, dos);

				if (writeMatAnmKeyFrameGroupHeader(MatAnimKeyFrameType.TEX, i, bt.textureIndices, dos)) {
					//using full ints for texture name offsets is a waste, but oh well. it's not like we live in the '90s anyway.
					//still about 10000x more resource effective than a Chromium Embedded Framework mobile app.

					dos.writeShort((short) bt.textureIndices[i].size());
					//no interpolation here. this stuff is always step

					for (KeyFrame kf : bt.textureIndices[i]) {
						dos.writeFloat(kf.frame);
						dos.writeString(bt.textureNames.get((int) kf.value));
					}
				}
			}

			int colTransformCount = 0;

			for (RGBAKeyFrameGroup rgbakfg : bt.materialColors) {
				if (getExistingElementCount(rgbakfg.r, rgbakfg.g, rgbakfg.b, rgbakfg.a) != 0) {
					colTransformCount++;
				}
			}

			dos.write(colTransformCount);

			for (int i = 0; i < bt.materialColors.length; i++) {
				RGBAKeyFrameGroup rgbakfg = bt.materialColors[i];
				if (getExistingElementCount(rgbakfg.r, rgbakfg.g, rgbakfg.b, rgbakfg.a) != 0) {
					dos.write(i);
					boolean hasR = !rgbakfg.r.isEmpty();
					boolean hasG = !rgbakfg.g.isEmpty();
					boolean hasB = !rgbakfg.b.isEmpty();
					boolean hasA = !rgbakfg.a.isEmpty();
					dos.writeBoolean(hasR);
					dos.writeBoolean(hasG);
					dos.writeBoolean(hasB);
					dos.writeBoolean(hasA);
					if (hasR) {
						writeFloatKeyFrameGroup(rgbakfg.r, dos);
					}
					if (hasG) {
						writeFloatKeyFrameGroup(rgbakfg.g, dos);
					}
					if (hasB) {
						writeFloatKeyFrameGroup(rgbakfg.b, dos);
					}
					if (hasA) {
						writeFloatKeyFrameGroup(rgbakfg.a, dos);
					}
				}
			}
		}
	}

	public static void writeCameraAnime(CameraAnimation cma, File f) {
		writeCameraAnime(cma, new DiskFile(f));
	}

	public static void writeCameraAnime(CameraAnimation cma, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(CAM_ANIME_MAGIC);
			writeCameraAnime(cma, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeCameraAnime(CameraAnimation a, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(CAM_ANIME_MAGIC);
		writeAnimeCommonHeader(a, dos);

		dos.writeShort(a.transforms.size());

		for (CameraBoneTransform bt : a.transforms) {
			dos.writeString(bt.name);
			int elemCount = getExistingElementCount(bt.tx, bt.ty, bt.tz);

			if (bt instanceof CameraLookAtBoneTransform) {
				CameraLookAtBoneTransform la = (CameraLookAtBoneTransform) bt;
				dos.writeEnum(Camera.Mode.LOOKAT);
				dos.writeBoolean(bt.isRadians);
				elemCount += getExistingElementCount(la.targetTX, la.targetTY, la.targetTZ, la.upX, la.upY, la.upZ, la.fov);
				dos.write(elemCount);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TGTX, la.targetTX, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TGTY, la.targetTY, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TGTZ, la.targetTZ, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.UPX, la.upX, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.UPY, la.upY, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.UPZ, la.upZ, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.FOV, la.fov, dos);
			} else if (bt instanceof CameraViewpointBoneTransform) {
				dos.writeEnum(Camera.Mode.PERSPECTIVE);
				dos.writeBoolean(bt.isRadians);
				CameraViewpointBoneTransform v = (CameraViewpointBoneTransform) bt;
				elemCount += getExistingElementCount(v.rx, v.ry, v.rz, v.fov);
				dos.write(elemCount);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.RX, v.rx, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.RY, v.ry, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.RZ, v.rz, dos);
				writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.FOV, v.fov, dos);
			} else {
				dos.writeByte(-1);
				dos.writeBoolean(bt.isRadians);
			}

			writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TX, bt.tx, dos);
			writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TY, bt.ty, dos);
			writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType.TZ, bt.tz, dos);
		}
	}

	private static void writeCamAnmFloatKeyFrameGroup(CamAnimKeyFrameType type, KeyFrameList kfl, DataIOStream dos) throws IOException {
		if (kfl != null && !kfl.isEmpty()) {
			dos.writeEnum(type);
			writeFloatKeyFrameGroup(kfl, dos);
		}
	}

	private static void writeMatAnmFloatKeyFrameGroup(MatAnimKeyFrameType type, int coordinator, KeyFrameList[] l, DataIOStream dos) throws IOException {
		if (writeMatAnmKeyFrameGroupHeader(type, coordinator, l, dos)) {
			writeFloatKeyFrameGroup(l[coordinator], dos);
		}
	}

	private static boolean writeMatAnmKeyFrameGroupHeader(MatAnimKeyFrameType type, int coordinator, KeyFrameList[] l, DataIOStream dos) throws IOException {
		if (l[coordinator] != null && !l[coordinator].isEmpty()) {
			dos.write(coordinator);
			dos.writeEnum(type);
			return true;
		}
		return false;
	}

	public static void writeSklAnime(SkeletalAnimation ska, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(SKL_ANIME_MAGIC);
			writeSklAnime(ska, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeSklAnime(SkeletalAnimation ska, File f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(SKL_ANIME_MAGIC);
			writeSklAnime(ska, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeSklAnime(SkeletalAnimation a, CMIFWriter dos) throws IOException {
		dos.write(SKL_ANIME_MAGIC.getBytes("ASCII"));
		writeAnimeCommonHeader(a, dos);

		dos.writeShort((short) a.bones.size());
		for (SkeletalBoneTransform bt : a.bones) {
			dos.writeString(bt.name);

			dos.write(getExistingElementCount(bt.tx, bt.ty, bt.tz, bt.rx, bt.ry, bt.rz, bt.sx, bt.sy, bt.sz));

			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.TX, bt.tx, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.TY, bt.ty, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.TZ, bt.tz, dos);

			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.RX, bt.rx, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.RY, bt.ry, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.RZ, bt.rz, dos);

			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.SX, bt.sx, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.SY, bt.sy, dos);
			writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType.SZ, bt.sz, dos);
		}
	}

	private static int getDeepExistingElementCount(List<KeyFrame>[]... lists) {
		int count = 0;
		for (int i = 0; i < lists.length; i++) {
			count += getExistingElementCount(lists[i]);
		}
		return count;
	}

	private static int getExistingElementCount(List<KeyFrame>... lists) {
		int count = 0;
		for (List<KeyFrame> l : lists) {
			if (l != null && !l.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private static void writeSklAnmFloatKeyFrameGroup(SklAnimKeyFrameType type, KeyFrameList l, DataIOStream dos) throws IOException {
		if (l != null && !l.isEmpty()) {
			dos.writeEnum(type);
			writeFloatKeyFrameGroup(l, dos);
		}
	}

	private static KeyFrameList readFloatKeyFrameGroup(DataIOStream dis, int fileVersion) throws IOException {
		KeyFrameList l = new KeyFrameList();

		KeyFrame.InterpolationMethod interpolation = KeyFrame.InterpolationMethod.values()[dis.read()];

		if (fileVersion >= Revisions.REV_ANM_CURVEINFO) {
			l.startFrame = dis.readFloat();
			l.endFrame = dis.readFloat();
			l.loopMode = LoopMode.values()[dis.read()];
		}

		int size = dis.readUnsignedShort();
		for (int i = 0; i < size; i++) {
			KeyFrame kf = new KeyFrame();
			kf.frame = dis.readFloat();
			kf.value = dis.readFloat();

			if (interpolation == KeyFrame.InterpolationMethod.HERMITE) {
				kf.inSlope = dis.readFloat();
				kf.outSlope = dis.readFloat();
			}
			kf.interpolation = interpolation;
			l.add(kf);
		}
		return l;
	}

	private static void writeFloatKeyFrameGroup(KeyFrameList l, DataIOStream dos) throws IOException {
		//preprocess
		if (l == null || l.isEmpty()) {
			return;
		}
		KeyFrame.InterpolationMethod interpolation = getKeyFrameGroupInterpolation(l);

		dos.writeEnum(interpolation);

		dos.writeFloat(l.startFrame);
		dos.writeFloat(l.endFrame);
		dos.writeEnum(l.loopMode);

		dos.writeShort((short) l.size());
		//float stuff
		for (KeyFrame kf : l) {
			dos.writeFloat(kf.frame);
			dos.writeFloat(kf.value);

			//thanks to the previous step, we can now safely scale according to the interpolation without data loss
			if (interpolation == KeyFrame.InterpolationMethod.HERMITE) {
				dos.writeFloat(kf.inSlope);
				dos.writeFloat(kf.outSlope);
			}
		}
	}

	private static KeyFrame.InterpolationMethod getKeyFrameGroupInterpolation(List<KeyFrame> l) {
		KeyFrame.InterpolationMethod interpolation = KeyFrame.InterpolationMethod.STEP;	//default
		for (KeyFrame kf : l) {
			switch (kf.interpolation) {
				case HERMITE:
					return KeyFrame.InterpolationMethod.HERMITE; //highest possible, return
				case LINEAR:
					interpolation = KeyFrame.InterpolationMethod.LINEAR;
					break;	//set to linear, but allow override by hermite
				//step has no behavior as it is the default case
			}
		}
		return interpolation;
	}

	private static void writeAnimeCommonHeader(AbstractAnimation a, CMIFWriter dos) throws IOException {
		dos.writeString(a.name);
		dos.writeFloat(a.frameCount);
		dos.write(a.isLooped ? 1 : 0);
		MetaDataUtil.writeMetaData(a.metaData, dos);
	}

	private static enum CamAnimKeyFrameType {
		TX,
		TY,
		TZ,
		RX,
		RY,
		RZ,
		TGTX,
		TGTY,
		TGTZ,
		UPX,
		UPY,
		UPZ,
		FOV
	}

	private static enum MatAnimKeyFrameType {
		TX,
		TY,
		R,
		SX,
		SY,
		TEX
	}

	private static enum SklAnimKeyFrameType {
		TX,
		TY,
		TZ,
		RX,
		RY,
		RZ,
		SX,
		SY,
		SZ
	}
}
