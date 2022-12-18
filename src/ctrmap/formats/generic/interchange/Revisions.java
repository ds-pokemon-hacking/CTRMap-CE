package ctrmap.formats.generic.interchange;

public class Revisions {
	public static final int REV_CURRENT = 22;
	public static final int REV_CURRENT_BW_COMPAT = 22;
	
	public static final int REV_FOUNDATION = 1;			//First version of the IF
	public static final int REV_OTHER_FILES = 2;		//Add support for generic embedded files - new pointer table
	public static final int REV_BACK_COMPAT = 3;		//Add a backwards compatibility value to the header so that old readers can still read the file, just without new stuff, without errors
	public static final int REV_STENCIL_TEST = 4;		//Supports IO of material stencil tests
	public static final int REV_META_DATA = 4;			//Supports IO of scene metadata
	public static final int REV_LEVEL0_VERSION = 4;		//Writes the file version into the Level0 section header
	public static final int REV_PRIMITIVE_TYPE = 4;		//Saves specified primitive type data of meshes
	public static final int REV_FLIP_TEXTURES = 4;		//Since CMIF writes direct MC renderer texture data, anything written with the old writer uses the old one which pre-flipped textures
	public static final int REV_SHADER_INFO = 5;		//Saves 3DS vertex shader archive and index
	public static final int REV_BUMP_MAPPING = 5;		//Writes bump mapping type and texture reference
	public static final int REV_TEX_META_DATA = 6;		//Texture metadata section
	public static final int REV_SKINNING_MODE = 7;		//Mesh skinning type info
	public static final int REV_BUFFER_COMP_LZ = 7;		//LZSS compression for various data buffers
	public static final int REV_LIGHTING_DATA = 8;		//Basic fragment lighting data writing
	public static final int REV_FACE_CULLING = 9;		//Material face culling information
	public static final int REV_LIGHTING_DATA_EX = 9;	//Extra fragment lighting data writing
	public static final int REV_LUT = 9;				//Material lookup table data
	public static final int REV_CAMERA = 10;			//Camera animation serialization
	public static final int REV_NEW_STRING_TABLE = 11;	//Major revision of the string table
	public static final int REV_BYTEARR_METADATA = 12;  //Allow raw byte arrays in metadata values
	public static final int REV_EMISSION_COLOR = 12;    //Write material emission color
	public static final int REV_CCOL_ASSIGNMENT = 13;	//Allow customizable constant color assignment (like H3D)
	public static final int REV_CCOL_ANIMATION = 13;	//Material color animation serialization
	public static final int REV_ANM_CURVEINFO = 13;		//Extended per-curve animation info
	public static final int REV_TEX_MAP_MODE = 14;		//Dynamic texture mapping mode
	public static final int REV_ANM_METADATA = 15;		//Animation metadata
	public static final int REV_CAM_METADATA = 15;		//Camera metadata
	public static final int REV_CAM_DEG_RAD_FIX = 16;	//Camera animation radians/degrees fix
	public static final int REV_JNT_EXTRA = 17;			//Joint billboard and IK attribute serialization. Backwards compatible.
	public static final int REV_LIGHTS = 17;			//Light de/serialization. Backwards compatible.
	public static final int REV_SCENE_TEMPLATES = 17;	//Scene template de/serialization. Backwards compatible.
	public static final int REV_VISGROUPS = 18;			//Mesh visibility group de/serialization. Backwards compatible to v16.
	public static final int REV_VISGROUP_ASSIGN = 19;	//Actually assigning meshes to visgroups. No longer backwards compatible.
	public static final int REV_INDEX_BUFFERS = 20;     //Writing mesh IBOs separately.
	public static final int REV_EXT_FSH_INFO = 20;      //Extended fragment shader parameters in materials
	public static final int REV_TBN = 21;				//Tangent/bitangent vertex attribute support
	public static final int REV_CAMERA_PLUS = 22;		//Serialization of the new camera structure
	
	public static final int REV_NDS_RESERVED = 4269;
}
