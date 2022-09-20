
package ctrmap.formats.ntr.common;

import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.vec.Vec3f;
import java.io.File;
import java.io.IOException;

public class NTRDataIOStream extends DataIOStream {
	public NTRDataIOStream() {
		super();
	}
	
	public NTRDataIOStream(byte[] bytes) {
		super(bytes);
	}
	
	public NTRDataIOStream(IOStream s) {
		super(s);
	}
	
	public NTRDataIOStream(File f) {
		super(f);
	}
	
	public NTRDataIOStream(FSFile fsf) {
		super(fsf.getIO());
	}
	
	public float readFX32() throws IOException {
		return FXIO.readFX32(this);
	}
	
	public float[] readFX32Array(int size) throws IOException {
		float[] arr = new float[size];
		for (int i = 0; i < size; i++) {
			arr[i] = readFX32();
		}
		return arr;
	}
	
	public void writeFX32(float value) throws IOException {
		FXIO.writeFX32(this, value);
	}
	
	public float readFX16() throws IOException {
		return FXIO.readFX16(this);
	}
	
	public void writeFX16(float value) throws IOException {
		FXIO.writeFX16(this, value);
	}
	
	public Vec3f readVecFX32() throws IOException {
		return FXIO.readVecFX32(this);
	}
	
	public Vec3f readVecFX16() throws IOException {
		return FXIO.readVecFX16(this);
	}
}
