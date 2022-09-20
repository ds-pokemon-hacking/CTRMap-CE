package ctrmap.renderer.backends.houston.gl4;

import ctrmap.renderer.backends.houston.common.GLExtensionSupport;
import com.jogamp.opengl.GL4;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.formats.TextureFormat;

public class GL4Material {

	public static int getGL4InternalTextureFormat(TextureFormat format, RenderSettings settings, GLExtensionSupport extensions) {
		if (!settings.USE_NATIVE_TEXTURE_FORMATS) {
			switch (format) {
				default:
					return GL4.GL_RGBA8;
				case NV_HILO8:
				case ETC1:
				case RGB565:
				case RGB8:
				case L4:
				case L8:
				case FLOAT32:
					return GL4.GL_RGB8;
			}
		} else {
			switch (format) {
				case A4:
					return GL4.GL_ALPHA8;
				case L4:
					return GL4.GL_LUMINANCE8;
				case L4A4:
					return GL4.GL_LUMINANCE4_ALPHA4;
				case L8A8:
					return GL4.GL_LUMINANCE8_ALPHA8;
				case RGB565:
					return GL4.GL_RGB565;
				case RGB5A1:
					return GL4.GL_RGB5_A1;
				case RGBA8:
					return GL4.GL_RGBA8;
				case RGB8:
					return GL4.GL_RGB8;
				case RGBA4:
					return GL4.GL_RGBA4;
				case NV_HILO8:
					return GL4.GL_RG8;
				case ETC1:
					return GL4.GL_RGB8;
				case ETC1A4:
					return GL4.GL_RGBA8;
				case FLOAT32:
					return GL4.GL_R32F;
			}
		}
		return GL4.GL_RGBA8;
	}

	public static int getGL4ExternalTextureFormat(TextureFormat format, GLExtensionSupport extensions) {
		switch (format) {
			case A4:
			case A8:
				return GL4.GL_ALPHA;
			case L4:
			case L8:
				return GL4.GL_LUMINANCE;
			case FLOAT32:
				return GL4.GL_RED;
			case L4A4:
			case L8A8:
				return GL4.GL_LUMINANCE_ALPHA;
			case RGB565:
			case RGB8:
			case ETC1:
				return GL4.GL_RGB;
			case RGBA8:
			case RGBA4:
			case RGB5A1:
			case ETC1A4:
				return GL4.GL_RGBA;
			case NV_HILO8:
				return GL4.GL_RG;
		}
		return GL4.GL_RGBA;
	}

	public static int getGL4TextureFormatDataType(TextureFormat format) {
		switch (format) {
			case RGBA4:
				return GL4.GL_UNSIGNED_SHORT_4_4_4_4;
			case RGB565:
				return GL4.GL_UNSIGNED_SHORT_5_6_5;
			case RGB5A1:
				return GL4.GL_UNSIGNED_SHORT_5_5_5_1;
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
				return GL4.GL_UNSIGNED_BYTE;
			case FLOAT32:
				return GL4.GL_FLOAT;
		}
		return GL4.GL_RGBA;
	}

	public static int getGL4StencilOp(MaterialParams.StencilOp op) {
		switch (op) {
			case DECREMENT:
				return GL4.GL_DECR;
			case DECREMENT_WRAP:
				return GL4.GL_DECR_WRAP;
			case INCREMENT:
				return GL4.GL_INCR;
			case INCREMENT_WRAP:
				return GL4.GL_INCR_WRAP;
			case INVERT:
				return GL4.GL_INVERT;
			case KEEP:
				return GL4.GL_KEEP;
			case REPLACE:
				return GL4.GL_REPLACE;
			case ZERO:
				return GL4.GL_ZERO;
		}
		return GL4.GL_KEEP;
	}

	public static int getGL4TextureMapMode(MaterialParams.TextureWrap mm) {
		switch (mm) {
			case CLAMP_TO_BORDER:
				return GL4.GL_CLAMP_TO_BORDER;
			case CLAMP_TO_EDGE:
				return GL4.GL_CLAMP_TO_EDGE;
			case REPEAT:
				return GL4.GL_REPEAT;
			case MIRRORED_REPEAT:
				return GL4.GL_MIRRORED_REPEAT;
		}
		return GL4.GL_REPEAT;
	}

	public static int getGL4TestFunc(MaterialParams.TestFunction op) {
		switch (op) {
			case ALWAYS:
				return GL4.GL_ALWAYS;
			case NEVER:
				return GL4.GL_NEVER;
			case EQ:
				return GL4.GL_EQUAL;
			case GEQ:
				return GL4.GL_GEQUAL;
			case GREATER:
				return GL4.GL_GREATER;
			case LEQ:
				return GL4.GL_LEQUAL;
			case LESS:
				return GL4.GL_LESS;
			case NEQ:
				return GL4.GL_NOTEQUAL;
		}
		return GL4.GL_ALWAYS;
	}

	public static int getGL4TextureMagFilter(MaterialParams.TextureMagFilter f) {
		switch (f) {
			case LINEAR:
				return GL4.GL_LINEAR;
			case NEAREST_NEIGHBOR:
				return GL4.GL_NEAREST;
		}
		return GL4.GL_NEAREST;
	}

	public static int getFallbackGL4TextureMinFilter(MaterialParams.TextureMinFilter f) {
		//similarly to SPICA, we don't support mipmaps, using the correct filters would break textured rendering
		switch (f) {
			case LINEAR_MIPMAP_LINEAR:
			case LINEAR_MIPMAP_NEAREST:
			case LINEAR:
				return GL4.GL_LINEAR;
			case NEAREST_MIPMAP_LINEAR:
			case NEAREST_MIPMAP_NEAREST:
			case NEAREST_NEIGHBOR:
				return GL4.GL_NEAREST;
		}
		return GL4.GL_LINEAR;
	}

	public static int getGL4TextureMinFilter(MaterialParams.TextureMinFilter f) {
		switch (f) {
			case LINEAR_MIPMAP_LINEAR:
				return GL4.GL_LINEAR_MIPMAP_LINEAR;
			case LINEAR_MIPMAP_NEAREST:
				return GL4.GL_LINEAR_MIPMAP_NEAREST;
			case LINEAR:
				return GL4.GL_LINEAR;
			case NEAREST_MIPMAP_LINEAR:
				return GL4.GL_NEAREST_MIPMAP_LINEAR;
			case NEAREST_MIPMAP_NEAREST:
				return GL4.GL_NEAREST_MIPMAP_NEAREST;
			case NEAREST_NEIGHBOR:
				return GL4.GL_NEAREST;
		}
		return GL4.GL_LINEAR;
	}

	public static int getGL4BlendEquation(MaterialParams.BlendEquation eq) {
		switch (eq) {
			case ADD:
				return GL4.GL_FUNC_ADD;
			case SUB:
				return GL4.GL_FUNC_SUBTRACT;
			case MAX:
				return GL4.GL_MAX;
			case MIN:
				return GL4.GL_MIN;
			case REVERSE_SUB:
				return GL4.GL_FUNC_REVERSE_SUBTRACT;
		}
		return GL4.GL_FUNC_ADD;
	}

	public static int getGL4BlendFunction(MaterialParams.BlendFunction fun) {
		switch (fun) {
			case CONSTANT_ALPHA:
				return GL4.GL_CONSTANT_ALPHA;
			case CONSTANT_COLOR:
				return GL4.GL_CONSTANT_COLOR;
			case DEST_ALPHA:
				return GL4.GL_DST_ALPHA;
			case DEST_COLOR:
				return GL4.GL_DST_COLOR;
			case ONE:
				return GL4.GL_ONE;
			case ONE_MINUS_CONSTANT_ALPHA:
				return GL4.GL_ONE_MINUS_CONSTANT_ALPHA;
			case ONE_MINUS_CONSTANT_COLOR:
				return GL4.GL_ONE_MINUS_CONSTANT_COLOR;
			case ONE_MINUS_DEST_ALPHA:
				return GL4.GL_ONE_MINUS_DST_ALPHA;
			case ONE_MINUS_DEST_COLOR:
				return GL4.GL_ONE_MINUS_DST_COLOR;
			case ONE_MINUS_SRC_ALPHA:
				return GL4.GL_ONE_MINUS_SRC_ALPHA;
			case ONE_MINUS_SRC_COLOR:
				return GL4.GL_ONE_MINUS_SRC_COLOR;
			case SOURCE_ALPHA_SATURATE:
				return GL4.GL_SRC_ALPHA_SATURATE;
			case SRC_ALPHA:
				return GL4.GL_SRC_ALPHA;
			case SRC_COLOR:
				return GL4.GL_SRC_COLOR;
			case ZERO:
				return GL4.GL_ZERO;
		}
		return GL4.GL_ONE;
	}
}
