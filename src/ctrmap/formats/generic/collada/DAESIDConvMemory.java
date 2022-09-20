
package ctrmap.formats.generic.collada;

import ctrmap.formats.generic.collada.structs.DAEIDAble;
import ctrmap.formats.generic.collada.structs.DAESIDAble;
import xstandard.INamed;
import java.util.Objects;

public class DAESIDConvMemory<I extends INamed, O extends DAEIDAble & DAESIDAble> extends DAEConvMemory<I, O> {

	public DAESIDConvMemory(DAEConvState state, String idSuffix) {
		super(state, idSuffix);
	}
	
	@Override
	protected void setID(O output, String id) {
		output.setSID(id);
	}
	
	@Override
	protected String getOmnisuffix() {
		return "-sid";
	}
	
	@Override
	protected boolean isNonunique(String id) {
		for (O out : map.values()) {
			if (Objects.equals(out.getSID(), id)) {
				return true;
			}
		}
		return false;
	}
}
