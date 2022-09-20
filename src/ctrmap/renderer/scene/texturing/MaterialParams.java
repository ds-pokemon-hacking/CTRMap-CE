package ctrmap.renderer.scene.texturing;

import xstandard.math.vec.RGBA;
import java.util.Objects;

public class MaterialParams {
	//Mostly from SPICA

	public static class DepthColorMask {

		public boolean enabled = true;

		public TestFunction depthFunction = TestFunction.LEQ;

		public boolean depthWrite = true;
		public boolean redWrite = true;
		public boolean greenWrite = true;
		public boolean blueWrite = true;
		public boolean alphaWrite = true;

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 61 * hash + (this.enabled ? 1 : 0);
			hash = 61 * hash + Objects.hashCode(this.depthFunction);
			hash = 61 * hash + (this.depthWrite ? 1 : 0);
			hash = 61 * hash + (this.redWrite ? 1 : 0);
			hash = 61 * hash + (this.greenWrite ? 1 : 0);
			hash = 61 * hash + (this.blueWrite ? 1 : 0);
			hash = 61 * hash + (this.alphaWrite ? 1 : 0);
			return hash;
		}
	}

	public static class AlphaTest {

		public boolean enabled = false;
		public TestFunction testFunction = TestFunction.GREATER;
		public int reference = 0;

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 31 * hash + (this.enabled ? 1 : 0);
			hash = 31 * hash + Objects.hashCode(this.testFunction);
			hash = 31 * hash + this.reference;
			return hash;
		}
	}
	
	public static class BlendOperation {
		public boolean enabled = true;
		
		public RGBA blendColor = RGBA.WHITE;
		
		public BlendEquation alphaEquation = BlendEquation.ADD;
		public BlendEquation colorEquation = BlendEquation.ADD;
		
		public BlendFunction alphaSrcFunc = BlendFunction.ONE;
		public BlendFunction alphaDstFunc = BlendFunction.ZERO;
		
		public BlendFunction colorSrcFunc = BlendFunction.ONE;
		public BlendFunction colorDstFunc = BlendFunction.ZERO;

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 83 * hash + (this.enabled ? 1 : 0);
			hash = 83 * hash + Objects.hashCode(this.blendColor);
			hash = 83 * hash + Objects.hashCode(this.alphaEquation);
			hash = 83 * hash + Objects.hashCode(this.colorEquation);
			hash = 83 * hash + Objects.hashCode(this.alphaSrcFunc);
			hash = 83 * hash + Objects.hashCode(this.alphaDstFunc);
			hash = 83 * hash + Objects.hashCode(this.colorSrcFunc);
			hash = 83 * hash + Objects.hashCode(this.colorDstFunc);
			return hash;
		}
	}
	
	public static class StencilTest{
		public boolean enabled = true;
		
		public TestFunction testFunction = TestFunction.ALWAYS;
		public int funcMask = 0xFF;
		public int bufferMask = 0xFF;
		public int reference = 255;
	}
	
	public static class StencilOperation{
		public StencilOp fail = StencilOp.KEEP;
		public StencilOp zFail = StencilOp.KEEP;
		public StencilOp zPass = StencilOp.REPLACE;
	}
	
	
	public enum LUTTarget {
		REFLEC_R,
		REFLEC_G,
		REFLEC_B,
		DIST_0,
		DIST_1,
		FRESNEL_PRI,
		FRESNEL_SEC,
	}
	
	public enum LUTSource {
		NORMAL_HALF,
		VIEW_HALF,
		NORMAL_VIEW,
		LIGHT_NORMAL,
		LIGHT_SPOT,
		PHI
	}
	
	public enum FaceCulling{
		NEVER,
		FRONT_FACE,
		BACK_FACE,
		FRONT_AND_BACK
	}
	
	public enum BumpMode{
		NONE,
		NORMAL,
		TANGENT
	}
	
	public enum StencilOp{
		KEEP,
		ZERO,
		REPLACE,
		INCREMENT,
		DECREMENT,
		INVERT,
		INCREMENT_WRAP,
		DECREMENT_WRAP
	}
	
	public enum BlendEquation
    {
        ADD,
        SUB,
        REVERSE_SUB,
        MIN,
        MAX
    }
	
	public enum BlendFunction
    {
        ZERO,
        ONE,
        SRC_COLOR,
        ONE_MINUS_SRC_COLOR,
        DEST_COLOR,
        ONE_MINUS_DEST_COLOR,
        SRC_ALPHA,
        ONE_MINUS_SRC_ALPHA,
        DEST_ALPHA,
        ONE_MINUS_DEST_ALPHA,
        CONSTANT_COLOR,
        ONE_MINUS_CONSTANT_COLOR,
        CONSTANT_ALPHA,
        ONE_MINUS_CONSTANT_ALPHA,
        SOURCE_ALPHA_SATURATE
    }

	public enum TextureWrap {
		CLAMP_TO_EDGE,
		CLAMP_TO_BORDER,
		REPEAT,
		MIRRORED_REPEAT
	}

	public enum TextureMagFilter {
		NEAREST_NEIGHBOR,
		LINEAR
	}

	public enum TextureMinFilter {
		NEAREST_NEIGHBOR,
		NEAREST_MIPMAP_NEAREST,
		NEAREST_MIPMAP_LINEAR,
		LINEAR,
		LINEAR_MIPMAP_NEAREST,
		LINEAR_MIPMAP_LINEAR
	}
	
	public enum TextureMapMode {
		UV_MAP,
		CUBE_MAP,
		SPHERE_MAP,
		PROJECTION_MAP
	}

	public enum TestFunction {
		NEVER,
		ALWAYS,
		EQ,
		NEQ,
		LESS,
		LEQ,
		GREATER,
		GEQ
	}
	
	public enum FragmentShaderType {
		CTR_COMBINER,
		USER_SHADER
	}
}
