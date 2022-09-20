
package ctrmap.renderer.scene.model.draw.buffers;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.util.ArrayList;
import java.util.List;

public class BufferManager {
	public List<Buffer> buffers = new ArrayList<>();
	
	public final void addBuffer(Buffer buf){
		buffers.add(buf);
	}
	
	public boolean bindBuffers(IRenderDriver drv){
		boolean ret = true;
		for (Buffer buf : buffers){
			ret &= buf.bind(drv);
		}
		return ret;
	}
	
	public void updateAll(){
		for (Buffer buf : buffers){
			buf.update();
		}
	}
}
