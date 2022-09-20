
package ctrmap.renderer.backends.base.flow;

import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;
import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import org.joml.Matrix3f;

public interface IRenderDriver {
	public Object getIdentity();
	
	//Non-standard methods to be called before scene draw
	public default void beginDrawEx(RenderState state){
		
	}
	
	//Non-standard methods to be called after scene draw
	public default void finishDrawEx(RenderState state){
		
	}
	
	//Setup
	public void clearColor(RGBA color);
	public void clearDepth(float depth);
	public void clearFramebuffer();
	
	//End
	public void flush();
	
	//Texture ops
	public int genTexture();
	public void deleteTextures(int... textures);
	public void activeTexture(int unitIndex);
	public void bindTexture(int name);
	public void setTexturingEnable(boolean value);
	public void texImage2D(TextureFormatHandler format, int w, int h, java.nio.Buffer data);
	public void setUpTextureMapper(TextureMapper tex);
	
	//Buffer ops
	public int genBuffer();
	public void deleteBuffers(int... buffers);
	public void bindBuffer(Buffer.BufferTarget target, int ptr);
	public void allocBuffer(Buffer.BufferTarget target, int size, VertexListUsage usage);
	public void uploadBufferData(Buffer.BufferTarget target, java.nio.Buffer buffer, int offset, int size);
	
	//Shaders
	public void useProgram(ShaderProgram program);
	public void deleteProgram(ShaderProgram program);
	public int linkProgram(int vsh, int fsh);
	public int compileShader(String source, ShaderType type);
	public void deleteShader(int shader);
	
	//Shader uniforms
	public void uniformMatrix4fv(int location, Matrix4... matrices);
	public void uniformMatrix3fv(int location, Matrix3f... matrices);
	
	public void uniform1i(int location, int value);
	public void uniform1iv(int location, int... values);
	
	public void uniform1f(int location, float value);
	public void uniform1fv(int location, float... values);
	
	public void uniform3fv(int location, Vec3f... values);
	
	public void uniform4fv(int location, Vec4f... values);
	
	public default void uniform4fv(int location, RGBA... values){
		Vec4f[] vectors = new Vec4f[values.length];
		for (int i = 0; i < values.length; i++) {
			vectors[i] = values[i].toVector4();
		}
		uniform4fv(location, vectors);
	}
	
	//High level ops
	public void drawMesh(Mesh mesh);
	public ShaderProgram getShaderProgram(Material mat);
	public int getUniformLocation(String name, ShaderProgram program);
	
	//Material state
	public void resetMaterialState();
	
	public void setUpBlend(MaterialParams.BlendOperation blend);
	public void setUpStencilTest(MaterialParams.StencilTest test, MaterialParams.StencilOperation op);
	public void setUpDepthTest(MaterialParams.DepthColorMask depthTest);
	public void setUpAlphaTest(MaterialParams.AlphaTest alphaTest);
	public void setUpFaceCulling(MaterialParams.FaceCulling faceCulling);
	
	public static enum ShaderType {
		VERTEX_SHADER,
		FRAGMENT_SHADER
	}
}
