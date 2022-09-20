
package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.Light;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.InvalidMagicException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LightUtil {
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Light", "*.iflt");
	
	public static final String LIGHT_MAGIC = "IFLT";
	
	public static void writeLight(Light light, File f) {
		writeLight(light, new DiskFile(f));
	}
	
	public static void writeLight(Light light, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(LIGHT_MAGIC);
			writeLight(light, dos);
			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void writeLight(Light light, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(LIGHT_MAGIC);
		
		dos.writeString(light.name);			//Name
		dos.writeShort(light.setIndex);			//Light set index
		dos.write(light.directional ? 0 : 1);	//Light type
		light.ambientColor.write(dos);
		light.diffuseColor.write(dos);
		light.specular0Color.write(dos);
		light.specular1Color.write(dos);
		light.direction.write(dos);
		if (!light.directional) {
			light.position.write(dos);
		}
		
		MetaDataUtil.writeMetaData(light.metaData, dos);
	}
	
	public static Light readLight(File f) {
		return readLight(new DiskFile(f));
	}
	
	public static Light readLight(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, LIGHT_MAGIC);
			
			Light light = readLight(l0.io, l0.fileVersion);
			
			l0.io.close();
			return light;
		} catch (IOException ex) {
			Logger.getLogger(LightUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static Light readLight(DataIOStream dis, int fileVersion) throws IOException{
		if (!StringIO.checkMagic(dis, LIGHT_MAGIC)){
			throw new InvalidMagicException("Invalid light magic.");
		}
		Light light = new Light(dis.readStringWithAddress());
		light.setIndex = dis.readShort();
		light.directional = dis.read() == 0;
		light.ambientColor.set(dis);
		light.diffuseColor.set(dis);
		light.specular0Color.set(dis);
		light.specular1Color.set(dis);
		light.direction.set(dis);
		if (!light.directional) {
			light.position.set(dis);
		}
		
		light.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		
		return light;
	}
}
