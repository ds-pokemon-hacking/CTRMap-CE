package ctrmap.renderer.scene.model.draw.buffers;

import ctrmap.renderer.backends.DriverBool;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.nio.ByteBuffer;

public abstract class BufferComponent {

	private Buffer buf;

	public final int setNo;

	private DriverBool needsUpload = new DriverBool();

	public BufferComponent(Buffer buf, int setNo) {
		this.buf = buf;
		this.setNo = setNo;
	}

	public BufferComponent(Buffer buf) {
		this(buf, 0);
	}

	public void deleteState(IRenderDriver drv) {
		needsUpload.remove(drv);
	}

	public abstract boolean isEnabled();

	public abstract java.nio.Buffer getBuffer();

	public abstract BufferComponentType getType();

	protected abstract void updateImpl();

	public void update() {
		invalidate();
		updateImpl();
	}

	public void invalidate() {
		needsUpload.resetAll();
	}
	
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

	public boolean bind(IRenderDriver drv) {
		if (isEnabled()) {
			if (needsUpload.get(drv)) {
				java.nio.Buffer b = getBuffer();
				if (b != null) {
					b.rewind();
					drv.uploadBufferData(buf.getTarget(), b, getOffset(), getByteSize());
				}
				else {
					drv.uploadBufferData(buf.getTarget(), EMPTY_BUFFER, getOffset(), getByteSize());
				}
				return false;
			}
		}
		return true;
	}

	public int getOffset() {
		int off = 0;
		for (BufferComponent c : buf.components) {
			if (this == c) {
				return off;
			}
			off += c.getByteSize();
		}
		return off;
	}

	public int getByteSize() {
		if (isEnabled()) {
			java.nio.Buffer b = getBuffer();
			if (b == null) {
				return 0;
			}
			return b.capacity() * getType().sizeof;
		}
		return 0;
	}

	public static enum BufferComponentType {
		FLOAT(Float.BYTES),
		INT(Integer.BYTES),
		SHORT(Short.BYTES),
		BYTE(Byte.BYTES);

		public final int sizeof;

		private BufferComponentType(int size) {
			sizeof = size;
		}
	}
}
