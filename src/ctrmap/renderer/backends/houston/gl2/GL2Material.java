package ctrmap.renderer.backends.houston.gl2;

import ctrmap.renderer.backends.houston.common.GLExtensionSupport;
import com.jogamp.opengl.GL2;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.formats.TextureFormat;

public class GL2Material {

	public static int getGL2InternalTextureFormat(TextureFormat format, RenderSettings settings, GLExtensionSupport extensions) {
		if (!settings.USE_NATIVE_TEXTURE_FORMATS) {
			switch (format) {
				default:
					return GL2.GL_RGBA8;
				case NV_HILO8:
					if (extensions.supportsExtension(GLExtensionSupport.NV_TEXTURE_SHADER3)) {
						return GL2.GL_HILO8_NV;
					}
					//else fall through
				case ETC1:
				case RGB565:
				case RGB8:
				case L4:
				case L8:
					return GL2.GL_RGB8;
				case FLOAT32:
					return GL2.GL_R32F;
			}
		} else {
			switch (format) {
				case A4:
					return GL2.GL_ALPHA4;
				case A8:
					return GL2.GL_ALPHA8;
				case L4:
					return GL2.GL_LUMINANCE4;
				case L8:
					return GL2.GL_LUMINANCE8;
				case L4A4:
					return GL2.GL_LUMINANCE4_ALPHA4;
				case L8A8:
					return GL2.GL_LUMINANCE8_ALPHA8;
				case RGB565:
					return GL2.GL_RGB565;
				case RGB5A1:
					return GL2.GL_RGB5_A1;
				case RGBA8:
					return GL2.GL_RGBA8;
				case RGB8:
					return GL2.GL_RGB8;
				case RGBA4:
					return GL2.GL_RGBA4;
				case NV_HILO8:
					if (extensions.supportsExtension(GLExtensionSupport.NV_TEXTURE_SHADER3)) {
						return GL2.GL_HILO8_NV;
					} else {
						return GL2.GL_RG8;
					}
				case ETC1:
					return GL2.GL_RGB8;
				case ETC1A4:
					return GL2.GL_RGBA8;
				case FLOAT32:
					return GL2.GL_R32F;
			}
		}
		return GL2.GL_RGBA8;
	}

	public static int getGL2ExternalTextureFormat(TextureFormat format, GLExtensionSupport extensions) {
		switch (format) {
			case A4:
			case A8:
				return GL2.GL_ALPHA;
			case L4:
			case L8:
				return GL2.GL_LUMINANCE;
			case FLOAT32:
				return GL2.GL_RED;
			case L4A4:
			case L8A8:
				return GL2.GL_LUMINANCE_ALPHA;
			case RGB565:
			case RGB8:
			case ETC1:
				return GL2.GL_RGB;
			case RGBA8:
			case RGBA4:
			case RGB5A1:
			case ETC1A4:
				return GL2.GL_RGBA;
			case NV_HILO8:
				if (extensions.supportsExtension(GLExtensionSupport.NV_TEXTURE_SHADER3)) {
					return GL2.GL_HILO_NV;
				} else {
					return GL2.GL_RG;
				}
		}
		return GL2.GL_RGBA;
	}

	public static int getGL2TextureFormatDataType(TextureFormat format) {
		switch (format) {
			case RGBA4:
				return GL2.GL_UNSIGNED_SHORT_4_4_4_4;
			case RGB565:
				return GL2.GL_UNSIGNED_SHORT_5_6_5;
			case RGB5A1:
				return GL2.GL_UNSIGNED_SHORT_5_5_5_1;
			case RGB8:
			case A4:
			case L4:
			case L4A4:
			//These two need to be expanded to 8 bytes
			case L8:
			case A8:
			case L8A8:
			case ETC1:
			case RGBA8:
			case ETC1A4:
			case NV_HILO8:
				return GL2.GL_UNSIGNED_BYTE;
			case FLOAT32:
				return GL2.GL_FLOAT;
		}
		return GL2.GL_RGBA;
	}

	public static int getGL2StencilOp(MaterialParams.StencilOp op) {
		switch (op) {
			case DECREMENT:
				return GL2.GL_DECR;
			case DECREMENT_WRAP:
				return GL2.GL_DECR_WRAP;
			case INCREMENT:
				return GL2.GL_INCR;
			case INCREMENT_WRAP:
				return GL2.GL_INCR_WRAP;
			case INVERT:
				return GL2.GL_INVERT;
			case KEEP:
				return GL2.GL_KEEP;
			case REPLACE:
				return GL2.GL_REPLACE;
			case ZERO:
				return GL2.GL_ZERO;
		}
		return GL2.GL_KEEP;
	}

	public static int getGL2TextureMapMode(MaterialParams.TextureWrap mm) {
		switch (mm) {
			case CLAMP_TO_BORDER:
				return GL2.GL_CLAMP_TO_BORDER;
			case CLAMP_TO_EDGE:
				return GL2.GL_CLAMP_TO_EDGE;
			case REPEAT:
				return GL2.GL_REPEAT;
			case MIRRORED_REPEAT:
				return GL2.GL_MIRRORED_REPEAT;
		}
		return GL2.GL_REPEAT;
	}

	public static int getGL2TestFunc(MaterialParams.TestFunction op) {
		switch (op) {
			case ALWAYS:
				return GL2.GL_ALWAYS;
			case NEVER:
				return GL2.GL_NEVER;
			case EQ:
				return GL2.GL_EQUAL;
			case GEQ:
				return GL2.GL_GEQUAL;
			case GREATER:
				return GL2.GL_GREATER;
			case LEQ:
				return GL2.GL_LEQUAL;
			case LESS:
				return GL2.GL_LESS;
			case NEQ:
				return GL2.GL_NOTEQUAL;
		}
		return GL2.GL_ALWAYS;
	}

	public static int getGL2TextureMagFilter(MaterialParams.TextureMagFilter f) {
		switch (f) {
			case LINEAR:
				return GL2.GL_LINEAR;
			case NEAREST_NEIGHBOR:
				return GL2.GL_NEAREST;
		}
		return GL2.GL_NEAREST;
	}
	
	public static int getFallbackGL2TextureMinFilter(MaterialParams.TextureMinFilter f) {
		//similarly to SPICA, we don't support mipmaps, using the correct filters would break textured rendering
		switch (f) {
			case LINEAR_MIPMAP_LINEAR:
			case LINEAR_MIPMAP_NEAREST:
			case LINEAR:
				return GL2.GL_LINEAR;
			case NEAREST_MIPMAP_LINEAR:
			case NEAREST_MIPMAP_NEAREST:
			case NEAREST_NEIGHBOR:
				return GL2.GL_NEAREST;
		}
		return GL2.GL_LINEAR;
	}

	public static int getGL2TextureMinFilter(MaterialParams.TextureMinFilter f) {
		switch (f) {
			case LINEAR_MIPMAP_LINEAR:
				return GL2.GL_LINEAR_MIPMAP_LINEAR;
			case LINEAR_MIPMAP_NEAREST:
				return GL2.GL_LINEAR_MIPMAP_NEAREST;
			case LINEAR:
				return GL2.GL_LINEAR;
			case NEAREST_MIPMAP_LINEAR:
				return GL2.GL_NEAREST_MIPMAP_LINEAR;
			case NEAREST_MIPMAP_NEAREST:
				return GL2.GL_NEAREST_MIPMAP_NEAREST;
			case NEAREST_NEIGHBOR:
				return GL2.GL_NEAREST;
		}
		return GL2.GL_LINEAR;
	}

	public static int getGL2BlendEquation(MaterialParams.BlendEquation eq) {
		switch (eq) {
			case ADD:
				return GL2.GL_FUNC_ADD;
			case SUB:
				return GL2.GL_FUNC_SUBTRACT;
			case MAX:
				return GL2.GL_MAX;
			case MIN:
				return GL2.GL_MIN;
			case REVERSE_SUB:
				return GL2.GL_FUNC_REVERSE_SUBTRACT;
		}
		return GL2.GL_FUNC_ADD;
	}

	public static int getGL2BlendFunction(MaterialParams.BlendFunction fun) {
		switch (fun) {
			case CONSTANT_ALPHA:
				return GL2.GL_CONSTANT_ALPHA;
			case CONSTANT_COLOR:
				return GL2.GL_CONSTANT_COLOR;
			case DEST_ALPHA:
				return GL2.GL_DST_ALPHA;
			case DEST_COLOR:
				return GL2.GL_DST_COLOR;
			case ONE:
				return GL2.GL_ONE;
			case ONE_MINUS_CONSTANT_ALPHA:
				return GL2.GL_ONE_MINUS_CONSTANT_ALPHA;
			case ONE_MINUS_CONSTANT_COLOR:
				return GL2.GL_ONE_MINUS_CONSTANT_COLOR;
			case ONE_MINUS_DEST_ALPHA:
				return GL2.GL_ONE_MINUS_DST_ALPHA;
			case ONE_MINUS_DEST_COLOR:
				return GL2.GL_ONE_MINUS_DST_COLOR;
			case ONE_MINUS_SRC_ALPHA:
				return GL2.GL_ONE_MINUS_SRC_ALPHA;
			case ONE_MINUS_SRC_COLOR:
				return GL2.GL_ONE_MINUS_SRC_COLOR;
			case SOURCE_ALPHA_SATURATE:
				return GL2.GL_SRC_ALPHA_SATURATE;
			case SRC_ALPHA:
				return GL2.GL_SRC_ALPHA;
			case SRC_COLOR:
				return GL2.GL_SRC_COLOR;
			case ZERO:
				return GL2.GL_ZERO;
		}
		return GL2.GL_ONE;
	}
}
