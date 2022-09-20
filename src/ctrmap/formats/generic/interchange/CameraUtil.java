
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
		
		dos.writeEnum(cam.mode);
		dos.writeBoolean(true); //camera angles as degrees, not radians
		
		dos.writeFloat(cam.zNear);
		dos.writeFloat(cam.zFar);
		cam.translation.write(dos);
		
		switch (cam.mode){
			case LOOKAT:
				cam.lookAtTarget.write(dos);
				cam.lookAtUpVec.write(dos);
				dos.writeFloat(cam.FOV);
				break;
			case PERSPECTIVE:
				cam.rotation.write(dos);
				dos.writeFloat(cam.FOV);
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
	
	public static Camera readCamera(DataIOStream dis, int fileVersion) throws IOException{
		if (!StringIO.checkMagic(dis, CAMERA_MAGIC)){
			throw new InvalidMagicException("Invalid camera magic.");
		}
		Camera cam = new Camera();
		
		cam.name = StringIO.readStringWithAddress(dis);
		Camera.Mode mode = Camera.Mode.values()[dis.read()];
		boolean isCameraRadians = !dis.readBoolean();
		
		cam.zNear = dis.readFloat();
		cam.zFar = dis.readFloat();
		cam.translation = new Vec3f(dis);
		
		switch (mode){
			case LOOKAT:
				cam.lookAtTarget = new Vec3f(dis);
				cam.lookAtUpVec = new Vec3f(dis);
				cam.FOV = dis.readFloat();
				break;
			case PERSPECTIVE:
				cam.rotation = new Vec3f(dis);
				cam.FOV = dis.readFloat();
				if (isCameraRadians){
					cam.FOV = (float)Math.toDegrees(cam.FOV);
				}
				break;
		}
		if (isCameraRadians){
			cam.rotation.x = (float)Math.toDegrees(cam.rotation.x);
			cam.rotation.y = (float)Math.toDegrees(cam.rotation.y);
			cam.rotation.z = (float)Math.toDegrees(cam.rotation.z);
		}
		
		if (fileVersion >= Revisions.REV_CAM_METADATA) {
			cam.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}
		
		return cam;
	}
}
