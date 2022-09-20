package ctrmap.renderer.scene.model.draw.buffers;

import ctrmap.renderer.backends.DriverBool;
import ctrmap.renderer.backends.DriverHandle;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Buffer {

	private DriverBool needsAllocate = new DriverBool();

	public List<BufferComponent> components = new ArrayList<>();

	public DriverHandle rendererPointers = new DriverHandle();

	public final void addComponent(BufferComponent comp) {
		components.add(comp);
	}
	
	public int getHandleForDriver(IRenderDriver drv) {
		return rendererPointers.get(drv);
	}

	public void deleteHandle(IRenderDriver drv) {
		rendererPointers.remove(drv);
		needsAllocate.remove(drv);
		for (BufferComponent comp : components) {
			comp.deleteState(drv);
		}
	}
	
	public boolean bind(IRenderDriver drv) {
		if (isBindEnabled()) {
			int ptr = rendererPointers.get(drv);
			if (ptr == -1) {
				ptr = drv.genBuffer();
				rendererPointers.set(drv, ptr);
			}
			boolean ret = true;
			drv.bindBuffer(getTarget(), ptr);
			if (needsAllocate.get(drv)) {
				drv.allocBuffer(getTarget(), getSize(), getDrawType());
				ret = false;
			}
			for (BufferComponent comp : components) {
				ret &= comp.bind(drv);
			}
			return ret;
		}
		return true;
	}

	public abstract BufferTarget getTarget();

	public abstract boolean isBindEnabled();

	public abstract VertexListUsage getDrawType();

	public void update() {
		needsAllocate.resetAll();
		for (BufferComponent comp : components) {
			comp.update();
		}
	}

	public void invalidate() {
		needsAllocate.resetAll();
		for (BufferComponent comp : components) {
			comp.invalidate();
		}
	}

	public int getSize() {
		int size = 0;
		for (BufferComponent comp : components) {
			size += comp.getByteSize();
		}
		return size;
	}

	public <T extends BufferComponent> T getComponent(Class<T> byClass) {
		return getComponent(byClass, 0);
	}

	public <T extends BufferComponent> T getComponent(Class<T> byClass, int setNo) {
		for (BufferComponent comp : components) {
			if (byClass.isAssignableFrom(comp.getClass()) && comp.setNo == setNo) {
				return (T) comp;
			}
		}
		return null;
	}

	public void updateComponent(Class<? extends BufferComponent> byClass) {
		for (BufferComponent comp : components) {
			if (byClass.isAssignableFrom(comp.getClass())) {
				comp.update();
			}
		}
	}

	public static enum BufferTarget {
		ARRAY_BUFFER,
		ELEMENT_ARRAY_BUFFER
	}
}
