package ctrmap.formats.generic.interchange;

import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.vec.Vec3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaDataUtil {

	public static final String META_DATA_MAGIC = "META";

	public static MetaData readMetaData(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, META_DATA_MAGIC)) {
			throw new InvalidMagicException("Invalid metadata magic.");
		}

		MetaData meta = new MetaData();
		int metaValueCount = dis.readInt();
		for (int i = 0; i < metaValueCount; i++) {
			List<Object> values = new ArrayList<>();
			String valueName = StringIO.readStringWithAddress(dis);
			MetaDataValue.Type type = MetaDataValue.Type.values()[dis.read()];
			int valueCount = dis.readInt();
			
			if (fileVersion < Revisions.REV_NEW_STRING_TABLE){
				if (type == MetaDataValue.Type.VEC3){
					type = MetaDataValue.Type.STRING;
				}
				else if (type == MetaDataValue.Type.STRING){
					type = MetaDataValue.Type.VEC3;
				}
			}

			for (int v = 0; v < valueCount; v++) {
				switch (type) {
					case FLOAT:
						values.add(dis.readFloat());
						break;
					case INT:
						values.add(dis.readInt());
						break;
					case STRING:
						if (fileVersion >= Revisions.REV_NEW_STRING_TABLE) {
							values.add(StringIO.readStringWithAddress(dis));
						} else {
							values.add(StringIO.readString(dis));
						}
						break;
					case VEC3:
						values.add(new Vec3f(dis));
						break;
					case RAW_BYTES:
						int size = dis.readInt();
						byte[] b = new byte[size];
						dis.read(b);
						values.add(b);
						break;
				}
			}

			meta.putValue(valueName, values);
		}

		return meta;
	}

	public static void writeMetaData(MetaData meta, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(META_DATA_MAGIC);
		List<MetaDataValue> values = meta.getWriteableValues();
		dos.writeInt(values.size());

		for (MetaDataValue v : values) {
			dos.writeString(v.getName());
			writeMetaValue(v, dos);
		}
	}

	public static void writeMetaValue(MetaDataValue v, DataIOStream dos) throws IOException {
		MetaDataValue.Type type = v.getType();
		dos.writeEnum(type);
		int valueCount;
		if (v.getValues() == null) {
			valueCount = 0;
		} else {
			valueCount = v.getValues().size();
		}
		dos.writeInt(valueCount);

		for (int i = 0; i < valueCount; i++) {
			switch (type) {
				case FLOAT:
					dos.writeFloat(v.floatValue(i));
					break;
				case INT:
					dos.writeInt(v.intValue(i));
					break;
				case STRING:
					dos.writeString(v.stringValue(i));
					break;
				case VEC3:
					v.vec3Value(i).write(dos);
					break;
				case RAW_BYTES:
					byte[] bytes = v.byteArrValue(i);
					dos.writeInt(bytes.length);
					dos.write(bytes);
					break;
			}
		}
	}
}
