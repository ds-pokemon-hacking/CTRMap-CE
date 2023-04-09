package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.texturing.LUT;
import xstandard.io.InvalidMagicException;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.TextureMapper;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MaterialUtil {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Material", "*.ifmt");

	public static final String MATERIAL_MAGIC = "IFMT";
	public static final String TEVCONF_MAGIC = "TENV";

	public static Material readMaterial(File f) {
		return readMaterial(new DiskFile(f));
	}

	public static Material readMaterial(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, MATERIAL_MAGIC);

			Material mat = readMaterial(l0.io, l0.fileVersion);

			l0.io.close();
			return mat;
		} catch (IOException ex) {
			Logger.getLogger(MaterialUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Material readLevel0Material(DataIOStream dis) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0Section(dis, MATERIAL_MAGIC);
			return readMaterial(l0.io, l0.fileVersion);
		} catch (IOException ex) {
			Logger.getLogger(MaterialUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Material readMaterial(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, MATERIAL_MAGIC)) {
			throw new InvalidMagicException("Invalid material magic.");
		}

		Material m = new Material();
		m.name = dis.readStringWithAddress();

		if (fileVersion != Revisions.REV_NDS_RESERVED) {
			if (fileVersion >= Revisions.REV_SHADER_INFO) {
				m.vertexShaderName = dis.readStringWithAddress();
				if (fileVersion >= Revisions.REV_EXT_FSH_INFO) {
					m.fragmentShaderName = dis.readStringWithAddress();
					m.fshType = MaterialParams.FragmentShaderType.values()[dis.read()];
					int shExtCount = dis.readUnsignedShort();
					for (int i = 0; i < shExtCount; i++) {
						m.shaderExtensions.add(dis.readStringWithAddress());
					}
				} else {
					int shaderIndex = dis.read();
					if (shaderIndex != 255) {
						m.vertexShaderName = shaderIndex + "@" + m.vertexShaderName;
					}
				}
			}
			if (fileVersion >= Revisions.REV_LIGHTING_DATA) {
				m.lightSetIndex = dis.read();
				m.ambientColor = new RGBA(dis);
				m.diffuseColor = new RGBA(dis);
			}
			if (fileVersion >= Revisions.REV_LIGHTING_DATA_EX) {
				m.specular0Color = new RGBA(dis);
				m.specular1Color = new RGBA(dis);
				if (fileVersion >= Revisions.REV_EMISSION_COLOR) {
					m.emissionColor = new RGBA(dis);
				}
				m.lightingLayer = dis.read();
			}
			if (fileVersion >= Revisions.REV_CCOL_ASSIGNMENT) {
				for (int i = 0; i < m.constantColors.length; i++) {
					m.constantColors[i] = new RGBA(dis);
				}
			}
		} else {
			m.lightSetIndex = dis.read();
			m.ambientColor = new RGBA(dis);
			m.diffuseColor = new RGBA(dis);
			m.specular0Color = new RGBA(dis);
			m.emissionColor = new RGBA(dis);
		}
		if (m.lightSetIndex == 255) {
			m.lightSetIndex = -1;
		}

		int count = dis.read();
		for (int i = 0; i < count; i++) {
			TextureMapper t = new TextureMapper();

			t.textureName = dis.readStringWithAddress();
			t.uvSetNo = dis.read();
			if (fileVersion >= Revisions.REV_TEX_MAP_MODE) {
				t.mapMode = MaterialParams.TextureMapMode.values()[dis.read()];
				
				if (t.mapMode != MaterialParams.TextureMapMode.UV_MAP && t.uvSetNo > 3) {
					t.uvSetNo = -1;
				}
			} else {
				switch (t.uvSetNo) {
					case 3:
						t.mapMode = MaterialParams.TextureMapMode.CUBE_MAP;
						break;
					case 4:
						t.mapMode = MaterialParams.TextureMapMode.SPHERE_MAP;
						break;
					case 5:
						t.mapMode = MaterialParams.TextureMapMode.PROJECTION_MAP;
						break;
					default:
						t.mapMode = MaterialParams.TextureMapMode.UV_MAP;
						break;
				}
				if (t.mapMode != MaterialParams.TextureMapMode.UV_MAP) {
					t.uvSetNo = -1;
				}
			}
			if (t.uvSetNo == 255) {
				t.uvSetNo = -1;
			}
			t.bindTranslation = new Vec2f(dis);
			t.bindRotation = dis.readFloat();
			t.bindScale = new Vec2f(dis);
			t.mapU = MaterialParams.TextureWrap.values()[dis.read()];
			t.mapV = MaterialParams.TextureWrap.values()[dis.read()];
			t.textureMagFilter = MaterialParams.TextureMagFilter.values()[dis.read()];
			t.textureMinFilter = MaterialParams.TextureMinFilter.values()[dis.read()];

			m.textures.add(t);
		}

		if (fileVersion != Revisions.REV_NDS_RESERVED && fileVersion >= Revisions.REV_LUT) {
			int lutsCount = dis.read();
			for (int i = 0; i < lutsCount; i++) {
				LUT lut = new LUT();
				lut.target = MaterialParams.LUTTarget.values()[dis.read()];
				lut.source = MaterialParams.LUTSource.values()[dis.read()];
				lut.textureName = dis.readStringWithAddress();
				m.LUTs.add(lut);
			}
		}

		m.depthColorMask.enabled = dis.readBoolean();
		if (m.depthColorMask.enabled) {
			byte depthConfig = dis.readByte();
			m.depthColorMask.depthFunction = MaterialParams.TestFunction.values()[depthConfig & 0b111];
			m.depthColorMask.redWrite = (depthConfig & 8) > 0;
			m.depthColorMask.greenWrite = (depthConfig & 16) > 0;
			m.depthColorMask.blueWrite = (depthConfig & 32) > 0;
			m.depthColorMask.alphaWrite = (depthConfig & 64) > 0;
			m.depthColorMask.depthWrite = (depthConfig & 128) > 0;
		}

		byte alphaTestByte = dis.readByte();
		m.alphaTest.enabled = (alphaTestByte & 128) > 0;
		m.alphaTest.testFunction = MaterialParams.TestFunction.values()[alphaTestByte & 0b111];
		m.alphaTest.reference = dis.readByte() & 0xFF;

		byte blendMasterByte = dis.readByte();
		byte blendRgbByte = dis.readByte();
		byte blendAlphaByte = dis.readByte();

		m.blendOperation.enabled = (blendMasterByte & 128) > 0;
		m.blendOperation.colorEquation = MaterialParams.BlendEquation.values()[blendMasterByte & 0b111];
		m.blendOperation.alphaEquation = MaterialParams.BlendEquation.values()[(blendMasterByte >> 3) & 0b111];
		m.blendOperation.colorSrcFunc = MaterialParams.BlendFunction.values()[blendRgbByte & 0b1111];
		m.blendOperation.colorDstFunc = MaterialParams.BlendFunction.values()[(blendRgbByte >> 4) & 0b1111];
		m.blendOperation.alphaSrcFunc = MaterialParams.BlendFunction.values()[blendAlphaByte & 0b1111];
		m.blendOperation.alphaDstFunc = MaterialParams.BlendFunction.values()[(blendAlphaByte >> 4) & 0b1111];
		m.blendOperation.blendColor = new RGBA(dis);

		if (fileVersion >= Revisions.REV_STENCIL_TEST) {
			int stencilCfg = dis.read();
			m.stencilTest.enabled = (stencilCfg & 128) > 0;
			m.stencilTest.testFunction = MaterialParams.TestFunction.values()[stencilCfg & 0x7F];
			m.stencilTest.reference = dis.read();
			m.stencilTest.funcMask = dis.read();
			m.stencilTest.bufferMask = dis.read();

			m.stencilOperation.fail = MaterialParams.StencilOp.values()[dis.read()];
			m.stencilOperation.zFail = MaterialParams.StencilOp.values()[dis.read()];
			m.stencilOperation.zPass = MaterialParams.StencilOp.values()[dis.read()];
		}

		if (fileVersion >= Revisions.REV_FACE_CULLING) {
			m.faceCulling = MaterialParams.FaceCulling.values()[dis.read()];
		}

		if (fileVersion >= Revisions.REV_BUMP_MAPPING) {
			byte bumpByte = dis.readByte();
			m.bumpMode = MaterialParams.BumpMode.values()[bumpByte & 3];
			m.bumpTextureIndex = bumpByte >>> 2;
		}

		if (!StringIO.checkMagic(dis, TEVCONF_MAGIC)) {
			throw new InvalidMagicException("Invalid TexEnvConfig magic.");
		}

		m.tevStages.inputBufferColor = new RGBA(dis);
		for (int stage = 0; stage < 6; stage++) {
			if (fileVersion < Revisions.REV_CCOL_ASSIGNMENT) {
				m.constantColors[stage] = new RGBA(dis);
				m.tevStages.stages[stage].constantColor = MaterialColorType.forCColIndex(stage);
			} else {
				m.tevStages.stages[stage].constantColor = MaterialColorType.forCColIndex(dis.read());
			}

			byte scalingByte = dis.readByte();
			m.tevStages.stages[stage].rgbScale = TexEnvConfig.Scale.values()[scalingByte & 3];
			m.tevStages.stages[stage].alphaScale = TexEnvConfig.Scale.values()[(scalingByte >> 2) & 3];
			m.tevStages.stages[stage].writeColorBuffer = (scalingByte & 64) > 0;
			m.tevStages.stages[stage].writeAlphaBuffer = (scalingByte & 128) > 0;

			m.tevStages.stages[stage].rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.values()[dis.readByte()];
			m.tevStages.stages[stage].alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.values()[dis.readByte()];

			for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(m.tevStages.stages[stage].rgbCombineOperator); i++) {
				m.tevStages.stages[stage].rgbSource[i] = TexEnvConfig.PICATextureCombinerSource.values()[dis.readByte()];
				m.tevStages.stages[stage].rgbOperand[i] = TexEnvConfig.PICATextureCombinerColorOp.values()[dis.readByte()];
			}

			for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(m.tevStages.stages[stage].alphaCombineOperator); i++) {
				m.tevStages.stages[stage].alphaSource[i] = TexEnvConfig.PICATextureCombinerSource.values()[dis.readByte()];
				m.tevStages.stages[stage].alphaOperand[i] = TexEnvConfig.PICATextureCombinerAlphaOp.values()[dis.readByte()];
			}
		}

		if (fileVersion >= Revisions.REV_META_DATA) {
			m.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}

		return m;
	}

	public static void writeMaterial(Material m, File f) {
		writeMaterial(m, new DiskFile(f));
	}

	public static void writeMaterial(Material m, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(MATERIAL_MAGIC);

			writeMaterial(m, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeMaterial(Material m, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(MATERIAL_MAGIC);			//MAGIC
		dos.writeString(m.name);								//Nameofs

		dos.writeString(m.vertexShaderName);
		dos.writeString(m.fragmentShaderName);
		dos.writeEnum(m.fshType);
		dos.writeShort(m.shaderExtensions.size());
		for (String shext : m.shaderExtensions) {
			dos.writeString(shext);
		}

		//LIGHTING DATA
		dos.write(m.lightSetIndex);
		m.ambientColor.write(dos);
		m.diffuseColor.write(dos);
		m.specular0Color.write(dos);
		m.specular1Color.write(dos);
		m.emissionColor.write(dos);
		dos.write(m.lightingLayer);

		//CONSTANT COLORs
		for (RGBA ccol : m.constantColors) {
			ccol.write(dos);
		}

		/* REV_NDS_RESERVED
		dos.write(m.lightSetIndex);
		m.ambientColor.write(dos);
		m.diffuseColor.write(dos);
		m.specular0Color.write(dos);
		m.emissionColor.write(dos);*/
		//TEXTURE DATA
		int count = 0;			//There can theoretically be blank textures before the last texture, although H3D does not support it
		for (TextureMapper t : m.textures) {
			if (t.textureName != null) {
				count++;
			}
		}

		dos.write(count);										//Texture count (3 at maximum, using 8 bits is a waste, sigh)
		for (TextureMapper t : m.textures) {
			writeTextureEntry(t, dos);
		}

		//LUTs
		dos.write(m.LUTs.size());
		for (LUT lut : m.LUTs) {
			dos.writeEnum(lut.target);
			dos.writeEnum(lut.source);
			dos.writeString(lut.textureName);						//Nameofs
		}

		//MATERIAL PARAMETERS
		//DEPTH TEST
		dos.write(m.depthColorMask.enabled ? 1 : 0);	//have to waste a whole byte since the next byte is completely full
		if (m.depthColorMask.enabled) {
			byte depthOp = (byte) m.depthColorMask.depthFunction.ordinal();
			//That's the function. Now push the booleans.
			//The depthOp has a maximum of 0x7, which takes up 3 bits. The remaining 5 bits are just enough for the bools.
			depthOp |= ((m.depthColorMask.redWrite ? 1 : 0) << 3);
			depthOp |= ((m.depthColorMask.greenWrite ? 1 : 0) << 4);
			depthOp |= ((m.depthColorMask.blueWrite ? 1 : 0) << 5);
			depthOp |= ((m.depthColorMask.alphaWrite ? 1 : 0) << 6);
			depthOp |= ((m.depthColorMask.depthWrite ? 1 : 0) << 7);
			dos.write(depthOp);
		}

		//ALPHA TEST
		byte mode = (byte) m.alphaTest.testFunction.ordinal();
		mode |= ((m.alphaTest.enabled ? 1 : 0) << 7);	//highest bit
		dos.write(mode);
		dos.write(m.alphaTest.reference);

		//BLEND OPERATION
		//blend functions are 4 bit
		//blend equations are 3 bit
		//we can use the remaining 2 bit space of blend equations for the enabled state
		int blendConfig = 0;
		blendConfig |= m.blendOperation.colorEquation.ordinal();
		blendConfig |= m.blendOperation.alphaEquation.ordinal() << 3;
		blendConfig |= ((m.blendOperation.enabled ? 1 : 0) << 7);
		dos.write(blendConfig);

		int blendFuncRgb = m.blendOperation.colorSrcFunc.ordinal() | (m.blendOperation.colorDstFunc.ordinal() << 4);
		int blendFuncAlpha = m.blendOperation.alphaSrcFunc.ordinal() | (m.blendOperation.alphaDstFunc.ordinal() << 4);
		dos.write(blendFuncRgb);
		dos.write(blendFuncAlpha);
		m.blendOperation.blendColor.write(dos);

		//STENCIL TEST
		dos.write(m.stencilTest.testFunction.ordinal() | ((m.stencilTest.enabled ? 1 : 0) << 7));
		dos.write(m.stencilTest.reference);
		dos.write(m.stencilTest.funcMask);
		dos.write(m.stencilTest.bufferMask);

		//STENCIL OPERATION
		dos.writeEnum(m.stencilOperation.fail);
		dos.writeEnum(m.stencilOperation.zFail);
		dos.writeEnum(m.stencilOperation.zPass);

		//FACE CULLING
		dos.writeEnum(m.faceCulling);

		//BUMP MAPPING
		dos.write(m.bumpMode.ordinal() | (m.bumpTextureIndex << 2));

		//Texture Environment config
		dos.writeStringUnterminated(TEVCONF_MAGIC);
		m.tevStages.inputBufferColor.write(dos);
		for (TexEnvStage s : m.tevStages.stages) {
			dos.writeEnum(s.constantColor);	//Constant color assignment
			//scaling
			int tevScale = s.rgbScale.ordinal() | (s.alphaScale.ordinal() << 2) | ((s.writeColorBuffer ? 1 : 0) << 6) | ((s.writeAlphaBuffer ? 1 : 0) << 7);
			dos.write(tevScale);		//Color and alpha scale + buffer writes

			dos.writeEnum(s.rgbCombineOperator);
			dos.writeEnum(s.alphaCombineOperator);

			for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(s.rgbCombineOperator); i++) {
				dos.writeEnum(s.rgbSource[i]);
				dos.writeEnum(s.rgbOperand[i]);
			}

			for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(s.alphaCombineOperator); i++) {
				dos.writeEnum(s.alphaSource[i]);
				dos.writeEnum(s.alphaOperand[i]);
			}
		}

		MetaDataUtil.writeMetaData(m.metaData, dos);
	}

	private static void writeTextureEntry(TextureMapper m, CMIFWriter dos) throws IOException {
		String name = m.textureName;

		if (name != null) {
			//valid
			dos.writeString(m.textureName);	//Nameofs
			dos.write(m.uvSetNo);						//UV map to use
			dos.writeEnum(m.mapMode);

			//TRANSFORMS
			m.bindTranslation.write(dos);	//Tra
			dos.writeFloat(m.bindRotation);	//Rot
			m.bindScale.write(dos);			//Sca

			//MAPPING AND FILTERING
			dos.writeEnum(m.mapU);				//U wrap
			dos.writeEnum(m.mapV);				//V wrap
			dos.writeEnum(m.textureMagFilter);	//Magnification filter
			dos.writeEnum(m.textureMinFilter);	//Minification filter
		}
	}
}
