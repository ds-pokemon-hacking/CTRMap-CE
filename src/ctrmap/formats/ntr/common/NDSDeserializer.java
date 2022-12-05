
package ctrmap.formats.ntr.common;

import java.nio.ByteOrder;
import xstandard.io.base.iface.IOStream;
import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.DecimalType;
import xstandard.io.serialization.ReferenceType;

public class NDSDeserializer extends BinaryDeserializer {

	public NDSDeserializer() {
		this(ReferenceType.ABSOLUTE_POINTER);
	}
	
	public NDSDeserializer(ReferenceType referenceType) {
		super(ByteOrder.LITTLE_ENDIAN, referenceType, DecimalType.FIXED_POINT_NNFX);
	}
	
	public NDSDeserializer(IOStream baseStream, ReferenceType referenceType) {
		super(baseStream, ByteOrder.LITTLE_ENDIAN, referenceType, DecimalType.FIXED_POINT_NNFX);
	}

}
