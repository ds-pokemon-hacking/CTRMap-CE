package ctrmap.formats.generic.xobj;

import ctrmap.formats.common.FormatIOExConfig;

public class OBJExportSettings implements FormatIOExConfig {

	public final boolean VCOL_ENABLE;
	public final boolean VCOL_TRIFINDO;
	public final boolean TEX_DIR_SEPARATE;

	public OBJExportSettings(boolean isVCol, boolean isVColTrifindoFormat, boolean isTexDirSeparate) {
		VCOL_ENABLE = isVCol;
		VCOL_TRIFINDO = isVColTrifindoFormat;
		TEX_DIR_SEPARATE = isTexDirSeparate;
	}
}
