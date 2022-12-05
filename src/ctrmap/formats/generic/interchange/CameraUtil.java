package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.Camera;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.InvalidMagicException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import xstandard.math.vec.Vec3f;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraUtil {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Camera", "*.ifcm");

	public static final String CAMERA_MAGIC = "IFCM";

	public static void writeCamera(Camera cam, File f) {
		writeCamera(cam, new DiskFile(f));
	}

	public static void writeCamera(Camera cam, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(CAMERA_MAGIC);
			writeCamera(cam, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeCamera(Camera cam, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(CAMERA_MAGIC);
		dos.writeString(cam.name);

		dos.writeEnum(cam.projMode);
		dos.writeEnum(cam.viewMode);
		
		dos.writeBoolean(true); //camera angles as degrees, not radians

		dos.writeFloat(cam.zNear);
		dos.writeFloat(cam.zFar);
		
		cam.translation.write(dos);

		switch (cam.projMode) {
			case ORTHO:
				dos.writeFloats(cam.left, cam.right, cam.top, cam.bottom);
				break;
			case PERSPECTIVE:
				dos.writeFloat(cam.FOV);
				dos.writeFloat(cam.aspect);
				break;
		}
		
		switch (cam.viewMode) {
			case LOOK_AT:
				cam.lookAtTarget.write(dos);
				cam.lookAtUpVec.write(dos);
				break;
			case ROTATE:
				cam.rotation.write(dos);
				break;
		}

		MetaDataUtil.writeMetaData(cam.metaData, dos);
	}

	public static Camera readCamera(File f) {
		return readCamera(new DiskFile(f));
	}

	public static Camera readCamera(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, CAMERA_MAGIC);

			Camera cam = readCamera(l0.io, l0.fileVersion);

			l0.io.close();
			return cam;
		} catch (IOException ex) {
			Logger.getLogger(CameraUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Camera readCamera(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, CAMERA_MAGIC)) {
			throw new InvalidMagicException("Invalid camera magic.");
		}
		Camera cam = new Camera();

		cam.name = StringIO.readStringWithAddress(dis);
		CameraModeCompat modeCompat = null;
		Camera.ProjectionMode projMode = null;
		Camera.ViewMode viewMode = null;
		if (fileVersion < Revisions.REV_CAMERA_PLUS) {
			modeCompat = CameraModeCompat.values()[dis.read()];
		} else {
			projMode = Camera.ProjectionMode.values()[dis.read()];
			viewMode = Camera.ViewMode.values()[dis.read()];
		}
		boolean isCameraRadians = !dis.readBoolean();

		cam.zNear = dis.readFloat();
		cam.zFar = dis.readFloat();
		
		cam.translation = new Vec3f(dis);

		if (modeCompat != null) {
			switch (modeCompat) {
				case PERSPECTIVE_LOOKAT:
					cam.lookAtTarget = new Vec3f(dis);
					cam.lookAtUpVec = new Vec3f(dis);
					cam.FOV = dis.readFloat();
					break;
				case PERSPECTIVE_ROTATE:
					cam.rotation = new Vec3f(dis);
					cam.FOV = dis.readFloat();
					break;
			}
		} else if (projMode != null && viewMode != null) {
			switch (projMode) {
				case ORTHO:
					cam.left = dis.readFloat();
					cam.right = dis.readFloat();
					cam.top = dis.readFloat();
					cam.bottom = dis.readFloat();
					break;
				case PERSPECTIVE:
					cam.FOV = dis.readFloat();
					cam.aspect = dis.readFloat();
					break;
			}
			switch (viewMode) {
				case LOOK_AT:
					cam.lookAtTarget = new Vec3f(dis);
					cam.lookAtUpVec = new Vec3f(dis);
					break;
				case ROTATE:
					cam.rotation = new Vec3f(dis);
					break;
			}
		}

		if (isCameraRadians) {
			cam.FOV = (float) Math.toDegrees(cam.FOV);
			cam.rotation.x = (float) Math.toDegrees(cam.rotation.x);
			cam.rotation.y = (float) Math.toDegrees(cam.rotation.y);
			cam.rotation.z = (float) Math.toDegrees(cam.rotation.z);
		}

		if (fileVersion >= Revisions.REV_CAM_METADATA) {
			cam.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}

		return cam;
	}

	public static enum CameraModeCompat {
		PERSPECTIVE_ROTATE,
		ORTHO_DEFAULT,
		PERSPECTIVE_LOOKAT
	}
}
