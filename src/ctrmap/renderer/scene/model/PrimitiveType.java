package ctrmap.renderer.scene.model;

/**
 *
 */
public enum PrimitiveType {
	TRIS,
	QUADS,
	LINES,
	TRISTRIPS,
	TRIFANS,
	QUADSTRIPS,
	LINESTRIPS;
	
	public static int getPrimitiveTypeSeparationSize(PrimitiveType pt, Mesh mesh) {
		return getPrimitiveTypeSeparationSize(pt, mesh.getVertexCount());
	}
	
	public static int getPrimitiveTypeSeparationSize(PrimitiveType pt, int vcount) {
		switch (pt) {
			case QUADS:
				return 4;
			case TRIS:
				return 3;
			case LINES:
				return 2;
			case QUADSTRIPS:
			case TRISTRIPS:
			case TRIFANS:
			case LINESTRIPS:
				return vcount;
		}
		return -1;
	}

	public static PrimitiveType forFacepointCount(int facepointsPerFace) {
		switch (facepointsPerFace) {
			case 2:
				return LINES;
			case 3:
				return TRIS;
			case 4:
				return QUADS;
		}
		return null;
	}
}
