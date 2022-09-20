package ctrmap.formats.ntr.common.gfx;

public enum GETextureFormat {
	NULL(0, 0),
	A3I5(8, 32),
	IDX2(2, 4),
	IDX4(4, 16),
	IDX8(8, 256),
	IDXCMPR(2, 16384),
	A5I3(8, 8),
	RGB5A1(16, -1);
	
	public final int bpp;
	public final int indexMax;
	
	private GETextureFormat(int bpp, int indexMax) {
		this.bpp = bpp;
		this.indexMax = indexMax;
	}
}
